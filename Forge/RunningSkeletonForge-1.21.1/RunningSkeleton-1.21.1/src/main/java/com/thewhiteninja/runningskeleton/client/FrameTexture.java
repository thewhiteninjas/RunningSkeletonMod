package com.thewhiteninja.runningskeleton.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * A GPU texture created from an already decoded image.
 *
 * <p>Unlike {@code DynamicTexture}, the CPU-side pixel data is closed right after the upload, so
 * only video memory is used while a frame is alive. Must be created on the render thread.</p>
 */
final class FrameTexture extends AbstractTexture {

    FrameTexture(NativeImage image) {
        RenderSystem.assertOnRenderThreadOrInit();
        TextureUtil.prepareImage(this.getId(), image.getWidth(), image.getHeight());
        this.bind();
        // Linear filtering keeps the downscaled 1024x1024 image smooth on typical window sizes.
        this.setFilter(true, false);
        // The last argument closes the image once the pixels are on the GPU.
        image.upload(0, 0, 0, true);
    }

    @Override
    public void load(ResourceManager resourceManager) {
        // Nothing to load: the pixel data is provided through the constructor.
    }
}
