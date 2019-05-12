package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

class CmdStart extends CoreSubCommand {

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
