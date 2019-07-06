package dev.m00nl1ght.bot;

import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.commands.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    private final MainListener parent;
    private TwitchMessage source;
    private boolean isWhisper = false;
    private String data;
    private int pos;
    private List<String> params = new ArrayList<>(10);
    private Command command;

    public CommandParser(MainListener parent) {
        this.parent = parent;
    }

    public Command parse(TwitchMessage source) {
        if (!source.getContent().startsWith("!")) return null;
        this.source = source;
        this.params.clear();
        this.data = source.getContent();
        this.isWhisper = false;
        pos = 1;
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        if (pos <= 1) return null;
        this.command = parent.commandManager.getCommand(data.substring(1, pos).toLowerCase());
        return command;
    }

    public Command parseWhisper(TwitchMessage source) {
        Command res = parse(source);
        this.isWhisper = true;
        return res;
    }

    public String getParam(int id) {
        if (params.size() >= id) return params.get(id - 1);
        String p = nextParam();
        while (params.size() < id && !p.isEmpty()) p = nextParam();
        return p;
    }

    public String getParamOrNull(int id) {
        String p = getParam(id);
        return p.isEmpty() ? null : p;
    }

    public String nextParam() {
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        int i = pos;
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        if (i >= pos) return "";
        String res = data.substring(i, pos);
        params.add(res);
        return res;
    }

    public void skip() {
        if (!params.isEmpty()) params.remove(params.size()-1);
    }

    public int nextParamInt(int or) {
        return intOr(this.nextParam(), or);
    }

    public static int intOr(String s, int or) {
        if (s.isEmpty()) return or;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            throw new CommandException("not a number: " + s);
        }
    }

    public String readAll() {
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        if (pos >= data.length()) return "";
        return data.substring(pos);
    }

    public String getAllAfter(int id) {
        String s = getParamOrNull(id), r; id++;
        if (s == null) return null;
        while (!(r = getParam(id)).isEmpty()) {
            s += " " + r; id++;
        }
        return s;
    }

    public boolean verboseFeedback() {
        return isWhisper || command.verboseFeedback();
    }

    public boolean isWhisper() {
        return isWhisper;
    }

    public void send(String msg) {
        if (isWhisper) {
            parent.sendWhisper(source.getUser(), msg);
        } else {
            parent.sendMessage(msg);
        }
    }

    public void sendResponse(String msg) {
        if (isWhisper) {
            parent.sendWhisper(source.getUser(), msg);
        } else {
            parent.sendMessage(source.getUser(), msg);
        }
    }

    public Command getCommand() {
        return command;
    }

    public TwitchMessage getSource() {
        return source;
    }

    public MainListener getParent() {
        return parent;
    }

}
