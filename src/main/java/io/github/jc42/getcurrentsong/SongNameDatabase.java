package io.github.jc42.getcurrentsong;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

public final class SongNameDatabase {
    public static final class SongInfo {
        private final String name, composer;
        private final String soundtrack;
        private final int trackNumber;

        public SongInfo(String name, String composer, String soundtrack, int trackNumber) {
            if ((soundtrack == null) ^ (trackNumber == -1)) {
                throw new IllegalArgumentException("trackNumber must be -1 if soundtrack is null");
            }
            this.name = name;
            this.composer = composer;
            this.soundtrack = soundtrack;
            this.trackNumber = trackNumber;
        }

        public SongInfo(String name, String composer) {
            this.name = name;
            this.composer = composer;
            this.soundtrack = null;
            this.trackNumber = -1;
        }

        SongInfo(JsonObject json) {
            if (json.has("name")) {
                this.name = json.get("name").getAsString();
            } else {
                throw new IllegalArgumentException("Missing required song field: name");
            }
            if (json.has("composer")) {
                this.composer = json.get("composer").getAsString();
            } else {
                throw new IllegalArgumentException("Missing required song field: composer");
            }
            if (json.has("soundtrack")) {
                this.soundtrack = json.get("soundtrack").getAsString();
            } else {
                this.soundtrack = null;
            }
            if (json.has("trackNumber")) {
                this.trackNumber = json.get("trackNumber").getAsInt();
            } else {
                this.trackNumber = -1;
            }
            if ((soundtrack == null) ^ (trackNumber == -1)) {
                throw new IllegalArgumentException("trackNumber must be -1 if soundtrack is null");
            }
        }

        public String getName() {
            return name;
        }

        public String getComposer() {
            return composer;
        }

        public String getSoundtrack() {
            return soundtrack;
        }

        public int getTrackNumber() {
            return trackNumber;
        }

        public String toString() {
            StringBuilder result = new StringBuilder(name)
                .append(" by ")
                .append(composer);
            if (soundtrack != null) {
                result.append(" (")
                    .append(soundtrack)
                    .append(" #")
                    .append(trackNumber)
                    .append(')');
            }
            return result.toString();
        }

        public static SongInfo fromIdentifier(Identifier id) {
            // Extract filename from identifier path (e.g., "minecraft:music/game/mice_on_venus" -> "mice_on_venus")
            String path = id.getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            
            // Convert underscores to spaces and capitalize each word
            String[] words = filename.split("_");
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (i > 0) {
                    nameBuilder.append(" ");
                }
                String word = words[i];
                if (word.length() > 0) {
                    nameBuilder.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        nameBuilder.append(word.substring(1));
                    }
                }
            }
            
            return new SongInfo(nameBuilder.toString(), "Unknown");
        }
    }

    private static final Map<Identifier, SongInfo> SONGS = new HashMap<>();
    static boolean initialized = false;

    private SongNameDatabase() {
    }

    static void clear() {
        synchronized (SONGS) {
            SONGS.clear();
        }
    }

    static SongInfo putSong(Identifier id, SongInfo name) {
        synchronized (SONGS) {
            return SONGS.put(id, name);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static SongInfo getSong(Identifier id) {
        return SONGS.get(id);
    }

    public static SongInfo getSong(String id) {
        return SONGS.get(new Identifier(id));
    }
}
