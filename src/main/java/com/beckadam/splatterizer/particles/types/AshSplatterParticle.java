package com.beckadam.splatterizer.particles.types;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.SplatterParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.SplatterizerMod;

public class AshSplatterParticle extends SplatterParticle {
    public AshSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ash_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ash_particle.png");
        particleGravity = 0.5f;
        for (int i = 0; i < ForgeConfigHandler.client.particleSubCount; i++) {
            Vec3d r = new Vec3d(rand.nextFloat()-0.5f, rand.nextFloat()-0.5f, rand.nextFloat()-0.5f)
                    .normalize().scale(ForgeConfigHandler.client.ashSubParticleBaseVelocity);
            r = new Vec3d(
                    vx + r.x,
                    vy + r.y,
                    vz + r.z
            );
            addSubparticle(new AshSubParticle(world, this.posX, this.posY, this.posZ, r.x, r.y, r.z));
        }
    }
}
