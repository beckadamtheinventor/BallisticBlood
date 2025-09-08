package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.*;
import com.beckadam.splatterizer.particles.types.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleClientHelper {
    private static final Random random = new Random();

    public static void splatter(ParticleType type, Vec3d position, Vec3d direction, float damage) {
        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        double spreadSize = ForgeConfigHandler.client.particleSpreadSize;
        double spreadVariance = ForgeConfigHandler.client.particleSpreadVariance;
        int count = Math.min(
                ForgeConfigHandler.client.particleSpreadMax,
                ParticleHelper.scaleCountByDamage(ForgeConfigHandler.client.particleSpreadCount, damage)
        );
//        int total = 1;
        for (int index=0; index<count; index++) {
//            Vec3d offset = getVertexOnCircleFacingDirection(index, total, direction.normalize(), direction.length());
//            Vec3d pos = position.add(offset);
            Vec3d dir = ParticleHelper.SpreadParticleVelocity(direction, index, count, spreadVariance, spreadSize);
            spawnParticle(type, world, position, new Vec3d(dir.x, dir.y, dir.z));
        }
    }
    public static void spawnParticle(ParticleType type, World world, Vec3d pos, Vec3d dir) {
        Particle particle;
        switch (type) {
            case BLOOD:
                particle = new BloodSplatterParticle(world, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                break;
            case DUST:
                particle = new DustSplatterParticle(world, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                break;
            case ASH:
                particle = new AshSplatterParticle(world, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                break;
            case SLIME:
                particle = new SlimeSplatterParticle(world, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                break;
            case ENDER:
                particle = new EnderSplatterParticle(world, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                break;
            default:
                particle = null;
                break;
        }
        if (particle != null) {
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }
}
