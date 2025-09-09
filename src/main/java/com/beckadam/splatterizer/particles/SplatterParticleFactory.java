package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class SplatterParticleFactory {
    private static final Random random = new Random(42069);
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
