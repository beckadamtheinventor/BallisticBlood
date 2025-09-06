package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.particles.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleSpawnHelper {
    private static final Random random = new Random();

    public static void splatter(ParticleType type, Vec3d position, Vec3d direction) {
        World world = Minecraft.getMinecraft().world;
        int total = ForgeConfigHandler.client.particleSpreadCount;
        double spreadSize = ForgeConfigHandler.client.particleSpreadSize;
        double spreadVariance = ForgeConfigHandler.client.particleSpreadVariance;
//        int total = 1;
        for (int index=0; index<total; index++) {
//            Vec3d offset = getVertexOnCircleFacingDirection(index, total, direction.normalize(), direction.length());
//            Vec3d pos = position.add(offset);
            double rand = (0.5f * random.nextFloat() - 0.5f) * spreadVariance;
            Vec3d dir = ParticleMathHelper.SpreadInCone(direction, index, total, spreadSize*rand);
            spawnParticle(type, world, position, new Vec3d(dir.x, dir.y, dir.z));
        }
    }
    public static void spawnParticle(ParticleType type, World world, Vec3d p, Vec3d d) {
        Particle particle;
        switch (type) {
            case BLOOD_SPLATTER:
                particle = new BloodSplatterParticle(world, p.x, p.y, p.z, d.x, d.y, d.z);
                break;
            case DUST_SPLATTER:
                particle = new DustSplatterParticle(world, p.x, p.y, p.z, d.x, d.y, d.z);
                break;
            case ASH_SPLATTER:
                particle = new AshSplatterParticle(world, p.x, p.y, p.z, d.x, d.y, d.z);
                break;
            case SLIME_SPLATTER:
                particle = new SlimeSplatterParticle(world, p.x, p.y, p.z, d.x, d.y, d.z);
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
