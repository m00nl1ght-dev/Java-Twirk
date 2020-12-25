package dev.m00nl1ght.bot.listener;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class HighlightTermListener implements MsgListener {

    public static final String ID = "highlight";

    private String term;

    @Override
    public boolean onMsg(TwitchMessage msg) {
        if (term == null) return false;
        if (msg.getContent().toLowerCase().contains(term)) {
            Logger.log("HLT " + msg.getContent());
        }
        return false;
    }

    @Override
    public JSONObject save() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("term", term == null ? "" : term);
        return object;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        term = data.optString("term", "");
        if (term.isEmpty()) term = null;
    }

    @Override
    public String getName() {
        return "highlight_" + term;
    }

    @Override
    public String getType() {
        return ID;
    }

    @Override
    public void fromCommand(String[] args) {
        if (args.length < 1) throw new CommandException("missing arg: highlight term");
        term = args[0];
    }

}
