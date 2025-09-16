package com.beckadam.ballisticblood.helpers;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import java.util.Random;

public class CommonHelper {
    // TODO: Make the seed not constant
    public static final Random random = new Random(133742069);
    public static int GetParticleTypeForEntity(Entity entity) {
        ResourceLocation rl = EntityList.getKey(entity);
        try {
            if (ForgeConfigHandler.server.entitySplatterTypeMap.containsKey(rl)) {
                return ForgeConfigHandler.server.entitySplatterTypeMap.get(rl);
            } else {
                String s = ForgeConfigHandler.server.entitySplatterTypeDefault;
                return BallisticBloodMod.particleTypes.get(s);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public static Vec3d GetParticlePosition(Entity target, DamageSource source) {
        Entity sourceEntity = source.getImmediateSource();
        if (sourceEntity == null) {
            sourceEntity = source.getTrueSource();
        }
        if (sourceEntity == null) {
            return target.getPositionVector();
        }
        if (source.isProjectile()) {
            return sourceEntity.getPositionVector();
        }
        AxisAlignedBB box = target.getEntityBoundingBox();
//        SplatterizerMod.LOGGER.log(Level.INFO, "Source Look Vector: " + source.getLookVec());
        RayTraceResult rt = box.calculateIntercept(
                sourceEntity.getPositionVector().add(0.0, sourceEntity.getEyeHeight(), 0.0),
                sourceEntity.getPositionVector()
                        .add(sourceEntity.getLookVec().scale(32.0))
        );
        if (rt != null) {
//            SplatterizerMod.LOGGER.log(Level.INFO, "Hit Vector: " + rt.hitVec);
            return rt.hitVec;
        }
        return target.getPositionVector();
    }

    public static Vec3d GetParticleVelocity(Entity target, DamageSource source) {
        Entity sourceEntity = source.getImmediateSource();
        Entity trueSource = source.getTrueSource();
        if (sourceEntity == null || trueSource == null) {
            return Vec3d.ZERO;
        }
        // for explosions, splatter outward from the explosion
        // for projectiles, splatter in the direction it's moving.
        // otherwise, use a weighted sum of the entity's motion vector
        // and the distance vector (normalized) from the entity to the target.
        if (source.isExplosion()) {
            Vec3d dist = target.getPositionVector().subtract(sourceEntity.getPositionVector());
            return dist.normalize().scale(Math.min(25.0, 10.0 / dist.lengthSquared()));
        } else if (source.isProjectile()) {
            return new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ);
        } else {
            return target.getPositionVector().subtract(trueSource.getPositionVector()).normalize()
                    .add(new Vec3d(trueSource.motionX, trueSource.motionY, trueSource.motionZ).scale(2.0f));
        }
    }

    public static int ScaleCountByDamage(int count, float extra, float amount) {
        return count + (int)(extra * amount);
    }

    public static Vec3d[] GetAxisAlignedQuad(EnumFacing dir, double width) {
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
        switch (dir) {
            case WEST:
                return new Vec3d[] {
                        v011.add(dX).scale(width),
                        v010.add(dX).scale(width),
                        v000.add(dX).scale(width),
                        v001.add(dX).scale(width)
                };
            case EAST:
                return new Vec3d[] {
                        v110.subtract(dX).scale(width),
                        v100.subtract(dX).scale(width),
                        v101.subtract(dX).scale(width),
                        v111.subtract(dX).scale(width)
                };
            case NORTH:
                return new Vec3d[] {
                        v110.add(dZ).scale(width),
                        v100.add(dZ).scale(width),
                        v000.add(dZ).scale(width),
                        v010.add(dZ).scale(width)
                };
            case SOUTH:
                return new Vec3d[] {
                        v101.subtract(dZ).scale(width),
                        v001.subtract(dZ).scale(width),
                        v011.subtract(dZ).scale(width),
                        v111.subtract(dZ).scale(width)
                };
            case DOWN:
                return new Vec3d[] {
                        v101.add(dY).scale(width),
                        v100.add(dY).scale(width),
                        v000.add(dY).scale(width),
                        v001.add(dY).scale(width)
                };
            case UP:
                return new Vec3d[] {
                        v110.subtract(dY).scale(width),
                        v010.subtract(dY).scale(width),
                        v011.subtract(dY).scale(width),
                        v111.subtract(dY).scale(width)
                };
            default:
                break;
        }
        return new Vec3d[] {};
    }

    public static Vec3d GetProjectileParticleVelocity(Vec3d dir, int index, int total, double variance, double spread) {
        if (total > 1) {
            double r = 0.1 * (random.nextFloat() - 0.5) * variance;
            double r2 = random.nextFloat() * variance;
            double dx = r + spread * 4.0 * ((double)index / (double)total - 0.5);
            double dy = 0.25 * random.nextFloat();
            double a0 = Math.atan2(dir.z, dir.x);
            double ang = a0 + dx * 0.5 * Math.PI;
            Vec3d offset = new Vec3d(Math.cos(ang), dy, Math.sin(ang));
//            BallisticBloodMod.LOGGER.log(Level.INFO, "Direction angle: " + ang + " offset vector: " + offset);
            return dir.add(offset.scale(r2));
        }
        return dir;
    }

    public static Vec3d GetRandomNormalizedVector() {
        return new Vec3d(random.nextFloat()-0.5, random.nextFloat()-0.5, random.nextFloat()-0.5).normalize();
    }
}
