package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.helpers.ClientHelper;
import com.beckadam.ballisticblood.helpers.CommonHelper;
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
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;


@SideOnly(Side.CLIENT)
public class SplatterParticleBase extends Particle {
    public ResourceLocation splatterParticleTexture;

    protected static final float particleTextureWidth = 8.0f;
    protected static final float particleTextureHeight = 8.0f;

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
    protected ArrayList<SplatterParticle> subParticles;
    protected int subParticleCount = 0;
    protected ParticleSubType subType, oldSubType;
    protected int ticksSinceLastEmission = 0;
    protected float emissionRate;
    protected int particleType;
    protected float particleSubVelocity;
    protected float colorMultiplier = 1.0f;
    protected float alphaMultiplier = 1.0f;

    protected static EntityPlayerSP player;
    protected static float ipx, ipy, ipz;

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
        this.fadeStart = Math.min(ForgeConfigHandler.client.particleFadeStart, this.particleMaxAge + 1);
        finalUVOffsets = new Vec2f[] { Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO };
        subParticles = new ArrayList<>();
        subType = ParticleSubType.BASE;
        splatterParticleTexture = null;
        player = Minecraft.getMinecraft().player;
        this.emissionRate = 0;
    }

    public void setLifetime(int lifetime, int fadeStart) {
        this.particleMaxAge = lifetime;
        this.fadeStart = Math.min(fadeStart, this.particleMaxAge + 1);
    }

    public void addSubparticle(SplatterParticle particle) {
        subParticles.add(particle);
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

    public void setParticleSubType(ParticleSubType type) {
        subType = type;
    }
    public void randomizeParticleTexture() {
        switch (subType) {
            case PROJECTILE:
            case DECAL:
                this.particleTextureIndexY = rand.nextInt(5); // rows 0, 1, 2, 3, 4
                break;
            case SPRAY:
                this.particleTextureIndexY = rand.nextInt(3) + 5; // rows 5, 6, 7
                break;
            case IMPACT:
            case BASE:
            default:
                break;
        }
    }

    public void setEmissionRate(float rate) {
        this.emissionRate = rate;
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
            if (this.particleAge >= this.fadeStart) {
                alpha -= ((float)(this.particleAge - this.fadeStart) / (float)(this.particleMaxAge - this.fadeStart));
            }
            ipx = (float)(player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
            ipy = (float)(player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
            ipz = (float)(player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

            GlStateManager.glBlendEquation(blendOp);
            GlStateManager.blendFunc(blendSourceFactor, blendDestFactor);
            GlStateManager.enableBlend();
            GlStateManager.disableNormalize();
            if (lightingEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(splatterParticleTexture);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            for (SplatterParticle sub : this.subParticles) {
                // set the alpha for the sub-particle
                sub.setAlphaF(alpha);
                // render sub-particle and its sub-particles
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            Tessellator.getInstance().draw();
            GlStateManager.glBlendEquation(32774); // "add" blend function
            GlStateManager.enableNormalize();
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
        this.subParticles.removeIf(p -> !p.isAlive());
        spawnSubParticles();
    }

    protected void spawnSubParticles() {
        if (subParticleCount >= ForgeConfigHandler.client.subParticleTotal) {
            return;
        }
        ticksSinceLastEmission++;
        if (emissionRate > 0 && ticksSinceLastEmission >= (20.0f / emissionRate)) {
            SplatterParticle particle = ClientHelper.makeParticle(
                    particleType, world, getPositionVector(),
                    getDirectionVector().add(
                            CommonHelper.GetRandomNormalizedVector()
                                    .scale(particleSubVelocity*ForgeConfigHandler.client.sprayParticleVelocity)
                    )
            );
            if (particle != null) {
                subParticleCount++;
                particle.setScale(ForgeConfigHandler.client.sprayParticleSize);
                particle.setGravity(ForgeConfigHandler.client.sprayParticleGravity);
                particle.setParticleSubType(ParticleSubType.SPRAY);
                particle.setLifetime(ForgeConfigHandler.client.sprayParticleLifetime, ForgeConfigHandler.client.sprayParticleFadeStart);
                particle.randomizeParticleTexture();
                addSubparticle(particle);
            }
        }
    }
}
