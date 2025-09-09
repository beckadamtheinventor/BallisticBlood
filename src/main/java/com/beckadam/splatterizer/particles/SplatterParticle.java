package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.helpers.ParticleHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;


public class SplatterParticle extends SplatterParticleBase {
    protected boolean hFlip, vFlip;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        hFlip = vFlip = false;
    }

    public void setFlip(boolean h, boolean v) {
        hFlip = h;
        vFlip = v;
    }

    @Override
    public void addSubparticle(SplatterParticle particle) {
        particle.setAllowSubparticles(false);
        subParticles.add(particle);
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
            float w = particleScale * this.width;
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
                    this.particleTextureIndexY = this.subType.ordinal() - 1;
                } else if (this.checkIsCovered()) {
                    this.setExpired();
                }
            }
        }
        spawnSubParticles(ForgeConfigHandler.client.particleSubMax);
    }

    public boolean checkShouldFall() {
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(-1)));
        boolean collided = world.checkBlockCollision(this.getBoundingBox());
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal));
        return !collided;
    }

    public boolean checkIsCovered() {
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal));
        boolean collided = world.checkBlockCollision(this.getBoundingBox());
        this.setBoundingBox(this.getBoundingBox().offset(hitNormal.scale(-1)));
        return collided;
    }

    private static Vec3d floorOrCeilVec3d(Vec3d vec, Vec3d normal) {
        double sx = Math.signum(normal.x);
        double sy = Math.signum(normal.y);
        double sz = Math.signum(normal.z);
        double x = sx>0 ? Math.floor(vec.x) : (sx==0 ? vec.x : Math.ceil(vec.x));
        double y = sy>0 ? Math.floor(vec.y) : (sy==0 ? vec.y : Math.ceil(vec.y));
        double z = sz>0 ? Math.floor(vec.z) : (sz==0 ? vec.z : Math.ceil(vec.z));
        return new Vec3d(x, y, z);
    }
    private void computeVertexOverhang() {
        if (!this.canCollide) {
            return;
        }
        Vec3d posVec = new Vec3d(posX, posY, posZ).subtract(hitNormal);
        Vec3d vert0 = finalQuad[0].add(posVec);
        Vec3d vert1 = finalQuad[1].add(posVec);
        Vec3d vert2 = finalQuad[2].add(posVec);
        Vec3d vert3 = finalQuad[3].add(posVec);
        boolean vert0attached = false;
        boolean vert1attached = false;
        boolean vert2attached = false;
        boolean vert3attached = false;
        for (AxisAlignedBB box : worldCollisionBoxes) {
            vert0attached |= box.contains(vert0);
            vert1attached |= box.contains(vert1);
            vert2attached |= box.contains(vert2);
            vert3attached |= box.contains(vert3);
        }
        Vec3d[] orig = finalQuad.clone();
        if (!vert0attached) {
            finalQuad[0] = floorOrCeilVec3d(finalQuad[0], hitNormal);
            SplatterizerMod.LOGGER.log(Level.INFO, "Moving Vertex 0 to " + finalQuad[0]);
        }
        if (!vert1attached) {
            finalQuad[1] = floorOrCeilVec3d(finalQuad[1], hitNormal);
            SplatterizerMod.LOGGER.log(Level.INFO, "Moving Vertex 1 to " + finalQuad[1]);
        }
        if (!vert2attached) {
            finalQuad[2] = floorOrCeilVec3d(finalQuad[2], hitNormal);
            SplatterizerMod.LOGGER.log(Level.INFO, "Moving Vertex 2 to " + finalQuad[2]);
        }
        if (!vert3attached) {
            finalQuad[3] = floorOrCeilVec3d(finalQuad[3], hitNormal);
            SplatterizerMod.LOGGER.log(Level.INFO, "Moving Vertex 3 to " + finalQuad[3]);
        }
//        finalUVs = new Vec2f[4];
//        for (int i=0; i<finalUVs.length; i++) {
//            if (hitNormal.x == 0) {
//                if (hitNormal.y == 0) {
//                    finalUVs[i] = new Vec2f((float)finalQuad[i].x, (float)finalQuad[i].y);
//                } else if (hitNormal.z == 0) {
//                    finalUVs[i] = new Vec2f((float)finalQuad[i].x, (float)finalQuad[i].z);
//                }
//            } else if (hitNormal.y == 0) {
//                finalUVs[i] = new Vec2f((float)finalQuad[i].y, (float)finalQuad[i].z);
//            }
//            finalUVs[i] = new Vec2f(
//                    (finalUVs[i].x - (float)orig[i].x) / particleTextureWidth,
//                    finalUVs[i].y - (float)orig[i].y
//            );
//        }
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
                this.finalQuad = ParticleHelper.getAxisAlignedQuad(facing, this.particleScale * this.width);
//                this.computeVertexOverhang();
                this.posY -= 0.05 * Math.signum(origY) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Floor position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else if (origX != dx) {
                this.hitNormal = new Vec3d(-Math.signum(origX), 0.0, 0.0);
                this.posX = pos.getX() + (origX < 0 ? 0 : 1);
                this.facing = (origX < 0 ? EnumFacing.WEST : EnumFacing.EAST);
                this.finalQuad = ParticleHelper.getAxisAlignedQuad(facing, this.particleScale * this.width);
//                this.computeVertexOverhang();
                this.posX -= 0.05 * Math.signum(origX) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Wall X position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else if (origZ != dz) {
                this.hitNormal = new Vec3d(0.0, 0.0, -Math.signum(origZ));
                this.posZ = pos.getZ() + (origZ < 0 ? 0 : 1);
                this.facing = (origZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
                this.finalQuad = ParticleHelper.getAxisAlignedQuad(facing, this.particleScale * this.width);
//                this.computeVertexOverhang();
                this.posZ -= 0.05 * Math.signum(origZ) * (0.6f + 0.4f * rand.nextFloat());
//                SplatterizerMod.LOGGER.log(Level.INFO, "Wall Z position: " + new Vec3d(this.posX, this.posY, this.posZ));
            } else {
                return;
            }
            this.onGround = true;
            this.particleTextureIndexY = ParticleSubType.DECAL.ordinal() - 1;
            this.motionX = motionY = motionZ = 0.0;
            this.prevPosX = posX;
            this.prevPosY = posY;
            this.prevPosZ = posZ;
        }

    }
}
