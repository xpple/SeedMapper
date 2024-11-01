package dev.xpple.seedmapper.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class SeedDatabaseHelper {

    private SeedDatabaseHelper() {
    }

    private static final String DATABASE_URL = "https://script.google.com/macros/s/AKfycbye87L-fEYq2EkgczvhKb_kGecp5wL1oX95vg45TRSwNvpv7K-53zoInGTeI1FZ0kv7DA/exec";
    private static final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final Map<String, Long> seeds = new HashMap<>();

    public static @Nullable Long getSeed(String connection) {
        return seeds.get(connection);
    }

    public static void fetchSeeds() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATABASE_URL))
            .timeout(TIMEOUT)
            .GET()
            .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(response -> {
                try {
                    JsonArray rows = JsonParser.parseString(response).getAsJsonArray();
                    parseRows(rows);
                } catch (RuntimeException ignored) {
                }
            });
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
