package com.beckadam.splatterizer.particles.types;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.SplatterParticle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnderSplatterParticle extends SplatterParticle {

    public EnderSplatterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ender_decal.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ender_particle.png");
        blendSourceFactor = GlStateManager.SourceFactor.DST_COLOR;
        blendDestFactor = GlStateManager.DestFactor.ZERO;
        for (int i = 0; i < ForgeConfigHandler.client.particleSubCount; i++) {
            addSubparticle(vx, vy, vz);
        }
    }

    public void addSubparticle(double vx, double vy, double vz) {
        Vec3d r = new Vec3d(rand.nextFloat()-0.5f, rand.nextFloat()-0.5f, rand.nextFloat()-0.5f)
                .normalize().scale(ForgeConfigHandler.client.enderSubParticleBaseVelocity);
        r = new Vec3d(
                vx + r.x,
                vy + r.y,
                vz + r.z
        );
        addSubparticle(new EnderSubParticle(world, this.posX, this.posY, this.posZ, r.x, r.y, r.z));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.particleAge % ForgeConfigHandler.client.particleSubEmissionRate == 0) {
            this.addSubparticle(this.motionX, this.motionY, this.motionZ);
        }
    }
}
