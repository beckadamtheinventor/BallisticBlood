package com.beckadam.splatterizer.particles.types;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.particles.SplatterSubParticle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EnderSubParticle extends SplatterSubParticle {
    public EnderSubParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        SPLATTER_DECAL_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ender_decal_small.png");
        SPLATTER_PARTICLE_TEXTURE = new ResourceLocation(SplatterizerMod.MODID, "textures/particle/ender_particle_small.png");
        blendSourceFactor = GlStateManager.SourceFactor.ONE;
        blendDestFactor = GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR;
        particleGravity = 1.0f;
        particleScale = 0.5f;
    }
}
