package dev.m00nl1ght.bot.gwent.card;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.ComplexCommand;
import dev.m00nl1ght.bot.gwent.GwentExtension;
import dev.m00nl1ght.bot.twist.dbd.CachedRequest;
import dev.m00nl1ght.bot.util.SearchUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CardCommand extends Command {

    public static final Type TYPE = new Type("gwentcard");

    private int queryCooldown = 0;
    private double treshSearch = 0.6D;

    protected CardCommand(Type type, MainListener parent, String name) {
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

            parser.send(found.formatInfo());
        }
    }

    public static class Type extends Command.Type<CardCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected CardCommand createInstance(MainListener parent, String name) {
            return new CardCommand(this, parent, name);
        }

    }

}
