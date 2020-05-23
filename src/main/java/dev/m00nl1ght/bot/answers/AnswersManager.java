package dev.m00nl1ght.bot.answers;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AnswersManager {

    private Mode mode = Mode.OFF;
    private final MainListener core;
    private final HashMap<String, Answer> answers = new HashMap<>();

    public AnswersManager(MainListener core) {
        this.core = core;
    }

    public boolean onMessage(TwitchMessage message) {
        if (mode == Mode.OFF) return false;
        final String msg = message.getContent().toLowerCase();
        final int q = msg.indexOf('?');
        if (q < 0) return false;
        if (q < msg.length() * 0.55) return false;

        for (final Answer a : answers.values()) {
            for (final Trigger trg : a.triggers) {
                if (trg.test(msg, q)) {
                    if (a.lastTrigger + a.cooldown > System.currentTimeMillis()) return false;

                    if (!a.response.isEmpty()) {
                        if (mode == Mode.MENTION) {
                            core.sendMessage(message.getUser(), a.response);
                        } else {
                            core.sendMessage(a.response);
                        }
                    }

                    a.lastTrigger = System.currentTimeMillis();
                    a.stat_total++;
                    return true;
                }
            }
        }
        return false;
    }

    public Answer getAnswer(String name) {
        Answer a = answers.get(name);
        if (a == null) throw new CommandException("Answer " + name + " does not exist");
        return a;
    }

    public Answer getOrCreateAnswer(String name, String response) {
        final Answer a = answers.computeIfAbsent(name, (n) -> new Answer(n, response));
        a.response = response;
        return a;
    }

    public void deleteAnswer(String name) {
        Object rem = answers.remove(name);
        if (rem == null) throw new CommandException("Answer " + name + " does not exist");
    }

    public void load(JSONObject object) throws JSONException {
        answers.clear();
        mode = Mode.valueOf(object.optString("mode", "off").toUpperCase());
        JSONArray aws = object.getJSONArray("answers");
        for (int i = 0; i < aws.length(); i++) {
            JSONObject aw = aws.getJSONObject(i);
            Answer a = Answer.load(core, aw);
            answers.put(a.getName(), a);
        }
    }

    public JSONObject save() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("mode", mode.name().toLowerCase());
        JSONArray aws = new JSONArray();
        for (Answer a : answers.values()) {
            JSONObject aw = a.save(a);
            aws.put(aw);
        }
        object.put("answers", aws);
        return object;
    }

    public enum Mode {
        OFF, TEXT, MENTION
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

}
