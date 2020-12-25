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
        return parse(source, source.getContent(), false);
    }

    public Command parse(TwitchMessage source, String cmd, boolean isWhisper) {
        if (!cmd.startsWith("!")) return null;
        this.source = source;
        this.params.clear();
        this.data = cmd;
        this.isWhisper = isWhisper;
        pos = 1;
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        if (pos <= 1) return null;
        this.command = parent.commandManager.getCommand(data.substring(1, pos).toLowerCase());
        return command;
    }

    public Command parseWhisper(TwitchMessage source, int offsetPos) {
        return parse(source, source.getContent().substring(offsetPos), true);
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
        boolean q = false;
        while (pos < data.length() && (q || !Character.isWhitespace(data.charAt(pos)))) {
            if (data.charAt(pos) == '"') q = !q;
            pos++;
        }
        if (i >= pos) return "";
        String res = data.substring(i, pos).replace("\"", "");
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
            throw new CommandException("Invalid argument: must be a number");
        }
    }

    public int nextParamInt() {
        try {
            String n = nextParam();
            return Integer.parseInt(n);
        } catch (Exception e) {
            throw new CommandException("Invalid argument: must be a number");
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
        if (msg.length() >= 500) {
            msg = "Failed to send response (too  long)";
        }

        if (isWhisper) {
            parent.sendWhisper(source.getUser(), msg.trim());
        } else {
            parent.sendMessage(msg.trim());
        }
    }

    public void sendResponse(String msg) {
        if (msg.length() >= 500) {
            msg = "Failed to send response (too  long)";
        }

        if (isWhisper) {
            parent.sendWhisper(source.getUser(), msg.trim());
        } else {
            parent.sendMessage(source.getUser(), msg.trim());
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
