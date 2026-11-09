package com.thewhiteninja.runningskeleton.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Locale;

public class SkeletonRunHud extends CustomUIHud {

    private static final String HUD_ID = "RunningSkeleton";
    private static final String HUD_MARKUP_PATH = "Huds/RunningSkeleton_Overlay.ui";
    private static final int DEFAULT_VISIBLE_FRAME = 1;

    private final int frameNumber;

    public SkeletonRunHud(@Nonnull PlayerRef playerRef, int frameNumber) {
        super(playerRef, HUD_ID);
        this.frameNumber = frameNumber;
    }

    @Override
    public void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append(HUD_MARKUP_PATH);

        if (this.frameNumber != DEFAULT_VISIBLE_FRAME) {
            uiCommandBuilder.set(frameSelector(DEFAULT_VISIBLE_FRAME) + ".Visible", false);
            uiCommandBuilder.set(frameSelector(this.frameNumber) + ".Visible", true);
        }
    }

    private static String frameSelector(int frameNumber) {
        return String.format(Locale.ROOT, "#Frame%02d", frameNumber);
    }
}
