package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

public class CounterCommand extends ComplexCommand {

    public static final Type TYPE = new Type("counter");
    public static final Function<String, CommandPattern.Segment> SEG_COUNTER = CounterCommand::counterSegment;

    protected CommandPattern pattern;
    protected int counter = 0;
    protected int modifyOnGet = 0;

    protected CounterCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Get(parent, ""));
        this.addSubCommand(new ModifyPlus(parent, "+"));
        this.addSubCommand(new ModifyMinus(parent, "-"));
        this.addSubCommand(new ModifySet(parent, "="));
        this.addSubCommand(new SetModOnGet(parent, "%"));
    }

    private static CommandPattern.Segment counterSegment(String arg) {
        if (arg.equals("c") || arg.equals("counter"))
            return (p) -> Integer.toString(((CounterCommand) p.getCommand()).counter);
        return null;
    }

    private static int toNumber(String s, boolean onlyPositive) {
        try {
            int i = Integer.parseInt(s);
            if (onlyPositive && i < 0) throw new IllegalArgumentException("must be positive");
            return i;
        } catch (Exception e) {
            throw new CommandException("Invalid number");
        }
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.counter = data.optInt("counter", this.counter);
        this.modifyOnGet = data.optInt("modifier", this.modifyOnGet);
        this.pattern = CommandPattern.compile(data.getString("pattern"), CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_COUNTER);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("counter", this.counter);
        data.put("modifier", this.modifyOnGet);
        data.put("pattern", this.pattern.source());
    }

    @Override
    public String printUsage() {
        return "!" + name + " [+|-|=|%] [value]";
    }

    public static class Type extends Command.Type<CounterCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public CounterCommand build(MainListener parent, String name, String pattern) {
            CounterCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern, CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_COUNTER);
            sc.verboseFeedback = false;
            return sc;
        }

        @Override
        protected CounterCommand createInstance(MainListener parent, String name) {
            return new CounterCommand(this, parent, name);
        }

    }

    protected class Get extends Command {

        protected Get(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            counter += modifyOnGet;
            if (counter < 0) counter = 0;
            parser.send(pattern.build(parser));
        }

    }

    protected class ModifyPlus extends Command {

        protected ModifyPlus(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            String p = parser.nextParam();
            if (p.isEmpty()) {
                counter++;
            } else {
                counter += toNumber(p, true);
            }
            parser.send(pattern.build(parser));
        }

    }

    protected class ModifyMinus extends Command {

        protected ModifyMinus(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            String p = parser.nextParam();
            if (p.isEmpty()) {
                counter--;
            } else {
                counter -= toNumber(p, true);
            }
            if (counter < 0) counter = 0;
            parser.send(pattern.build(parser));
        }

    }

    protected class ModifySet extends Command {

        protected ModifySet(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            counter = toNumber(parser.nextParam(), true);
            parser.send(pattern.build(parser));
        }

    }

    protected class SetModOnGet extends Command {

        protected SetModOnGet(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            modifyOnGet = toNumber(parser.nextParam(), false);
            parser.sendResponse("Updated modifier.");
        }

    }

}
