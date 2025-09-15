package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.ClientHelper;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SplatterImpactParticle extends SplatterParticleBase {
    protected List<SplatterSprayParticle> sprayParticles;
    protected List<SplatterProjectileParticle> projectileParticles;
    protected int subParticleCount = 0;

    public SplatterImpactParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        sprayParticles = new ArrayList<>();
        projectileParticles = new ArrayList<>();
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        // check whether particles are disabled; destroy if so
        if (!ForgeConfigHandler.client.enableSplatterParticles) {
            this.setExpired();
        }
        if (!(this.sprayParticles.isEmpty() && this.projectileParticles.isEmpty())) {
            ipx = (float)(player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
            ipy = (float)(player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
            ipz = (float)(player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

            GlStateManager.blendFunc(blendSourceFactor, blendDestFactor);
            GlStateManager.glBlendEquation(blendOp);
            if (lightingEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(splatterParticleTexture);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            // render the spray particles
            for (SplatterSprayParticle sub : this.sprayParticles) {
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            // render the projectile particles
            for (SplatterProjectileParticle sub : this.projectileParticles) {
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            Tessellator.getInstance().draw();
        }
    }


    public void addParticle(SplatterSprayParticle particle) {
        sprayParticles.add(particle);
    }

    public void addParticle(SplatterProjectileParticle particle) {
        projectileParticles.add(particle);
    }

    @Override
    public void prune() {
        sprayParticles.removeIf(particle -> !particle.isAlive());
    }


    @Override
    public void onUpdate() {
        for (SplatterSprayParticle sub : this.sprayParticles) {
            sub.onUpdate();
        }
        for (SplatterProjectileParticle sub : this.projectileParticles) {
            sub.onUpdate();
        }
        spawnSubParticles();
    }

    protected void spawnSubParticles() {
        if (subParticleCount >= ForgeConfigHandler.client.subParticleTotal) {
            return;
        }
        ticksSinceLastEmission++;
        if (emissionRate > 0 && ticksSinceLastEmission >= (20.0f / emissionRate)) {
            SplatterSprayParticle particle = ClientHelper.makeSprayParticle(
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
                particle.setParticleSubType(ParticleDisplayType.SPRAY);
                particle.setLifetime(ForgeConfigHandler.client.sprayParticleLifetime, ForgeConfigHandler.client.sprayParticleFadeStart);
                particle.randomizeParticleTexture();
                addParticle(particle);
            }
        }
    }

}
