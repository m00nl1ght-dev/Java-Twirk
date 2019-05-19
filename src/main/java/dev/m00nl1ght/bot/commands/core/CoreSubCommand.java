package dev.m00nl1ght.bot.commands.core;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;

public abstract class CoreSubCommand extends Command {

    protected CoreSubCommand(MainListener parent, String name) {
        super(null, parent, name);
    }

    @Override
    public boolean canExecute(CommandParser parser) {
        return parser.getSource().getUser().hasPermission(perm);
    }

    @Override
    public void onDenied(CommandParser parser) {
        if (verboseFeedback)
            parser.sendResponse("You don't have permission to use this command.");
    }

}
