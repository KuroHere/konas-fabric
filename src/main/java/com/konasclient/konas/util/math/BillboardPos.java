package com.konasclient.konas.util.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public class BillboardPos {
    private static final float POSITIVE = Float.intBitsToFloat(0x7f800000);
    private static final float NEGATIVE = Float.intBitsToFloat(0xff800000);

    private final Quaternion pos;

    private Matrix4f modelMatrix;
    private Matrix4f projectionMatrix;

    private final int scaleFactor;

    private Vec2d projectedPos;

    public BillboardPos(Quaternion pos, Matrix4f modelMatrix, Matrix4f projectionMatrix, int scaleFactor) {
        this.pos = pos;
        this.modelMatrix = modelMatrix;
        this.projectionMatrix = projectionMatrix;
        this.scaleFactor = scaleFactor;
    }

    public Vec2d getProjectedPos() {
        if (projectedPos != null) return projectedPos;

        Quaternion q = MatrixUtil.mult(projectionMatrix, MatrixUtil.mult(modelMatrix, pos));

        if (q.getW() < 0F) return null;

        float d = 1F / q.getW() * 0.5F;

        q = new Quaternion(q.getX() * d + 0.5F, q.getY() * d + 0.5F, q.getZ() * d + 0.5F, d);

        float x = q.getX() * MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        float y = q.getY() * MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        if (x == POSITIVE || x == NEGATIVE || y == POSITIVE || y == NEGATIVE) return null;
        
        projectedPos = new Vec2d(x / scaleFactor, (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - y) / scaleFactor);
        return projectedPos;
    }

    public boolean intersects(Box box) {
        BillboardPos[] poses = new BillboardPos[8];
        poses[0] = MatrixUtil.getBillboardPos(new Vec3d(box.minX, box.minY, box.minZ));
        poses[1] = MatrixUtil.getBillboardPos(new Vec3d(box.maxX, box.minY, box.minZ));
        poses[2] = MatrixUtil.getBillboardPos(new Vec3d(box.minX, box.maxY, box.minZ));
        poses[3] = MatrixUtil.getBillboardPos(new Vec3d(box.minX, box.minY, box.maxZ));
        poses[4] = MatrixUtil.getBillboardPos(new Vec3d(box.minX, box.maxY, box.maxZ));
        poses[5] = MatrixUtil.getBillboardPos(new Vec3d(box.maxX, box.minY, box.maxZ));
        poses[6] = MatrixUtil.getBillboardPos(new Vec3d(box.maxX, box.maxY, box.minZ));
        poses[7] = MatrixUtil.getBillboardPos(new Vec3d(box.maxX, box.maxY, box.maxZ));

        BillboardPos min = poses[0];
        BillboardPos max = poses[0];

        for (BillboardPos bp : poses) {
            if (bp.getProjectedPos().x < min.getProjectedPos().x && bp.getProjectedPos().y < min.getProjectedPos().y) {
                min = bp;
            }
            if (bp.getProjectedPos().x > max.getProjectedPos().x && bp.getProjectedPos().y > max.getProjectedPos().y) {
                max = bp;
            }
        }

        return getProjectedPos().x > min.getProjectedPos().x &&
                getProjectedPos().y > min.getProjectedPos().y &&
                getProjectedPos().x < max.getProjectedPos().x &&
                getProjectedPos().y < max.getProjectedPos().y;
    }

    public void update(Matrix4f modelMatrix, Matrix4f projectionMatrix) {
        this.modelMatrix = modelMatrix;
        this.projectionMatrix = projectionMatrix;
        this.projectedPos = null;
    }
}
