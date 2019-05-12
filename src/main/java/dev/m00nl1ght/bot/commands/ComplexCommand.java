package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;

import java.util.HashMap;

public abstract class ComplexCommand extends Command {

    protected final HashMap<String, Command> sub = new HashMap<>();

    protected ComplexCommand(MainListener parent, String name) {
        super(parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        String sc = parser.nextParam();
        Command cmd = sub.get(sc);
        if (sc.isEmpty() || cmd == null) {
            parent.sendResponse("@" + parser.getSource().getUser().getDisplayName() + " Usage: " + printUsage());
            return;
        }
        if (cmd.canExecute(parser.getSource())) {
            cmd.execute(parser);
        } else {
            Logger.log("CMD -sub_denied " + parser.getSource().getContent());
            cmd.onDenied(parser.getSource());
        }
    }

    public String printUsage() {
        String u = "";
        for (String s : sub.keySet()) {
            if (!u.isEmpty()) u += "|";
            u += s;
        }
        return "!" + name + " <" + u + ">";
    }

    public void addSubCommand(Command cmd) {
        sub.put(cmd.name, cmd);
    }

}
