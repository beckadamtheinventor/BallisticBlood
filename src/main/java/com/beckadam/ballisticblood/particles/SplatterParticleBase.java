package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.helpers.CommonHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


@SideOnly(Side.CLIENT)
public class SplatterParticleBase extends Particle {
    public ResourceLocation splatterParticleTexture;

    protected static final int[] hFlipVertexIndex = new int[] { 1, 0, 3, 2 };
    protected static final int[] vFlipVertexIndex = new int[] { 2, 3, 0, 1 };
    protected static final float particleTextureWidth = 8.0f;
    protected static final float particleTextureHeight = 8.0f;
    protected static final double SMALL_AMOUNT = 1.0f / 16.0f;
    protected static final double TINY_AMOUNT = 1.0f / 64.0f;
    protected static float ipx, ipy, ipz;

    protected GlStateManager.SourceFactor blendSourceFactor = GlStateManager.SourceFactor.SRC_ALPHA;
    protected GlStateManager.DestFactor blendDestFactor = GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
    protected int blendOp = 32774; // "add" blend mode
    protected boolean lightingEnabled = true;
    protected int fadeStart;
    protected float decalScale;

    protected Vec3d hitNormal;
    protected Vec3d hitOffset;
    protected Vec3d[] finalQuad;
    protected Vec2f[] finalUVOffsets;
    protected EnumFacing facing;
    protected ParticleDisplayType displayType, oldDisplayType;
    protected int particleType;
    protected float colorMultiplier = 1.0f;
    protected float alphaMultiplier = 1.0f;
    protected boolean hFlip, vFlip, rotate;

    public SplatterParticleBase(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z);
        setParticleTextureIndex(rand.nextInt((int)particleTextureWidth));
        this.width = ForgeConfigHandler.client.particleSize;
        this.height = 0.1f;
        this.particleScale = 1.0f;
        this.particleGravity = 1.0f;
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;
        this.decalScale = ForgeConfigHandler.client.decalScale;
        this.particleMaxAge = ForgeConfigHandler.client.particleLifetime;
        this.fadeStart = this.particleMaxAge;
        finalUVOffsets = new Vec2f[] { Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO };
        displayType = ParticleDisplayType.BASE;
        splatterParticleTexture = null;
        hFlip = vFlip = rotate = false;
    }

    public void setLifetime(int lifetime, int fadeStart) {
        this.particleMaxAge = lifetime;
        this.fadeStart = Math.min(fadeStart, lifetime);
    }

    public void setFlip(boolean h, boolean v, boolean r) {
        hFlip = h;
        vFlip = v;
        rotate = r;
    }

    public void setTexture(ResourceLocation tex) {
        splatterParticleTexture = tex;
    }

    public void setGravity(float g) {
        particleGravity = g * ForgeConfigHandler.client.particleGravityBase / 20.0f;
    }

    public void setScale(float s) { particleScale = s; }

    public void setMultipliers(float color, float alpha) {
        colorMultiplier = color;
        alphaMultiplier = alpha;
    }

    public void setBlendFactors(GlStateManager.SourceFactor source, GlStateManager.DestFactor dest, int op, boolean light) {
        blendSourceFactor = source;
        blendDestFactor = dest;
        lightingEnabled = light;
        blendOp = op;
    }

    public void setType(int type) {
        this.particleType = type;
    }

    public void setDisplayType(ParticleDisplayType type) {
        displayType = type;
    }
    public void randomizeParticleTexture() {
        switch (displayType) {
            case DECAL:
                this.particleTextureIndexY = rand.nextInt(5); // rows 0, 1, 2, 3, 4
                break;
            case PROJECTILE:
                this.particleTextureIndexY = rand.nextInt(2) + 5; // rows 5 and 6
                break;
            case SPRAY:
                this.particleTextureIndexY = 7;
                break;
            case IMPACT:
            case BASE:
            default:
                break;
        }
        this.particleTextureIndexX = rand.nextInt((int)particleTextureWidth);
    }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) {
        this.particleTextureIndexX = particleTextureIndex % (int)particleTextureWidth;
    }

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
        if (playerEntity.getDistanceSq(this.posX, this.posY, this.posZ) >= ForgeConfigHandler.client.particleRenderDistanceCubed) {
            return;
        }
//        // TODO: don't render particle if it's behind the player
//        double ang = Math.atan2(playerEntity.posX - this.posX, playerEntity.posZ - this.posZ);
//        if (ang >= Math.PI*0.5 && ang < Math.PI*1.5) {
//            return;
//        }
        float alpha;
        if (this.particleAge >= this.fadeStart) {
            alpha = (1.0f - ((float)(this.particleAge - this.fadeStart) / (float)(this.particleMaxAge - this.fadeStart)))
                    * this.particleAlpha * alphaMultiplier;;
        } else {
            alpha = this.particleAlpha * alphaMultiplier;
        }

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
        int j = 0;
        if (this.hFlip) {
//            float t = u0;
//            u0 = u1;
//            u1 = t;
            j ^= 1;
        }
        if (this.vFlip) {
//            float t = v0;
//            v0 = v1;
//            v1 = t;
            j ^= 2;
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
                .tex(finalUVOffsets[j].x+u1, finalUVOffsets[j].y+v1)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alpha)
                .lightmap(lx, ly)
                .endVertex();
        buffer.pos(px + quad[1].x, py + quad[1].y, pz + quad[1].z)
                .tex(finalUVOffsets[j^1].x+u1, finalUVOffsets[j^1].y+v0)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alpha)
                .lightmap(lx, ly)
                .endVertex();
        buffer.pos(px + quad[2].x, py + quad[2].y, pz + quad[2].z)
                .tex(finalUVOffsets[j^2].x+u0, finalUVOffsets[j^2].y+v0)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alpha)
                .lightmap(lx, ly)
                .endVertex();
        buffer.pos(px + quad[3].x, py + quad[3].y, pz + quad[3].z)
                .tex(finalUVOffsets[j^3].x+u0, finalUVOffsets[j^3].y+v1)
                .color(colorMultiplier, colorMultiplier, colorMultiplier, alpha)
                .lightmap(lx, ly)
                .endVertex();
//        GL11.glPopMatrix();
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
            this.setExpired();
            return;
        }
        // check whether this particle has expired; destroy if so
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }
        if (this.posY <= -128.0) {
            this.setExpired();
            return;
        }
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;
        if (this.canCollide) {
            // recompute vertex overhang if necessary
            this.computeVertexOverhang();
            if (this.onGround && this.checkIsHovering()) {
                this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, this.width * this.decalScale);
                this.setPositionVector(getQuadFaceMiddle(this.getPositionVector(), hitNormal));
                this.finalUVOffsets = new Vec2f[] {Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO};
                if (this.checkIsHovering()) {
                    this.setExpired();
                }
                return;
            }
        }
        if (!onGround) {
            // update velocity
            this.motionY -= this.particleGravity;
        }
        // try to move the particle first
        this.move(this.motionX, this.motionY, this.motionZ);
        // if the particle is currently a decal
        if (this.canCollide && this.onGround) {
            // check if covered
            if (this.checkIsCovered()) {
                // expire if covered
                this.setExpired();
            }
        }
    }

    public void prune() {}

    public boolean checkIsHovering() {
        if (this.finalQuad == null) {
            return false;
        }
        int hovering = 0;
        for (Vec3d vert : this.finalQuad) {
            BlockPos pos = new BlockPos(vert.add(this.getPositionVector()).subtract(this.hitNormal.scale(SMALL_AMOUNT)));
            IBlockState block = this.world.getBlockState(pos);
            if (block.getCollisionBoundingBox(this.world, pos) == null) {
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
        AxisAlignedBB boundingBox = this.getBoundingBox().offset(dir);
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

    private static Vec3d getCorrectOffsetToSnapTo(Vec3d v, Vec3d p, Vec3d n, AxisAlignedBB box) {
        Vec3d sx = snapOffsetX(v, p, box);
        Vec3d sy = snapOffsetY(v, p, box);
        Vec3d sz = snapOffsetZ(v, p, box);
        double dx = sx != null ? sx.lengthSquared() : 0;
        double dy = sy != null ? sy.lengthSquared() : 0;
        double dz = sz != null ? sz.lengthSquared() : 0;
        dx = dx == 0 ? 1e6 : dx;
        dy = dy == 0 ? 1e6 : dy;
        dz = dz == 0 ? 1e6 : dz;
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
        if (!this.onGround) {
            return;
        }
        if (!this.canCollide || this.finalQuad == null) {
            return;
        }
        if (!ForgeConfigHandler.client.enableExperimentalOverhangClipping) {
            return;
        }
        // only run this every 4 client ticks
        if ((this.particleAge & 3) != 0) {
            return;
        }

        double w = this.width*this.decalScale*0.5+2.5;
        List<AxisAlignedBB> worldCollisionBoxes =
                world.getCollisionBoxes(Minecraft.getMinecraft().player, new AxisAlignedBB(
                        this.getPositionVector().subtract(w,w,w),
                        this.getPositionVector().add(w,w,w)
                ));
        if (worldCollisionBoxes.isEmpty()) {
            this.setExpired();
            return;
        }
        // get a position that is in the middle of the quad, offset against the normal direction
        Vec3d mid = finalQuad[0].add(finalQuad[1]).add(finalQuad[2]).add(finalQuad[3]).scale(0.25)
                .add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT*2.0));
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
            // for each edge of the quad
            for (int i = 0; i < finalQuad.length; i++) {
                Vec3d v = finalQuad[i].add(finalQuad[(i+1)&3])
                        .scale(0.499)
                        .subtract(hitNormal.scale(SMALL_AMOUNT))
                        .add(getPositionVector());
                boolean anchored = false;
                for (AxisAlignedBB box : worldCollisionBoxes) {
                    if (box.contains(v)) {
                        anchored = true;
                        break;
                    }
                }
                if (!anchored) {
                    // get the absolute position to snap the first vertex to
                    Vec3d boxVec = getCorrectOffsetToSnapTo(finalQuad[i], getPositionVector(), hitNormal, anchorBox);
                    // offset both vertices to align with the bounding box
                    Vec3d temp = perAxisTernary(boxVec, finalQuad[i], hitNormal);
                    finalQuad[(i+1)&3] = finalQuad[(i+1)&3].add(temp).subtract(finalQuad[i]);
                    finalQuad[i] = temp;
                }
            }
            // for each vertex of the quad box
            for (int i = 0; i < finalQuad.length; i++) {
                // get a position just barely inside the decal's bounding box
                // that is next to the corner
                Vec3d v = finalQuad[i].subtract(getQuadVertexOffset(i, hitNormal)).add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT));
                //            double dx=v.x, dy=v.y, dz=v.z;
                boolean anchored = false;
                for (AxisAlignedBB box : worldCollisionBoxes) {
                    if (box.contains(v)) {
                        anchored = true;
                        break;
                    }
                }
                if (!anchored) {
                    // get the absolute position to snap the vertex to
                    Vec3d boxVec = getCorrectOffsetToSnapTo(finalQuad[i], getPositionVector(), hitNormal, anchorBox);
                    // offset the vertex to align it with the bounding box
                    finalQuad[i] = perAxisTernary(boxVec, finalQuad[i], hitNormal);
                    // check if vertex is now anchored after being moved
//                    v = finalQuad[i].scale(0.99).add(getPositionVector()).subtract(hitNormal.scale(SMALL_AMOUNT));
//                    for (AxisAlignedBB box : worldCollisionBoxes) {
//                        if (box.contains(v)) {
//                            anchored = true;
//                        }
//                    }
//                    if (!anchored) {
//                        this.setExpired();;
//                    }
                }
            }
        }

        if (finalUVOffsets == null) {
            finalUVOffsets = new Vec2f[4];
        }
        Vec3d[] orig = CommonHelper.GetAxisAlignedQuad(facing, this.width * this.decalScale);
        for (int i = 0; i < finalUVOffsets.length; i++) {
            Vec3d ud = finalQuad[i].subtract(orig[i]).scale(1.0f / (this.width * this.decalScale * particleTextureWidth));
            int j = i;
            if (hFlip) { j = hFlipVertexIndex[j]; }
            if (vFlip) { j = vFlipVertexIndex[j]; }
            if (hitNormal.x == 0) {
                if (hitNormal.y == 0) {
                    finalUVOffsets[j] = new Vec2f((float)ud.x, (float)ud.y);
                } else if (hitNormal.z == 0) {
                    finalUVOffsets[j] = new Vec2f((float)ud.x, (float)ud.z);
                }
            } else if (hitNormal.y == 0) {
                finalUVOffsets[j] = new Vec2f((float)ud.z, (float)ud.y);
            }
        }
    }

    public void computeFacing(double dx, double dy, double dz, double origX, double origY, double origZ) {
        double ddx = Math.abs(posX - prevPosX);
        double ddy = Math.abs(posY - prevPosY);
        double ddz = Math.abs(posZ - prevPosZ);
        if (dz != origZ && ddz < ddx && ddz < ddy) {
            this.hitNormal = new Vec3d(0.0, 0.0, -Math.signum(origZ));
            this.facing = (origZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
        } else if (dx != origX && ddx < ddz) {
            this.hitNormal = new Vec3d(-Math.signum(origX), 0.0, 0.0);
            this.facing = (origX < 0 ? EnumFacing.WEST : EnumFacing.EAST);
        } else if (dy != origY) {
            this.hitNormal = new Vec3d(0.0, -Math.signum(origY), 0.0);
            this.facing = (origY < 0 ? EnumFacing.DOWN : EnumFacing.UP);
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        double origX = dx;
        double origY = dy;
        double origZ = dz;
        if (this.canCollide && world != null) {
            // compute new bounding box based on the existing bounding box and the world
            List<AxisAlignedBB> worldCollisionBoxes = world.getCollisionBoxes(Minecraft.getMinecraft().player, this.getBoundingBox().expand(dx, dy, dz));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dy = axisalignedbb.calculateYOffset(this.getBoundingBox(), dy);
            }
            this.setBoundingBox(this.getBoundingBox().offset(0, dy, 0));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dx = axisalignedbb.calculateXOffset(this.getBoundingBox(), dx);
            }
            this.setBoundingBox(this.getBoundingBox().offset(dx, 0, 0));
            for(AxisAlignedBB axisalignedbb : worldCollisionBoxes) {
                dz = axisalignedbb.calculateZOffset(this.getBoundingBox(), dz);
            }
            this.setBoundingBox(this.getBoundingBox().offset(0, 0, dz));
        } else {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        }

        // update particle position to match bounding box
        this.resetPositionToBB();

        // if the particle ran into something
        if (origX != dx || origY != dy || origZ != dz) {
            // if the particle is not already on the ground,
            // compute a quad for the axis it landed on and
            // offset the position against the normal direction to minimize Z-fighting
            if (!onGround) {
                // figure out which face the particle landed on
                computeFacing(dx, dy, dz, origX, origY, origZ);
                BlockPos pos = new BlockPos(posX, posY, posZ);
                float quadOffset = -ForgeConfigHandler.client.decalSurfaceOffsetMultiplier * (1.5f * rand.nextFloat() + 0.5f);
                if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                    this.posY = pos.getY() + (origY < 0 ? 0 : 1);
                    quadOffset = (float)(quadOffset*Math.signum(origY));
                } else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                    this.posX = pos.getX() + (origX < 0 ? 0 : 1);
                    quadOffset = (float)(quadOffset*Math.signum(origX) - 1.333f * SMALL_AMOUNT);
                } else if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                    this.posZ = pos.getZ() + (origZ < 0 ? 0 : 1);
                    quadOffset = (float)(quadOffset*Math.signum(origZ) - 1.333f * SMALL_AMOUNT);
                } else {
                    return;
                }
                // spray particles don't make decals
                if (this.displayType == ParticleDisplayType.SPRAY) {
                    this.setExpired();
                } else {
                    // particle just hit the ground, fix it in position,
                    // generate decal quad that is separate from the bounding box
                    this.finalQuad = CommonHelper.GetAxisAlignedQuad(facing, this.width * this.decalScale);
                    this.hitOffset = this.hitNormal.scale(quadOffset);

                    this.onGround = true;
                    this.oldDisplayType = this.displayType;
                    setDisplayType(ParticleDisplayType.DECAL);
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
    }}
