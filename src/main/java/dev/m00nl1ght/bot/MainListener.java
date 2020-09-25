package dev.m00nl1ght.bot;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.clearChat.ClearChat;
import com.gikk.twirk.types.hostTarget.HostTarget;
import com.gikk.twirk.types.mode.Mode;
import com.gikk.twirk.types.notice.Notice;
import com.gikk.twirk.types.roomstate.Roomstate;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.usernotice.Usernotice;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.answers.AnswersManager;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.listener.MsgListener;
import dev.m00nl1ght.bot.listener.MsgListenerTypes;
import dev.m00nl1ght.bot.util.TwitchAPI;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainListener implements TwirkListener {

    private final Twirk bot;
    private final Profile profile;
    private final TwitchAPI twitchAPI;
    public final CommandManager commandManager = new CommandManager(this);
    public final AnswersManager answersManager = new AnswersManager(this);
    public final CommandParser parser = new CommandParser(this);
    public final Map<String, MsgListener> msgListeners = new HashMap<>();
    protected boolean active = true;
    public boolean logVerbose = false;

    public MainListener(Twirk bot, Profile profile) {
        this.bot = bot;
        this.profile = profile;
        this.twitchAPI = new TwitchAPI(profile.TWITCH_CLIENT_ID);
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
            }
            else {
                Logger.log("CMD -denied @" + message.getUser().getDisplayName() + " " + message.getContent());
                cmd.onDenied(parser);
            }
        } else if (!checkMsgListeners(message) && answersManager.onMessage(message)) {
            Logger.log("AWQ " + message.getContent());
        } else if (logVerbose) {
            Logger.log("MSG @" + message.getUser().getDisplayName() + " " + message.getContent());
        }
    }

    private boolean checkMsgListeners(TwitchMessage msg) {
        for (MsgListener listener : msgListeners.values())
            if (listener.onMsg(msg)) return true;
        return false;
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
    public void onClearChat(ClearChat clearChat) {
        Logger.warn("PRG @" + clearChat.getTarget() + " ( " + clearChat.getDuration() + " ) " + clearChat.getReason());
    }

    @Override
    public void onRoomstate(Roomstate roomstate) {
        if (logVerbose)
            Logger.warn("RMS LANG=" + roomstate.getBroadcasterLanguage() + " 9k=" + roomstate.get9kMode() + " SLOWMODE=" + roomstate.getSlowModeTimer() + " SUBMODE=" + roomstate.getSubMode());
    }

    @Override
    public void onMode(Mode mode) {
        Logger.warn("MOD @" + mode.getUser() + " " + mode.getEvent());
    }

    @Override
    public void onHost(HostTarget hostNotice) {
        Logger.warn("HOS @" + hostNotice.getTarget() + " " + hostNotice.getMode() + " " + hostNotice.getViewerCount());
    }

    @Override
    public void onUnknown(String unformatedMessage) {
        if (logVerbose)
            Logger.error("UNK " + unformatedMessage);
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
        save(profile.CORE);
    }

    public void save(File target) {
        try {
            target.delete();
            JSONObject object = new JSONObject();
            object.put("cmd", commandManager.save());
            object.put("aws", answersManager.save());
            object.put("listeners", MsgListenerTypes.save(this));
            FileWriter w = new FileWriter(target);
            w.write(object.toString(2));
            w.close();
        } catch (Exception e) {
            Logger.error("Failed to save profile!");
            e.printStackTrace();
        }
    }

    public void load() {
        load(profile.CORE);
    }

    public void load(File target) {
        if (target.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(target));
                JSONObject object = new JSONObject(tokener);
                if (!object.has("cmd")) { // old format
                    commandManager.load(object);
                } else {
                    MsgListenerTypes.load(this, object.optJSONObject("listeners"));
                    commandManager.load(object.getJSONObject("cmd"));
                    answersManager.load(object.getJSONObject("aws"));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load profile!", e);
            }
        } else {
            Logger.log("Core profile does not exist, creating default one.");
            commandManager.loadDefault();
            this.save(target);
        }
    }

    public void backup() {
        save(profile.backupFile());
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

    public String getSteamAPI() {
        return profile.STEAM_API_KEY;
    }

    public File getDataFile(String name) {
        return new File(profile.BASE, name);
    }

    public void addMsgListener(MsgListener listener) {
        msgListeners.put(listener.getName(), listener);
    }

    public <T extends MsgListener> T getOrCreateListener(String name, String type, Class<T> clazz) {
        MsgListener msgListener = msgListeners.get(name);
        if (msgListener == null) {
            final Supplier<MsgListener> factory = MsgListenerTypes.get(type);
            if (factory == null) throw new IllegalStateException("missing listener type: " + type);
            msgListener = factory.get();
            addMsgListener(msgListener);
        }
        return (T) msgListener;
    }

    public boolean removeMsgListener(String name) {
        return msgListeners.remove(name) != null;
    }

}
