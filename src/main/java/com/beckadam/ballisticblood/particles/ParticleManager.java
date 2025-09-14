package com.beckadam.ballisticblood.particles;

import com.beckadam.ballisticblood.BallisticBloodMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SideOnly(Side.CLIENT)
public class ParticleManager extends Particle {
    public static ParticleManager instance = null;

    protected static List<SplatterParticleBase> particles = Collections.synchronizedList(new ArrayList<>());
    protected static Lock lock = new ReentrantLock();
    protected static boolean addedToFX = false;
    protected int dimensionId = 0;

    public static void register(EventBus bus) {
        bus.register(ParticleManager.class);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        MakeParticleManager(event.player.world);
    }

    public static void MakeParticleManager(World world) {
        try {
            if (lock.tryLock(10, TimeUnit.MICROSECONDS)) {
                if (instance != null) {
                    instance.clear();
                }
                instance = new ParticleManager(world, 0.0, 0.0, 0.0);
                Minecraft.getMinecraft().effectRenderer.addEffect(instance);
                lock.unlock();
            }
        } catch (InterruptedException ignored) {}
    }

    protected ParticleManager(World world, double x, double y, double z) {
        super(world, x, y, z);
        dimensionId = world.provider.getDimension();
    }

    public void add(SplatterParticleBase particle) {
        try {
            if (lock.tryLock(10, TimeUnit.MICROSECONDS)) {
                particles.add(particle);
                lock.unlock();
            }
        } catch (InterruptedException ignored) {}
    }

    public void clear() {
        try {
            if (lock.tryLock(10, TimeUnit.MICROSECONDS)) {
                for (SplatterParticleBase particle : particles) {
                    particle.setExpired();
                }
                particles.clear();
                lock.unlock();
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity playerEntity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        // don't render particles for this particle manager if it's not in the same dimension
        if (Minecraft.getMinecraft().world.provider.getDimension() != dimensionId) {
            return;
        }
        try {
            if (lock.tryLock(2, TimeUnit.MICROSECONDS)) {
                GlStateManager.pushAttrib();
//                GlStateManager.enableFog();
                GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.enableColorMaterial();
                GlStateManager.disableNormalize();
                GlStateManager.enableBlend();
                for (SplatterParticleBase particle : particles) {
                    particle.renderParticle(buffer, playerEntity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
                }
                GlStateManager.popAttrib();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.glBlendEquation(32774); // "add" blend function
                GlStateManager.enableNormalize();
                lock.unlock();
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        // don't update particles for this particle manager if it's not in the same dimension
        if (Minecraft.getMinecraft().world.provider.getDimension() != dimensionId) {
            return;
        }
        try {
            if (lock.tryLock(10, TimeUnit.MICROSECONDS)) {
                for (SplatterParticleBase particle : particles) {
                    particle.onUpdate();
                }
                lock.unlock();
            }
        } catch (InterruptedException ignored) {}
    }
}
