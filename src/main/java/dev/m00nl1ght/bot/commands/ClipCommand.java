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

public class ClipCommand extends ComplexCommand {

    public static final Type TYPE = new Type("clip");

    private final Map<String, String> NAME_TO_ID = new HashMap<>();

    protected ClipCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Find(parent, "find"));
        this.addSubCommand(new Save(parent, "save"));
        this.addSubCommand(new Remove(parent, "remove"));
    }

    protected class Find extends Command {

        protected Find(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            final String query = parser.readAll().trim().toLowerCase();
            if (query.isEmpty()) {
                parser.sendResponse("Usage: !" + ClipCommand.this.name + " " + name + " <name>");
            } else {
                Optional<String> ret = SearchUtil.findMatch(NAME_TO_ID, query, 0.5D);
                if (ret.isPresent()) {
                    final String url = clipIdToUrl(ret.get());
                    parser.send(url);
                } else {
                    parser.sendResponse("Clip not found.");
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
            final String params = parser.readAll();
            final int li = params.lastIndexOf(' ');
            if (li < 0) {
                parser.sendResponse("Usage: !" + ClipCommand.this.name + " " + name + " <name> <url>");
                return;
            } else {
                final String clipUrl = params.substring(li + 1).trim();
                final String clipId = clipUrlToId(clipUrl);
                final String name = params.substring(0, li).trim().toLowerCase();
                if (name.length() < 5) throw new CommandException("clip name must be at least 5 characters long");
                final String existing = NAME_TO_ID.putIfAbsent(name, clipId);
                if (existing == null) {
                    parser.sendResponse("Clip saved: " + name);
                } else {
                    parser.sendResponse("Error: A clip with this name already exists!");
                }
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
                parser.sendResponse("Usage: !" + ClipCommand.this.name + " " + name + " <name>");
            } else {
                Optional<String> ret = SearchUtil.findMatchKey(NAME_TO_ID, query, 0.5D);
                if (ret.isPresent() && NAME_TO_ID.remove(ret.get()) != null) {
                    parser.sendResponse("Clip removed: " + ret.get());
                } else {
                    parser.sendResponse("Clip not found.");
                }
            }
        }

    }

    public static String clipUrlToId(String url) {
        if (!url.contains("twitch.tv/")) throw new CommandException("Invalid clip url");
        final int si = url.lastIndexOf("/");
        final int qi = url.indexOf("?");
        final String ret = qi < 0 ? url.substring(si + 1) : url.substring(si + 1, qi);
        if (ret.length() < 10) throw new CommandException("Invalid clip url");
        return ret;
    }

    public static String clipIdToUrl(String id) {
        return "https://clips.twitch.tv/" + id;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.NAME_TO_ID.clear();
        final JSONObject saved = data.getJSONObject("saved");
        final Iterator keys = saved.keys();
        while (keys.hasNext()) {
            final String key = keys.next().toString();
            NAME_TO_ID.put(key, saved.getString(key));
        }
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        final JSONObject saved = new JSONObject();
        for (Map.Entry<String, String> entry : NAME_TO_ID.entrySet())
            saved.put(entry.getKey(), entry.getValue());
        data.put("saved", saved);
    }

    public static class Type extends Command.Type<ClipCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public ClipCommand build(MainListener parent, String name, String pattern) {
            ClipCommand sc = super.build(parent, name, pattern);
            sc.verboseFeedback = true;
            return sc;
        }

        @Override
        protected ClipCommand createInstance(MainListener parent, String name) {
            return new ClipCommand(this, parent, name);
        }

    }

}
