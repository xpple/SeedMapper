package dev.xpple.seedmapper.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
    private static final String url = "https://script.google.com/macros/s/AKfycbxvJa3GPh2B_atqbxFdlpInPw4XGKk1jR7lHqALx1durf0X-VXi6bG4zi7Jg-FCU3DfFg/exec";
    private static final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final int DURATION = 10; // seconds

    private static final Map<String, Long> seeds = new HashMap<>();

    public static Long getSeed(String connection) {
        return seeds.get(connection);
    }

    public static void fetchSeeds() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(DURATION))
            .GET()
            .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(response -> MinecraftClient.getInstance().send(() -> {
                JsonArray rows = JsonParser.parseString(response).getAsJsonArray();
                parseRows(rows);
            }));
    }

    private static void parseRows(JsonArray rows) {
        for (JsonElement row : rows) {
            JsonArray seedEntry = row.getAsJsonArray();
            String key = seedEntry.get(0).getAsString();
            String value = seedEntry.get(2).getAsString();
            long seed = Long.parseLong(value.substring(0, value.length() - 1));
            seeds.put(key, seed);
        }
    }
}
