package dev.m00nl1ght.bot.commands.core;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.ComplexCommand;

public class CoreCommand extends ComplexCommand {

    public CoreCommand(MainListener parent, String name) {
        super(parent, name);
        this.perm = USER_LEVEL.OWNER;
        this.addSubCommand(new CmdStart(parent, "start"));
        this.addSubCommand(new CmdStop(parent, "stop"));
        this.addSubCommand(new CmdExit(parent, "exit"));
        this.addSubCommand(new CmdExit(parent, "reload"));
    }

    @Override
    public boolean canExecute(TwitchMessage source) {
        return perm == USER_LEVEL.DEFAULT || source.getUser().hasPermission(perm);
    }

    @Override
    public void onDenied(TwitchMessage source) {
        if (verboseFeedback) parent.sendResponse("@" + source.getUser().getDisplayName() + " You don't have permission to use this command.");
    }

    private class CmdStart extends Command {

        protected CmdStart(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            if (parent.isActive()) {
                parent.sendResponse(parser.getSource().getUser(), "The bot is already active.");
            } else {
                parent.setActive(true);
                parent.sendResponse(parser.getSource().getUser(), "Started.");
            }
        }

    }

    private class CmdStop extends Command {

        protected CmdStop(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            if (parent.isActive()) {
                parent.setActive(false);
                parent.sendResponse(parser.getSource().getUser(), "Stopped.");
            } else {
                parent.sendResponse(parser.getSource().getUser(), "The bot is already stopped.");
            }
        }

    }

    private class CmdExit extends Command {

        protected CmdExit(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            parent.sendResponse(parser.getSource().getUser(), "Disconnecting...");
            parent.exit();
        }

    }

    private class CmdReload extends Command {

        protected CmdReload(MainListener parent, String name) {
            super(parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            parent.sendResponse(parser.getSource().getUser(), "Reloading resources...");
            parent.load();
        }

    }

}
