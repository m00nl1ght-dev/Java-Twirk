package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;

public class InfoCommand extends TextCommand {

    public static final Type TYPE = new TypeInfo("info");

    protected InfoCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        String to = parser.getParam(1);
        if (to.isEmpty()) {
            parser.send(pattern.build(parser));
        } else {
            if (to.startsWith("@")) to = to.substring(1);
            parser.send("@" + to + " " + pattern.build(parser));
        }
    }

    public static class TypeInfo extends Command.Type<InfoCommand> {

        protected TypeInfo(String name) {
            super(name);
            defaultCooldown = 10000;
        }

        @Override
        public InfoCommand build(MainListener parent, String name, String pattern) {
            InfoCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern);
            return sc;
        }

        @Override
        protected InfoCommand createInstance(MainListener parent, String name) {
            return new InfoCommand(this, parent, name);
        }

    }

}
