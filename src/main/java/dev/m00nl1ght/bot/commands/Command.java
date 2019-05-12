package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONObject;

public abstract class Command {

    protected final MainListener parent;
    protected final String name;
    protected USER_LEVEL perm = USER_LEVEL.DEFAULT;
    protected boolean verboseFeedback = true;

    protected Command(MainListener parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public abstract void execute(CommandParser parser);

    public void load(JSONObject obj) {}

    public void save(JSONObject obj) {}

    public boolean canExecute(TwitchMessage source) {
        return parent.isActive() && (perm == USER_LEVEL.DEFAULT || source.getUser().hasPermission(perm));
    }

    public void onDenied(TwitchMessage source) {
        if (verboseFeedback && parent.isActive()) parent.sendResponse(source.getUser(), "You don't have permission to use this command.");
    }

    public void setVerboseFeedback(boolean verboseFeedback) {
        this.verboseFeedback = verboseFeedback;
    }

}
