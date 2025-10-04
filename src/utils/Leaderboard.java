package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Simple leaderboard persisted as CSV files in release/ directory.
 * CSV columns: name,score,elapsedMillis,timestamp
 */
public class Leaderboard {

    public static class Entry {
        public final String name;
        public final int score;
        public final long elapsedMillis;
        public final String timestamp;

        public Entry(String name, int score, long elapsedMillis, String timestamp) {
            this.name = name;
            this.score = score;
            this.elapsedMillis = elapsedMillis;
            this.timestamp = timestamp;
        }
    }

    private final String gameId;
    private final String speedLabel;
    private final File file;

    public Leaderboard(String gameId, String speedLabel) {
        this.gameId = sanitize(gameId);
        this.speedLabel = sanitize(speedLabel);
        File releaseDir = new File("release");
        if (!releaseDir.exists()) releaseDir.mkdirs();
        this.file = new File(releaseDir, String.format("leaderboards-%s-%s.csv", this.gameId, this.speedLabel));
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public synchronized void addEntry(String name, int score, long elapsedMillis) throws IOException {
        String cleanName = (name == null ? "Player" : name.replaceAll("[\r\n]", " ")).trim();
        if (cleanName.isEmpty()) cleanName = "Player";
        if (cleanName.length() > 20) {
            cleanName = cleanName.substring(0, 20);
        }
        boolean exists = file.exists();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            if (!exists) {
                w.write("name,score,elapsedMillis,timestamp\n");
            }
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String line = String.format("%s,%d,%d,%s\n", escape(cleanName), score, elapsedMillis, ts);
            w.write(line);
        }
    }

    public synchronized List<Entry> readTop(int limit) throws IOException {
        List<Entry> entries = new ArrayList<>();
        if (!file.exists()) return entries;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            r.readLine(); // skip header
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length < 4) continue;
                String name = unescape(parts[0]);
                int score = Integer.parseInt(parts[1]);
                long elapsed = Long.parseLong(parts[2]);
                String ts = parts[3];
                entries.add(new Entry(name, score, elapsed, ts));
            }
        }
        // sort by score desc, then time asc
        Collections.sort(entries, Comparator.comparingInt((Entry e) -> -e.score).thenComparingLong(e -> e.elapsedMillis));
        if (entries.size() > limit) return entries.subList(0, limit);
        return entries;
    }

    private String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == ',') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaping) {
                sb.append(c);
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
