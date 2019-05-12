package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;

class CmdExit extends CoreSubCommand {

    protected CmdExit(MainListener parent, String name) {
        super(parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        parent.sendResponse(parser.getSource().getUser(), "Disconnecting...");
        try {Thread.sleep(1000);} catch (InterruptedException e) {}
        parent.exit();
    }

}
