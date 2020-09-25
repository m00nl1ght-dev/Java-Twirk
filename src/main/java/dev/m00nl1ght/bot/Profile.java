package dev.m00nl1ght.bot;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Profile {

    private final static SimpleDateFormat bakFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    protected final File CONFIG, CORE, BASE;

    public String USERNAME;
    public String OAUTH;
    public String CHANNEL;
    public String OWNER;
    public String ABOUT;
    public String GOOGLE_API_ID;
    public String STEAM_API_KEY;
    public String TWITCH_CLIENT_ID;
    public String LOGFILE;

    public int RECONNECT_DELAY_MIN;
    public int RECONNECT_DELAY_MAX;

    public Profile(File base) {
        BASE = base;
        CONFIG = new File(base, "config.json");
        CORE = new File(base, "core.json");
    }

    public void load() {
        if (CONFIG.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(CONFIG));
                JSONObject object = new JSONObject(tokener);
                USERNAME = object.getString("username");
                OAUTH = object.getString("oauth");
                CHANNEL = object.getString("channel");
                OWNER = object.getString("owner");
                ABOUT = object.optString("about", "");
                GOOGLE_API_ID = object.optString("google_api", "");
                STEAM_API_KEY = object.optString("steam_api", "");
                TWITCH_CLIENT_ID = object.optString("client_id", "");
                LOGFILE = object.optString("log_file");
                RECONNECT_DELAY_MIN = object.getInt("reconnect_delay_min");
                RECONNECT_DELAY_MAX = object.getInt("reconnect_delay_max");
            } catch (Exception e) {
                Logger.error("Failed to load config!");
                e.printStackTrace();
            }
        }
    }

    public void save() {
        try {
            CONFIG.delete();
            JSONObject object = new JSONObject();
            object.put("username", USERNAME);
            object.put("oauth", OAUTH);
            object.put("channel", CHANNEL);
            object.put("owner", OWNER);
            object.put("about", ABOUT);
            object.put("google_api", GOOGLE_API_ID);
            object.put("steam_api", STEAM_API_KEY);
            object.put("client_id", TWITCH_CLIENT_ID);
            object.put("log_file", LOGFILE);
            object.put("reconnect_delay_min", RECONNECT_DELAY_MIN);
            object.put("reconnect_delay_max", RECONNECT_DELAY_MAX);
            FileWriter w = new FileWriter(CONFIG);
            w.write(object.toString(2));
            w.close();
        } catch (Exception e) {
            Logger.error("Failed to save config!");
            e.printStackTrace();
        }
    }

    public File backupFile() {
        String dateString = bakFormat.format(new Date());
        return new File(BASE, "core-" + dateString + ".json");
    }

}
