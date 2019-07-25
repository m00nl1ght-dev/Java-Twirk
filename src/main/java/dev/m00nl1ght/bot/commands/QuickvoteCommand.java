package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class QuickvoteCommand extends ComplexCommand {

    public static final Type TYPE = new Type("quickvote");
    private static final int updateTick = 1000;
    private static final int noticeMsgGap = 10;

    private int defaultDuration = 1000 * 30;
    private VoteHandler activeVote = null;

    protected QuickvoteCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Start(parent, "start"));
        this.addSubCommand(new Cancel(parent, "cancel"));
        this.addSubCommand(new End(parent, "end"));
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.defaultDuration = data.optInt("defaultDuration", this.defaultDuration);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("defaultDuration", this.defaultDuration);
    }

    private class VoteHandler extends Thread implements TwirkListener {

        private int duration;
        private boolean canceled;
        private int msgSinceNotice = 0;
        private long startTime;
        private HashMap<String, Integer> votes = new HashMap<>();
        private String[] options = new String[] {"VoteYea", "VoteNay"};

        public VoteHandler(int duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            parent.sendMessage("/me Quickvote started: Use the emotes VoteYea or VoteNay to vote. Time remaining: " + formatTime(duration));
            parent.getBot().addIrcListener(this);
            while (!canceled) {
                int remaining = (int) (duration - (System.currentTimeMillis() - startTime));
                if (remaining > 0) {
                    if (msgSinceNotice >= noticeMsgGap) {
                        parent.sendMessage("/me Quickvote active: Use the emotes VoteYea or VoteNay to vote. Time remaining: " + formatTime(remaining));
                        msgSinceNotice = 0;
                    }
                    this.wait(updateTick);
                } else {
                    break;
                }
            }
            parent.getBot().removeIrcListener(this);
            activeVote = null;
            if (!canceled) {
                parent.sendMessage("/me Quickvote ended!");
                this.wait(1000);
                this.printResults();
            }
        }

        @Override
        public void onPrivMsg(TwitchMessage message) {
            msgSinceNotice++;
            for (int i = 0; i < options.length; i++) {
                if (message.getContent().trim().startsWith(options[i])) {
                    votes.put(message.getUser().getUserName(), i);
                    return;
                }
            }
        }

        private void printResults() {
            int[] res = new int[options.length];
            for (int v : votes.values()) res[v]++;
            String r = "/me Results: ";
            for (int i = 0; i < res.length; i++) {
                r += res[i] + "x " + options[i] + "  ";
            }
            parent.sendMessage(r);
        }

        private void cancel() {
            this.canceled = true;
        }

        private void end() {
            this.duration = 0;
        }

        private void wait(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {}
        }

        private String formatTime(int millis) {
            return Integer.toString(millis/1000) + "s";
        }

    }

    protected class Start extends Command {

        protected Start(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            int d = parser.nextParamInt(defaultDuration);
            if (d > 600 || d < 10) {
                throw new CommandException("duration must be between 10s and 600s");
            }
            if (activeVote != null) {
                parser.sendResponse("There is already an active vote.");
                return;
            }
            activeVote = new VoteHandler(d * 1000);
            activeVote.start();
        }

    }

    protected class Cancel extends Command {

        protected Cancel(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            if (activeVote == null) {
                parser.sendResponse("No active vote.");
            } else {
                activeVote.cancel();
                parser.sendResponse("Canceled active quickvote.");
            }
        }

    }

    protected class End extends Command {

        protected End(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            if (activeVote == null) {
                parser.sendResponse("No active vote.");
            } else {
                activeVote.end();
            }
        }

    }

    public static class Type extends Command.Type<QuickvoteCommand> {

        protected Type(String name) {
            super(name);
            defaultPerm = USER_LEVEL.MOD.value;
        }

        @Override
        public QuickvoteCommand build(MainListener parent, String name, String pattern) {
            QuickvoteCommand sc = super.build(parent, name, pattern);
            sc.defaultDuration = CommandParser.intOr(pattern, 30);
            return sc;
        }

        @Override
        protected QuickvoteCommand createInstance(MainListener parent, String name) {
            return new QuickvoteCommand(this, parent, name);
        }

    }

}
