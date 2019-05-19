package dev.m00nl1ght.bot.commands.core;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.ComplexCommand;

public class CoreCommand extends ComplexCommand {

    public static final Type TYPE = new Type("core");

    public CoreCommand(MainListener parent, String name) {
        super(TYPE, parent, name);
        this.addSubCommand(new About(parent, ""));
        CoreMaintanance.register(this);
        CoreCmdManagement.register(this);
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

    public static class Type extends Command.Type<CoreCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected CoreCommand createInstance(MainListener parent, String name) {
            return new CoreCommand(parent, name);
        }

    }

    private static class About extends CoreSubCommand {

        protected About(MainListener parent, String name) {
            super(parent, name);
            this.perm = USER_LEVEL.DEFAULT.value;
            this.cooldown = 15000;
        }

        @Override
        public void execute(CommandParser parser) {
            String info = parent.getBotInfo();
            if (!info.isEmpty()) parser.send(info);
        }

    }

}
