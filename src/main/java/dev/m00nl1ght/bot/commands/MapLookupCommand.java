package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.util.SearchUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class MapLookupCommand extends ComplexCommand {

    public static final Type TYPE = new Type("lookup");

    private final Map<String, String> NAME_TO_MSG = new HashMap<>();
    private float lookupThr = 0f;

    protected MapLookupCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Get(parent, "*"));
        this.addSubCommand(new Save(parent, "save"));
        this.addSubCommand(new Remove(parent, "remove"));
    }

    protected class Get extends Command {

        protected Get(MainListener parent, String name) {
            super(null, parent, name);
            this.verboseFeedback = false;
        }

        @Override
        public void execute(CommandParser parser) {
            final String query = parser.getParam(1).trim().toLowerCase();
            if (query.isEmpty()) {
                if (verboseFeedback) parser.sendResponse("Usage: !" + MapLookupCommand.this.name + " <lookup>");
            } else {
                Optional<String> ret = lookupThr <= 0f
                        ? Optional.ofNullable(NAME_TO_MSG.get(query))
                        : SearchUtil.findMatch(NAME_TO_MSG, query, lookupThr);
                if (ret.isPresent() && !ret.get().isEmpty()) {
                    parser.send(ret.get());
                } else {
                    if (verboseFeedback) parser.sendResponse("Not found.");
                }
            }
        }

    }

    protected class Save extends Command {

        protected Save(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            final String name = parser.nextParam().trim().toLowerCase();
            if (name.isEmpty()) {
                parser.sendResponse("Usage: !" + MapLookupCommand.this.name + " " + name + " <name> <content>");
                return;
            } else {
                final String content = parser.readAll().trim();
                if (content.length() < 1) throw new CommandException("content must not be empty");
                final String existing = NAME_TO_MSG.put(name, content);
                parser.sendResponse("Entry saved: " + name);
            }
        }

    }

    protected class Remove extends Command {

        protected Remove(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            final String query = parser.readAll().trim().toLowerCase();
            if (query.isEmpty()) {
                parser.sendResponse("Usage: !" + MapLookupCommand.this.name + " " + name + " <name>");
            } else {
                if (NAME_TO_MSG.remove(query) != null) {
                    parser.sendResponse("Entry removed: " + query);
                } else {
                    parser.sendResponse("Not found.");
                }
            }
        }

    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.lookupThr = data.optFloat("lookupThr", 0f);
        this.NAME_TO_MSG.clear();
        final JSONObject saved = data.getJSONObject("saved");
        final Iterator keys = saved.keys();
        while (keys.hasNext()) {
            final String key = keys.next().toString();
            NAME_TO_MSG.put(key, saved.getString(key));
        }
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("lookupThr", lookupThr);
        final JSONObject saved = new JSONObject();
        for (Map.Entry<String, String> entry : NAME_TO_MSG.entrySet())
            saved.put(entry.getKey(), entry.getValue());
        data.put("saved", saved);
    }

    public static class Type extends Command.Type<MapLookupCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public MapLookupCommand build(MainListener parent, String name, String pattern) {
            MapLookupCommand sc = super.build(parent, name, pattern);
            sc.verboseFeedback = false;
            return sc;
        }

        @Override
        protected MapLookupCommand createInstance(MainListener parent, String name) {
            return new MapLookupCommand(this, parent, name);
        }

    }

}
