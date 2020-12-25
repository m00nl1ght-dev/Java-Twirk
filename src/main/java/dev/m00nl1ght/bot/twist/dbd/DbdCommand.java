package dev.m00nl1ght.bot.twist.dbd;

import com.gikk.twirk.enums.USER_LEVEL;
import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.Logger;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.commands.Command;
import dev.m00nl1ght.bot.commands.ComplexCommand;
import dev.m00nl1ght.bot.util.SearchUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DbdCommand extends ComplexCommand {

    public static final Type TYPE = new Type("dbd");
    private static final Map<String, Perk> DBD_PERKS_ID = new HashMap<>();
    private static final Map<String, Perk> DBD_PERKS_SEARCH = new HashMap<>();
    private static final SimpleDateFormat eventFormat = new SimpleDateFormat("dd.MM. HH:mm");
    private static final SimpleDateFormat jsonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final CachedRequest<JSONObject> DBD_API_SHRINE = new CachedRequest<>("https://dbd-stats.info/api/shrineofsecrets", JSONObject::new, 30);
    private static final CachedRequest<JSONArray> DBD_API_EVENT = new CachedRequest<>("https://dbd.onteh.net.au/api/events", JSONArray::new, 30);
    private static final CachedRequest<JSONObject> DBD_API_RESET = new CachedRequest<>("https://dbd.onteh.net.au/api/rankreset", JSONObject::new, 30);

    private static final CachedRequest<JSONObject> DBD_API_STATS_MAIN = new CachedRequest<>("https://dbd.onteh.net.au/api/playerstats?steamid=76561197960296580", JSONObject::new, 300);
    private final CachedRequest<Map<String, String>> DBD_STEAM_STATS_MAIN = new CachedRequest<>(
            "http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=381210&key="
                    + parent.getSteamAPI() + "&steamid=76561197960296580", this::readStats, 10);

    private static final CachedRequest<JSONObject> DBD_API_STATS_SWF = new CachedRequest<>("https://dbd.onteh.net.au/api/playerstats?steamid=76561198211239091", JSONObject::new, 300);
    private final CachedRequest<Map<String, String>> DBD_STEAM_STATS_SWF = new CachedRequest<>(
            "http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=381210&key="
                    + parent.getSteamAPI() + "&steamid=76561198211239091", this::readStats, 10);

    private int queryCooldown = 0;
    private double treshShrine = 0.8D;
    private double treshSearch = 0.6D;

    protected DbdCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
        this.addSubCommand(new Shrine(parent, "shrine"));
        this.addSubCommand(new Event(parent, "event"));
        this.addSubCommand(new Reset(parent, "reset"));
        this.addSubCommand(new PerkQuery(parent, "perk"));
        //this.addSubCommand(new Stats(parent, "stats"));
        this.addSubCommand(new Rank(parent, "rank"));
        this.addSubCommand(new SetCd(parent, "setcd"));
        this.readData(parent.getDataFile("dbd_data.json"));
    }

    private void readData(File dataFile) {
        if (dataFile.exists()) {
            try {
                JSONTokener tokener = new JSONTokener(new FileReader(dataFile));
                JSONObject object = new JSONObject(tokener);
                this.treshSearch = object.optDouble("treshSearch", 0.6D);
                this.treshShrine = object.optDouble("treshShrine", 0.8D);
                JSONArray perks = object.getJSONArray("perks");
                for (int i = 0; i < perks.length(); i++) {
                    final JSONObject po = perks.getJSONObject(i);
                    Perk perk = new Perk(po);
                    DBD_PERKS_ID.put(perk.id, perk);
                    if (!perk.name.isEmpty()) {
                        final String pname = perk.name.replaceAll("[^A-Za-z ]", "").toLowerCase();
                        DBD_PERKS_SEARCH.put(pname, perk);
                        if (pname.startsWith("hex: ")) DBD_PERKS_SEARCH.put(pname.substring(4).trim(), perk);
                    }
                    if (!perk.nameLoc.isEmpty()) {
                        final String pnameLoc = perk.nameLoc.replaceAll("[^A-Za-z ]", "").toLowerCase();
                        DBD_PERKS_SEARCH.put(pnameLoc, perk);
                        if (pnameLoc.startsWith("fluch: ")) DBD_PERKS_SEARCH.put(pnameLoc.substring(6).trim(), perk);
                    }
                    String alias = po.optString("alias", "");
                    for (String al : alias.split(",")) if (!al.isEmpty()) DBD_PERKS_SEARCH.put(al.toLowerCase(), perk);
                }
            } catch (Exception e) {
                Logger.error("Failed to dbd data");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        this.queryCooldown = data.optInt("queryCooldown", 0);
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        data.put("queryCooldown", queryCooldown);
    }

    private static class Perk {
        final String id, name, nameLoc, textLoc, character;
        final int type;
        long lastQuery = 0L;
        private int tcLev() {return type==1?30:type==2?35:type==3?40:0;}
        private Perk(JSONObject data) throws JSONException {
            this.id = data.getString("id");
            this.name = data.getString("name");
            this.nameLoc = data.optString("nameLoc", "");
            this.textLoc = data.optString("textLoc", "");
            this.character = data.optString("char", "");
            this.type = data.optInt("type", 0);
        }
    }

    protected static class Shrine extends Command {

        protected Shrine(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            JSONObject data = DBD_API_SHRINE.get();
            try {
                String msg = "Aktuelle Perks im Schrein: ";
                JSONArray perks = jsonGetArray(data, "Items", "items", "Perks", "perks");
                for (int i = 0; i < perks.length(); i++) {
                    JSONObject perk = perks.getJSONObject(i);
                    String id = jsonGetString(perk, "Id", "id", "Name", "name");
                    Perk perkInfo = DBD_PERKS_ID.get(id);
                    if (perkInfo == null) {
                        Optional<Perk> ret = SearchUtil.findMatch(DBD_PERKS_SEARCH, id, 0.7D);
                        if (ret.isPresent()) {
                            String name = ret.get().name;
                            String character = ret.get().character;
                            msg += name + " ("+(character)+"), ";
                            Logger.warn("DbD perk entry unknown: " + id + " (assumed to be " + name + ")");
                        } else {
                            msg += id + " (Unknown), ";
                            Logger.warn("DbD perk entry unknown: " + id);
                        }
                    } else {
                        String name = perkInfo.name;
                        String character = perkInfo.character;
                        msg += name + " ("+(character)+"), ";
                    }
                }

                try {
                    Date endDate = jsonFormat.parse(jsonGetString(data, "endDate", "EndDate"));
                    long reset = endDate.getTime();
                    msg += " n\u00E4chster Reset in " + timeDiff(reset - System.currentTimeMillis());
                } catch (Exception e) {
                    msg += " n\u00E4chster Reset unbekannt";
                }

                parser.send(msg);
            } catch (Exception e) {
                DBD_API_SHRINE.invalidate();
                Logger.error("DbD API error: " + e.getMessage());
                throw new CommandException("DbD API returned invalid data", e);
            }
        }

    }

    protected static class Event extends Command {

        protected Event(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            JSONArray data = DBD_API_EVENT.get();
            try {
                int total = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject evt = data.getJSONObject(i);
                    String msg = "Event \"" + evt.getString("name") + '"';
                    long begin = evt.getLong("start") * 1000;
                    long end = evt.getLong("end") * 1000;
                    long atm = System.currentTimeMillis();
                    if (end < atm) continue;
                    if (begin < atm) {
                        msg += " ist aktiv seit dem " + eventFormat.format(new Date(begin));
                        msg += " Uhr, endet am " + eventFormat.format(new Date(end)) + " Uhr (in " + timeDiff(end - atm) + ").";
                    } else {
                        msg += " beginnt am " + eventFormat.format(new Date(begin));
                        msg += " Uhr (in " + timeDiff(begin - atm) + "), endet am " + eventFormat.format(new Date(end)) + " Uhr.";
                    }
                    parser.send(msg);
                    total++;
                }

                if (total <= 0) {
                    parser.send("Aktuell sind keine Events aktiv.");
                }
            } catch (Exception e) {
                DBD_API_EVENT.invalidate();
                throw new CommandException("DbD API returned invalid data");
            }
        }

    }

    protected static class Reset extends Command {

        protected Reset(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            JSONObject data = DBD_API_RESET.get();
            try {
                long t = data.getLong("rankreset") * 1000;
                parser.send("N\u00E4chster Rank-Reset ist am " + eventFormat.format(new Date(t)) + " Uhr (in " + timeDiff(t - System.currentTimeMillis()) + ")");
            } catch (Exception e) {
                DBD_API_RESET.invalidate();
                throw new CommandException("DbD API returned invalid data");
            }
        }

    }

    protected class PerkQuery extends Command {

        protected PerkQuery(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            String query = parser.readAll().replaceAll("[^A-Za-z ]", "").toLowerCase();
            if (query.isEmpty()) {
                parser.sendResponse("Missing perk name");
                return;
            }

            Optional<Perk> ret = SearchUtil.findMatch(DBD_PERKS_SEARCH, query, 0.7D);
            while(!ret.isPresent()) {
                int li = query.lastIndexOf(' ');
                if (li < 0) break;
                query = query.substring(0, li);
                ret = SearchUtil.findMatch(DBD_PERKS_SEARCH, query, 0.7D);
            }

            if (!ret.isPresent()) {
                parser.sendResponse("Sorry, perk not found");
            } else {
                Perk found = ret.get();
                if (!parser.isWhisper() && System.currentTimeMillis() - found.lastQuery < queryCooldown) return;
                String s = found.name;
                if (!found.nameLoc.isEmpty()) s += " / " + found.nameLoc;
                if (!found.character.isEmpty()) s += " (" + found.character + " Lv. " + (found.tcLev()) + ")";
                s+=": " + found.textLoc;
                if (!parser.isWhisper()) found.lastQuery = System.currentTimeMillis();
                parser.send(s);
            }
        }

    }

    protected class SetCd extends Command {

        protected SetCd(MainListener parent, String name) {
            super(null, parent, name);
            this.perm = USER_LEVEL.MOD.value;
        }

        @Override
        public void execute(CommandParser parser) {
            int cd = parser.nextParamInt();
            queryCooldown = cd;
            parser.sendResponse("Updated query cooldown");
        }

    }

    protected class Stats extends Command {

        protected Stats(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            Map<String, String> data = DBD_STEAM_STATS_MAIN.get();
            // TODO
        }

    }

    protected static class Rank extends Command {

        protected Rank(MainListener parent, String name) {
            super(null, parent, name);
        }

        @Override
        public void execute(CommandParser parser) {
            JSONObject main = DBD_API_STATS_MAIN.get();
            JSONObject swf = DBD_API_STATS_SWF.get();
            try {
                String ret = "Hauptaccount: Survivor Rang " + main.getString("survivor_rank") + ", Killer Rang " + main.getString("killer_rank") + ", ";
                ret += "Nebenaccount: Survivor Rang " + swf.getString("survivor_rank") + ", Killer Rang " + swf.getString("killer_rank") + ", ";
                ret += "zuletzt aktualisiert vor " + timeDiff(System.currentTimeMillis() - main.getLong("updated_at") * 1000);
                parser.send(ret);
            } catch (Exception e) {
                DBD_API_STATS_MAIN.invalidate();
                DBD_API_STATS_SWF.invalidate();
                throw new CommandException("DbD API returned invalid data");
            }
        }

    }

    private Map<String, String> readStats(String str) throws JSONException {
        final Map<String, String> map = new HashMap<>();
        final JSONArray data = new JSONObject(str).getJSONObject("playerstats").getJSONArray("stats");
        for (int i = 0; i < data.length(); i++) {
            final JSONObject stat = data.getJSONObject(i);
            map.put(stat.getString("name"), String.valueOf(stat.getInt("value")));
        }
        return map;
    }

    @Override
    public String printUsage() {
        return "!" + name + " <perk|shrine|event|reset|rank>";
    }

    private static String timeDiff(long diff) {
        int d = (int)(diff / 86400000); diff -= d * 86400000L;
        int h = (int)(diff / 3600000); diff -= h * 3600000L;
        int m = (int)(diff / 60000); diff -= m * 60000L;
        String r = "";
        if (d>0) r += d + "d ";
        if (h>0) r += h + "h ";
        if (m>0) r += m + "m ";
        return r.isEmpty()?"0m":r.trim();
    }

    public static class Type extends Command.Type<DbdCommand> {

        protected Type(String name) {
            super(name);
        }

        @Override
        protected DbdCommand createInstance(MainListener parent, String name) {
            return new DbdCommand(this, parent, name);
        }

    }

    private static String jsonGetString(JSONObject json, String... name) {
        for (String n : name) {
            String ret = json.optString(n, null);
            if (ret != null) return ret;
        }
        throw new IllegalStateException("Invalid json: tag <" + name[0] + "> not found");
    }

    private static JSONArray jsonGetArray(JSONObject json, String... name) {
        for (String n : name) {
            JSONArray ret = json.optJSONArray(n);
            if (ret != null) return ret;
        }
        throw new IllegalStateException("Invalid json: tag <" + name[0] + "> not found");
    }

    private static long jsonGetLong(JSONObject json, String... name) {
        for (String n : name) {
            long ret = json.optLong(n, -1L);
            if (ret < 0L) return ret;
        }
        throw new IllegalStateException("Invalid json: tag <" + name[0] + "> not found");
    }

}
