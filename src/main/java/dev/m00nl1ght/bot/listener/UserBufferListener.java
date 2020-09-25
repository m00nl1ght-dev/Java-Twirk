package dev.m00nl1ght.bot.listener;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class UserBufferListener implements MsgListener {

    private static final Random random = new Random();
    public static final String ID = "user_buffer";

    private String[] data;
    private int count = 0;

    public UserBufferListener() {
        init(50);
    }

    public void put(String value) {
        final int pointer = count % data.length;
        data[pointer] = value;
        count++;
    }

    public String get(int idx) {
        if (idx < 0 || idx > data.length || idx > count) throw new ArrayIndexOutOfBoundsException();
        return data[count <= data.length ? idx : (count + idx) % data.length];
    }

    public String getRandom() {
        return count <= 0 ? null : get(random.nextInt(getSize()));
    }

    public int getSize() {
        return Math.min(data.length, count);
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean onMsg(TwitchMessage msg) {
        put(msg.getUser().getDisplayName());
        return false;
    }

    @Override
    public String getName() {
        return "user_buffer";
    }

    @Override
    public String getType() {
        return ID;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        final int capacity = data.optInt("capacity", 50);
        init(capacity);
    }

    @Override
    public JSONObject save() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("capacity", data.length);
        return object;
    }

    @Override
    public void fromCommand(String[] args) {
        try {
            final int capacity = args.length > 0 ? Integer.parseInt(args[1]) : 50;
            init(capacity);
        } catch (NumberFormatException e) {
            throw new CommandException("not a number: " + args[1], e);
        }
    }

    private void init(int capacity) {
        this.data = new String[capacity];
        count = 0;
    }

}
