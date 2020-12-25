package dev.m00nl1ght.bot.twist;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.ComplexCommand;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtensionCommand extends ComplexCommand {

    public static final Type TYPE = new Type("mb_ext");

    protected ExtensionCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new SetResubNotice(parent, "resub"));
        this.addSubCommand(new SetRaidNotice(parent, "raid"));
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        TwistExtension.INSTANCE.resubMsg = data.optString("resub_notice", "");
        TwistExtension.INSTANCE.raidMsg = data.optString("raid_notice", "");
        TwistExtension.INSTANCE.moduloResub = data.optInt("resub_modulo", 0);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("resub_notice", TwistExtension.INSTANCE.resubMsg);
        data.put("raid_notice", TwistExtension.INSTANCE.raidMsg);
        data.put("resub_modulo", TwistExtension.INSTANCE.moduloResub);
    }

    protected class SetResubNotice extends Command {

        protected SetResubNotice(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            int mod = parser.nextParamInt(0);
            TwistExtension.INSTANCE.resubMsg = parser.readAll();
            TwistExtension.INSTANCE.moduloResub = mod;
            parser.sendResponse("Updated resub notice (months % " + mod + " == 0)");
        }

    }

    protected class SetRaidNotice extends Command {

        protected SetRaidNotice(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            TwistExtension.INSTANCE.raidMsg = parser.readAll();
            parser.sendResponse("Updated raid notice");
        }

    }

    public static class Type extends Command.Type<ExtensionCommand> {

        protected Type(String name) {
            super(name);
            defaultPerm = USER_LEVEL.OWNER.value;
        }

        @Override
        protected ExtensionCommand createInstance(MainListener parent, String name) {
            return new ExtensionCommand(this, parent, name);
        }

    }

}
