package dev.m00nl1ght.bot.listener;

import dev.m00nl1ght.bot.MainListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MsgListenerTypes {

    private static final Map<String, Supplier<MsgListener>> registered = new HashMap<>();

    static {
        register(HighlightTermListener.ID, HighlightTermListener::new);
    }

    public static void register(String id, Supplier<MsgListener> factory) {
        registered.put(id, factory);
    }

    public static Supplier<MsgListener> get(String type) {
        return registered.get(type);
    }

    public static void load(MainListener main, JSONObject data) throws JSONException {
        if (data == null) return;
        final JSONArray list = data.getJSONArray("list");
        for (int i = 0; i < list.length(); i++) {
            final JSONObject object = list.getJSONObject(i);
            final String type = object.getString("type");
            final Supplier<MsgListener> factory = registered.get(type);
            if (factory == null) throw new IllegalStateException("missing listener type: " + type);
            final MsgListener msgListener = factory.get();
            msgListener.load(object);
            main.addMsgListener(msgListener);
        }
    }

    public static JSONObject save(MainListener main) throws JSONException {
        final JSONObject object = new JSONObject();
        final JSONArray list = new JSONArray();

        for (final MsgListener listener : main.msgListeners.values()) {
            final JSONObject data = listener.save();
            data.put("type", listener.getType());
            list.put(data);
        }

        object.put("list", list);
        return object;
    }

}
