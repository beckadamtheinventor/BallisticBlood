package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;


public class SplatterParticle extends SplatterParticleBase {
    protected static final double SMALL_AMOUNT = 1.0f / 16.0f;
    protected static final double TINY_AMOUNT = 1.0f / 64.0f;
    protected boolean hFlip, vFlip, rotate;
//    protected int displayMatrixWidth;
//    protected boolean[] displayMatrix;

    public SplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        hFlip = vFlip = false;
    }

    public void setFlip(boolean h, boolean v, boolean r) {
        hFlip = h;
        vFlip = v;
        rotate = r;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float alpha = 1.0f;
        if (this.particleAge >= this.fadeStart) {
            alpha -= ((float)(this.particleAge - this.fadeStart) / (float)(this.particleMaxAge - this.fadeStart));
        }
        alpha *= this.particleAlpha;

        int i = this.getBrightnessForRender(partialTicks);
        int lx = (i >> 16) & 65535;
        int ly = i & 65535;

        Vec3d[] quad;
        if (hitOffset == null) {
            hitOffset = Vec3d.ZERO;
        }
        double px = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - ipx) + hitOffset.x;
        double py = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - ipy) + hitOffset.y;
        double pz = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - ipz) + hitOffset.z;
        float w = this.particleScale * this.width;
        if (this.onGround && this.finalQuad != null) {
            quad = finalQuad;
        } else {
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
            u0 = u1;
            u1 = t;
        }
        if (this.vFlip) {
            float t = v0;
            v0 = v1;
            v1 = t;
        }
//        if (this.rotate) {
//            float t = u0;
//            u0 = u1;
//            u1 = v1;
//            v1 = v0;
//            v0 = t;
//        }

//        GlStateManager.disableLighting();

//        GL11.glPushMatrix();
        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z)
                .tex(finalUVOffsets[0].x+u1, finalUVOffsets[0].y+v1)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alphaMultiplier*alpha)
                .lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z)
                .tex(finalUVOffsets[1].x+u1, finalUVOffsets[1].y+v0)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alphaMultiplier*alpha)
                .lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z)
                .tex(finalUVOffsets[2].x+u0, finalUVOffsets[2].y+v0)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alphaMultiplier*alpha)
                .lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z)
                .tex(finalUVOffsets[3].x+u0, finalUVOffsets[3].y+v1)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alphaMultiplier*alpha)
                .lightmap(lx, ly).endVertex();
//        GL11.glPopMatrix();
    }

    @Override
    public void onUpdate() {
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;
        // recompute vertex overhang if necessary
        this.computeVertexOverhang();
        if (this.checkIsHovering()) {
            this.setExpired();
            return;
        } else if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }
        for (SplatterParticle p : this.subParticles) {
            p.onUpdate();
        }
        this.subParticles.removeIf(p -> !p.isAlive());
        // update velocity
        this.motionY -= this.particleGravity;
        // try to move the particle first
        this.move(this.motionX, this.motionY, this.motionZ);
        // if the particle is currently a decal
        if (this.onGround) {
            // check if covered
            if (this.checkIsCovered()) {
                // expire if covered
                this.setExpired();
            }
        }
    }

    public boolean checkIsHovering() {
        if (finalQuad == null) {
            return false;
        }
        int hovering = 0;
        for (Vec3d vert : finalQuad) {
            BlockPos pos = new BlockPos(vert.add(this.getPositionVector()).subtract(hitNormal));
            IBlockState block = world.getBlockState(pos);
            if (block.getCollisionBoundingBox(world, pos) == null) {
                hovering++;
            }
        }
        return hovering > ForgeConfigHandler.client.floatingVertexFallThreshold;
    }

    public boolean checkIsCovered() {
        return checkIsColliding(hitNormal.scale(0.5));
    }

    public boolean checkIsColliding(Vec3d dir) {
        AxisAlignedBB boundingBox = this.getBoundingBox();
        List<AxisAlignedBB> worldCollisionBoxes = world.getCollisionBoxes(null, boundingBox.offset(dir.x, dir.y, dir.z));
        for(AxisAlignedBB box : worldCollisionBoxes) {
            if (box.intersects(boundingBox)) {
                return true;
            }
        }
        return false;
    }

    private static Vec3d getCorrectOffsetToSnapTo(Vec3d v, Vec3d p, Vec3d n, AxisAlignedBB box) {
        if (n.x == 0) {
            if (v.x < 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "-x");
                v = new Vec3d(box.minX-p.x, v.y, v.z);
            } else if (v.x > 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "+x");
                v = new Vec3d(box.maxX-p.x, v.y, v.z);
            }
        }
        if (n.y == 0) {
            if (v.y < 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "-y");
                v = new Vec3d(v.x, box.minY-p.y, v.z);
            } else if (v.y > 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "+y");
                v = new Vec3d(v.x, box.maxY-p.y, v.z);
            }
        }
        if (n.z == 0) {
            if (v.z < 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "-z");
                v = new Vec3d(v.x, v.y, box.minZ-p.z);
            } else if (v.z > 0) {
//                SplatterizerMod.LOGGER.log(Level.INFO, "+z");
                v = new Vec3d(v.x, v.y, box.maxZ-p.z);
            }
        }
        return v;
    }

    private Vec3d perAxisTernary(Vec3d a, Vec3d b, Vec3d c) {
        return new Vec3d(
                c.x==0 ? a.x : b.x,
                c.y==0 ? a.y : b.y,
                c.z==0 ? a.z : b.z
        );
    }

    private Vec3d getQuadVertexOffset(int i, Vec3d n) {
        Vec3d[] table = new Vec3d[] {
                new Vec3d(1, 0, 1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 0),
                new Vec3d(0, 0, 1)
        };
        if (n.y != 0) {
            return new Vec3d(table[i].x, 0, table[i].z).scale(SMALL_AMOUNT);
        }
        if (n.x != 0) {
            return new Vec3d(0, table[i].x, table[i].z).scale(SMALL_AMOUNT);
        }
        if (n.z != 0) {
            return new Vec3d(table[i].x, table[i].z, 0).scale(SMALL_AMOUNT);
        }
        return Vec3d.ZERO;
    }


    // kinda works right now
    private void computeVertexOverhang() {
        if (!this.onGround) {
            return;
        }
        if (!this.canCollide || this.finalQuad == null) {
            return;
        }
        if (!ForgeConfigHandler.client.enableExperimentalOverhangClipping) {
            return;
        }
        Vec3d[] orig = finalQuad.clone();

        double w = this.width*this.decalScale*0.5+2.0;
        List<AxisAlignedBB> worldCollisionBoxes =
                world.getCollisionBoxes(null, new AxisAlignedBB(
                        this.getPositionVector().subtract(w,w,w),
                        this.getPositionVector().add(w,w,w)
                ));
        if (worldCollisionBoxes.isEmpty()) {
            return;
        }
//        SplatterizerMod.LOGGER.log(Level.INFO, "worldCollisionBoxes.size() = " + worldCollisionBoxes.size());
        int anchors = 4;
        // get a quad identical to the final quad, offset by a small amount against the normal direction
        Vec3d mid = getPositionVector().subtract(hitNormal.scale(SMALL_AMOUNT));
        // find the bounding box directly underneath the decal
        AxisAlignedBB anchorBox = null;
        for (AxisAlignedBB box : worldCollisionBoxes) {
            if (box.contains(mid)) {
                anchorBox = box;
//                SplatterizerMod.LOGGER.log(Level.INFO, "underneath main: " + box);
                break;
            }
        }
        // if a bounding box was found directly beneath
        if (anchorBox != null) {
            // for each vertex of the bounding box
            for (int i = 0; i < finalQuad.length; i++) {
                // get a position just barely inside the decal's bounding box
                // that is next to the corner
                Vec3d v = finalQuad[i].add(getQuadVertexOffset(i, hitNormal)).add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT));
                //            double dx=v.x, dy=v.y, dz=v.z;
//                SplatterizerMod.LOGGER.log(Level.INFO, v);
                boolean anchored = false;
                for (AxisAlignedBB box : worldCollisionBoxes) {
//                    SplatterizerMod.LOGGER.log(Level.INFO, box);
                    if (box.contains(v)) {
                        anchored = true;
                        break;
                    }
                }
                if (!anchored) {
//                    SplatterizerMod.LOGGER.log(Level.INFO, "working???");
//                    SplatterizerMod.LOGGER.log(Level.INFO, "no box underneath vertex " + i);
                    anchors--;
                    // get the absolute position to snap the vertex to
                    Vec3d boxVec = getCorrectOffsetToSnapTo(finalQuad[i], getPositionVector(), hitNormal, anchorBox);
                    // if the movement is larger than a small amount
//                    if (v2.lengthSquared() >= SMALL_AMOUNT) {
                        // offset the vertex to align it with the bounding box
//                        SplatterizerMod.LOGGER.log(Level.INFO, i + " from " + finalQuad[i].add(this.getPositionVector()));
                        finalQuad[i] = perAxisTernary(boxVec, finalQuad[i], hitNormal);
//                        SplatterizerMod.LOGGER.log(Level.INFO, i + " to " + finalQuad[i].add(this.getPositionVector()));
//                    }
                }
            }
        }

        if (finalUVOffsets == null) {
            finalUVOffsets = new Vec2f[4];
        }
        for (int i = 0; i < finalUVOffsets.length; i++) {
            if (finalUVOffsets[i] == null) {
                finalUVOffsets[i] = Vec2f.ZERO;
            }
            if (hitNormal.x == 0) {
                if (hitNormal.y == 0) {
                    finalUVOffsets[i] = new Vec2f(
                            finalUVOffsets[i].x-(float)(finalQuad[i].x-orig[i].x)/(decalScale * width * particleTextureWidth),
                            finalUVOffsets[i].y-(float)(finalQuad[i].y-orig[i].y)/(decalScale * width * particleTextureHeight)
                    );
                } else if (hitNormal.z == 0) {
                    finalUVOffsets[i] = new Vec2f(
                            finalUVOffsets[i].x-(float)(finalQuad[i].x-orig[i].x)/(decalScale * width * particleTextureWidth),
                            finalUVOffsets[i].y-(float)(finalQuad[i].z-orig[i].z)/(decalScale * width * particleTextureHeight)
                    );
                }
            } else if (hitNormal.y == 0) {
                finalUVOffsets[i] = new Vec2f(
                        finalUVOffsets[i].x-(float)(finalQuad[i].y-orig[i].y)/(decalScale * width * particleTextureWidth),
                        finalUVOffsets[i].y-(float)(finalQuad[i].z-orig[i].z)/(decalScale * width * particleTextureHeight)
                );
            }
        }
    }

    public void computeFacing(double dx, double dy, double dz, double origX, double origY, double origZ) {
        if (origY != dy) {
            this.hitNormal = new Vec3d(0.0, -Math.signum(origY), 0.0);
            this.facing = (origY < 0 ? EnumFacing.DOWN : EnumFacing.UP);
        } else if (origX != dx) {
            this.hitNormal = new Vec3d(-Math.signum(origX), 0.0, 0.0);
            this.facing = (origX < 0 ? EnumFacing.WEST : EnumFacing.EAST);
        } else if (origZ != dz) {
            this.hitNormal = new Vec3d(0.0, 0.0, -Math.signum(origZ));
            this.facing = (origZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        double origX = dx;
        double origY = dy;
        double origZ = dz;
//        SplatterizerMod.LOGGER.log(Level.INFO, "move(" + dx + ", " + dy + ", " + dz + ")");
        if (this.canCollide && world != null) {
            // compute new bounding box based on the existing bounding box and the world
            AxisAlignedBB bb = this.getBoundingBox();
            List<AxisAlignedBB> worldCollisionBoxes = world.getCollisionBoxes(null, bb.expand(dx, dy, dz));
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

        // update particle position to match bounding box
        this.resetPositionToBB();

        // if the particle ran into something
        if (origX != dx || origY != dy || origZ != dz) {
//            SplatterizerMod.LOGGER.log(Level.INFO, "Landed!" + (onGround ? " Already on ground." : ""));
            // if the particle is not already on the ground,
            // compute a quad for the axis it landed on and
            // offset the position against the normal direction to minimize Z-fighting
            if (!onGround) {
                // figure out which face the particle landed on
                computeFacing(dx, dy, dz, origX, origY, origZ);
                BlockPos pos = new BlockPos(posX, posY, posZ);
                float quadOffset = -ForgeConfigHandler.client.decalSurfaceOffsetMultiplier * (0.6f * rand.nextFloat() + 0.4f);
                if (origY != dy) {
                    this.posY = pos.getY() + (origY < 0 ? 0 : 1);
                    quadOffset *= (float)Math.signum(origY);
                } else if (origX != dx) {
                    this.posX = pos.getX() + (origX < 0 ? 0 : 1);
                    quadOffset *= (float)Math.signum(origX);
                } else if (origZ != dz) {
                    this.posZ = pos.getZ() + (origZ < 0 ? 0 : 1);
                    quadOffset *= (float)Math.signum(origZ);
                } else {
                    return;
                }

                // spray particles don't make decals
                if (this.subType == ParticleSubType.SPRAY) {
                    this.setExpired();
                } else {
                    // particle just hit the ground, fix it in position,
                    // generate decal quad that is separate from the bounding box
                    this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, 1.0);
                    for (int i=0; i<this.finalQuad.length; i++) {
                        this.finalQuad[i] = this.finalQuad[i].scale(this.width * this.decalScale);
                    }
                    this.hitOffset = this.hitNormal.scale(quadOffset);

                    this.onGround = true;
                    this.oldSubType = this.subType;
                    setParticleSubType(ParticleSubType.DECAL);
//                    this.prevPosX = posX;
//                    this.prevPosY = posY;
//                    this.prevPosZ = posZ;
                }
                // zero the particle velocity
                this.motionX = this.motionY = this.motionZ = 0.0;
            }
//        } else {
//            // if moved more than a small amount
//            if (Math.abs(prevPosX-posX) >= SMALL_AMOUNT || Math.abs(prevPosY-posY) >= SMALL_AMOUNT || Math.abs(prevPosZ-posZ) >= SMALL_AMOUNT) {
//                // update the bounding box
//                this.setPosition(posX, posY, posZ);
//                this.onGround = false;
//            } else {
//                this.posX = prevPosX;
//                this.posY = prevPosY;
//                this.posZ = prevPosZ;
//                this.resetPositionToBB();
//            }
        }
    }
}
