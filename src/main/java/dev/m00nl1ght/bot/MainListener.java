package dev.m00nl1ght.bot;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.core.CoreCommand;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.HashMap;

public class MainListener implements TwirkListener {

    private final Twirk bot;
    private final Profile profile;
    protected final HashMap<String, Command> commands = new HashMap<>();
    protected final CommandParser parser = new CommandParser(this);
    protected boolean active = true;

    public MainListener(Twirk bot, Profile profile) {
        this.bot = bot;
        this.profile = profile;
    }

    @Override
    public void onPrivMsg(TwitchMessage message) {
        Command cmd = parser.parse(message);
        if (cmd != null) {
            if (cmd.canExecute(message)) {
                Logger.log("CMD " + message.getContent());
                try {
                    cmd.execute(parser);
                } catch (Exception e) {
                    Logger.error("Exception thrown while executing command: " + message.getContent());
                    e.printStackTrace();
                    sendResponse(message.getUser(), "Sorry, an unknown error occured.");
                }
            } else {
                Logger.log("CMD -denied " + message.getContent());
                cmd.onDenied(message);
            }
        }
    }

    public void sendResponse(String msg) {
        Logger.log("OUT " + msg);
        bot.channelMessage(msg);
    }

    public void sendResponse(TwitchUser user, String msg) {
        sendResponse("@" + user.getDisplayName() + " " + msg);
    }

    @Override
    public void onConnect() {
        Logger.log("Sucessfully connected.");
    }

    @Override
    public void onDisconnect() {
        int delay = profile.RECONNECT_DELAY_MIN;
        Logger.warn("Disconnected! Trying to reconnect...");
        while (true) {
            try {
                if(bot.connect()) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.warn("Failed to reconnect! Trying again in " + delay + " ms");
            try {Thread.sleep(delay);} catch (Exception e) {}
            delay *= 2;
            if (delay > profile.RECONNECT_DELAY_MAX) delay = profile.RECONNECT_DELAY_MAX;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean act) {
        this.active = act;
    }

    public void exit() {
        Logger.log("Shutting down, as requested by command...");
        bot.close();
    }

    public void load() {
        commands.clear();
        if (profile.CORE.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(profile.CORE));
                JSONObject object = new JSONObject(tokener);

            } catch (Exception e) {
                throw new RuntimeException("Failed to load commands!", e);
            }
        } else {
            Logger.log("Core profile does not exist, creating default one.");
            commands.put("mb", new CoreCommand(this, "mb"));
            this.save();
        }
    }

    public void save() {

    }

}
