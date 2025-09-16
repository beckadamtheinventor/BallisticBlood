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

import java.util.ArrayList;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class ClientHelper {
    private static final Random random = new Random();
    private static final ArrayList<SplatterParticleMain> mainParticleList = new ArrayList<>(512);

    public static void clearParticles() {
        for (SplatterParticleMain particle : mainParticleList) {
            particle.setExpired();
        }
        mainParticleList.clear();
    }

    public static void clearParticles(int amount) {
        for (int i=0; i<amount; i++) {
            mainParticleList.get(i).setExpired();
        }
        mainParticleList.removeIf(particle -> !particle.isAlive());
    }

    public static int countParticles() {
        return mainParticleList.size();
    }

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
            clearParticles();
            return;
        }

        if (countParticles() >= ForgeConfigHandler.client.maximumProjectileParticles) {
            clearParticles(32);
        }

        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        double spreadSize = ForgeConfigHandler.client.particleSpreadSize;
        double spreadVariance = ForgeConfigHandler.client.particleSpreadVariance;
        int projectileCount = Math.min(
                ForgeConfigHandler.client.particleSpreadMax,
                CommonHelper.ScaleCountByDamage(ForgeConfigHandler.client.particleSpreadCount, ForgeConfigHandler.client.sprayParticlePerHeart, damage)
        );
        int sprayCount = Math.min(
                ForgeConfigHandler.client.sprayParticleMax,
                CommonHelper.ScaleCountByDamage(ForgeConfigHandler.client.sprayParticleCount, ForgeConfigHandler.client.sprayParticlePerHeart, damage)
        );
        GlStateManager.SourceFactor srcFactor = BlendModeHelper.getSourceFactor(cfg.blendMode[0]);
        GlStateManager.DestFactor destFactor = BlendModeHelper.getDestFactor(cfg.blendMode[1]);
        int blendOp = BlendModeHelper.getBlendFunction(cfg.blendOp);
        boolean lightingEnabled = cfg.lighting;

        // Note: the main particle is just the spray emitter and doesn't render
        // The projectile particles and spray particles do render
        SplatterParticleMain mainParticle = makeMainParticle(cfg, world, position, Vec3d.ZERO);

        // set the particle subtype so the main impact particle doesn't get rendered
        mainParticle.setDisplayType(ParticleDisplayType.IMPACT);

        // set the lifetime and fade start time
        mainParticle.setLifetime(ForgeConfigHandler.client.particleLifetime, ForgeConfigHandler.client.particleLifetime);

        // set no gravity so it stays in the same position
        mainParticle.setGravity(0.0f);

        // set color/alpha blending factors
        mainParticle.setBlendFactors(srcFactor, destFactor, blendOp, lightingEnabled);

        // create projectiles depending on config and damage amount
        for (int index = 0; index < projectileCount; index++) {
            // grab the direction/velocity vector for this particle
            // (should spread in an arc-like pattern)
            Vec3d dir = CommonHelper.GetProjectileParticleVelocity(direction, index, projectileCount, spreadVariance, spreadSize);

            // create a projectile particle at the hit position using the velocity scaled by config
            SplatterParticleProjectile projectileParticle = makeProjectileParticle(cfg, world, position,
                    dir.scale(ForgeConfigHandler.client.projectileParticleVelocity)
            );

            // set the particle subtype and pick a random texture from the atlas
            // (texture selected from rows 0, 1, 2, 3, and 4)
            projectileParticle.setDisplayType(ParticleDisplayType.PROJECTILE);

            // randomize the particle texture
            projectileParticle.randomizeParticleTexture();

            // set gravity from config
            projectileParticle.setGravity(ForgeConfigHandler.client.projectileParticleGravity);

            // set scale from config
            projectileParticle.setScale(ForgeConfigHandler.client.projectileParticleSize);

            // set lifetime specific to this projectile
            projectileParticle.setLifetime(ForgeConfigHandler.client.projectileParticleLifetime, ForgeConfigHandler.client.projectileParticleFadeStart);

            // set the values to multiply texture color/alpha with when drawing
            projectileParticle.setMultipliers(cfg.colorMultiplier, cfg.alphaMultiplier);

            // add the projectile particle to the main impact particle
            mainParticle.addParticle(projectileParticle);
        }

        // create spray particles for initial hit
        for (int index = 0; index < sprayCount; index++) {
            // get a random direction per spray particle
            Vec3d dir = CommonHelper.GetRandomNormalizedVector();

            // create a projectile particle at the hit position using the velocity scaled by config
            SplatterParticleSpray sprayParticle = makeSprayParticle(cfg, world, position,
                    dir.add(direction.normalize()).scale(ForgeConfigHandler.client.sprayParticleVelocity)
            );

            // set the particle subtype and pick a random texture from the atlas
            // (texture selected from rows 0, 1, 2, 3, and 4)
            sprayParticle.setDisplayType(ParticleDisplayType.SPRAY);

            // randomize the particle texture
            sprayParticle.randomizeParticleTexture();

            // set gravity from config
            sprayParticle.setGravity(ForgeConfigHandler.client.sprayParticleGravity);

            // set scale from config
            sprayParticle.setScale(ForgeConfigHandler.client.sprayParticleSize);

            // set lifetime specific to this projectile
            sprayParticle.setLifetime(ForgeConfigHandler.client.sprayParticleLifetime, ForgeConfigHandler.client.sprayParticleFadeStart);

            // set the values to multiply texture color/alpha with when drawing
            sprayParticle.setMultipliers(cfg.colorMultiplier, cfg.alphaMultiplier);

            // add the projectile particle to the main impact particle
            mainParticle.addParticle(sprayParticle);
        }
        Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);
        mainParticleList.add(mainParticle);
    }

    ///   Create and initialize a main (initial) particle at position (pos) with velocity (dir)
    public static SplatterParticleMain makeMainParticle(ForgeConfigHandler.ParticleConfig cfg, World world, Vec3d pos, Vec3d dir) {
        float v = cfg.velocity;
        SplatterParticleMain particle = new SplatterParticleMain(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
        particle.setType(cfg.type);
        particle.setTexture(cfg.texture);
        particle.setGravity(cfg.gravity);
        return particle;
    }

    ///  Create and initialize a Spray particle at position (pos) with velocity (dir)
    public static SplatterParticleSpray makeSprayParticle(ForgeConfigHandler.ParticleConfig cfg, World world, Vec3d pos, Vec3d dir) {
        float v = cfg.sprayVelocity;
        SplatterParticleSpray particle = new SplatterParticleSpray(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
        particle.setType(cfg.type);
        particle.setGravity(cfg.gravity);
        particle.setFlip(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
        return particle;
    }

    ///  Create and initialize a Projectile particle at position (pos) with velocity (dir)
    public static SplatterParticleProjectile makeProjectileParticle(ForgeConfigHandler.ParticleConfig cfg, World world, Vec3d pos, Vec3d dir) {
        float v = cfg.velocity;
        SplatterParticleProjectile particle = new SplatterParticleProjectile(world, pos.x, pos.y, pos.z, v*dir.x, v*dir.y, v*dir.z);
        particle.setType(cfg.type);
        particle.setGravity(cfg.gravity);
        particle.setFlip(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
        return particle;
    }


}
