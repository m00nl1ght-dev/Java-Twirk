package dev.m00nl1ght.bot.commands;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Command {

    private static final long deafultTime = System.currentTimeMillis();

    public final Type type;
    public final MainListener parent;
    public final String name;
    protected int perm = USER_LEVEL.DEFAULT.value;
    public int stat_total = 0, stat_fail = 0;
    protected boolean verboseFeedback = true;
    protected long lastExe = deafultTime;
    protected int cooldown = 0;

    protected Command(Type type, MainListener parent, String name) {
        this.type = type;
        this.parent = parent;
        this.name = name;
    }

    public abstract void execute(CommandParser parser);

    public boolean canExecute(CommandParser parser) {
        return parent.isActive() && (perm <= 0 || parser.getSource().getUser().hasPermission(perm));
    }

    public void onDenied(CommandParser parser) {
        if (parser.verboseFeedback() && parent.isActive())
            parser.sendResponse("You don't have permission to use this command.");
    }

    public void setVerboseFeedback(boolean verboseFeedback) {
        this.verboseFeedback = verboseFeedback;
    }

    public boolean verboseFeedback() {
        return this.verboseFeedback;
    }

    public void setPerm(int val) {
        this.perm = val;
    }

    public void load(JSONObject data) throws JSONException {
        this.stat_total = data.optInt("stat_t", this.stat_total);
        this.stat_fail = data.optInt("stat_f", this.stat_fail);
        this.perm = data.optInt("perm", this.perm);
        this.cooldown = data.optInt("cd", 0);
    }

    public void save(JSONObject data) throws JSONException {
        data.put("stat_t", this.stat_total);
        data.put("stat_f", this.stat_fail);
        data.put("perm", this.perm);
        if (cooldown > 0) data.put("cd", cooldown);
    }

    public void resetCooldown() {
        if (cooldown > 0) this.lastExe = System.currentTimeMillis();
    }

    public boolean isOnCooldown() {
        return cooldown > 0 && System.currentTimeMillis() - lastExe < cooldown;
    }

    public void setCooldown(int value) {
        this.cooldown = value;
    }

    public String printStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Used " + stat_total + " times, ");
        sb.append("Failed " + stat_fail + " times, ");
        sb.append("Perm level: " + perm + ", ");
        sb.append("Cooldown: " + cooldown/1000 + "s, ");
        sb.append("Type: " + (type==null ? "sub" : type.name) + (verboseFeedback?" (v)":""));
        return sb.toString();
    }

    public static abstract class Type<T extends Command> {

        public final String name;
        public int defaultPerm = USER_LEVEL.DEFAULT.value;
        public int defaultCooldown = 0;

        protected Type(String name) {
            this.name = name;
        }

        public T build(MainListener parent, String name, String pattern) {
            T c = createInstance(parent, name);
            c.perm = defaultPerm;
            c.cooldown = defaultCooldown;
            return c;
        }

        public T load(MainListener parent, JSONObject data) throws JSONException {
            String n = data.getString("name");
            T c = createInstance(parent, n);
            c.load(data);
            return c;
        }

        public JSONObject save(T c) throws JSONException {
            JSONObject data = new JSONObject();
            c.save(data);
            data.put("name", c.name);
            return data;
        }

        protected abstract T createInstance(MainListener parent, String name);

    }

}
