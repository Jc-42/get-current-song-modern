package io.github.jc42.getcurrentsong.mixin;

import io.github.jc42.getcurrentsong.GetCurrentSongConfig;
import io.github.jc42.getcurrentsong.SongNameDatabase;
import io.github.jc42.getcurrentsong.SongNameDatabase.SongInfo;
import io.github.jc42.getcurrentsong.mixin.MusicTrackerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderSongInfo(
            net.minecraft.client.util.math.MatrixStack matrices,
            int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!GetCurrentSongConfig.showSongInPauseMenu) return;
        MinecraftClient client = MinecraftClient.getInstance();
        MusicTracker musicTracker = client.getMusicTracker();
        SoundInstance currentSoundInstance = ((MusicTrackerAccessor)musicTracker).getCurrent();
        String text;
        if (currentSoundInstance == null) {
            text = "Song: No song";
        } else {
            Sound currentSound = currentSoundInstance.getSound();
            Identifier songId = currentSound.getIdentifier();
            if (SongNameDatabase.isInitialized()) {
                SongInfo songInfo = SongNameDatabase.getSong(songId);
                if (songInfo != null) {
                    text = "Song: " + songInfo;
                } else {
                    text = "Song: " + songId;
                }
            } else {
                text = "Song: " + songId;
            }
        }
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = GetCurrentSongConfig.pauseMenuTextX;
        int y;
        // Try to place below the last button to avoid overlap
        net.minecraft.client.gui.screen.Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null && !screen.children().isEmpty()) {
            int maxY = 0;
            for (var child : screen.children()) {
                if (child instanceof net.minecraft.client.gui.widget.ClickableWidget widget) {
                    int bottom = widget.y + widget.getHeight();
                    if (bottom > maxY) maxY = bottom;
                }
            }
            y = maxY + 4; // 4px margin
        } else {
            y = GetCurrentSongConfig.pauseMenuTextY;
            if (y < 0) y = height + y;
        }
        client.textRenderer.drawWithShadow(matrices, text, x, y, GetCurrentSongConfig.pauseMenuTextColor);
    }
}
