package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.helpers.ParticleHelper;
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

import java.util.ArrayList;
import java.util.List;


public class SplatterParticleBase extends Particle {
    protected ResourceLocation splatterParticleTexture;
    protected GlStateManager.SourceFactor blendSourceFactor = GlStateManager.SourceFactor.SRC_ALPHA;
    protected GlStateManager.DestFactor blendDestFactor = GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
    protected static int fadeStart;
    protected static final float particleTextureWidth = 4.0f;
    protected static final float particleTextureHeight = 4.0f;

    protected List<AxisAlignedBB> worldCollisionBoxes;
    protected Vec3d hitNormal;
    protected Vec3d[] finalQuad;
    protected Vec2f[] finalUVs;
    protected EnumFacing facing;
    protected ArrayList<SplatterParticle> subParticles;
    protected ParticleSubType subType;
    protected int impactEmissionRate;
    protected int projectileEmissionRate;
    protected int decalEmissionRate;
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
        this.particleScale = 2.0f;
        this.particleGravity = 1.0f;
        this.particleMaxAge = ForgeConfigHandler.client.particleLifetime;
        this.motionX = vx * ForgeConfigHandler.client.particleVelocityMultiplier;
        this.motionY = vy * ForgeConfigHandler.client.particleVelocityMultiplier;
        this.motionZ = vz * ForgeConfigHandler.client.particleVelocityMultiplier;
        fadeStart = Math.min(ForgeConfigHandler.client.particleFadeStart, this.particleMaxAge + 1);
        finalUVs = new Vec2f[] { Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO };
        subParticles = new ArrayList<>();
        subType = ParticleSubType.BASE;
        splatterParticleTexture = null;
        player = Minecraft.getMinecraft().player;
        this.impactEmissionRate = this.projectileEmissionRate = this.decalEmissionRate = 0;
    }

    public void addSubparticle(SplatterParticle particle) {
        if (subParticles.size() < ForgeConfigHandler.client.particleSubMax) {
            subParticles.add(particle);
        }
    }

    public void setTexture(ResourceLocation tex) {
        splatterParticleTexture = tex;
    }

    public void setGravity(float g) {
        particleGravity = g;
    }

    public void setBlendFactors(GlStateManager.SourceFactor source, GlStateManager.DestFactor dest) {
        blendSourceFactor = source;
        blendDestFactor = dest;
    }

    public void setType(int type) {
        this.particleType = type;
    }

    public void setParticleSubType(ParticleSubType type) {
        subType = type;
        this.particleTextureIndexY = type.ordinal() - 1;
    }

    public void setEmissionRates(int impactEmissionRate, int projectileEmissionRate, int decalEmissionRate) {
        this.impactEmissionRate = impactEmissionRate;
        this.projectileEmissionRate = projectileEmissionRate;
        this.decalEmissionRate = decalEmissionRate;
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
                alpha -= ((float)(this.particleAge - fadeStart) / (this.particleMaxAge - fadeStart));
            }
            ipx = (float)(player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
            ipy = (float)(player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
            ipz = (float)(player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

            GlStateManager.enableBlend();
            GlStateManager.enableNormalize();
            GlStateManager.blendFunc(blendSourceFactor, blendDestFactor);

            for (ParticleSubType type : ParticleSubType.values()) {
                if (type == ParticleSubType.BASE) {
                    continue;
                }
                Minecraft.getMinecraft().getTextureManager().bindTexture(splatterParticleTexture);
                boolean startedDrawing = false;
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
                if (startedDrawing) {
                    Tessellator.getInstance().draw();
                }
            }

            GlStateManager.disableBlend();
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
        if (impactEmissionRate > 0 && particleAge % impactEmissionRate == 0) {
            addSubparticle(SplatterParticleFactory.makeParticle(
                    particleType, world, getPositionVector(),
                    getDirectionVector().add(
                            ParticleHelper.GetRandomNormalizedVector()
                                    .scale(particleSubVelocity)
                    )
            ));
        }
        if (projectileEmissionRate > 0 || decalEmissionRate > 0) {
            ArrayList<SplatterParticle> newParticles = new ArrayList<>();
            for (SplatterParticle sub : subParticles) {
                if (sub.onGround) {
                    if (decalEmissionRate > 0 && particleAge % decalEmissionRate == 0) {
                        newParticles.add(SplatterParticleFactory.makeParticle(
                                particleType, world, sub.getPositionVector(),
                                sub.getDirectionVector().add(
                                        ParticleHelper.GetRandomNormalizedVector()
                                                .scale(particleSubVelocity)
                                )
                        ));
                    }
                } else if (sub.subType == ParticleSubType.PROJECTILE) {
                    if (projectileEmissionRate > 0 && particleAge % projectileEmissionRate == 0) {
                        newParticles.add(SplatterParticleFactory.makeParticle(
                                particleType, world, sub.getPositionVector(),
                                sub.getDirectionVector().add(
                                        ParticleHelper.GetRandomNormalizedVector()
                                                .scale(particleSubVelocity)
                                )
                        ));
                    }
                }
            }
            for (SplatterParticle particle : newParticles) {
                addSubparticle(particle);
            }
        }
    }
}
