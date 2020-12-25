package dev.m00nl1ght.bot.twist.dbd;

import dev.m00nl1ght.bot.CommandException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CachedRequest<T> {

    private final String request;
    private final Factory<T> func;
    private final long maxCache;
    private T cached;
    private long timestamp = -1L;

    public CachedRequest(String request, Factory<T> func, int chacheMins) {
        this.request = request;
        this.maxCache = chacheMins * 60000;
        this.func = func;
    }

    public CachedRequest(String request, String param, Factory<T> func, int chacheMins) {
        this(request + URLEncoder.encode(param), func, chacheMins);
    }

    public T get() {
        long t = System.currentTimeMillis();
        if (cached == null || t - timestamp >= maxCache) {
            cached = httpGet(request, func);
            timestamp = t;
        }
        return cached;
    }

    public void invalidate() {
        cached = null;
    }

    private static URLConnection openConnection(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection)(url.openConnection());
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8");
        return urlConnection;
    }

    private static <T> T httpGet(String urlString, Factory<T> factory) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(openConnection(new URL(urlString)).getInputStream()))) {
            String response = in.lines().collect(Collectors.joining());
            in.close();
            return factory.get(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException("Failed to get data from DbD API", e);
        }
    }

    public interface Factory<T> {
        T get(String str) throws Exception;
    }

}
