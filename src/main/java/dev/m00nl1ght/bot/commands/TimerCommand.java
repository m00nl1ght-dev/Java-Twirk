package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TimerCommand extends ComplexCommand {

    public static final Type TYPE = new Type("timer");
    public static final Type TYPE_CASUAL = new TypeCasual("timer_casual");
    public static final Function<String, CommandPattern.Segment> SEG_TIMER = TimerCommand::elapsedTimeSegment;

    protected CommandPattern pattern;
    protected long startTime = 0;

    protected TimerCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Get(parent, ""));
        this.addSubCommand(new Start(parent, "start"));
        this.addSubCommand(new Stop(parent, "stop"));
    }

    private static CommandPattern.Segment elapsedTimeSegment(String arg) {
        if (arg.equals("t") || arg.equals("time"))
            return (p) -> ((TimerCommand) p.getCommand()).timeString();
        return null;
    }

    private String timeString() {
        if (startTime == 0) return "0s";
        long t = System.currentTimeMillis() - startTime;
        long h = TimeUnit.MILLISECONDS.toHours(t);
        t -= TimeUnit.HOURS.toMillis(h);
        long m = TimeUnit.MILLISECONDS.toMinutes(t);
        t -= TimeUnit.MINUTES.toMillis(m);
        long s = TimeUnit.MILLISECONDS.toSeconds(t);
        String r = s + "s";
        if (m > 0) r = m + "min " + r;
        if (h > 0) r = h + "h " + r;
        return r;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.startTime = data.optLong("start", this.startTime);
        this.pattern = CommandPattern.compile(data.getString("pattern"), CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_TIMER);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("start", this.startTime);
        data.put("pattern", this.pattern.source());
    }

    @Override
    public String printUsage() {
        return "!" + name + " [start]";
    }

    public static class Type extends Command.Type<TimerCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public TimerCommand build(MainListener parent, String name, String pattern) {
            TimerCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern, CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_TIMER);
            sc.verboseFeedback = false;
            return sc;
        }

        @Override
        protected TimerCommand createInstance(MainListener parent, String name) {
            return new TimerCommand(this, parent, name);
        }

    }

    public static class TypeCasual extends Type {

        protected TypeCasual(String name) {
            super(name);
        }

        @Override
        public TimerCommand build(MainListener parent, String name, String pattern) {
            TimerCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern, CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_TIMER);
            sc.verboseFeedback = false;
            sc.addSubCommand(sc.new Get(parent, "*"));
            return sc;
        }

        @Override
        protected TimerCommand createInstance(MainListener parent, String name) {
            TimerCommand sc = new TimerCommand(this, parent, name);
            sc.addSubCommand(sc.new Get(parent, "*"));
            return sc;
        }

    }

    protected class Get extends Command {

        protected Get(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            if (startTime != 0) {
                parser.send(pattern.build(parser));
            } else {
                parser.sendResponse("No timer active.");
            }
        }

    }

    protected class Start extends Command {

        protected Start(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
                parser.sendResponse("Timer started.");
            } else {
                startTime = System.currentTimeMillis();
                parser.sendResponse("Timer restarted.");
            }
        }

    }

    protected class Stop extends Command {

        protected Stop(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            if (startTime != 0) {
                parser.sendResponse("Timer stopped: " + timeString());
                startTime = 0;
            } else {
                parser.sendResponse("No timer active.");
            }
        }

    }

}
