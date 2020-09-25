package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.CommandPattern;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuotesCommand extends ComplexCommand {

    public static final Type TYPE = new Type("quotes");
    private static final Random random = new Random();

    protected CommandPattern basePattern;
    protected List<CommandPattern> quotes = new ArrayList<>();

    protected QuotesCommand(Command.Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.verboseFeedback = false;
        this.addSubCommand(new Get(parent, "*"));
        this.addSubCommand(new Add(parent, "add"));
        this.addSubCommand(new Remove(parent, "remove"));
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.basePattern = CommandPattern.compile(data.getString("basePattern"));
        final JSONArray list = data.getJSONArray("quotes");
        for (int i = 0; i < list.length(); i++) {
            quotes.add(CommandPattern.compile(list.getString(i)));
        }
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("basePattern", this.basePattern.source());
        final JSONArray list = new JSONArray();
        for (CommandPattern quote : quotes) list.put(quote.source());
        data.put("quotes", list);
    }

    public static class Type extends Command.Type<QuotesCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        public QuotesCommand build(MainListener parent, String name, String pattern) {
            QuotesCommand sc = super.build(parent, name, pattern);
            sc.basePattern = CommandPattern.compile(pattern);
            sc.verboseFeedback = true;
            return sc;
        }

        @Override
        protected QuotesCommand createInstance(MainListener parent, String name) {
            return new QuotesCommand(this, parent, name);
        }

    }

    protected class Get extends Command {

        protected Get(MainListener parent, String name) {
            super(null, parent, name);
            setCooldown(5000);
        }

        @Override
        public void execute(CommandParser parser) {
            final String param = parser.getParam(1);
            if (!param.isEmpty()) {
                try {
                    final int idx = Integer.parseInt(param) - 1;
                    if (idx < 0 || idx >= quotes.size()) throw new CommandException("invalid quote id");
                    final CommandPattern pattern = quotes.get(idx);
                    parser.send(basePattern.build(parser) + " " + pattern.build(parser));
                    return;
                } catch (NumberFormatException e) {
                    //NO-OP
                }
            }

            if (quotes.isEmpty()) {
                parser.sendResponse("No quotes saved.");
            } else {
                final int idx = random.nextInt(quotes.size());
                final CommandPattern pattern = quotes.get(idx);
                parser.send(basePattern.build(parser) + " " + pattern.build(parser));
            }
        }

    }

    protected class Add extends Command {

        protected Add(MainListener parent, String name) {
            super(null, parent, name);
            setPerm(USER_LEVEL.MOD.value);
            setCooldown(0);
        }

        @Override
        public void execute(CommandParser parser) {
            final String pattern = parser.readAll();
            quotes.add(CommandPattern.compile(pattern));
            parser.sendResponse("Added quote #" + (quotes.size()) + ".");
        }

    }

    protected class Remove extends Command {

        protected Remove(MainListener parent, String name) {
            super(null, parent, name);
            setPerm(USER_LEVEL.MOD.value);
            setCooldown(0);
        }

        @Override
        public void execute(CommandParser parser) {
            final String param = parser.nextParam();
            if (!param.isEmpty()) {
                try {
                    final int idx = Integer.parseInt(param) - 1;
                    if (idx < 0 || idx >= quotes.size()) throw new CommandException("invalid quote id");
                    quotes.remove(idx);
                    parser.sendResponse("Removed quote #" + (idx + 1) + ".");
                    return;
                } catch (NumberFormatException e) {
                    throw new CommandException("invalid quote id");
                }
            } else {
                throw new CommandException("missing quote id");
            }
        }

    }

}
