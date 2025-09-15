package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ParticleManager extends Particle {
    // note: the synchronized keyword seems to be necessary despite Collections.synchronizedList being threadsafe...
    // (ConcurrentModification errors result otherwise)
    public static ParticleManager instance = null;

    protected final List<SplatterParticleBase> particles = Collections.synchronizedList(new ArrayList<>());
    protected int dimensionId;

    public static void register(EventBus bus) {
        bus.register(ParticleManager.class);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        MakeParticleManager(event.player.world);
    }

    public static void MakeParticleManager(World world) {
        if (instance != null) {
            instance.clear();
        }
        instance = new ParticleManager(world, 0.0, 0.0, 0.0);
        Minecraft.getMinecraft().effectRenderer.addEffect(instance);
    }

    protected ParticleManager(World world, double x, double y, double z) {
        super(world, x, y, z);
        dimensionId = world.provider.getDimension();
    }

    public synchronized void add(SplatterParticleBase particle) {
//        BallisticBloodMod.LOGGER.log(Level.INFO, "add");
        if (particles.size() >= ForgeConfigHandler.client.maximumProjectileParticles) {
//            BallisticBloodMod.LOGGER.log(Level.INFO, "expiring 25 projectiles");
            for (int i=0; i<25; i++) {
                particles.get(i).setExpired();
            }
        }
        particles.add(particle);
    }

    public synchronized void clear() {
        for (SplatterParticleBase particle : particles) {
            particle.setExpired();
        }
        particles.clear();
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity playerEntity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        // don't render particles for this particle manager if it's not in the same dimension
        if (Minecraft.getMinecraft().world.provider.getDimension() != dimensionId) {
            return;
        }
        synchronized (particles) {
            GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE);
            GlStateManager.enableColorMaterial();
            GlStateManager.disableNormalize();
            GlStateManager.enableBlend();
            for (SplatterParticleBase particle : particles) {
                particle.renderParticle(buffer, playerEntity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.glBlendEquation(32774); // "add" blend function
            GlStateManager.enableNormalize();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void onUpdate() {
        // don't update particles for this particle manager if it's not in the same dimension
        if (Minecraft.getMinecraft().world.provider.getDimension() != dimensionId) {
            return;
        }
//        BallisticBloodMod.LOGGER.log(Level.INFO, "onUpdate");
        synchronized (particles) {
            for (SplatterParticleBase particle : particles) {
                particle.onUpdate();
            }
            particles.removeIf(particle -> !particle.isAlive());
        }
    }
}
