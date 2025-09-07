package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

public class ParticleHelper {
    public static ParticleType getParticleTypeForEntity(Entity entity) {
        if (ForgeConfigHandler.server.entitySplatterTypeMap == null) {
            ForgeConfigHandler.ParseSplatterTypes();
        }
        ResourceLocation rl = EntityList.getKey(entity);
        try {
            if (ForgeConfigHandler.server.entitySplatterTypeMap.containsKey(rl)) {
                return ParticleType.valueOf(ForgeConfigHandler.server.entitySplatterTypeMap.get(rl));
            } else {
                return ParticleType.valueOf(ForgeConfigHandler.server.entitySplatterTypeDefault);
            }
        } catch (Exception e) {
            return ParticleType.BLOOD;
        }
    }

    public static Vec3d getParticlePosition(Entity entity) {
        return new Vec3d(
                entity.posX*2.0f - entity.prevPosX,
                entity.posY*2.0f + entity.getEyeHeight() - entity.prevPosY,
                entity.posZ*2.0f - entity.prevPosZ
        );
    }

    public static Vec3d getParticleVelocity(Vec3d target, DamageSource source) {
        Entity sourceEntity = source.getImmediateSource();
        Entity trueSource = source.getTrueSource();
        if (sourceEntity == null || trueSource == null) {
            return Vec3d.ZERO;
        }
        // for projectiles, splatter in the direction it's moving.
        // otherwise, use a weighted sum of the entity's motion vector
        // and the distance vector (normalized) from the entity to the target.
        if (source.isProjectile()) {
            return new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ);
        } else {
            return target.subtract(trueSource.getPositionVector()).normalize()
                    .add(new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ).scale(2.0f))
                    .scale(0.5);
        }
    }

    public static int scaleCountByDamage(int count, float amount) {
        return (int)(count * (1.0f + ForgeConfigHandler.client.extraParticlesPerHeartOfDamage * amount));
    }

    private static final double EPSILON = 0.000001;
    private static Vec3d getVertexOnCircleFacingDirection(Vec3d direction, int index, int total, double radius, double vertical_radius) {
        double angle = Math.atan2(direction.x, direction.z) + Math.PI * 0.5;
        double rot = (2.0 * Math.PI * index) / (double)total;
        double CX = Math.cos(rot);
        double SX = Math.sin(rot);
        double CY = Math.cos(angle);
        double SY = Math.sin(angle);
        return new Vec3d(
                radius * (CY*direction.x + SY*direction.z),
                vertical_radius * (CX*direction.y - SX*direction.z),
                radius * (-SY*direction.x + CY*direction.z)*(SX*direction.y + CX*direction.z)
        );
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
            Vec3d offset = getVertexOnCircleFacingDirection(dir, index, total, 1.0, 0.1);
            return dir.add(offset.scale(spread));
        }
        return dir;
    }
}
