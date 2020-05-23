package dev.m00nl1ght.bot.answers;

import dev.m00nl1ght.bot.MainListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Answer {

    protected final String name;
    protected String response;
    protected final List<Trigger> triggers = new ArrayList<>();

    protected long lastTrigger = -1L;
    protected int cooldown = 5000;
    protected int stat_total = 0;

    public Answer(String name, String response) {
        this.name = name;
        this.response = response;
    }

    public static Answer load(MainListener core, JSONObject data) throws JSONException {
        final String name = data.getString("name");
        final String response = data.getString("response");
        final JSONArray trg = data.getJSONArray("triggers");
        final Answer aw = new Answer(name, response);
        for (int i = 0; i < trg.length(); i++) aw.triggers.add(Trigger.fromPattern(trg.getString(i)));
        aw.cooldown = data.optInt("cooldown", 5000);
        aw.stat_total = data.optInt("stat_total", 0);
        return aw;
    }

    public JSONObject save(Answer a) throws JSONException {
        final JSONObject data = new JSONObject();
        data.put("name", a.name);
        data.put("response", a.response);
        final JSONArray trg = new JSONArray();
        for (final Trigger t : a.triggers) trg.put(t.pattern());
        data.put("triggers", trg);
        data.put("cooldown", a.cooldown);
        data.put("stat_total", a.stat_total);
        return data;
    }

    public String getName() {
        return name;
    }

    public int getStatTotal() {
        return stat_total;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public String printStats() {
        String ret = "Triggered " + stat_total + " times, Cooldown: " + cooldown/1000 + "s, Triggers: ";
        ret += triggers.stream().map(Trigger::pattern).collect(Collectors.joining(", "));
        return ret + ".";
    }

}
