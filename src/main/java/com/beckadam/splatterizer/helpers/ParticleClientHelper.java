package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleClientHelper {
    private static final Random random = new Random();

    public static void splatter(int type, Vec3d position, Vec3d direction, float damage) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            splatter(ForgeConfigHandler.particleConfigIntMap.get(type), position, direction, damage);
        } else {
            SplatterizerMod.LOGGER.log(Level.WARN, "Ignoring unknown splatter particle type " + type);
        }
    }

    public static void splatter(ForgeConfigHandler.ParticleConfig cfg, Vec3d position, Vec3d direction, float damage) {
        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        double spreadSize = ForgeConfigHandler.client.particleSpreadSize;
        double spreadVariance = ForgeConfigHandler.client.particleSpreadVariance;
        int count = Math.min(
                ForgeConfigHandler.client.particleSpreadMax,
                ParticleHelper.scaleCountByDamage(ForgeConfigHandler.client.particleSpreadCount, damage)
        );
        if (count <= 0) {
            return;
        }
        GlStateManager.SourceFactor srcFactor = BlendModeHelper.getSourceFactor(cfg.blendMode);
        GlStateManager.DestFactor destFactor = BlendModeHelper.getDestFactor(cfg.blendMode);
        boolean lightingEnabled = BlendModeHelper.getShouldLight(cfg.blendMode);
        SplatterParticleBase mainParticle = makeParticleBase(cfg.type, world, position, Vec3d.ZERO);
        if (mainParticle != null) {
            mainParticle.setParticleSubType(ParticleSubType.IMPACT);
            mainParticle.setBlendFactors(srcFactor, destFactor, lightingEnabled);
            mainParticle.setEmissionRates(
                    cfg.impactEmissionRate, cfg.projectileEmissionRate, cfg.decalEmissionRate
            );
            mainParticle.setEmissionVelocity(cfg.emissionVelocity);
            for (int index = 0; index < count; index++) {
                Vec3d dir = ParticleHelper.SpreadParticleVelocity(direction, index, count, spreadVariance, spreadSize);
                SplatterParticle part = makeParticle(cfg.type, world, position, dir);
                if (part != null) {
                    part.setParticleSubType(ParticleSubType.PROJECTILE);
                    part.setBlendFactors(srcFactor, destFactor, lightingEnabled);
                    mainParticle.addSubparticle(part);
                }
                Vec3d randDir = new Vec3d(
                        random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f
                ).normalize();
                SplatterParticle part2 = makeParticle(cfg.type, world, position, randDir);
                if (part2 != null) {
                    part2.setParticleSubType(ParticleSubType.SPRAY);
                    part2.setBlendFactors(srcFactor, destFactor, lightingEnabled);
                    mainParticle.addSubparticle(part2);
                }
            }
            Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);
        }
    }

    public static SplatterParticleBase makeParticleBase(int type, World world, Vec3d pos, Vec3d dir) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            ForgeConfigHandler.ParticleConfig cfg = ForgeConfigHandler.particleConfigIntMap.get(type);
            float v = cfg.velocity;
            SplatterParticleBase particle = new SplatterParticleBase(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
            particle.setType(type);
            particle.setTexture(cfg.texture);
            particle.setGravity(cfg.gravity);
            return particle;
        }
        return null;
    }
    public static SplatterParticle makeParticle(int type, World world, Vec3d pos, Vec3d dir) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            ForgeConfigHandler.ParticleConfig cfg = ForgeConfigHandler.particleConfigIntMap.get(type);
            float v = cfg.velocity;
            SplatterParticle particle = new SplatterParticle(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
            particle.setType(type);
//            particle.setTexture(cfg.texture);
            particle.setGravity(cfg.gravity);
            particle.setFlip(random.nextBoolean(), random.nextBoolean());
            return particle;
        }
        return null;
    }
}
