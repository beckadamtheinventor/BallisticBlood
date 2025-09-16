package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.ClientHelper;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.Iterator;
import java.util.Queue;

public class SplatterParticleMain extends SplatterParticleBase {
    protected Queue<SplatterParticleSpray> sprayParticles = Queues.newArrayDeque();;
    protected Queue<SplatterParticleProjectile> projectileParticles = Queues.newArrayDeque();;
//    protected int subParticleCount = 0;

    public SplatterParticleMain(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (!(this.sprayParticles.isEmpty() && this.projectileParticles.isEmpty())) {
            ipx = (float)(entityIn.prevPosX + (entityIn.posX - entityIn.prevPosX) * partialTicks);
            ipy = (float)(entityIn.prevPosY + (entityIn.posY - entityIn.prevPosY) * partialTicks);
            ipz = (float)(entityIn.prevPosZ + (entityIn.posZ - entityIn.prevPosZ) * partialTicks);

            GlStateManager.disableNormalize();
            GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE);
            GlStateManager.enableColorMaterial();
            GlStateManager.glBlendEquation(blendOp);
            GlStateManager.blendFunc(blendSourceFactor, blendDestFactor);
            GlStateManager.enableBlend();
            if (lightingEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(splatterParticleTexture);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            // render the spray particles
            for (SplatterParticleSpray sub : this.sprayParticles) {
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            // render the projectile particles
            for (SplatterParticleProjectile sub : this.projectileParticles) {
                sub.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            Tessellator.getInstance().draw();
            GlStateManager.disableBlend();
//            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
            GlStateManager.enableNormalize();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
        }
    }

    public void addParticle(SplatterParticleSpray particle) {
        if (particle != null) {
            sprayParticles.add(particle);
        }
    }

    public void addParticle(SplatterParticleProjectile particle) {
        if (particle != null) {
            projectileParticles.add(particle);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!sprayParticles.isEmpty()) {
            Iterator<SplatterParticleSpray> iterator = sprayParticles.iterator();
            while (iterator.hasNext()) {
                SplatterParticleSpray particle = iterator.next();
                particle.onUpdate();
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }
        if (!projectileParticles.isEmpty()) {
            Iterator<SplatterParticleProjectile> iterator = projectileParticles.iterator();
            while (iterator.hasNext()) {
                SplatterParticleProjectile particle = iterator.next();
                particle.onUpdate();
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }
    }

}
