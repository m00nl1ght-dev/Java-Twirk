package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

class CmdBackup extends CoreSubCommand {

    protected CmdBackup(MainListener parent, String name) {
        super(parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        parent.sendResponse(parser.getSource().getUser(), "Creating backup...");
        parent.backup();
    }

}
