package com.beckadam.splatterizer.particles;

import com.beckadam.splatterizer.SplatterizerMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SlimeSplatterParticle extends SplatterParticle {
    public SlimeSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/slime_particle.png");
    }
}
