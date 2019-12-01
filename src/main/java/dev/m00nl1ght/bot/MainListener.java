package dev.m00nl1ght.bot;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.notice.Notice;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.usernotice.Usernotice;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.commands.Command;

import java.io.File;

public class MainListener implements TwirkListener {

    private final Twirk bot;
    private final Profile profile;
    public final CommandManager commandManager = new CommandManager(this);
    public final CommandParser parser = new CommandParser(this);
    protected boolean active = true;
    public boolean logPrvMsg = false;

    public MainListener(Twirk bot, Profile profile) {
        this.bot = bot;
        this.profile = profile;
    }

    @Override
    public void onPrivMsg(TwitchMessage message) {
        Command cmd = parser.parse(message);
        if (cmd != null) {
            if (cmd.isOnCooldown()) return;
            if (cmd.canExecute(parser)) {
                Logger.log("CMD @" + message.getUser().getDisplayName() + " " + message.getContent());
                cmd.resetCooldown();
                cmd.stat_total++;
                try {
                    cmd.execute(parser);
                } catch (CommandException ce) {
                    cmd.stat_fail++;
                    Logger.warn("CER " + ce.getMessage());
                    if (parser.verboseFeedback())
                        parser.sendResponse("Error: " + ce.getMessage());
                } catch (Exception e) {
                    cmd.stat_fail++;
                    Logger.error("CFE " + e.getMessage());
                    e.printStackTrace();
                    if (parser.verboseFeedback())
                        parser.sendResponse("Sorry, an unknown error occured.");
                }
            } else {
                Logger.log("CMD -denied @" + message.getUser().getDisplayName() + " " + message.getContent());
                cmd.onDenied(parser);
            }
        } else if (logPrvMsg) {
            Logger.log("MSG @" + message.getUser().getDisplayName() + " " + message.getContent());
        } else if (commandManager.getHighlightTerm() != null && message.getContent().toLowerCase().contains(commandManager.getHighlightTerm())) {
            Logger.log("MSG @" + message.getUser().getDisplayName() + " " + message.getContent());
        }
    }

    @Override
    public void onWhisper(TwitchMessage message) {
        Command cmd = parser.parseWhisper(message);
        if (cmd != null) {
            if (cmd.canExecute(parser)) {
                Logger.log("CMD -whisper @" + message.getUser().getDisplayName() + " " + message.getContent());
                cmd.stat_total++;
                try {
                    cmd.execute(parser);
                } catch (CommandException ce) {
                    cmd.stat_fail++;
                    Logger.warn("CER " + ce.getMessage());
                    if (parser.verboseFeedback())
                        parser.sendResponse("Error: " + ce.getMessage());
                } catch (Exception e) {
                    cmd.stat_fail++;
                    Logger.error("CFE " + e.getMessage());
                    e.printStackTrace();
                    if (parser.verboseFeedback())
                        parser.sendResponse("Sorry, an unknown error occured.");
                }
            } else {
                Logger.log("CMD -whisper -denied @" + message.getUser().getDisplayName() + " " + message.getContent());
                cmd.onDenied(parser);
            }
        } else {
            Logger.log("UWM @" + message.getUser().getDisplayName() + " " + message.getContent());
        }
    }

    @Override
    public void onNotice(Notice notice) {
        Logger.warn("TIN " + notice.getMessage());
    }

    @Override
    public void onUsernotice(TwitchUser user, Usernotice usernotice) {
        commandManager.onSubEvent(user, usernotice);
    }

    public void sendMessage(String msg) {
        Logger.log("OUT " + msg);
        bot.channelMessage(msg);
    }

    public void sendMessage(TwitchUser user, String msg) {
        sendMessage("@" + user.getDisplayName() + " " + msg);
    }

    public void sendWhisper(TwitchUser user, String msg) {
        Logger.log("WPO @" + user.getDisplayName() + " " + msg);
        bot.whisper(user, msg);
    }

    @Override
    public void onConnect() {
        Logger.log("Sucessfully connected.");
    }

    @Override
    public void onDisconnect() {
        int delay = profile.RECONNECT_DELAY_MIN;
        Logger.warn("Disconnected! Trying to reconnect...");
        while (!bot.isConnected()) {
            try {
                if (bot.connect()) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.warn("Failed to reconnect! Trying again in " + delay + " ms");
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
            }
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
        Logger.dispose();
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

    public String getBotInfo() {
        return profile.ABOUT;
    }

    public Twirk getBot() {
        return bot;
    }

    public String getGoogleAPI() {
        return profile.GOOGLE_API_ID;
    }

    public File getDataFile(String name) {
        return new File(profile.BASE, name);
    }

}
