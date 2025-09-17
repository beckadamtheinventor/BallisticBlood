package com.beckadam.ballisticblood.particles;

import net.minecraft.world.World;

public class SplatterParticleSpray extends SplatterParticleBase {
    public SplatterParticleSpray(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        canDecal = false;
    }
}
