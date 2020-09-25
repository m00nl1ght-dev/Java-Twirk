package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;

import java.net.URLEncoder;

public class GoogleCommand extends TextCommand {

    public static final Type TYPE = new TypeGoogle("google");

    protected GoogleCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        final String[] params = parser.readAll().trim().split(" ");

        String lp = params[params.length - 1];
        boolean hasTarget = false;
        if (lp.startsWith("@")) {
            lp = lp.substring(1);
            hasTarget = true;
        } else if (!lp.isEmpty() && parent.getBot().isUserOnline(lp)) {
            hasTarget = true;
        }

        if (params.length < (hasTarget ? 2 : 1)) {
            throw new CommandException("missing search query");
        }

        String ret = "www.google.com/search?q=" + URLEncoder.encode(params[0]);
        for (int i = 1; i < params.length - (hasTarget ? 1 : 0); i++) {
            if (params[i].isEmpty()) continue;
            ret += "+" + URLEncoder.encode(params[i].trim());
        }

        if (hasTarget) {
            parent.sendMessage("@" + lp + " " + pattern.build(parser) + " " + ret);
        } else {
            parser.send(pattern.build(parser) + " " + ret);
        }
    }

    public static class TypeGoogle extends Type<GoogleCommand> {

        protected TypeGoogle(String name) {
            super(name);
            defaultCooldown = 10000;
        }

        @Override
        public GoogleCommand build(MainListener parent, String name, String pattern) {
            GoogleCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern);
            return sc;
        }

        @Override
        protected GoogleCommand createInstance(MainListener parent, String name) {
            return new GoogleCommand(this, parent, name);
        }

    }

}
