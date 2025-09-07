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
        this.width = this.height = ForgeConfigHandler.client.particleSize;
        this.particleScale = 2.0f;
        this.particleGravity = 1.0f;
        this.particleMaxAge = ForgeConfigHandler.client.particleLifetime;
        this.motionX = vx * ForgeConfigHandler.client.particleVelocityMultiplier;
        this.motionY = vy * ForgeConfigHandler.client.particleVelocityMultiplier;
        this.motionZ = vz * ForgeConfigHandler.client.particleVelocityMultiplier;
        fadeStart = ForgeConfigHandler.client.particleFadeStart;
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
                this.finalQuad = ParticleHelper.getAxisAlignedQuad(this.hitNormal, w);
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
        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z).tex(u1, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z).tex(u1, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z).tex(u0, v0).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z).tex(u0, v1).color(1, 1, 1, alpha).lightmap(j, k).endVertex();
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
        } else {
            if ((this.particleAge & 3) == 0) {
                if (this.checkCovered()) {
                    this.setExpired();
                } else if (this.checkShouldFall()) {
                    this.onGround = false;
                    this.finalQuad = null;
                }
            }
        }
    }

    public boolean checkShouldFall() {
//        double fallArea = 0.0;
//        double totalArea = 0.0;
        int falls = 0;
        for (Vec3d q : finalQuad) {
            Vec3d posVec = new Vec3d(this.posX + q.x - hitNormal.x, this.posY + q.y - hitNormal.y, this.posZ + q.z - hitNormal.z);
            BlockPos behind = new BlockPos(posVec);
            IBlockState block = world.getBlockState(behind);
//            double ax = Math.abs(posVec.x - this.posX);
//            double ay = Math.abs(posVec.y - this.posY);
//            double az = Math.abs(posVec.z - this.posZ);
//            double area = 1.0;
//            if (ax >= 0.001) {
//                area *= ax;
//            }
//            if (ay >= 0.001) {
//                area *= ay;
//            }
//            if (az >= 0.001) {
//                area *= az;
//            }
//            totalArea += area;
//            if (!block.isFullBlock()) {
//                fallArea += area;
//            }
            if (!block.isFullBlock()) {
                falls++;
            }
        }
//        return (fallArea / totalArea) >= 0.05;
        return falls >= 3;
    }

    public boolean checkCovered() {
//        double coveredArea = 0.0;
//        double totalArea = 0.0;
        int covered = 0;
        for (Vec3d q : finalQuad) {
            Vec3d posVec = new Vec3d(this.posX + q.x + hitNormal.x * 0.5, this.posY + q.y + hitNormal.y * 0.5, this.posZ + q.z + hitNormal.z * 0.5);
            BlockPos above = new BlockPos(posVec);
            IBlockState block = world.getBlockState(above);
//            double ax = Math.abs(posVec.x - this.posX);
//            double ay = Math.abs(posVec.y - this.posY);
//            double az = Math.abs(posVec.z - this.posZ);
//            double area = 1.0;
//            if (ax >= 0.001) {
//                area *= ax;
//            }
//            if (ay >= 0.001) {
//                area *= ay;
//            }
//            if (az >= 0.001) {
//                area *= az;
//            }
//            totalArea += area;
//            if (block.isFullBlock()) {
//                coveredArea += area;
//            }
            if (block.isFullBlock()) {
                covered++;
            }
        }
//        return (coveredArea / totalArea) >= 0.05;
        return covered > 0;
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
                    this.posZ -= 0.0025 * Math.signum(dz) * (0.8f + 0.4f * rand.nextFloat());
                    break;
                case EAST:
                case WEST:
                    this.hitNormal = new Vec3d(-Math.signum(dx), 0.0, 0.0);
                    this.posX = pos.getX() + (dx >= 0 ? 0 : 1);
                    this.posX -= 0.0025 * Math.signum(dx) * (0.8f + 0.4f * rand.nextFloat());
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
