package com.thewhiteninja.runningskeleton;

import net.minecraft.resources.Identifier;

/**
 * Resolves the texture identifiers for the 14 animation frames.
 * The frame files must be provided by the user under
 * {@code src/main/resources/assets/runningskeleton/textures/animation/frame_01.png} through {@code frame_14.png}.
 */
public final class SkeletonTextures {

    public static final int FRAME_COUNT = 14;

    private static final Identifier[] FRAMES = buildFrameIdentifiers();

    private SkeletonTextures() {
    }

    private static Identifier[] buildFrameIdentifiers() {
        Identifier[] frames = new Identifier[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            String path = "textures/animation/frame_%02d.png".formatted(i + 1);
            frames[i] = Identifier.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, path);
        }
        return frames;
    }

    public static Identifier frame(int index) {
        return FRAMES[index];
    }
}
