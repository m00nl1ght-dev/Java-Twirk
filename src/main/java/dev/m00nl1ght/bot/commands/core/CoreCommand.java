package dev.m00nl1ght.bot.commands.core;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.ComplexCommand;

public class CoreCommand extends ComplexCommand {

    public CoreCommand(MainListener parent, String name) {
        super(parent, name);
        this.perm = USER_LEVEL.OWNER;
        this.addSubCommand(new CmdStart(parent, "start"));
        this.addSubCommand(new CmdStop(parent, "stop"));
        this.addSubCommand(new CmdExit(parent, "exit"));
        this.addSubCommand(new CmdReload(parent, "reload"));
        this.addSubCommand(new CmdSave(parent, "save"));
        this.addSubCommand(new CmdBackup(parent, "backup"));
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