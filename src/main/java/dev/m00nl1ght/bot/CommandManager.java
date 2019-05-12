package dev.m00nl1ght.bot;

import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.core.CoreCommand;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class CommandManager {

    private final HashMap<String, Command> types = new HashMap<>();
    private final HashMap<String, Command> commands = new HashMap<>();
    private final MainListener core;

    public CommandManager(MainListener core) {
        this.core = core;
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public void load(File traget) {
        commands.clear();
        if (traget.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(traget));
                JSONObject object = new JSONObject(tokener);
                //TODO
            } catch (Exception e) {
                throw new RuntimeException("Failed to load commands!", e);
            }
        } else {
            Logger.log("Core profile does not exist, creating default one.");
            commands.put("mb", new CoreCommand(core, "mb"));
            this.save(traget);
        }
    }

    public void save(File target) {
        try {
            target.delete();
            JSONObject object = new JSONObject();
            //TODO
            FileWriter w = new FileWriter(target);
            w.write(object.toString());
            w.close();
        } catch (Exception e) {
            Logger.error("Failed to save commands!");
            e.printStackTrace();
        }
    }

}
