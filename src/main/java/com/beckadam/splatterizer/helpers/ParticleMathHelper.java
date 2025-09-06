package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class ParticleMathHelper {
    public static ParticleType getParticleTypeForEntity(Entity entity) {
        ParticleType particleType;
        // hardcoded for now, could probably make this more configurable
        if (entity instanceof EntitySkeleton) {
            // dust particles/decals
            particleType = ParticleType.DUST_SPLATTER;
        } else if (entity instanceof EntityWitherSkeleton || entity instanceof EntityWither) {
            // ash particles/decals
            particleType = ParticleType.ASH_SPLATTER;
        } else if (entity instanceof EntitySlime) {
            // slime particles/decals
            particleType = ParticleType.SLIME_SPLATTER;
        } else {
            // blood particles/decals
            particleType = ParticleType.BLOOD_SPLATTER;
        }
        return particleType;
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
            return trueSource.getPositionVector().subtract(target).normalize().scale(3.0)
                    .add(new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ).scale(5.0f));
        }
    }


}
