package io.github.jc42.getcurrentsong;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jc42.getcurrentsong.SongNameDatabase.SongInfo;
import io.github.jc42.getcurrentsong.mixin.MusicTrackerAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.fabricmc.api.ClientModInitializer;

public class GetCurrentSongMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("getcurrentsong");

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            private static final Identifier FABRIC_ID = new Identifier("getcurrentsong", "song_names");
            private static final Identifier SONG_NAMES_JSON = new Identifier("getcurrentsong", "song_names.json");

            @Override
            public Identifier getFabricId() {
                return FABRIC_ID;
            }

            @Override
            public void reload(ResourceManager manager) {
                SongNameDatabase.initialized = false;
                SongNameDatabase.clear();
                int loadedCount = 0;
                try {
                    loadedCount = loadSongName(manager);
                } catch (Exception e) {
                    LOGGER.error("Failed to load song names", e);
                }
                LOGGER.info("Loaded " + loadedCount + " song information records");
                SongNameDatabase.initialized = true;
            }

            private int loadSongName(ResourceManager manager) throws IOException {
                int loadedCount = 0;
                List<Resource> songNamesResources = manager.getAllResources(SONG_NAMES_JSON);
                for (Resource resource : songNamesResources) {
                    try (
                        InputStream is = resource.getInputStream();
                        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    ) {
                        JsonObject jsonObject = JsonHelper.deserialize(reader);
                        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                            if (SongNameDatabase.putSong(
                                new Identifier(entry.getKey()),
                                new SongInfo(entry.getValue().getAsJsonObject())
                            ) == null) loadedCount++;
                        }
                    }
                }
                return loadedCount;
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("getsong")
                    .executes(context -> this.getCurrentSongCommandExecutor(context, false))
            );
            dispatcher.register(
                ClientCommandManager.literal("getsongid")
                    .executes(context -> this.getCurrentSongCommandExecutor(context, true))
            );
            dispatcher.register(
                // Simple helper command for testing
                ClientCommandManager.literal("forceplaysong")
                    .executes(context -> {
                        MusicTracker musicTracker = getMusicTracker(context);
                        MusicSound musicSound = context.getSource().getClient().getMusicType();
                        musicTracker.play(musicSound);
                        context.getSource().sendFeedback(Text.of("Now playing song from " + musicSound.getSound().getId()));
                        return 0;
                    })
            );
        });
        LOGGER.info("Initialized");
    }

    private MusicTracker getMusicTracker(CommandContext<FabricClientCommandSource> context) {
        return context.getSource().getClient().getMusicTracker();
    }

    private int getCurrentSongCommandExecutor(CommandContext<FabricClientCommandSource> context, boolean raw) {
        MusicTracker musicTracker = getMusicTracker(context);
        SoundInstance currentSoundInstance = ((MusicTrackerAccessor)musicTracker).getCurrent();
        if (currentSoundInstance == null) {
            context.getSource().sendFeedback(Text.of("No song currently playing!"));
            return 0;
        }
        Sound currentSound = currentSoundInstance.getSound();
        Identifier songId = currentSound.getIdentifier();
        String resultString;
        if (raw || !SongNameDatabase.isInitialized()) {
            resultString = songId.toString();
        } else {
            SongInfo songInfo = SongNameDatabase.getSong(songId);
            if (songInfo == null) {
                resultString = songId.toString();
            } else {
                resultString = songInfo.toString();
            }
        }
        context.getSource().sendFeedback(Text.of(resultString));
        return 0;
    }
}
