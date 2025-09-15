package com.beckadam.ballisticblood.particles;

import net.minecraft.world.World;

public class SplatterSprayParticle extends SplatterParticleBase {
    public SplatterSprayParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        canCollide = false;
    }
}
