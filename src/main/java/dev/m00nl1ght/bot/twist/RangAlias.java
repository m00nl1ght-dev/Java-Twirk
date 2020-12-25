package dev.m00nl1ght.bot.twist;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;

public class RangAlias extends Command {

    public static final Type TYPE = new Type("twist_rang");

    protected RangAlias(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        if (parser.nextParam().isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            parent.sendMessage("!rang " + parser.getSource().getUser().getDisplayName());
        }
    }

    public static class Type extends Command.Type<RangAlias> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected RangAlias createInstance(MainListener parent, String name) {
            return new RangAlias(this, parent, name);
        }

    }

}
