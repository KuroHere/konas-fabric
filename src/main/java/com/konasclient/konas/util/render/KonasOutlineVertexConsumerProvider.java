package com.konasclient.konas.util.render;

import com.konasclient.konas.module.modules.render.ESP;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class KonasOutlineVertexConsumerProvider extends OutlineVertexConsumerProvider {
    private final VertexConsumerProvider.Immediate parentK;
    private final VertexConsumerProvider.Immediate plainDrawerK = VertexConsumerProvider.immediate(new BufferBuilder(256));
    private int redK = 255;
    private int greenK = 255;
    private int blueK = 255;
    private int alphaK = 255;

    public KonasOutlineVertexConsumerProvider(VertexConsumerProvider.Immediate immediate) {
        super(immediate);
        this.parentK = immediate;
    }

    @Override
    public void setColor(int red, int green, int blue, int alpha) {
        this.redK = red;
        this.greenK = green;
        this.blueK = blue;
        this.alphaK = alpha;
    }

    @Override
    public void draw() {
        this.plainDrawerK.draw();
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer renderLayer) {
        if (ESP.meshPass) {
            return new KonasOutlineVertexConsumerProvider.KonasOutlineVertexConsumer(null, this.redK, this.greenK, this.blueK, this.alphaK, true);
        }
        VertexConsumer vertexConsumer2;
        if (renderLayer.isOutline()) {
            vertexConsumer2 = this.plainDrawerK.getBuffer(renderLayer);
            return new KonasOutlineVertexConsumerProvider.KonasOutlineVertexConsumer(vertexConsumer2, this.redK, this.greenK, this.blueK, this.alphaK, true);
        } else {
            vertexConsumer2 = this.parentK.getBuffer(renderLayer);
            Optional<RenderLayer> optional = renderLayer.getAffectedOutline();
            if (optional.isPresent()) {
                VertexConsumer vertexConsumer3 = this.plainDrawerK.getBuffer((RenderLayer)optional.get());
                KonasOutlineVertexConsumerProvider.KonasOutlineVertexConsumer outlineVertexConsumer = new KonasOutlineVertexConsumerProvider.KonasOutlineVertexConsumer(vertexConsumer3, this.redK, this.greenK, this.blueK, this.alphaK, false);
                return VertexConsumers.dual(outlineVertexConsumer, vertexConsumer2);
            } else {
                return vertexConsumer2;
            }
        }
    }

    static class KonasOutlineVertexConsumer extends FixedColorVertexConsumer {
        private final VertexConsumer delegate;
        private double x;
        private double y;
        private double z;
        private float u;
        private float v;
        private boolean isOutline;

        private KonasOutlineVertexConsumer(VertexConsumer delegate, int red, int green, int blue, int alpha, boolean isOutline) {
            this.delegate = delegate;
            super.fixedColor(red, green, blue, alpha);
        }

        public void fixedColor(int red, int green, int blue, int alpha) {
        }

        public VertexConsumer vertex(double x, double y, double z) {
            ESP.putVertex(x, y, z);
            if (!ESP.meshPass) {
                this.x = x;
                this.y = y;
                this.z = z;
            }
            return this;
        }

        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        public VertexConsumer texture(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        public VertexConsumer light(int u, int v) {
            return this;
        }

        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            if (ESP.meshPass) {
                ESP.putVertex(x, y, z);
            } else {
                this.delegate.vertex((double) x, (double) y, (double) z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(u, v).next();
            }
        }

        public void next() {
            if (!ESP.meshPass) {
                this.delegate.vertex(this.x, this.y, this.z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(this.u, this.v).next();
            }
        }
    }

}
