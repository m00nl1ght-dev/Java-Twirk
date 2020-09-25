package dev.m00nl1ght.bot;

import dev.m00nl1ght.bot.listener.UserBufferListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandPattern {

    public static final Function<String, Segment> SEG_STRING = CommandPattern::segmentString;
    public static final Function<String, Segment> SEG_ARGUMENT = CommandPattern::segmentArgument;
    public static final Function<String, Segment> SEG_ARGUMENT_USERNAME = CommandPattern::segmentArgumentUsername;
    public static final Function<String, Segment> SEG_RANDOM_USER = CommandPattern::segmentRandomUser;
    public static final Function<String, Segment> SEG_SENDER = CommandPattern::segmentSender;
    protected static final Pattern ARG_PATTERN = Pattern.compile("<(.*?)>");
    protected static final Segment EMPTY_SEGMENT = (p) -> "";

    protected final List<Segment> segments;
    protected final String pattern;

    private CommandPattern(String pattern, List<Segment> segments) {
        this.segments = segments;
        this.pattern = pattern;
    }

    public static CommandPattern compile(String pattern) {
        return compile(pattern, SEG_STRING, SEG_ARGUMENT, SEG_ARGUMENT_USERNAME, SEG_SENDER, SEG_RANDOM_USER);
    }

    public static CommandPattern compile(String pattern, Function<String, Segment>... segmentProviders) {
        List<Segment> list = new ArrayList<>();
        Matcher m = ARG_PATTERN.matcher(pattern);
        int lastIdx = 0;
        while (m.find()) {
            if (m.start() > 0) list.add(rawString(pattern.substring(lastIdx, m.start())));
            String g = m.group(1);
            list.add(getSegment(g, segmentProviders));
            lastIdx = m.end();
        }
        if (lastIdx <= pattern.length()) list.add(rawString(pattern.substring(lastIdx)));
        return new CommandPattern(pattern, list);
    }

    private static Segment getSegment(String arg, Function<String, Segment>... segmentProviders) {
        int ori = arg.indexOf('|');
        String s = ori < 0 ? arg : arg.substring(0, ori);
        Segment seg = null;
        for (Function<String, Segment> provider : segmentProviders) {
            seg = provider.apply(s);
            if (seg != null) break;
        }
        if (seg == null) throw new CommandException("Invalid pattern segment: <" + s + ">");
        if (ori < 0) return seg;
        final Segment finalSeg = seg;
        return (p) -> applyOr(p, finalSeg, getSegment(arg.substring(ori + 1), segmentProviders));
    }

    private static String applyOr(CommandParser p, Segment a, Segment b) {
        String res = a.apply(p);
        return res == null ? b.apply(p) : res;
    }

    private static Segment segmentString(String arg) {
        if (!arg.startsWith("\"") || !arg.endsWith("\"")) return null;
        return rawString(arg.substring(1, arg.length() - 1));
    }

    private static Segment rawString(String str) {
        return (p) -> str;
    }

    private static Segment segmentArgument(String arg) {
        try {
            if (arg.endsWith("+")) {
                int id = Integer.parseInt(arg.substring(0, arg.length() - 1));
                return (p) -> p.getAllAfter(id);
            } else {
                int id = Integer.parseInt(arg);
                return (p) -> p.getParamOrNull(id);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Segment segmentArgumentUsername(String arg) {
        try {
            if (!arg.startsWith("@")) return null;
            arg = arg.substring(1);
            int id = Integer.parseInt(arg);
            return (p) -> {
                String r = p.getParamOrNull(id);
                return r.startsWith("@") ? r.substring(1) : r;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static Segment segmentRandomUser(String arg) {
        if (arg.equals("r")) {
            return (p) -> {
                final UserBufferListener userBuffer = p.getParent().getOrCreateListener(
                        UserBufferListener.ID, UserBufferListener.ID, UserBufferListener.class);
                final String ret = userBuffer.getRandom();
                return ret == null ? "" : ("@" + ret);
            };
        } else {
            return null;
        }
    }

    private static Segment segmentSender(String arg) {
        if (arg.equals("s") || arg.equals("sender")) return (p) -> p.getSource().getUser().getDisplayName();
        return null;
    }

    public String build(CommandParser parser) {
        StringBuilder sb = new StringBuilder();
        for (Segment seg : segments) {
            String part = seg.apply(parser);
            if (part == null) throw new CommandException("failed to resolve segment");
            sb.append(part);
        }
        return sb.toString();
    }

    public String source() {
        return this.pattern;
    }

    public interface Segment {

        String apply(CommandParser p);

    }

}
