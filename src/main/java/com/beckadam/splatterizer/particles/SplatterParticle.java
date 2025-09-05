package com.beckadam.splatterizer.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;

import java.util.List;

public class SplatterParticle extends Particle {
    protected static ResourceLocation SPLATTER_DECAL_TEXTURE;
    protected static ResourceLocation SPLATTER_PARTICLE_TEXTURE;
//    protected Vec3d facing;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
//        facing = new Vec3d(vx, vy, vz).normalize().subtractReverse(Vec3d.ZERO);
        setPosition(posX, posY, posZ);
        setParticleTextureIndex(rand.nextInt(8));
        setMaxAge(ForgeConfigHandler.client.particleLifetime);
        setSize(ForgeConfigHandler.client.particleSize, ForgeConfigHandler.client.particleSize);
    }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) {
        this.particleTextureIndexX = particleTextureIndex % 4;
        this.particleTextureIndexY = Math.min(2, particleTextureIndex / 4);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (SPLATTER_PARTICLE_TEXTURE == null || SPLATTER_DECAL_TEXTURE == null) {
            return;
        }
        float alpha = 1.0f;
        int fadeStart = ForgeConfigHandler.client.particleFadeStart;
        if (particleAge >= fadeStart) {
            alpha -= ((float)(particleAge - fadeStart) / (particleMaxAge - fadeStart));
        }
        alpha *= particleAlpha;

        float f3 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float f4 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float f5 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
        float width = particleScale * 0.09F;
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[]{new Vec3d((double) (-rotationX * width - rotationXY * width), (double) (-rotationZ * width), (double) (-rotationYZ * width - rotationXZ * width)), new Vec3d((double) (-rotationX * width + rotationXY * width), (double) (rotationZ * width), (double) (-rotationYZ * width + rotationXZ * width)), new Vec3d((double) (rotationX * width + rotationXY * width), (double) (rotationZ * width), (double) (rotationYZ * width + rotationXZ * width)), new Vec3d((double) (rotationX * width - rotationXY * width), (double) (-rotationZ * width), (double) (rotationYZ * width - rotationXZ * width))};
        GlStateManager.enableBlend();
        GlStateManager.enableNormalize();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        float f8 = (float)Math.PI / 2 + this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
        float f9 = (float) Math.cos(f8 * 0.5F);
        float f9b = (float) Math.sin(f8 * 0.5f);
        float f10 = f9b * (float)cameraViewDir.x;
        float f11 = f9b * (float)cameraViewDir.y;
        float f12 = f9b * (float)cameraViewDir.z;
        Vec3d vec3d = new Vec3d((double) f10, (double) f11, (double) f12);
        for (int l = 0; l < 4; ++l) {
            avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double) (f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double) (2.0F * f9)));
        }

        if (onGround) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_DECAL_TEXTURE);
        } else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_PARTICLE_TEXTURE);
        }

        GlStateManager.disableLighting();
//        GL11.glPushMatrix();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos((double)f3 + avec3d[0].x, (double)f4 + avec3d[0].y, (double)f5 + avec3d[0].z).tex(0, 1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)f3 + avec3d[1].x, (double)f4 + avec3d[1].y, (double)f5 + avec3d[1].z).tex(1, 1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)f3 + avec3d[2].x, (double)f4 + avec3d[2].y, (double)f5 + avec3d[2].z).tex(1, 0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)f3 + avec3d[3].x, (double)f4 + avec3d[3].y, (double)f5 + avec3d[3].z).tex(0, 0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
//        Tessellator.getInstance().draw();
//        GL11.glPopMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int i = super.getBrightnessForRender(partialTick);
        int j = i & 255;
        int k = (i >> 16) & 255;
        if (j > 240) {
            j = 240;
        }
        return j | (k << 16);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void move(double x, double y, double z) {
        double d0 = y;
        double origX = x;
        double origZ = z;

        if (!onGround) {
            List<AxisAlignedBB> list = world.getCollisionBoxes((Entity) null, getBoundingBox().expand(x, y, z));

            for (AxisAlignedBB axisalignedbb : list) {
                y = axisalignedbb.calculateYOffset(getBoundingBox(), y);
            }

            setBoundingBox(getBoundingBox().offset(0.0D, y, 0.0D));

            for (AxisAlignedBB axisalignedbb1 : list) {
                x = axisalignedbb1.calculateXOffset(getBoundingBox(), x);
            }

            setBoundingBox(getBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb2 : list) {
                z = axisalignedbb2.calculateZOffset(getBoundingBox(), z);
            }

            setBoundingBox(getBoundingBox().offset(0.0D, 0.0D, z));
        } else {
            setBoundingBox(getBoundingBox().offset(x, y, z));
        }

        resetPositionToBB();
        onGround = d0 != y && d0 < 0.0D;

        if (origX != x) {
            motionX = 0.0D;
        }

        if (origZ != z) {
            motionZ = 0.0D;
        }
    }
}
