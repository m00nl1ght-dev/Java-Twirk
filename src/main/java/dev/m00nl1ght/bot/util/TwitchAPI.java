package dev.m00nl1ght.bot.util;

import dev.m00nl1ght.bot.CommandException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class TwitchAPI {

    private final String clientId;

    public TwitchAPI(String clientId) {
        this.clientId = clientId;
    }

    public JSONObject get(String query) {
        try {
            final URL url = new URL(("https://api.twitch.tv/helix/" + query));
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Client-ID", clientId);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = in.lines().collect(Collectors.joining());
                return new JSONObject(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data from Twitch API", e);
        }
    }

}
