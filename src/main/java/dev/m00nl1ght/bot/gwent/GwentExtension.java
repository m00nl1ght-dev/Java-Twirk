package dev.m00nl1ght.bot.gwent;

import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.gwent.card.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class GwentExtension {

    public static GwentExtension INSTANCE;

    protected final MainListener core;
    protected CardDatabase gwentDB;
    protected Map<String, Card> cardSearch = new HashMap<>();
    protected Map<String, String> keywordSearch = new HashMap<>();

    public GwentExtension(MainListener core) {this.core = core;}

    public static void register(MainListener core) {
        INSTANCE = new GwentExtension(core);
        INSTANCE.readData(core.getDataFile("gwent_database.json"));
        core.commandManager.registerType(CardCommand.TYPE);
        core.commandManager.registerType(GwentOneCommand.TYPE);
        core.commandManager.registerType(KeywordCommand.TYPE);
    }

    private void readData(File dataFile) {
        if (dataFile.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(dataFile));
                JSONObject object = new JSONObject(tokener);
                gwentDB = new CardDatabase(object);
                cardSearch.clear();
                for (final Card card : gwentDB.getCards().values()) {
                    final String searchKey = card.name.replaceAll("[^A-Za-z ]", "").toLowerCase();
                    cardSearch.put(searchKey, card);
                    for (final String split : card.name.split(" ")) {
                        final String splitKey = split.replaceAll("[^A-Za-z ]", "").toLowerCase();
                        if (splitKey.length() < 4) continue;
                        cardSearch.put(splitKey, card);
                    }
                }
                for (final Map.Entry<String, String> keyword : gwentDB.getKeywords().entrySet()) {
                    final String searchKey = keyword.getKey().replaceAll("[^A-Za-z ]", "").toLowerCase();
                    keywordSearch.put(searchKey, keyword.getKey() + " -> " + keyword.getValue());
                }
            } catch (Exception e) {
                Logger.error("Failed to load gwent card database", e);
                e.printStackTrace();
            }
        }
    }

    public CardDatabase getGwentDB() {
        return gwentDB;
    }

    public Map<String, Card> getCardSearch() {
        return cardSearch;
    }

    public Map<String, String> getKeywordSearch() {
        return keywordSearch;
    }

}
