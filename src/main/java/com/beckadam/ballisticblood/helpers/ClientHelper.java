package com.beckadam.ballisticblood.helpers;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.particles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ClientHelper {
    private static final Random random = new Random();

    ///  Spawn splatter particles for a given splatter type at position facing direction (also used for velocity) scaling by damage
    ///  Uses values from both client and server config
    public static void splatter(int type, Vec3d position, Vec3d direction, float damage) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            splatter(ForgeConfigHandler.particleConfigIntMap.get(type), position, direction, damage);
        } else {
            BallisticBloodMod.LOGGER.log(Level.WARN, "Ignoring unknown splatter particle type " + type);
        }
    }

    ///  Spawn splatter particles for a given splatter config at position facing direction (also used for velocity) scaling by damage
    ///  Uses values from client config
    public static void splatter(ForgeConfigHandler.ParticleConfig cfg, Vec3d position, Vec3d direction, float damage) {
        // if we have splatter particles disabled, return before doing anything
        if (!ForgeConfigHandler.client.enableSplatterParticles) {
            return;
        }
        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        double spreadSize = ForgeConfigHandler.client.particleSpreadSize;
        double spreadVariance = ForgeConfigHandler.client.particleSpreadVariance;
        int count = Math.min(
                ForgeConfigHandler.client.particleSpreadMax,
                CommonHelper.ScaleCountByDamage(ForgeConfigHandler.client.particleSpreadCount, damage)
        );
        if (count <= 0) {
            return;
        }
        GlStateManager.SourceFactor srcFactor = BlendModeHelper.getSourceFactor(cfg.blendMode[0]);
        GlStateManager.DestFactor destFactor = BlendModeHelper.getDestFactor(cfg.blendMode[1]);
        int blendOp = BlendModeHelper.getBlendFunction(cfg.blendOp);
        boolean lightingEnabled = cfg.lighting;
//        SplatterizerMod.LOGGER.log(Level.INFO, "srcFactor: " + srcFactor + ", destFactor: " + destFactor + ", blendOp: " + blendOp + ", lighting: " + lightingEnabled);

        // Note: the main particle is just the spray emitter and doesn't render
        // The projectile particles and spray particles do render

        // create the main impact particle at the hit position with no velocity
        SplatterParticleBase mainParticle = makeParticleBase(cfg.type, world, position, Vec3d.ZERO);
        if (mainParticle != null) {
            // set the particle subtype so the main impact particle doesn't get rendered
            mainParticle.setParticleSubType(ParticleSubType.IMPACT);

            // set the lifetime and fade start time
            mainParticle.setLifetime(ForgeConfigHandler.client.particleLifetime, ForgeConfigHandler.client.particleLifetime);

            // set no gravity so it stays in the same position
            mainParticle.setGravity(0.0f);

            // set the rate at which the impact particle emits spray particles
            mainParticle.setEmissionRate(cfg.emissionRate);

            // set the velocity for emitted spray particles
            mainParticle.setEmissionVelocity(cfg.emissionVelocity);

            // set color/alpha blending factors
            mainParticle.setBlendFactors(srcFactor, destFactor, blendOp, lightingEnabled);

            // set the values to multiply texture color/alpha with when drawing
            mainParticle.setMultipliers(cfg.colorMultiplier, cfg.alphaMultiplier);

            // create projectiles depending on config and damage amount
            for (int index = 0; index < count; index++) {
                // grab the direction/velocity vector for this particle
                // (should spread in an arc-like pattern)
                Vec3d dir = CommonHelper.GetProjectileParticleVelocity(direction, index, count, spreadVariance, spreadSize);

                // create a projectile particle at the hit position using the velocity scaled by config
                SplatterParticle particle = makeParticle(cfg.type, world, position, dir.scale(ForgeConfigHandler.client.projectileParticleVelocity));

                // if the particle was successfully created...
                if (particle != null) {
                    // set the particle subtype and pick a random texture from the atlas
                    // (texture selected from rows 0, 1, 2, 3, and 4)
                    particle.setParticleSubType(ParticleSubType.PROJECTILE);

                    // randomize the particle texture
                    particle.randomizeParticleTexture();

                    // set gravity from config
                    particle.setGravity(ForgeConfigHandler.client.projectileParticleGravity);

                    // set scale from config
                    particle.setScale(ForgeConfigHandler.client.projectileParticleSize);

                    // set lifetime specific to this projectile
                    particle.setLifetime(ForgeConfigHandler.client.projectileParticleLifetime, ForgeConfigHandler.client.projectileParticleFadeStart);

                    // add the projectile particle to the main impact particle
                    mainParticle.addSubparticle(particle);
                }
            }
            // finally, add the impact particle to the particle manager
            // ParticleManager.renderParticle is called by the game to draw all of our particle
            //   the function draws the projectile and spray particles
            // ParticleManager.onUpdate is called by the game to update all of our particles
            //   the function updates the initial particle's lifetime and updates the projectile and spray particles
//            BallisticBloodMod.LOGGER.log(Level.INFO, "splatter");
            if (ParticleManager.instance == null) {
                ParticleManager.MakeParticleManager(world);
            }
            ParticleManager.instance.add(mainParticle);
        }
    }

    ///   Create and initialize an Impact (initial) particle at position (pos) with velocity (dir)
    public static SplatterParticleBase makeParticleBase(int type, World world, Vec3d pos, Vec3d dir) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            ForgeConfigHandler.ParticleConfig cfg = ForgeConfigHandler.particleConfigIntMap.get(type);
            if (cfg == null) {
                return null;
            }
            float v = cfg.velocity;
            SplatterParticleBase particle = new SplatterParticleBase(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
            particle.setType(type);
            particle.setTexture(cfg.texture);
            particle.setGravity(cfg.gravity);
            return particle;
        }
        return null;
    }

    ///  Create and initialize a Projectile or Spray particle at position (pos) with velocity (dir)
    public static SplatterParticle makeParticle(int type, World world, Vec3d pos, Vec3d dir) {
        if (ForgeConfigHandler.particleConfigIntMap.containsKey(type)) {
            ForgeConfigHandler.ParticleConfig cfg = ForgeConfigHandler.particleConfigIntMap.get(type);
            float v = cfg.velocity;
            SplatterParticle particle = new SplatterParticle(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
            particle.setType(type);
//            particle.setTexture(cfg.texture);
            particle.setGravity(cfg.gravity);
            particle.setFlip(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
            return particle;
        }
        return null;
    }
}
