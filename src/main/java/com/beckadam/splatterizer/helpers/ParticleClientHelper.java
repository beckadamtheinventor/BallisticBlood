package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.*;
import com.beckadam.splatterizer.render.SplatterParticleRenderer;
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
                ParticleHelper.ScaleCountByDamage(ForgeConfigHandler.client.particleSpreadCount, damage)
        );
        if (count <= 0) {
            return;
        }
        GlStateManager.SourceFactor srcFactor = BlendModeHelper.getSourceFactor(cfg.blendMode[0]);
        GlStateManager.DestFactor destFactor = BlendModeHelper.getDestFactor(cfg.blendMode[1]);
        int blendOp = BlendModeHelper.getBlendFunction(cfg.blendMode[2]);
        boolean lightingEnabled = BlendModeHelper.getShouldLight(cfg.blendMode[3]);
        SplatterizerMod.LOGGER.log(Level.INFO, "srcFactor: " + cfg.blendMode[0] + ", destFactor: " + cfg.blendMode[1] + ", blendOp: " + cfg.blendMode[2] + ", lighting: " + cfg.blendMode[3]);

        SplatterParticleBase mainParticle = makeParticleBase(cfg.type, world, position, Vec3d.ZERO);
        if (mainParticle != null) {
            mainParticle.setParticleSubType(ParticleSubType.IMPACT);
            mainParticle.setBlendFactors(srcFactor, destFactor, blendOp, lightingEnabled);
            mainParticle.setEmissionRates(cfg.impactEmissionRate);
            mainParticle.setEmissionVelocity(cfg.impactEmissionVelocity);
            for (int index = 0; index < count; index++) {
                Vec3d dir = ParticleHelper.GetProjectileParticleVelocity(direction, index, count, spreadVariance, spreadSize);
                SplatterParticle part = makeParticle(cfg.type, world, position, dir);
                if (part != null) {
                    part.setParticleSubType(ParticleSubType.PROJECTILE);
                    part.setBlendFactors(srcFactor, destFactor, blendOp, lightingEnabled);
                    mainParticle.addSubparticle(part);
                }
//                Vec3d randDir = ParticleHelper.GetRandomNormalizedVector()
//                        .scale(ForgeConfigHandler.client.sprayParticleVelocity);
//                SplatterParticle part2 = makeParticle(cfg.type, world, position, randDir);
//                if (part2 != null) {
//                    part2.setParticleSubType(ParticleSubType.SPRAY);
//                    part2.setBlendFactors(srcFactor, destFactor, blendOp, lightingEnabled);
//                    mainParticle.addSubparticle(part2);
//                }
            }
            SplatterParticleRenderer.add(mainParticle);
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
