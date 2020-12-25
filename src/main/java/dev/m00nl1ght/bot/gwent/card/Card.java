package dev.m00nl1ght.bot.gwent.card;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Card {

    public final String id;
    public final String name;
    public final int power;
    public final int armor;
    public final int provision;
    public final String ability;
    public final String faction;
    public final String set;
    public final Color color;
    public final Type type;
    public final String rarity;
    public final String flavor;
    public final List<String> categories;

    long lastQuery = 0L;

    public Card(JSONObject json) {
        this.id = json.getString("id");
        this.name = json.getString("name");
        this.power = json.getInt("power");
        this.armor = json.getInt("armor");
        this.provision = json.getInt("provision");
        this.ability = json.getString("ability");
        this.faction = json.getString("faction");
        this.set = json.getString("set");
        this.color = Color.valueOf(json.getString("color").toUpperCase());
        this.type = Type.valueOf(json.getString("type").toUpperCase());
        this.rarity = json.getString("rarity");
        this.flavor = json.optString("flavor", null);
        final JSONArray cArray = json.getJSONArray("categories");
        this.categories = IntStream.range(0, cArray.length())
                .mapToObj(cArray::getString)
                .collect(Collectors.toList());
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("power", power);
        json.put("armor", armor);
        json.put("provision", provision);
        json.put("ability", ability);
        json.put("faction", faction);
        json.put("set", set);
        json.put("color", color);
        json.put("type", type);
        json.put("rarity", rarity);
        if (flavor != null) json.put("flavor", flavor);
        json.put("categories", new JSONArray(categories));
        return json;
    }

    public String formatInfo() {
        String str = name + " (" + abbrFaction(faction);
        if (power > 0) str += ", " + power + " Str";
        if (provision > 0) str += ", " + provision + " Prov";
        str += ") -> " + ability;
        return str;
    }

    public String abbrFaction(String faction) {
        if (faction.equals("northern_realms")) return "NR";
        if (faction.equals("skellige")) return "SK";
        if (faction.equals("scoiatael")) return "ST";
        if (faction.equals("monster")) return "MO";
        if (faction.equals("nilfgaard")) return "NG";
        if (faction.equals("syndicate")) return "SY";
        if (faction.equals("neutral")) return "NE";
        return "??";
    }

    public enum Type {
        ABILITY, STRATAGEM, UNIT, ARTIFACT, SPECIAL
    }

    public enum Color {
        LEADER, GOLD, BRONZE
    }

    public enum Diff {
        NONE, ADDED, REMOVED, VALUES, ABILITY, OTHER
    }

    public static Diff compare(Card one, Card other) {
        if (one == null) {
            if (other == null) return Diff.NONE;
            return Diff.ADDED;
        } else if (other == null) {
            return Diff.REMOVED;
        } else {
            if (!one.id.equals(other.id)) return Diff.OTHER;
            if (!one.name.equals(other.name)) return Diff.OTHER;
            if (!one.faction.equals(other.faction)) return Diff.OTHER;
            if (!one.set.equals(other.set)) return Diff.OTHER;
            if (one.color != other.color) return Diff.OTHER;
            if (one.type != other.type) return Diff.OTHER;
            if (!one.rarity.equals(other.rarity)) return Diff.OTHER;
            if (!one.categories.equals(other.categories)) return Diff.OTHER;
            if (!one.ability.equals(other.ability)) return Diff.ABILITY;
            if (one.power != other.power) return Diff.VALUES;
            if (one.armor != other.armor) return Diff.VALUES;
            if (one.provision != other.provision) return Diff.VALUES;
            return Diff.NONE;
        }
    }

    public static int defaultSort(Card a, Card b) {
        final int byType = Integer.compare(a.type.ordinal(), b.type.ordinal());
        if (byType != 0) return byType;
        final int byColor = Integer.compare(a.color.ordinal(), b.color.ordinal());
        if (byColor != 0) return byColor;
        final int byProv = Integer.compare(a.provision, b.provision);
        if (byProv != 0) return -byProv;
        final int byPower = Integer.compare(a.power, b.power);
        if (byPower != 0) return -byPower;
        return 0;
    }

}
