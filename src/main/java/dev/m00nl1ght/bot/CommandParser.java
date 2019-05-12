package dev.m00nl1ght.bot;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.commands.Command;

public class CommandParser {

    private final MainListener parent;
    private TwitchMessage source;
    private String data;
    private int pos;

    public CommandParser(MainListener parent) {
        this.parent = parent;
    }

    public Command parse(TwitchMessage source) {
        if (!source.getContent().startsWith("!")) return null;
        data = source.getContent(); pos = 1;
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {pos++;}
        if (pos <= 1) return null;
        return parent.commands.get(data.substring(1, pos));
    }

    public String nextParam() {
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {pos++;}
        int i = pos;
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {pos++;}
        if (i >= pos) return "";
        return data.substring(i, pos);
    }

    public String readAll() {
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {pos++;}
        if (pos >= data.length()) return "";
        return data.substring(pos);
    }

    public TwitchMessage getSource() {
        return source;
    }

    public MainListener getParent() {
        return parent;
    }

}
