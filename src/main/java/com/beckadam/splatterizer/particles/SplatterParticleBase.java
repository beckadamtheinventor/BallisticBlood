package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.helpers.ClientHelper;
import com.beckadam.splatterizer.helpers.CommonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;


@SideOnly(Side.CLIENT)
public class SplatterParticleBase extends Particle {
    public ResourceLocation splatterParticleTexture;

    protected static final float particleTextureWidth = 8.0f;
    protected static final float particleTextureHeight = 8.0f;

    protected GlStateManager.SourceFactor blendSourceFactor = GlStateManager.SourceFactor.SRC_ALPHA;
    protected GlStateManager.DestFactor blendDestFactor = GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
    protected int blendOp = 32774; // "add" blend mode
    protected boolean lightingEnabled = true;
    protected static int fadeStart;
    protected float decalScale;

    protected List<AxisAlignedBB> worldCollisionBoxes;
    protected Vec3d hitNormal;
    protected Vec3d[] finalQuad;
    protected Vec2f[] finalUVs;
    protected EnumFacing facing;
    protected ArrayList<SplatterParticle> subParticles;
    protected int subParticleCount = 0;
    protected ParticleSubType subType, oldSubType;
    protected int impactEmissionRate;
    protected int particleType;
    protected float particleSubVelocity;

    protected static EntityPlayerSP player;
    protected static float ipx, ipy, ipz;

    //    protected Vec3d facing;

    public SplatterParticleBase(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z);
        setParticleTextureIndex(rand.nextInt((int)particleTextureWidth));
        this.width = ForgeConfigHandler.client.particleSize;
        this.height = 0.1f;
        this.particleScale = 1.0f;
        this.particleGravity = 1.0f;
        this.particleMaxAge = ForgeConfigHandler.client.particleLifetime;
        this.motionX = vx * ForgeConfigHandler.client.primaryParticleVelocityMultiplier;
        this.motionY = vy * ForgeConfigHandler.client.primaryParticleVelocityMultiplier;
        this.motionZ = vz * ForgeConfigHandler.client.primaryParticleVelocityMultiplier;
        this.decalScale = ForgeConfigHandler.client.decalScale;
        fadeStart = Math.min(ForgeConfigHandler.client.particleFadeStart, this.particleMaxAge + 1);
        finalUVs = new Vec2f[] { Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO };
        subParticles = new ArrayList<>();
        subType = ParticleSubType.BASE;
        splatterParticleTexture = null;
        player = Minecraft.getMinecraft().player;
        this.impactEmissionRate = 0;
    }

    public void addSubparticle(SplatterParticle particle) {
        subParticles.add(particle);
    }

    public void setTexture(ResourceLocation tex) {
        splatterParticleTexture = tex;
    }

    public void setGravity(float g) {
        particleGravity = g;
    }

    public void setScale(float s) { particleScale = s; }

    public void setBlendFactors(GlStateManager.SourceFactor source, GlStateManager.DestFactor dest, int op, boolean light) {
        blendSourceFactor = source;
        blendDestFactor = dest;
        lightingEnabled = light;
        blendOp = op;
    }

    public void setType(int type) {
        this.particleType = type;
    }

    public void setParticleSubType(ParticleSubType type) {
        subType = type;
        switch (type) {
            case DECAL:
                this.particleTextureIndexY = rand.nextInt(5); // rows 0, 1, 2, 3, 4
                break;
            case SPRAY:
                this.particleTextureIndexY = rand.nextInt(3) + 4; // rows 5, 6, 7
                break;
            case PROJECTILE:
            case IMPACT:
            case BASE:
            default:
                break;
        }
    }

    public void setEmissionRates(int impactEmissionRate) {
        this.impactEmissionRate = impactEmissionRate;
    }

    public void setEmissionVelocity(float particleSubVelocity) {
        this.particleSubVelocity = particleSubVelocity;
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

    // The Base splatter particle renders all the particles for each splatter
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (!this.subParticles.isEmpty()) {
            float alpha = 1.0f;
            if (this.particleAge >= fadeStart) {
                alpha -= ((float)(this.particleAge - fadeStart) / (float)(this.particleMaxAge - fadeStart));
            }
            ipx = (float)(player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
            ipy = (float)(player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
            ipz = (float)(player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

            GlStateManager.blendFunc(blendSourceFactor, blendDestFactor);
            if (lightingEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(splatterParticleTexture);

            boolean startedDrawing = false;
            for (ParticleSubType type : ParticleSubType.values()) {
                if (type == ParticleSubType.BASE) {
                    continue;
                }
                for (SplatterParticle sub : this.subParticles) {
                    // the alpha of each sub-particle only needs to be set once
                    if (type.ordinal() == 1) {
                        sub.setAlphaF(alpha);
                    }
                    // render particles for each texture type
                    if (type == sub.subType) {
                        if (!startedDrawing) {
                            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            startedDrawing = true;
                        }
                        sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
                    }
                }
            }
            if (startedDrawing) {
                Tessellator.getInstance().draw();
            }

        }

    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        for (SplatterParticle sub : this.subParticles) {
            sub.onUpdate();
        }
        this.subParticles.removeIf(splatterSubParticle -> !splatterSubParticle.isAlive());
        spawnSubParticles(ForgeConfigHandler.client.particleSpreadMax);
    }

    protected void spawnSubParticles(int maximum) {
        if (this.subParticles.size() >= maximum) {
            return;
        }
        if (subParticleCount > ForgeConfigHandler.client.subParticleTotal) {
            return;
        }
        subParticleCount++;
        if (impactEmissionRate > 0 && particleAge % impactEmissionRate == 0) {
            SplatterParticle particle = ClientHelper.makeParticle(
                    particleType, world, getPositionVector(),
                    getDirectionVector().add(
                            CommonHelper.GetRandomNormalizedVector()
                                    .scale(particleSubVelocity*ForgeConfigHandler.client.sprayParticleVelocity)
                    )
            );
            if (particle != null) {
                particle.setScale(ForgeConfigHandler.client.sprayParticleSize);
                particle.setGravity(ForgeConfigHandler.client.sprayParticleGravity);
                particle.setParticleSubType(ParticleSubType.SPRAY);
                addSubparticle(particle);
            }
        }
    }
}
