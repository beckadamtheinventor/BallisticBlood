package com.beckadam.splatterizer.particles.types;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.SplatterParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SlimeSplatterParticle extends SplatterParticle {
    public SlimeSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_particle.png");
        for (int i = 0; i < ForgeConfigHandler.client.particleSubCount; i++) {
            Vec3d r = new Vec3d(rand.nextFloat()-0.5f, rand.nextFloat()-0.5f, rand.nextFloat()-0.5f)
                    .normalize().scale(ForgeConfigHandler.client.slimeSubParticleBaseVelocity);
            r = new Vec3d(
                    vx + r.x,
                    vy + r.y,
                    vz + r.z
            );
            addSubparticle(new SlimeSubParticle(world, x, y, z, r.x, r.y, r.z));
        }
    }
}
