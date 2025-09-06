package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.helpers.ParticleSpawnHelper;

public class ClientProxy extends CommonProxy {

    @SideOnly(Side.CLIENT)
    @Override
    public void SpawnParticle(Particle particle) {
        if (particle != null) {
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }

    @Override
    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
        // return early if disabled client-side
        if (!ForgeConfigHandler.client.enableSplatterParticles) {
//            SplatterizerMod.LOGGER.log(Level.INFO, "Entity attacked, not doing particles: mod is disabled client-side!");
            return;
        }
//        SplatterizerMod.LOGGER.log(Level.INFO, "Doing particles");

        Entity sourceEntity = source.getImmediateSource();
        Entity trueSource = source.getTrueSource();
        // don't summon a particle if the source entity or true source doesn't exist
        if (sourceEntity == null || trueSource == null) {
            return;
        }
        Vec3d particlePos = new Vec3d(
                entity.posX*2.0f - entity.prevPosX,
                entity.posY*2.0f + entity.getEyeHeight() - entity.prevPosY,
                entity.posZ*2.0f - entity.prevPosZ
        );
        Vec3d velocity;
        // for projectiles, splatter in the direction it's moving.
        // otherwise, use a weighted sum of the entity's motion vector
        // and the distance vector (normalized) from the entity to the target.
        if (source.isProjectile()) {
            velocity = new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ);
        } else {
            velocity =
                    trueSource.getPositionVector().subtract(entity.getPositionVector()).normalize().scale(3.0)
                    .add(new Vec3d(sourceEntity.motionX, sourceEntity.motionY, sourceEntity.motionZ).scale(5.0f));
        }

        SplatterizerMod.LOGGER.log(Level.INFO, "Sending splatter at velocity: " + velocity);

        World world = entity.getEntityWorld();
        ParticleType particleType = getParticleType(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        // Note that this function spawns multiple particles in a cone shape facing the velocity direction
        ParticleSpawnHelper.splatter(world, particleType, particlePos, velocity);
    }

    private static ParticleType getParticleType(Entity entity) {
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

}