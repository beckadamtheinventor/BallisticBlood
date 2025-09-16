package com.beckadam.ballisticblood.particles;

import net.minecraft.world.World;

public class SplatterParticleProjectile extends SplatterParticleBase {
    public SplatterParticleProjectile(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
    }

    @Override
    public void onUpdate() {
        boolean wasOnGround = onGround;
        super.onUpdate();
        if (!wasOnGround && onGround) {
            oldDisplayType = displayType;
            displayType = ParticleDisplayType.DECAL;
            randomizeParticleTexture();
        } else if (wasOnGround && !onGround) {
            displayType = oldDisplayType;
            randomizeParticleTexture();
        }
    }
}
