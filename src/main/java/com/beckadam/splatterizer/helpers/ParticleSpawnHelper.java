package com.beckadam.splatterizer.helpers;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.handlers.MessageParticleHandler;
import com.beckadam.splatterizer.handlers.PacketHandler;
import com.beckadam.splatterizer.particles.*;

import java.util.ArrayList;

public class ParticleSpawnHelper {
    private static Vec3d computeOffset(int index, int total, Vec3d direction) {
        double rot = (Math.PI * index) / total;
        Vec3d axis = direction.normalize();
        Vec3d axis2 = axis.scale(Math.sin(rot));
        double axis2W = Math.cos(rot) * 2.0;
        Vec3d wv = axis2.crossProduct(direction);
        Vec3d wwv = axis2.crossProduct(wv);
        return direction.add(wv.scale(axis2W)).add(wwv.scale(2.0));
    }
    public static void splatter(World world, ParticleType type, Vec3d position, Vec3d direction) {
        ArrayList<MessageParticleHandler.MessageParticleFX.Particle> network_particles = new ArrayList<>();
        ArrayList<ParticleType> network_particle_types = new ArrayList<>();
        int total = ForgeConfigHandler.client.particleSpreadCount;
        Vec3d norm_direction = direction.normalize();
        for (int index=0; index<total; index++) {
            Vec3d offset = computeOffset(index, total, direction);
            Vec3d pos = position.add(offset);
            spawnParticle(type, world, pos, norm_direction);
            network_particles.add(new MessageParticleHandler.MessageParticleFX.Particle(pos, norm_direction));
            network_particle_types.add(type);
        }
        MessageParticleHandler.MessageParticleFX message =
                new MessageParticleHandler.MessageParticleFX(network_particle_types, network_particles);
        PacketHandler.instance.sendToAll(message);
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
            default:
                particle = null;
                break;
        }
        if (particle != null) {
            SplatterizerMod.PROXY.SpawnParticle(particle);
        }
    }
}
