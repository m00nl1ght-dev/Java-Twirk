package dev.m00nl1ght.bot.listener;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import org.json.JSONException;
import org.json.JSONObject;

public interface MsgListener {

    boolean onMsg(TwitchMessage msg);

    String getName();

    String getType();

    default void load(JSONObject data) throws JSONException {
        //NO-OP
    }

    default JSONObject save() throws JSONException {
        return new JSONObject();
    }

    default void fromCommand(String[] args) {
        //NO-OP
    }

}
