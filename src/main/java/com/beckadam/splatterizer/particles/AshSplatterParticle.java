package com.beckadam.splatterizer.particles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import com.beckadam.splatterizer.SplatterizerMod;

public class AshSplatterParticle extends SplatterParticle {
    public AshSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ash_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ash_particle.png");
    }
}
