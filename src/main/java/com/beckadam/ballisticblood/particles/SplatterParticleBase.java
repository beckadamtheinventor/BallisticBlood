package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.handlers.ParticleConfig;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.List;


@SideOnly(Side.CLIENT)
public class SplatterParticleBase extends Particle {
    protected static final int[] hFlipVertexIndex = new int[] { 1, 0, 3, 2 };
    protected static final int[] vFlipVertexIndex = new int[] { 2, 3, 0, 1 };
    protected static final double SMALL_AMOUNT = 1.0f / 8.0f;
    protected static final double TINY_AMOUNT = 1.0f / 64.0f;
    protected static float ipx, ipy, ipz;

    protected ParticleConfig cfg;
    protected int fadeStart;
    protected GlStateManager.SourceFactor sourceFactor;
    protected GlStateManager.DestFactor destFactor;
    protected int blendOp;
    protected boolean canDecal;

    protected Vec3d hitNormal;
    protected Vec3d hitOffset;
    protected Vec3d[] finalQuad;
    protected Vec2f[] finalUVOffsets;
    protected EnumFacing facing;
    protected ParticleDisplayType displayType, oldDisplayType;
    protected boolean hFlip, vFlip, rotate;

    public SplatterParticleBase(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z);
        width = ForgeConfigHandler.client.particleSize;
        height = 0.1f;
        particleScale = 1.0f;
        particleGravity = 1.0f;
        motionX = vx;
        motionY = vy;
        motionZ = vz;
        particleMaxAge = ForgeConfigHandler.client.particleLifetime;
        finalUVOffsets = new Vec2f[] { Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO };
        displayType = ParticleDisplayType.BASE;
        hFlip = vFlip = rotate = false;
        canDecal = true;
    }

    public void setConfig(ParticleConfig config, float gravity, float size) {
        cfg = config;
        particleGravity = cfg.gravity * gravity * ForgeConfigHandler.client.particleGravityBase / 20.0f;
        particleScale = cfg.size * size;
    }

    public void setFlip(boolean h, boolean v, boolean r) {
        hFlip = h;
        vFlip = v;
        rotate = r;
    }

    public void setLifetime(int end, int fade) {
        fadeStart = fade;
        particleMaxAge = end;
    }

    public void setDisplayType(ParticleDisplayType type) {
        displayType = type;
    }

    public void randomizeParticleTexture() {
        switch (displayType) {
            case DECAL:
                particleTextureIndexY = rand.nextInt(5); // rows 0, 1, 2, 3, 4
                break;
            case PROJECTILE:
                particleTextureIndexY = rand.nextInt(2) + 5; // rows 5 and 6
                break;
            case SPRAY:
                particleTextureIndexY = 7;
                break;
            case IMPACT:
            case BASE:
            default:
                break;
        }
        particleTextureIndexX = rand.nextInt(cfg.tiling[0]);
        particleTextureIndexY %= cfg.tiling[1];
        float u0 = (float)particleTextureIndexX / (float)cfg.tiling[0];
        float v0 = (float)particleTextureIndexY / (float)cfg.tiling[1];
        float u1 = u0 + 1.0f / (float)cfg.tiling[0];
        float v1 = v0 + 1.0f / (float)cfg.tiling[1];
        finalUVOffsets = new Vec2f[] {
                new Vec2f(u1, v1),
                new Vec2f(u1, v0),
                new Vec2f(u0, v0),
                new Vec2f(u0, v1)
        };
    }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) {}

    public Vec3d getPositionVector() {
        return new Vec3d(posX, posY, posZ);
    }

    public Vec3d getDirectionVector() {
        return new Vec3d(motionX, motionY, motionZ);
    }

    public void setPositionVector(Vec3d v) {
        posX = v.x; posY = v.y; posZ = v.z;
    }


    @Override
    public void renderParticle(BufferBuilder buffer, Entity playerEntity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        // don't render particle if it's out of render distance
        if (playerEntity.getDistanceSq(posX, posY, posZ) >= ForgeConfigHandler.client.particleRenderDistanceCubed) {
            return;
        }
//        // TODO: don't render particle if it's behind the player
//        double ang = Math.atan2(playerEntity.posX - posX, playerEntity.posZ - posZ);
//        if (ang >= Math.PI*0.5 && ang < Math.PI*1.5) {
//            return;
//        }
        float alpha;
        if (particleAge >= fadeStart) {
            alpha = (1.0f - ((float)(particleAge - fadeStart) / (float)(particleMaxAge - fadeStart)))
                    * particleAlpha * cfg.alphaMultiplier;
        } else {
            alpha = particleAlpha * cfg.alphaMultiplier;
        }

        int i = getBrightnessForRender(partialTicks);
        int lx = (i >> 16) & 65535;
        int ly = i & 65535;

        Vec3d[] quad;
        if (hitOffset == null) {
            hitOffset = Vec3d.ZERO;
        }
        double px = (prevPosX + (posX - prevPosX) * partialTicks - ipx) + hitOffset.x;
        double py = (prevPosY + (posY - prevPosY) * partialTicks - ipy) + hitOffset.y;
        double pz = (prevPosZ + (posZ - prevPosZ) * partialTicks - ipz) + hitOffset.z;
        float w = particleScale * width;
        if (onGround && finalQuad != null) {
            quad = finalQuad;
        } else {
            quad = new Vec3d[] {
                    new Vec3d((-rotationX * w - rotationXY * w), (-rotationZ * w), (-rotationYZ * w - rotationXZ * w)),
                    new Vec3d((-rotationX * w + rotationXY * w), (rotationZ * w), (-rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w + rotationXY * w), (rotationZ * w), (rotationYZ * w + rotationXZ * w)),
                    new Vec3d((rotationX * w - rotationXY * w), (-rotationZ * w), (rotationYZ * w - rotationXZ * w))
            };
        }

//        if (rotate) {
//            float t = u0;
//            u0 = u1;
//            u1 = v1;
//            v1 = v0;
//            v0 = t;
//        }

        float cr = cfg.colorMultiplierR * particleRed;
        float cg = cfg.colorMultiplierG * particleGreen;
        float cb = cfg.colorMultiplierB * particleBlue;

        buffer.pos(px + quad[0].x, py + quad[0].y, pz + quad[0].z)
                .tex(finalUVOffsets[0].x, finalUVOffsets[0].y)
                .color(cr, cg, cb, alpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z)
                .tex(finalUVOffsets[1].x, finalUVOffsets[1].y)
                .color(cr, cg, cb, alpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z)
                .tex(finalUVOffsets[2].x, finalUVOffsets[2].y)
                .color(cr, cg, cb, alpha).lightmap(lx, ly).endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z)
                .tex(finalUVOffsets[3].x, finalUVOffsets[3].y)
                .color(cr, cg, cb, alpha).lightmap(lx, ly).endVertex();
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void setExpired() {
        super.setExpired();
    }

    @Override
    public void onUpdate() {
        // check whether particles are disabled; destroy if so
        if (!ForgeConfigHandler.client.enableSplatterParticles) {
            setExpired();
            return;
        }
        // check whether this particle has expired; destroy if so
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        if (posY <= -128.0) {
            setExpired();
            return;
        }
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (canCollide && canDecal) {
            // recompute vertex overhang if necessary
            computeVertexOverhang();
            if (onGround && checkIsHovering()) {
                finalQuad = CommonHelper.GetAxisAlignedQuad(facing, width * ForgeConfigHandler.client.decalScale);
                setPositionVector(getQuadFaceMiddle(getPositionVector(), hitNormal));
                if (checkIsHovering()) {
                    setExpired();
                }
                return;
            }
        }
        if (!onGround) {
            // update velocity
            motionY -= particleGravity;
        }
        // try to move the particle first
        move(motionX, motionY, motionZ);
        // if the particle is currently a decal
        if (canCollide && onGround && canDecal) {
            // check if covered
            if (checkIsCovered()) {
                // expire if covered
                setExpired();
            }
        }
    }

    public boolean checkIsHovering() {
        if (finalQuad == null) {
            return false;
        }
        int hovering = 0;
        for (Vec3d vert : finalQuad) {
            BlockPos pos = new BlockPos(vert.add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT)));
            IBlockState block = world.getBlockState(pos);
            if (block.getCollisionBoundingBox(world, pos) == null) {
                hovering++;
            }
        }
        return hovering > ForgeConfigHandler.client.floatingVertexFallThreshold;
    }

    public static Vec3d getQuadFaceMiddle(Vec3d p, Vec3d n) {
        return new Vec3d(
                n.x<0 ? Math.ceil(p.x) : (n.x>0 ? Math.floor(p.x) : 0),
                n.y<0 ? Math.ceil(p.y) : (n.y>0 ? Math.floor(p.y) : 0),
                n.z<0 ? Math.ceil(p.z) : (n.z>0 ? Math.floor(p.z) : 0)
        );
    }

    public boolean checkIsCovered() {
        return checkIsColliding(hitNormal.scale(SMALL_AMOUNT));
    }

    public boolean checkIsColliding(Vec3d dir) {
        AxisAlignedBB boundingBox = getBoundingBox().offset(dir);
        List<AxisAlignedBB> worldCollisionBoxes = world.getCollisionBoxes(Minecraft.getMinecraft().player, boundingBox.expand(2.0, 2.0, 2.0));
        for(AxisAlignedBB box : worldCollisionBoxes) {
            if (box.intersects(boundingBox)) {
                return true;
            }
        }
        return false;
    }

    private static Vec3d snapOffsetX(Vec3d v, Vec3d p, AxisAlignedBB box) {
        if (v.x < 0) {
            return new Vec3d(box.minX+TINY_AMOUNT-p.x, v.y, v.z);
        } else if (v.x > 0) {
            return new Vec3d(box.maxX-TINY_AMOUNT-p.x, v.y, v.z);
        }
        return null;
    }

    private static Vec3d snapOffsetY(Vec3d v, Vec3d p, AxisAlignedBB box) {
        if (v.y < 0) {
            return new Vec3d(v.x, box.minY+TINY_AMOUNT-p.y, v.z);
        } else if (v.y > 0) {
            return new Vec3d(v.x, box.maxY-TINY_AMOUNT-p.y, v.z);
        }
        return null;
    }

    private static Vec3d snapOffsetZ(Vec3d v, Vec3d p, AxisAlignedBB box) {
        if (v.z < 0) {
            return new Vec3d(v.x, v.y, box.minZ+TINY_AMOUNT-p.z);
        } else if (v.z > 0) {
            return new Vec3d(v.x, v.y, box.maxZ-TINY_AMOUNT-p.z);
        }
        return null;
    }

    private static Vec3d getOffsetToSnapTo(Vec3d v, Vec3d p, Vec3d n, AxisAlignedBB box) {
        Vec3d sx = snapOffsetX(v, p, box);
        Vec3d sy = snapOffsetY(v, p, box);
        Vec3d sz = snapOffsetZ(v, p, box);
        double dx = sx != null ? sx.lengthSquared() : 0;
        double dy = sy != null ? sy.lengthSquared() : 0;
        double dz = sz != null ? sz.lengthSquared() : 0;
        dx = dx < SMALL_AMOUNT ? 1e6 : dx;
        dy = dy < SMALL_AMOUNT ? 1e6 : dy;
        dz = dz < SMALL_AMOUNT ? 1e6 : dz;
        // this is the ugly if ladder that picks the axis with the least non-zero distance from the collision box
        if (dx < dy && dx < dz) {
            if (n.x == 0 && sx != null) {
                return sx;
            }
            if (dz < dy) {
                if (n.z == 0 && sz != null) {
                    return sz;
                }
                if (n.y == 0 && sy != null) {
                    return sy;
                }
            } else {
                if (n.y == 0 && sy != null) {
                    return sy;
                }
                if (n.z == 0 && sz != null) {
                    return sz;
                }
            }
        } else if (dz < dy) {
            if (n.z == 0 && sz != null) {
                return sz;
            }
            if (n.y == 0 && sy != null) {
                return sy;
            }
            if (n.x == 0 && sx != null) {
                return sx;
            }
        } else {
            if (n.y == 0 && sy != null) {
                return sy;
            }
            if (n.z == 0 && sz != null) {
                return sz;
            }
            if (n.x == 0 && sx != null) {
                return sx;
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
                new Vec3d( 1, 0,  1),
                new Vec3d( 1, 0, -1),
                new Vec3d(-1, 0, -1),
                new Vec3d(-1, 0,  1)
        };
        if (n.y != 0) {
            return new Vec3d(table[i].x, 0, table[i].z).scale(SMALL_AMOUNT*0.5);
        }
        if (n.x != 0) {
            return new Vec3d(0, table[i].x, table[i].z).scale(SMALL_AMOUNT*0.5);
        }
        if (n.z != 0) {
            return new Vec3d(table[i].x, table[i].z, 0).scale(SMALL_AMOUNT*0.5);
        }
        return Vec3d.ZERO;
    }


    // kinda works
    private void computeVertexOverhang() {
        if (!onGround) {
            return;
        }
        if (!canCollide || finalQuad == null) {
            return;
        }
        if (!ForgeConfigHandler.client.enableExperimentalOverhangClipping) {
            return;
        }
        // only run this every 4 client ticks
        if ((particleAge & 3) != 0) {
            return;
        }

        double w = width*ForgeConfigHandler.client.decalScale*0.5+1.0;
        List<AxisAlignedBB> worldCollisionBoxes =
                world.getCollisionBoxes(Minecraft.getMinecraft().player, new AxisAlignedBB(
                        getPositionVector().subtract(w,w,w),
                        getPositionVector().add(w,w,w)
                ));
        if (worldCollisionBoxes.isEmpty()) {
            setExpired();
            return;
        }
        Vec3d[] orig = finalQuad.clone();
        // get a position that is in the middle of the quad, offset against the normal direction
        Vec3d mid = finalQuad[0].add(finalQuad[1]).add(finalQuad[2]).add(finalQuad[3]).scale(0.25)
                .add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT));
        // find the bounding box directly underneath the decal
        AxisAlignedBB anchorBox = null;
        for (AxisAlignedBB box : worldCollisionBoxes) {
            if (box.contains(mid)) {
                anchorBox = box;
                break;
            }
        }
        // if a bounding box was found directly beneath
        if (anchorBox != null) {
            // for each corner of the quad
            for (int i = 0; i < finalQuad.length; i++) {
                // get a position just barely inside the decal's bounding box
                // that is next to the corner
                Vec3d v = finalQuad[i].subtract(finalQuad[i].normalize().scale(TINY_AMOUNT)).add(getPositionVector());;
                Vec3d v2 = v.subtract(hitNormal.scale(SMALL_AMOUNT));

                //            double dx=v.x, dy=v.y, dz=v.z;
                boolean anchored = false;
                boolean covered = false;
                for (AxisAlignedBB box : worldCollisionBoxes) {
                    if (box.contains(v)) {
                        covered = true;
                        break;
                    }
                    if (box.contains(v2)) {
                        anchored = true;
                        break;
                    }
                }
                if (covered || !anchored) {
                    // get the absolute position to snap the vertex to
                    Vec3d boxVec = getOffsetToSnapTo(finalQuad[i], getPositionVector(), hitNormal, anchorBox);
                    // offset the vertex to align it with the bounding box
//                    Vec3d old = finalQuad[i];
                    finalQuad[i] = perAxisTernary(boxVec, finalQuad[i], hitNormal);
//                    if (finalQuad[i].squareDistanceTo(old) > TINY_AMOUNT*TINY_AMOUNT) {
//                        BallisticBloodMod.LOGGER.log(Level.INFO, finalQuad[i].toString() + " -> " + old.toString());
//                    }
                    // check if vertex is now anchored after being moved
//                    v = finalQuad[i].scale(0.99).add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT));
//                    for (AxisAlignedBB box : worldCollisionBoxes) {
//                        if (box.contains(v)) {
//                            anchored = true;
//                        }
//                    }
//                    if (!anchored) {
//                        setExpired();;
//                    }
                }
            }
            // for each edge of the quad
            for (int i = 0; i < finalQuad.length; i++) {
                Vec3d v = finalQuad[i].add(finalQuad[(i+1)&3]).scale(0.5);
                v = v.subtract(v.normalize().scale(TINY_AMOUNT))
                        .subtract(hitNormal.scale(SMALL_AMOUNT)).add(getPositionVector());
                boolean anchored = false;
                for (AxisAlignedBB box : worldCollisionBoxes) {
                    if (box.contains(v)) {
                        anchored = true;
                        break;
                    }
                }
                if (!anchored) {
                    // get the absolute position to snap the vertices to
                    Vec3d boxVec = getOffsetToSnapTo(finalQuad[i], getPositionVector(), hitNormal, anchorBox);
                    Vec3d boxVec2 = getOffsetToSnapTo(finalQuad[(i+1)&3], getPositionVector(), hitNormal, anchorBox);
                    finalQuad[i] = perAxisTernary(boxVec, finalQuad[i], hitNormal);
                    finalQuad[(i+1)&3] = boxVec2;
                }
            }
        }

        for (int i = 0; i < finalUVOffsets.length; i++) {
            Vec3d ud = finalQuad[i].subtract(orig[i]).scale(1.0f / (width * ForgeConfigHandler.client.decalScale * cfg.tiling[0]));
            if (hitNormal.x == 0) {
                if (hitNormal.y == 0) {
                    // facing North or South
                    if (hitNormal.z > 0) {
                        finalUVOffsets[i] = new Vec2f(
                                finalUVOffsets[i].x + (float) ud.x,
                                finalUVOffsets[i].y + (float) ud.y);
                    } else {
                        finalUVOffsets[i] = new Vec2f(
                                finalUVOffsets[i].x - (float) ud.x,
                                finalUVOffsets[i].y + (float) ud.y);
                    }
                } else if (hitNormal.z == 0) {
                    // facing up or down
                    if (hitNormal.y > 0) {
                        finalUVOffsets[i] = new Vec2f(
                                finalUVOffsets[i].x - (float) ud.z,
                                finalUVOffsets[i].y - (float) ud.x);
                    } else {
                        finalUVOffsets[i] = new Vec2f(
                                finalUVOffsets[i].x + (float) ud.x,
                                finalUVOffsets[i].y + (float) ud.z);
                    }
                }
            } else if (hitNormal.y == 0) {
                // facing East or West
                if (hitNormal.z > 0) {
                    finalUVOffsets[i] = new Vec2f(
                            finalUVOffsets[i].x + (float) ud.z,
                            finalUVOffsets[i].y + (float) ud.y);
                } else {
                    finalUVOffsets[i] = new Vec2f(
                            finalUVOffsets[i].x + (float) ud.z,
                            finalUVOffsets[i].y + (float) ud.y);
                }
            }
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        double origX = dx;
        double origY = dy;
        double origZ = dz;
        if (canCollide && world != null) {
            // compute new bounding box based on the existing bounding box and the world
            List<AxisAlignedBB> worldCollisionBoxes = world.getCollisionBoxes(Minecraft.getMinecraft().player, getBoundingBox().expand(dx, dy, dz));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dy = axisalignedbb.calculateYOffset(getBoundingBox(), dy);
            }
            setBoundingBox(getBoundingBox().offset(0, dy, 0));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dx = axisalignedbb.calculateXOffset(getBoundingBox(), dx);
            }
            setBoundingBox(getBoundingBox().offset(dx, 0, 0));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dz = axisalignedbb.calculateZOffset(getBoundingBox(), dz);
            }
            setBoundingBox(getBoundingBox().offset(0, 0, dz));
        } else {
            setBoundingBox(getBoundingBox().offset(dx, dy, dz));
        }

        // update particle position to match bounding box
        resetPositionToBB();

        // if the particle is not already on the ground,
        // compute a quad for the axis it landed on and
        // offset the position against the normal direction to minimize Z-fighting
        if (canDecal && !onGround) {
            if (origX != dx || origY != dy || origZ != dz) {
                BlockPos pos = new BlockPos(posX, posY, posZ);
                float quadOffset = ForgeConfigHandler.client.decalSurfaceOffsetMultiplier * (1.0f + rand.nextFloat());
                if (dx != origX) {
                    facing = (origX > 0 ? EnumFacing.WEST : EnumFacing.EAST);
                    hitNormal = new Vec3d(-Math.signum(origX), 0.0, 0.0);
                    posX = pos.getX() + (origX > 0 ? 1 : 0);
                    quadOffset = (float)(quadOffset*Math.signum(origX) - 0.75*SMALL_AMOUNT);
                } else if (dz != origZ) {
                    facing = (origZ > 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
                    hitNormal = new Vec3d(0.0, 0.0, -Math.signum(origZ));
                    posZ = pos.getZ() + (origZ > 0 ? 1 : 0);
                    quadOffset = (float) (quadOffset * Math.signum(origZ) - 0.75*SMALL_AMOUNT);
                } else if (origY != dy) {
                    facing = (origY > 0 ? EnumFacing.DOWN : EnumFacing.UP);
                    hitNormal = new Vec3d(0.0, -Math.signum(origY), 0.0);
                    posY = pos.getY() + (origY > 0 ? 1 : 0);
                    quadOffset = -(float)(quadOffset*Math.signum(origY));
                } else {
                    return;
                }
                // spray particles don't make decals
                if (displayType == ParticleDisplayType.SPRAY) {
                    setExpired();
                } else {
                    // particle just hit the ground, fix it in position,
                    // generate decal quad that is separate from the bounding box
                    finalQuad = CommonHelper.GetAxisAlignedQuad(facing, width * ForgeConfigHandler.client.decalScale);
                    hitOffset = hitNormal.scale(quadOffset);

                    onGround = true;
                    oldDisplayType = displayType;
                    setDisplayType(ParticleDisplayType.DECAL);
                }
                // zero the particle velocity
                motionX = motionY = motionZ = 0.0;
            }
        }
    }}
