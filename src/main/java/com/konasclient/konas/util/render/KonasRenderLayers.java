package com.konasclient.konas.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class KonasRenderLayers {
    public static KonasRenderLayers INSTANCE;

    private final RenderPhase.ShadeModel smoothModel = new RenderPhase.ShadeModel(true);
    private final RenderPhase.Lightmap enableLightmap = new RenderPhase.Lightmap(true);
    private final RenderPhase.Texture mipmapTexture = new RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true);

    private final RenderLayer solidFiltered = RenderLayer.of(
            "konas_solid_filtered",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            GL11.GL_QUADS,
            2097152,
            true,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .shadeModel(smoothModel)
                    .lightmap(enableLightmap)
                    .texture(mipmapTexture)
                    .build(false)
    );

    private final List<RenderLayer> layers = Arrays.asList(solidFiltered);

    public RenderLayer getSolidFiltered() {
        return solidFiltered;
    }

    public List<RenderLayer> getLayers() {
        return layers;
    }
}