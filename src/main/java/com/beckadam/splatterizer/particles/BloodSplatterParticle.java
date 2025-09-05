package com.beckadam.splatterizer.particles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import com.beckadam.splatterizer.SplatterizerMod;

public class BloodSplatterParticle extends SplatterParticle {
    public BloodSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/blood_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/blood_particle.png");
    }
}
