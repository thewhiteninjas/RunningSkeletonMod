package com.thewhiteninja.runningskeleton.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class BlankSkeletonHud extends CustomUIHud {

    private static final String HUD_ID = "RunningSkeleton";

    public BlankSkeletonHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, HUD_ID);
    }

    @Override
    public void build(@Nonnull UICommandBuilder uiCommandBuilder) {
    }
}
