package dev.m00nl1ght.bot;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.commands.Command;

public class MainListener implements TwirkListener {

    private final Twirk bot;
    private final Profile profile;
    protected final CommandManager commandManager = new CommandManager(this);
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
                Logger.log("CMD @" + message.getUser().getDisplayName() + " " + message.getContent());
                try {
                    cmd.execute(parser);
                } catch (Exception e) {
                    Logger.error("Exception thrown while executing command: " + message.getContent());
                    e.printStackTrace();
                    sendResponse(message.getUser(), "Sorry, an unknown error occured.");
                }
            } else {
                Logger.log("CMD -denied @" + message.getUser().getDisplayName() + " " + message.getContent());
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
        Logger.log("Shutting down...");
        bot.close();
        this.save();
        Logger.log("Finished.");
    }

    public void save() {
        commandManager.save(profile.CORE);
    }

    public void load() {
        commandManager.load(profile.CORE);
    }

    public void backup() {
        commandManager.save(profile.backupFile());
    }

}
