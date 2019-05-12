package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

class CmdStop extends CoreSubCommand {

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
