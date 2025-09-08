package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.helpers.ParticleHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;

public class SplatterSubParticle extends SplatterParticleBase {
    protected ResourceLocation SPLATTER_DECAL_TEXTURE;
    protected ResourceLocation SPLATTER_PARTICLE_TEXTURE;

    public SplatterSubParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.particleScale = 1.0f;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (SPLATTER_PARTICLE_TEXTURE == null || SPLATTER_DECAL_TEXTURE == null) {
            return;
        }
        float alpha = particleAlpha;

        int i = this.getBrightnessForRender(partialTicks);
        int j = (i >> 16) & 65535;
        int k = i & 65535;
        float w = particleScale * this.width * (this.onGround ? 1.5f : 1.0f);

        Vec3d[] quad;
        double px = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - ipx);
        double py = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - ipy);
        double pz = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - ipz);
        if (this.onGround && this.finalQuad != null) {
            quad = this.finalQuad;
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
//        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z)
                .tex(finalUVs[0].x+u1, finalUVs[0].y+v1)
                .color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z)
                .tex(finalUVs[1].x+u1, finalUVs[1].y+v0)
                .color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z)
                .tex(finalUVs[2].x+u0, finalUVs[2].y+v0)
                .color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z)
                .tex(finalUVs[3].x+u0, finalUVs[3].y+v1)
                .color(1, 1, 1, alpha).lightmap(j, k).endVertex();
//        Tessellator.getInstance().draw();
//        GL11.glPopMatrix();
//        GlStateManager.disableBlend();
    }
}
