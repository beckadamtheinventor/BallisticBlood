package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.helpers.CommonHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SplatterParticle extends SplatterParticleBase {
    protected boolean hFlip, vFlip;
    protected int displayMatrixWidth;
    protected boolean[] displayMatrix;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        hFlip = vFlip = false;
    }

    public void setFlip(boolean h, boolean v) {
        hFlip = h;
        vFlip = v;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        int i = this.getBrightnessForRender(partialTicks);
        int lx = (i >> 16) & 65535;
        int ly = i & 65535;

        Vec3d[] quad;
        double px = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - ipx);
        double py = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - ipy);
        double pz = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - ipz);
        if (this.onGround && this.finalQuad != null) {
            quad = finalQuad;
        } else {
            float w = this.particleScale * this.width;
            quad = new Vec3d[] {
                    new Vec3d((-rotationX * w - rotationXY * w), (-rotationZ * w), (-rotationYZ * w - rotationXZ * w)),
                    new Vec3d((-rotationX * w + rotationXY * w), (rotationZ * w), (-rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w + rotationXY * w), (rotationZ * w), (rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w - rotationXY * w), (-rotationZ * w), (rotationYZ * w - rotationXZ * w))
            };
        }

        float u0 = (float)this.particleTextureIndexX / particleTextureWidth;
        float v0 = (float)this.particleTextureIndexY / particleTextureHeight;
        float u1 = u0 + 1.0f / particleTextureWidth;
        float v1 = v0 + 1.0f / particleTextureHeight;
        if (this.hFlip) {
            float t = u0;
            u0 = u1; u1 = t;
        }
        if (this.vFlip) {
            float t = v0;
            v0 = v1; v1 = t;
        }

//        GlStateManager.disableLighting();

//        GL11.glPushMatrix();
        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z)
                .tex(finalUVs[0].x+u1, finalUVs[0].y+v1)
                .color(1, 1, 1, particleAlpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z)
                .tex(finalUVs[1].x+u1, finalUVs[1].y+v0)
                .color(1, 1, 1, particleAlpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z)
                .tex(finalUVs[2].x+u0, finalUVs[2].y+v0)
                .color(1, 1, 1, particleAlpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z)
                .tex(finalUVs[3].x+u0, finalUVs[3].y+v1)
                .color(1, 1, 1, particleAlpha).lightmap(lx, ly).endVertex();
//        GL11.glPopMatrix();
    }

    @Override
    public void onUpdate() {
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
        this.subParticles.removeIf(splatterSubParticle -> !splatterSubParticle.isAlive());
        // move the particle until it hits something
        if (!this.onGround) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= 0.04 * (double)this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
        } else {
            if ((this.particleAge & 3) == 0) {
                if (this.checkShouldFall()) {
                    this.onGround = false;
                    this.finalQuad = null;
                    Arrays.fill(finalUVs, Vec2f.ZERO);
                    this.setParticleSubType(this.oldSubType);
                } else if (this.checkIsCovered()) {
                    this.setExpired();
                }
            }
        }
    }

    public boolean checkShouldFall() {
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(-0.5)));
        boolean collided = world.checkBlockCollision(this.getBoundingBox());
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(0.5)));
        return !collided;
    }

    public boolean checkIsCovered() {
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(0.5)));
        boolean collided = world.checkBlockCollision(this.getBoundingBox());
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(-0.5)));
        return collided;
    }

    private static Vec3d doVertexClip(Vec3d vec, Vec3d normal) {
        double x = vec.x, y = vec.y, z = vec.z;
        if (normal.x == 0 && normal.z == 0) {
            y = vec.y > 0 ? Math.floor(vec.y) : (vec.y == 0 ? vec.y : Math.ceil(vec.y));
        }
        if (normal.y == 0 && normal.z == 0) {
            x = vec.x > 0 ? Math.floor(vec.x) : (vec.x == 0 ? vec.x : Math.ceil(vec.x));
        }
        if (normal.x == 0 && normal.y == 0) {
            z = vec.z > 0 ? Math.floor(vec.z) : (vec.z == 0 ? vec.z : Math.ceil(vec.z));
        }
        return new Vec3d(x, y, z);
    }

    private void computeVertexOverhang() {
        if (!this.canCollide) {
            return;
        }
        Vec3d posVec = new Vec3d(posX, posY, posZ).subtract(hitNormal);

        Vec3d[] orig = finalQuad.clone();
//        AxisAlignedBB box = new AxisAlignedBB(-1e18, -1e18, -1e18, 1e18, 1e18, 1e18);
        for (int i=0; i<finalQuad.length; i++) {
            Vec3d pv = finalQuad[i].add(posVec);
            BlockPos pos = new BlockPos(pv);
            IBlockState block = world.getBlockState(pos);
//            List<AxisAlignedBB> blockBoundingBoxes = new ArrayList<>();
//            block.addCollisionBoxToList(world, pos, box, blockBoundingBoxes, null, false);
//            boolean attached = false;
//            for (AxisAlignedBB bb : blockBoundingBoxes) {
//                attached |= bb.contains(pv);
//            }
//            if (!attached) {
            if (!block.isFullBlock()) {
                finalQuad[i] = doVertexClip(pv, hitNormal).subtract(pv);
                SplatterizerMod.LOGGER.log(Level.INFO, "Moving Vertex " + i + " to " + finalQuad[i]);
            }
//            }
        }

        finalUVs = new Vec2f[4];
        for (int i=0; i<finalUVs.length; i++) {
            if (finalUVs[i] == null) {
                finalUVs[i] = Vec2f.ZERO;
            }
            if (hitNormal.x == 0) {
                if (hitNormal.y == 0) {
                    finalUVs[i] = new Vec2f(
                            finalUVs[i].x+(float)(finalQuad[i].x-orig[i].x)/particleTextureWidth,
                            finalUVs[i].y+(float)(finalQuad[i].y-orig[i].y)/particleTextureHeight
                    );
                } else if (hitNormal.z == 0) {
                    finalUVs[i] = new Vec2f(
                            finalUVs[i].x+(float)(finalQuad[i].x-orig[i].x)/particleTextureWidth,
                            finalUVs[i].y+(float)(finalQuad[i].z-orig[i].z)/particleTextureHeight
                    );
                }
            } else if (hitNormal.y == 0) {
                finalUVs[i] = new Vec2f(
                        finalUVs[i].x+(float)(finalQuad[i].y-orig[i].y)/particleTextureWidth,
                        finalUVs[i].y+(float)(finalQuad[i].z-orig[i].z)/particleTextureHeight
                );
            }
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        double origX = dx;
        double origY = dy;
        double origZ = dz;
        if (this.canCollide && world != null) {
            AxisAlignedBB bb = this.getBoundingBox();
            worldCollisionBoxes = world.getCollisionBoxes(null, bb.expand(dx, dy, dz));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dy = axisalignedbb.calculateYOffset(bb, dy);
            }
            this.setBoundingBox(bb.offset(0, dy, 0));
            bb = this.getBoundingBox();
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dx = axisalignedbb.calculateXOffset(bb, dx);
            }
            this.setBoundingBox(bb.offset(dx, 0, 0));
            bb = this.getBoundingBox();
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dz = axisalignedbb.calculateZOffset(bb, dz);
            }
            this.setBoundingBox(bb.offset(0, 0, dz));
        } else {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        }

        this.resetPositionToBB();

        if (origX != dx || origY != dy || origZ != dz) {
            BlockPos pos = new BlockPos(posX, posY, posZ);
            if (origY != dy) {
                this.hitNormal = new Vec3d(0.0, -Math.signum(origY), 0.0);
                this.posY = pos.getY() + (origY < 0 ? 0 : 1);
                this.facing = (origY < 0 ? EnumFacing.DOWN : EnumFacing.UP);
                this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, this.particleScale * this.width * this.decalScale);
                this.computeVertexOverhang();
                this.posY -= 0.05 * Math.signum(origY) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Floor position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else if (origX != dx) {
                this.hitNormal = new Vec3d(-Math.signum(origX), 0.0, 0.0);
                this.posX = pos.getX() + (origX < 0 ? 0 : 1);
                this.facing = (origX < 0 ? EnumFacing.WEST : EnumFacing.EAST);
                this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, this.particleScale * this.width);
                this.computeVertexOverhang();
                this.posX -= 0.05 * Math.signum(origX) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Wall X position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else if (origZ != dz) {
                this.hitNormal = new Vec3d(0.0, 0.0, -Math.signum(origZ));
                this.posZ = pos.getZ() + (origZ < 0 ? 0 : 1);
                this.facing = (origZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
                this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, this.particleScale * this.width);
                this.computeVertexOverhang();
                this.posZ -= 0.05 * Math.signum(origZ) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Wall Z position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else {
                return;
            }
            this.onGround = true;
            this.oldSubType = this.subType;
            setParticleSubType(ParticleSubType.DECAL);
            this.motionX = motionY = motionZ = 0.0;
            this.prevPosX = posX;
            this.prevPosY = posY;
            this.prevPosZ = posZ;
        }

    }
}
