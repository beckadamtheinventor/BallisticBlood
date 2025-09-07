package com.beckadam.splatterizer.particles.types;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.particles.SplatterSubParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SlimeSubParticle extends SplatterSubParticle {
    public SlimeSubParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_decal_small.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_particle_small.png");
        particleGravity = 1.0f;
        particleScale = 0.5f;
    }
}
