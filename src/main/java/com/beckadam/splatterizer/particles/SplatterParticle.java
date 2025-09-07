package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.helpers.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.LinkedList;


public class SplatterParticle extends SplatterParticleBase {
    protected LinkedList<SplatterSubParticle> subParticles;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        subParticles = new LinkedList<>();
    }

    public void addSubparticle(SplatterSubParticle particle) {
        if (subParticles.size() < ForgeConfigHandler.client.particleSubMax) {
            subParticles.add(particle);
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (SPLATTER_PARTICLE_TEXTURE == null || SPLATTER_DECAL_TEXTURE == null) {
            return;
        }
        float alpha = 1.0f;
        if (this.particleAge >= fadeStart) {
            if (this.particleMaxAge <= fadeStart) {
                this.setExpired();
                return;
            }
            alpha -= ((float)(this.particleAge - fadeStart) / (this.particleMaxAge - fadeStart));
        }

        int i = this.getBrightnessForRender(partialTicks);
        int j = (i >> 16) & 65535;
        int k = i & 65535;
        float w = particleScale * this.width * (this.onGround ? 1.5f : 1.0f);

        GlStateManager.enableBlend();
        GlStateManager.enableNormalize();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        GlStateManager.disableLighting();


        Vec3d[] quad;
        float ipx = (float)(player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
        float ipy = (float)(player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
        float ipz = (float)(player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
        double px = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - ipx);
        double py = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - ipy);
        double pz = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - ipz);
        if (this.onGround) {
            if (this.finalQuad == null) {
                this.finalQuad = ParticleHelper.getAxisAlignedQuad(this.hitNormal, w);
//                SplatterizerMod.LOGGER.log(Level.INFO, "Setting particle to Normal Axis Aligned on wall/floor: " + this.hitNormal.toString());
            }
            quad = finalQuad;
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_DECAL_TEXTURE);
        } else {
            quad = new Vec3d[] {
                    new Vec3d((-rotationX * w - rotationXY * w), (-rotationZ * w), (-rotationYZ * w - rotationXZ * w)),
                    new Vec3d((-rotationX * w + rotationXY * w), (rotationZ * w), (-rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w + rotationXY * w), (rotationZ * w), (rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w - rotationXY * w), (-rotationZ * w), (rotationYZ * w - rotationXZ * w))
            };
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_PARTICLE_TEXTURE);
        }

        float u0 = (float)this.particleTextureIndexX / particleTextureWidth;
        float v0 = 0.0f;
        float u1 = u0 + 1.0f / particleTextureWidth;
        float v1 = 1.0f;

//        GL11.glPushMatrix();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z).tex(u1, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z).tex(u1, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z).tex(u0, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z).tex(u0, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        Tessellator.getInstance().draw();
        if (!this.subParticles.isEmpty()) {
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            for (SplatterParticleBase sub : this.subParticles) {
                sub.setAlphaF(alpha);
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            Tessellator.getInstance().draw();
        }
//        GL11.glPopMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        for (SplatterParticleBase sub : this.subParticles) {
            sub.onUpdate();
            if (!sub.isAlive()) {
                this.subParticles.remove(sub);
            }
        }
    }
}
