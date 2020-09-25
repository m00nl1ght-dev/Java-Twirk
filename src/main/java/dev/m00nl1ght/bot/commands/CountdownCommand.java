package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CountdownCommand extends Command {

    public static final Command.Type TYPE = new Type("countdown");
    private static final SimpleDateFormat createFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
    public static final Function<String, CommandPattern.Segment> SEG_COUNTDOWN = CountdownCommand::countdownSegment;

    protected CommandPattern pattern;
    private long endTime;

    protected CountdownCommand(Command.Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        parser.send(pattern.build(parser));
    }

    private static CommandPattern.Segment countdownSegment(String arg) {
        if (arg.equals("cd") || arg.equals("countdown"))
            return (p) -> ((CountdownCommand) p.getCommand()).timeString();
        return null;
    }

    private String timeString() {
        if (endTime == 0) return "<null>";
        long t = endTime - System.currentTimeMillis();
        if (t <= 0) return "0s";
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
        this.endTime = data.getLong("endTime");
        this.pattern = CommandPattern.compile(data.getString("pattern"), CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_COUNTDOWN);

    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("endTime", this.endTime);
        data.put("pattern", this.pattern.source());
    }

    public static class Type extends Command.Type<CountdownCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public CountdownCommand build(MainListener parent, String name, String pattern) {
            CountdownCommand sc = super.build(parent, name, pattern);

            int d = pattern.indexOf(' ');
            if (d < 0) throw new CommandException("Missing timestamp");
            final String timeString = pattern.substring(0, d);

            pattern = pattern.substring(d + 1);
            sc.pattern = CommandPattern.compile(pattern, CommandPattern.SEG_STRING, CommandPattern.SEG_SENDER, SEG_COUNTDOWN);

            try {
                sc.endTime = Long.parseLong(timeString);
            } catch (NumberFormatException e) {
                try {
                    sc.endTime = createFormat.parse(timeString).getTime();
                } catch (ParseException parseException) {
                    throw new CommandException("Invalid timestamp");
                }
            }
            return sc;
        }

        @Override
        protected CountdownCommand createInstance(MainListener parent, String name) {
            return new CountdownCommand(this, parent, name);
        }

    }

}
