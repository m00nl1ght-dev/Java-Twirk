package dev.m00nl1ght.bot.gwent.card;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CardDatabase {

    private final String gameVersion;
    private final Map<String, Card> cards = new HashMap<>();
    private final Map<String, String> keywords = new HashMap<>();

    public CardDatabase(String gameVersion, Collection<Card> cards, Map<String, String> keywords) {
        this.gameVersion = gameVersion;
        this.keywords.putAll(keywords);
        for (final Map.Entry<String, String> entry : keywords.entrySet())
            if (entry.getValue().isEmpty())
                System.out.println("[WARN] Empty keyword description for <" + entry.getKey() + ">.");
        for (final Card card : cards) {
            final Card old = this.cards.put(card.id, card);
            if (old != null) System.out.println("[WARN] Duplicate card: " + card.name);
        }
    }

    public CardDatabase(JSONObject json) {

        this.gameVersion = json.getString("gameVersion");

        final JSONArray cardData = json.getJSONArray("cards");
        for (int i = 0; i < cardData.length(); i++) {
            final Card card = new Card(cardData.getJSONObject(i));
            this.cards.put(card.id, card);
        }

        final JSONArray keywordData = json.getJSONArray("keywords");
        for (int i = 0; i < keywordData.length(); i++) {
            final JSONObject data = keywordData.getJSONObject(i);
            this.keywords.put(data.getString("name"), data.getString("description"));
        }

    }

    public JSONObject toJson() {

        final JSONArray cardData = new JSONArray();
        for (final Card card : cards.values()) {
            cardData.put(card.toJson());
        }

        final JSONArray keywordData = new JSONArray();
        for (final Map.Entry<String, String> keyword : keywords.entrySet()) {
            final JSONObject data = new JSONObject();
            data.put("name", keyword.getKey());
            data.put("description", keyword.getValue());
            keywordData.put(data);
        }

        final JSONObject json = new JSONObject();
        json.put("gameVersion", gameVersion);
        json.put("cards", cardData);
        json.put("keywords", keywordData);
        return json;

    }

    public Map<String, Card> getCards() {
        return Collections.unmodifiableMap(cards);
    }

    public Map<String, String> getKeywords() {
        return Collections.unmodifiableMap(keywords);
    }

    public String getGameVersion() {
        return gameVersion;
    }

}
