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


public class SplatterParticleBase extends Particle {
    protected ResourceLocation SPLATTER_DECAL_TEXTURE;
    protected ResourceLocation SPLATTER_PARTICLE_TEXTURE;
    protected static int fadeStart;
    protected static final float particleTextureWidth = 4.0f;

    protected Vec3d hitNormal;
    protected Vec3d[] finalQuad;

    protected static final EntityPlayerSP player = Minecraft.getMinecraft().player;
    protected float ipx;
    protected float ipy;
    protected float ipz;

    //    protected Vec3d facing;

    public SplatterParticleBase(World world, double x, double y, double z, double vx, double vy, double vz) {
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

    // The BASE splatter particle should never be rendered directly
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {}

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
        if (finalQuad == null || hitNormal == null) {
            return false;
        }
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
            if (block == null || !block.isFullBlock()) {
                falls++;
            }
        }
//        return (fallArea / totalArea) >= 0.05;
        return falls >= 3;
    }

    public boolean checkCovered() {
//        double coveredArea = 0.0;
//        double totalArea = 0.0;
        if (finalQuad == null || hitNormal == null) {
            return false;
        }
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
            if (block != null && block.isFullBlock()) {
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
