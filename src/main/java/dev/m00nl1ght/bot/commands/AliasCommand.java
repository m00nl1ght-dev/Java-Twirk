package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

public class AliasCommand extends Command {

    public static final Command.Type TYPE = new Type("alias");

    private String command = "";

    protected AliasCommand(Command.Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser orgParser) {
        String full = command + " " + orgParser.readAll();
        CommandParser parser = new CommandParser(parent);
        Command cmd = parser.parse(orgParser.getSource(), full, orgParser.isWhisper());
        if (cmd != null) {
            if (cmd.isOnCooldown()) return;
            if (cmd.canExecute(parser)) {
                cmd.resetCooldown();
                cmd.stat_total++;
                try {
                    cmd.execute(parser);
                } catch (CommandException ce) {
                    cmd.stat_fail++;
                    Logger.warn("CER " + ce.getMessage());
                    if (parser.verboseFeedback())
                        parser.sendResponse("Error: " + ce.getMessage());
                } catch (Exception e) {
                    cmd.stat_fail++;
                    Logger.error("CFE " + e.getMessage());
                    e.printStackTrace();
                    if (parser.verboseFeedback())
                        parser.sendResponse("Sorry, an unknown error occured.");
                }
            } else {
                Logger.log("CMD -denied @" + parser.getSource().getUser().getDisplayName() + " " + full);
                cmd.onDenied(parser);
            }
        }
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.command = data.getString("cmd");
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("cmd", this.command);
    }

    public static class Type extends Command.Type<AliasCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public AliasCommand build(MainListener parent, String name, String pattern) {
            AliasCommand sc = super.build(parent, name, pattern);
            sc.command = pattern;
            if (!sc.command.startsWith("!")) sc.command = "!" + sc.command;
            return sc;
        }

        @Override
        protected AliasCommand createInstance(MainListener parent, String name) {
            return new AliasCommand(this, parent, name);
        }

    }

}
