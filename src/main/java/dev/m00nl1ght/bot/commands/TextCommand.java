package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

public class TextCommand extends Command {

    public static final Type TYPE = new TypeText("text");

    protected CommandPattern pattern;

    protected TextCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.verboseFeedback = false;
    }

    @Override
    public void execute(CommandParser parser) {
        parser.send(pattern.build(parser));
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.pattern = CommandPattern.compile(data.getString("pattern"));
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("pattern", this.pattern.source());
    }

    public static class TypeText extends Command.Type<TextCommand> {

        protected TypeText(String name) {
            super(name);
            defaultCooldown = 2500;
        }

        @Override
        public TextCommand build(MainListener parent, String name, String pattern) {
            TextCommand sc = super.build(parent, name, pattern);
            sc.pattern = CommandPattern.compile(pattern);
            return sc;
        }

        @Override
        protected TextCommand createInstance(MainListener parent, String name) {
            return new TextCommand(this, parent, name);
        }

    }

}
