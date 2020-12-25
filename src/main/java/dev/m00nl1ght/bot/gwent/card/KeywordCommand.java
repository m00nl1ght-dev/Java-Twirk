package dev.m00nl1ght.bot.gwent.card;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.gwent.GwentExtension;
import dev.m00nl1ght.bot.util.SearchUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class KeywordCommand extends Command {

    public static final Type TYPE = new Type("gwentkeyword");

    private double treshSearch = 0.6D;

    protected KeywordCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        this.treshSearch = data.optDouble("treshSearch", 0.6D);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        data.put("treshSearch", treshSearch);
    }

    @Override
    public void execute(CommandParser parser) {
        String query = parser.readAll().replaceAll("[^A-Za-z ]", "").toLowerCase();
        if (query.isEmpty()) {
            parser.sendResponse("Missing keyword name.");
            return;
        }

        Optional<String> ret = SearchUtil.findMatch(GwentExtension.INSTANCE.getKeywordSearch(), query, 0.7D);
        if (!ret.isPresent()) {
            parser.sendResponse("Sorry, keyword not found. Was it spelled correctly?");
        } else {
            String found = ret.get();
            parser.send(found);
        }
    }

    public static class Type extends Command.Type<KeywordCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected KeywordCommand createInstance(MainListener parent, String name) {
            return new KeywordCommand(this, parent, name);
        }

    }

}
