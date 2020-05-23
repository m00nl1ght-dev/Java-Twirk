package dev.m00nl1ght.bot.commands;

import dev.m00nl1ght.bot.CommandException;
import dev.m00nl1ght.bot.CommandParser;
import dev.m00nl1ght.bot.MainListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class TranslateCommand extends Command {

    public static final Type TYPE = new Type("translate");
    private String langFrom = "";
    private String langTo = "";

    protected TranslateCommand(Type type, MainListener parent, String name) {
        super(type, parent, name);
    }

    @Override
    public void execute(CommandParser parser) {
        String from = parser.readAll();
        if (from.isEmpty()) return;
        String res = translate(from);
        if (res.isEmpty()) {
            parser.sendResponse("Translation failed.");
        } else {
            parser.sendResponse(from + " --> " + res + " [" + langTo + "]");
        }
    }

    private String translate(String text) {
        String api = parent.getGoogleAPI();
        if (api.isEmpty()) throw new CommandException("api not supported");
        try {
            String urlStr = "https://script.google.com/macros/s/" + api + "/exec" +
                    "?q=" + URLEncoder.encode(text, "UTF-8") +
                    "&target=" + langTo +
                    "&source=" + langFrom;
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();
            return response;
        } catch (Exception e) {
            throw new CommandException("Translation failed.");
        }
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        this.langTo = data.getString("langTo");
        this.langFrom = data.getString("langFrom");
    }

    @Override
    public void save(JSONObject data) throws JSONException {
        super.save(data);
        data.put("langTo", this.langTo);
        data.put("langFrom", this.langFrom);
    }

    public static class Type extends Command.Type<TranslateCommand> {

        protected Type(String name) {
            super(name);
            defaultCooldown = 2000;
        }

        @Override
        public TranslateCommand build(MainListener parent, String name, String pattern) {
            TranslateCommand sc = super.build(parent, name, pattern);
            String[] p = pattern.split(" ");
            if (p.length < 1) throw new CommandException("missing lang param");
            if (p.length == 1) {
                sc.langFrom = "";
                sc.langTo = p[0];
            } else {
                sc.langFrom = p[0];
                sc.langTo = p[1];
            }
            return sc;
        }

        @Override
        protected TranslateCommand createInstance(MainListener parent, String name) {
            return new TranslateCommand(this, parent, name);
        }

    }

}
