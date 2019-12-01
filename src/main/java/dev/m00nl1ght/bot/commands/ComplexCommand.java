package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class ComplexCommand extends Command {

    protected final HashMap<String, Command> sub = new HashMap<>();

    protected ComplexCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        String sc = parser.getParam(1);
        Command cmd = sub.get(sc.toLowerCase());
        if (cmd == null) {
            cmd = sub.get("*");
            if (cmd == null) {
                parser.sendResponse("Usage: " + printUsage());
                return;
            }
        }
        if (cmd.isOnCooldown()) return;
        cmd.resetCooldown();
        cmd.stat_total++;
        if (cmd.canExecute(parser)) {
            cmd.stat_fail++; // not ideal, but it works
            cmd.execute(parser);
            cmd.stat_fail--;
        } else {
            Logger.log("CMD -sub_denied " + parser.getSource().getContent());
            cmd.onDenied(parser);
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

    public Command getSubCommand(String name) {
        return sub.get(name);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        JSONObject subData = new JSONObject();
        for (Command c : sub.values()) {
            JSONObject d = new JSONObject();
            c.save(d);
            subData.put(c.name, d);
        }
        data.put("sub", subData);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        JSONObject subData = data.optJSONObject("sub");
        if (subData != null) {
            for (Command c : sub.values()) {
                JSONObject d = subData.optJSONObject(c.name);
                if (d != null) c.load(d);
            }
        }
    }

}
