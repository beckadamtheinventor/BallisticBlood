package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.helpers.ParticleSpawnHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class SplatterParticle extends Particle {
    protected ResourceLocation SPLATTER_DECAL_TEXTURE;
    protected ResourceLocation SPLATTER_PARTICLE_TEXTURE;
    protected static int fadeStart;

    protected Vec3d hitNormal;
    protected Vec3d[] finalQuad;
//    protected Vec3d facing;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z);
        setParticleTextureIndex(rand.nextInt(8));
        this.width = this.height = ForgeConfigHandler.server.particleSize;
        this.particleScale = 2.0f;
        this.particleGravity = 1.0f;
        this.particleMaxAge = ForgeConfigHandler.server.particleLifetime;
        this.motionX = vx * ForgeConfigHandler.server.particleVelocityMultiplier;
        this.motionY = vy * ForgeConfigHandler.server.particleVelocityMultiplier;
        this.motionZ = vz * ForgeConfigHandler.server.particleVelocityMultiplier;
        fadeStart = ForgeConfigHandler.server.particleFadeStart;
    }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) {
        this.particleTextureIndexX = particleTextureIndex % 16;
//        this.particleTextureIndexY = Math.min(2, particleTextureIndex / 4);
        this.particleTextureIndexY = 0;
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
//        alpha *= particleAlpha;

        int i = this.getBrightnessForRender(partialTicks);
        int j = (i >> 16) & 65535;
        int k = i & 65535;
//        float f9b = (float) Math.sin(f8 * 0.5f);
        float w = particleScale * this.width * (this.onGround ? 1.5f : 1.0f);

        GlStateManager.enableBlend();
        GlStateManager.enableNormalize();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        GlStateManager.disableLighting();


        Vec3d[] quad;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double ipx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double ipy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double ipz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        double px = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - (float)ipx);
        double py = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - (float)ipy);
        double pz = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - (float)ipz);
        if (this.onGround) {
            if (this.finalQuad == null) {
                this.finalQuad = ParticleSpawnHelper.getAxisAlignedQuad(this.hitNormal, w);
//                SplatterizerMod.LOGGER.log(Level.INFO, "Setting particle to Normal Axis Aligned on wall/floor: " + this.hitNormal.toString());
            }
            quad = finalQuad;
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_DECAL_TEXTURE);
        } else {
//            float f8 = 0.5f * ((float)Math.PI / 2 + this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks);
            quad = new Vec3d[] {
                    new Vec3d((-rotationX * w - rotationXY * w), (-rotationZ * w), (-rotationYZ * w - rotationXZ * w)),
                    new Vec3d((-rotationX * w + rotationXY * w), (rotationZ * w), (-rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w + rotationXY * w), (rotationZ * w), (rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w - rotationXY * w), (-rotationZ * w), (rotationYZ * w - rotationXZ * w))
            };
//            float f9 = MathHelper.cos(f8);
//            Vec3d vec3d = new Vec3d(motionX, motionY, motionZ).scale(MathHelper.sin(f8));
//            for (int l = 0; l < 4; ++l) {
//                quad[l] = vec3d.scale((double)2.0F * quad[l].dotProduct(vec3d)).add(quad[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(quad[l]).scale(2.0F * f9));
//            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(SPLATTER_PARTICLE_TEXTURE);
        }

        float u0 = (float)this.particleTextureIndexX / 16.0f;
        float v0 = 0.0f;
        float u1 = u0 + 1.0f / 16.0f;
        float v1 = 1.0f;

//        if (this.particleTexture != null) {
//            u0 = this.particleTexture.getMinU();
//            u1 = this.particleTexture.getMaxU();
//            v0 = this.particleTexture.getMinV();
//            v1 = this.particleTexture.getMaxV();
//        }
//        GL11.glPushMatrix();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos((double)px + quad[0].x, (double)py + quad[0].y, (double)pz + quad[0].z).tex(u1, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)px + quad[1].x, (double)py + quad[1].y, (double)pz + quad[1].z).tex(u1, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)px + quad[2].x, (double)py + quad[2].y, (double)pz + quad[2].z).tex(u0, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos((double)px + quad[3].x, (double)py + quad[3].y, (double)pz + quad[3].z).tex(u0, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        Tessellator.getInstance().draw();
//        GL11.glPopMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
        // move the particle until it hits something
        if (!this.onGround) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= 0.04 * (double)this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            if ((this.particleAge & 3) == 0) {
                this.nextTextureIndexX();
            }
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        RayTraceResult result = world.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(dx, dy, dz).normalize());
        setPosition(posX+dx, posY+dy, posZ+dz);
        if (result == null) {
            return;
        }

//        List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(xx, yy, zz));
//
//
//        for(AxisAlignedBB axisalignedbb : list) {
//            yy = axisalignedbb.calculateYOffset(this.getBoundingBox(), yy);
//        }
//
//        this.setBoundingBox(this.getBoundingBox().offset(0.0, yy, 0.0));
//
//        for(AxisAlignedBB axisalignedbb : list) {
//            xx = axisalignedbb.calculateXOffset(this.getBoundingBox(), xx);
//        }
//
//        this.setBoundingBox(this.getBoundingBox().offset(xx, 0.0, 0.0));
//
//        for(AxisAlignedBB axisalignedbb : list) {
//            zz = axisalignedbb.calculateZOffset(this.getBoundingBox(), zz);
//        }
//
//        this.setBoundingBox(this.getBoundingBox().offset(0.0F, 0.0, zz));



//        this.resetPositionToBB();
//        this.onGround = Math.abs(dx)<EPSILON && Math.abs(dy)<EPSILON && Math.abs(dz)<EPSILON;
        if (world.checkBlockCollision(this.getBoundingBox())) {
            BlockPos pos = result.getBlockPos();
            switch (result.sideHit) {
                case NORTH:
                case SOUTH:
                    this.hitNormal = new Vec3d(0.0, 0.0, -Math.signum(dz));
                    this.posZ = pos.getZ() + (dz >= 0 ? 0 : 1);
                    this.posZ -= 0.0025 * Math.signum(dz) * (0.6f + 0.4f * rand.nextFloat());
                    break;
                case EAST:
                case WEST:
                    this.hitNormal = new Vec3d(-Math.signum(dx), 0.0, 0.0);
                    this.posX = pos.getX() + (dx >= 0 ? 0 : 1);
                    this.posX -= 0.0025 * Math.signum(dx) * (0.6f + 0.4f * rand.nextFloat());
                    break;
                case UP:
                default:
                    this.hitNormal = new Vec3d(0.0, -Math.signum(dy), 0.0);
                    this.posY = pos.getY() + (dy >= 0 ? 0 : 1);
                    this.posY -= 0.0025 * Math.signum(dy) * (0.6f + 0.4f * rand.nextFloat());
                    break;
            }
            this.onGround = true;
            this.motionX = motionY = motionZ = 0.0;
            this.prevPosX = posX;
            this.prevPosY = posY;
            this.prevPosZ = posZ;
        }
    }
}
