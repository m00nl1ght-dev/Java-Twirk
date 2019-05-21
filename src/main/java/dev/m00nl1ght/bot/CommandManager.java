package dev.m00nl1ght.bot;

import com.gikk.twirk.types.usernotice.Usernotice;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.commands.*;
import dev.m00nl1ght.bot.commands.core.CoreCommand;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandManager {

    private final HashMap<String, Command.Type> types = new HashMap<>();
    private final HashMap<String, Command> commands = new HashMap<>();
    private final List<ChannelEventHandler> subHandlers = new ArrayList<>();
    private final MainListener core;

    public CommandManager(MainListener core) {
        this.core = core;
        registerType(CoreCommand.TYPE);
        registerType(TextCommand.TYPE);
        registerType(InfoCommand.TYPE);
        registerType(CounterCommand.TYPE);
        registerType(QuickvoteCommand.TYPE);
        registerType(TranslateCommand.TYPE);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public void createCommand(String type, String name, String pattern) {
        Command.Type t = types.get(type);
        if (t == null) throw new CommandException("Invalid command type: " + type);
        Command c = t.build(core, name, pattern);
        commands.put(c.name, c);
    }

    public void deleteCommand(String name) {
        Object rem = commands.remove(name);
        if (rem == null) throw new CommandException("Command !" + name + " does not exist");
    }

    public Command getCommandOrSub(String name) {
        int i = name.indexOf(':');
        Command c = commands.get(i < 0 ? name : name.substring(0, i));
        if (i >= 0 && c instanceof ComplexCommand) {
            c = ((ComplexCommand) c).getSubCommand(name.substring(i + 1));
        }
        if (c == null) throw new CommandException("Command !" + name + " does not exist");
        return c;
    }

    public void registerType(Command.Type type) {
        types.put(type.name, type);
    }

    public void load(File target) {
        commands.clear();
        if (target.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(target));
                JSONObject object = new JSONObject(tokener);
                JSONArray comms = object.getJSONArray("commands");
                for (int i = 0; i < comms.length(); i++) {
                    JSONObject co = comms.getJSONObject(i);
                    Command c = types.get(co.getString("type")).load(core, co);
                    commands.put(c.name, c);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load commands!", e);
            }
        } else {
            Logger.log("Core profile does not exist, creating default one.");
            commands.put("mb", new CoreCommand(core, "mb"));
            this.save(target);
        }
    }

    public void save(File target) {
        try {
            target.delete();
            JSONObject object = new JSONObject();
            JSONArray comms = new JSONArray();
            for (Command c : commands.values()) {
                JSONObject co = c.type.save(c);
                co.put("type", c.type.name);
                comms.put(co);
            }
            object.put("commands", comms);
            FileWriter w = new FileWriter(target);
            w.write(object.toString(2));
            w.close();
        } catch (Exception e) {
            Logger.error("Failed to save commands!");
            e.printStackTrace();
        }
    }

    public void onSubEvent(TwitchUser user, Usernotice notice) {
        for (ChannelEventHandler handler : subHandlers) handler.onUsernotice(user, notice);
    }

    public void register(ChannelEventHandler handler) {
        subHandlers.add(handler);
    }

}
