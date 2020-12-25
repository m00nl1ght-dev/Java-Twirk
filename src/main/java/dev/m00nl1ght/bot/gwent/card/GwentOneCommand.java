package dev.m00nl1ght.bot.gwent.card;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.gwent.GwentExtension;
import dev.m00nl1ght.bot.util.SearchUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class GwentOneCommand extends Command {

    public static final Type TYPE = new Type("gwentone");

    private int queryCooldown = 0;
    private double treshSearch = 0.6D;

    protected GwentOneCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        this.queryCooldown = data.optInt("queryCooldown", 0);
        this.treshSearch = data.optDouble("treshSearch", 0.6D);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        data.put("queryCooldown", queryCooldown);
        data.put("treshSearch", treshSearch);
    }

    @Override
    public void execute(CommandParser parser) {
        String query = parser.readAll().replaceAll("[^A-Za-z ]", "").toLowerCase();
        if (query.isEmpty()) {
            parser.sendResponse("Missing card name.");
            return;
        }

        Optional<Card> ret = SearchUtil.findMatch(GwentExtension.INSTANCE.getCardSearch(), query, 0.7D);
        if (!ret.isPresent()) {
            parser.sendResponse("Sorry, card not found. Was the name spelled correctly?");
        } else {
            Card found = ret.get();
            if (!parser.isWhisper()) {
                if (System.currentTimeMillis() - found.lastQuery < queryCooldown) return;
                found.lastQuery = System.currentTimeMillis();
            }

            parser.send(found.name + " -> " + "https://gwent.one/en/card/" + found.id);
        }
    }

    public static class Type extends Command.Type<GwentOneCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected GwentOneCommand createInstance(MainListener parent, String name) {
            return new GwentOneCommand(this, parent, name);
        }

    }

}
