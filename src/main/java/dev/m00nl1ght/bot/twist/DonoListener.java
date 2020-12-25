package dev.m00nl1ght.bot.twist;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.listener.MsgListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.LogManager;

public class DonoListener implements MsgListener {

    public static final String ID = "twist_dono";

    private double total = 0F;

    @Override
    public boolean onMsg(TwitchMessage msg) {
        if (msg.getUser().getUserName().equals("Bot_on_Fire")) {
            if (msg.getContent().startsWith("twiH twiH ")) {
                final String m = msg.getContent();
                final int p = m.indexOf(" Euro");
                int h = p - 1; while (!Character.isWhitespace(m.charAt(h))) h--;
                final String amount = m.substring(h, p).trim();
                try {
                    final double a = Double.parseDouble(amount.replace(',', '.'));
                    total += a;
                    Logger.log("DON amount:" + amount + " total: " + total);
                    return true;
                } catch (Exception e) {
                    Logger.log("DON error: ", e);
                }
            }
        }

        return false;
    }

    @Override
    public JSONObject save() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("total", total);
        return object;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        total = data.optDouble("total", 0D);
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public String getType() {
        return ID;
    }

}
