package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
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
    public void AttackEntityFromHandler(DamageSource source, float amount) {
        // return early if disabled client-side
        if (!ForgeConfigHandler.client.enableSplatterParticles) {
            SplatterizerMod.LOGGER.log(Level.INFO, "Entity attacked, not doing particles: mod is disabled client-side!");
            return;
        }
        SplatterizerMod.LOGGER.log(Level.INFO, "Entity attacked, trying to do particles");
        double veloX=0, veloY=0, veloZ=0;

        Entity sourceEntity = source.getImmediateSource();
        Entity trueSource = source.getTrueSource();
        // don't summon a particle if the source entity or true source doesn't exist
        if (sourceEntity == null || trueSource == null) {
            return;
        }
        Vec3d damageLoc = source.getDamageLocation();
        if (damageLoc == null) {
            damageLoc = new Vec3d(sourceEntity.posX, sourceEntity.posY, sourceEntity.posZ);
        }
        if (source.isProjectile()) {
            veloX = sourceEntity.posX - sourceEntity.prevPosX;
            veloY = sourceEntity.posY - sourceEntity.prevPosY;
            veloZ = sourceEntity.posZ - sourceEntity.prevPosZ;
        } else {
            veloX = trueSource.posX - trueSource.prevPosX;
            veloY = trueSource.posY - trueSource.prevPosY;
            veloZ = trueSource.posZ - trueSource.prevPosZ;
        }

        World world = sourceEntity.getEntityWorld();
        ParticleType particle;
        if (sourceEntity instanceof EntitySkeleton) {
            // dust particles
            particle = ParticleType.DUST_SPLATTER;
            SplatterizerMod.LOGGER.log(Level.INFO, "Dust particle");
        } else if (sourceEntity instanceof EntityWitherSkeleton || sourceEntity instanceof EntityWither) {
            // ash particles
            particle = ParticleType.ASH_SPLATTER;
            SplatterizerMod.LOGGER.log(Level.INFO, "Ash particle");
        } else {
            // blood particles
            SplatterizerMod.LOGGER.log(Level.INFO, "Blood particle");
            particle = ParticleType.BLOOD_SPLATTER;
        }
        Vec3d velocity = new Vec3d(veloX, veloY, veloZ).scale(1.0f + amount * 0.0625f);
        ParticleSpawnHelper.splatter(world, particle, damageLoc, velocity);
    }

}