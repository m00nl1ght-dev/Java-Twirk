package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

class CmdSave extends CoreSubCommand {

    protected CmdSave(MainListener parent, String name) {
        super(parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        parent.sendResponse(parser.getSource().getUser(), "Saving resources...");
        parent.save();
    }

}
