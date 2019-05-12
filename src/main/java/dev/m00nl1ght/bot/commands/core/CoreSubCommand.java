package dev.m00nl1ght.bot.commands.core;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;

public abstract class CoreSubCommand extends Command {

    protected CoreSubCommand(MainListener parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean canExecute(TwitchMessage source) {
        return perm == USER_LEVEL.DEFAULT || source.getUser().hasPermission(perm);
    }

    @Override
    public void onDenied(TwitchMessage source) {
        if (verboseFeedback) parent.sendResponse("@" + source.getUser().getDisplayName() + " You don't have permission to use this command.");
    }

}
