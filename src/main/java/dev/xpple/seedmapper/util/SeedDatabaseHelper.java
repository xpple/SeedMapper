package dev.xpple.seedmapper.util;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SeedDatabaseHelper {

    private SeedDatabaseHelper() {
    }

    private static final URI DATABASE_URL = URI.create("https://docs.google.com/spreadsheets/d/1tuQiE-0leW88em9OHbZnH-RFNhVqgoHhIt9WQbeqqWw/export?format=csv&gid=0");
    private static final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final Map<String, Long> connectionToSeed = new HashMap<>();
    private static final Map<Long, Long> hashedSeedToSeed = new HashMap<>();

    public static @Nullable SeedIdentifier getSeed(String connection, long hashedSeed) {
        return Optional
            .ofNullable(connectionToSeed.get(connection))
            .or(() -> Optional.ofNullable(hashedSeedToSeed.get(hashedSeed)))
            .map(SeedIdentifier::new)
            .orElse(null);
    }

    public static void fetchSeeds() {
        HttpRequest request = HttpRequest.newBuilder(DATABASE_URL)
            .timeout(TIMEOUT)
            .GET()
            .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(SeedDatabaseHelper::parseCsv);
    }

    private static void parseCsv(String csv) {
        Arrays.stream(csv.split("\n")).skip(1).forEach(row -> {
            try {
                String[] seedEntry = row.split(",");
                String connection = seedEntry[0];
                String seedString = seedEntry[2];
                long seed = Long.parseLong(seedString.substring(0, seedString.length() - 1));
                connectionToSeed.put(connection, seed);
                long hashedSeed = Long.parseLong(seedEntry[6]);
                hashedSeedToSeed.put(hashedSeed, seed);
            } catch (RuntimeException _) {
            }
        });
    }
}
