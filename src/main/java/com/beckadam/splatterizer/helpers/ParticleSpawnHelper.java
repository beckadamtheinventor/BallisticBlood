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
    private static final double EPSILON = 0.000001;
    private static final Random random = new Random();
    private static Vec3d getVertexOnCircleFacingDirection(Vec3d direction, int index, int total, double radius) {
        double angle = Math.atan2(direction.x, direction.z) + Math.PI * 0.5;
        double rot = (2.0 * Math.PI * index) / (double)total;
        double CX = Math.cos(rot);
        double SX = Math.sin(rot);
        double CY = Math.cos(angle);
        double SY = Math.sin(angle);
        return new Vec3d(
                (CY*direction.x + SY*direction.z),
                (CX*direction.y - SX*direction.z),
                (-SY*direction.x + CY*direction.z)*(SX*direction.y + CX*direction.z)
        ).scale(radius);
    }

    public static Vec3d[] getAxisAlignedQuad(Vec3d direction, double width) {
        Vec3d v000 = new Vec3d(-1, -1, -1);
        Vec3d v001 = new Vec3d(-1, -1,  1);
        Vec3d v010 = new Vec3d(-1,  1, -1);
        Vec3d v011 = new Vec3d(-1,  1,  1);
        Vec3d v100 = new Vec3d( 1, -1, -1);
        Vec3d v101 = new Vec3d( 1, -1,  1);
        Vec3d v110 = new Vec3d( 1,  1, -1);
        Vec3d v111 = new Vec3d( 1,  1,  1);
        Vec3d dX = new Vec3d(1, 0, 0);
        Vec3d dY = new Vec3d(0, 1, 0);
        Vec3d dZ = new Vec3d(0, 0, 1);
        width /= 2.0;
        // vertex order per quad: ++, +-, --, -+
        if (Math.abs(direction.z) < EPSILON && Math.abs(direction.y) < EPSILON) {
            if (direction.x < 0) { // facing -X
                return new Vec3d[] {
                        v011.add(dX).scale(width),
                        v010.add(dX).scale(width),
                        v000.add(dX).scale(width),
                        v001.add(dX).scale(width)
                };
            } else { // facing +X
                return new Vec3d[] {
                        v110.subtract(dX).scale(width),
                        v100.subtract(dX).scale(width),
                        v101.subtract(dX).scale(width),
                        v111.subtract(dX).scale(width)
                };
            }
        } else if (Math.abs(direction.x) < EPSILON && Math.abs(direction.y) < EPSILON) {
            if (direction.z < 0) { // facing -Z
                return new Vec3d[] {
                        v110.add(dZ).scale(width),
                        v100.add(dZ).scale(width),
                        v000.add(dZ).scale(width),
                        v010.add(dZ).scale(width)
                };
            } else { // facing +Z
                return new Vec3d[] {
                        v101.subtract(dZ).scale(width),
                        v001.subtract(dZ).scale(width),
                        v011.subtract(dZ).scale(width),
                        v111.subtract(dZ).scale(width)
                };
            }
        } else {
            if (direction.y < 0) { // facing -Y
                return new Vec3d[] {
                        v101.add(dY).scale(width),
                        v100.add(dY).scale(width),
                        v000.add(dY).scale(width),
                        v001.add(dY).scale(width)
                };
            } else { // facing +Y
                return new Vec3d[] {
                        v110.subtract(dY).scale(width),
                        v010.subtract(dY).scale(width),
                        v011.subtract(dY).scale(width),
                        v111.subtract(dY).scale(width)
                };
            }
        }
    }

    public static Vec3d SpreadInCone(Vec3d dir, int index, int total, double spread) {
        if (total > 1) {
            Vec3d offset = getVertexOnCircleFacingDirection(dir, index, total, ForgeConfigHandler.client.particleSpreadSize);
            return dir.add(offset.scale(spread));
        }
        return dir;
    }
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
            Vec3d dir = SpreadInCone(direction.scale(-0.1), index, total, spreadSize*rand);
            spawnParticle(type, world, position, new Vec3d(dir.x, dir.y*0.1f, dir.z));
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
