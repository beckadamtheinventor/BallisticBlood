package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.helpers.ParticleMathHelper;
import com.beckadam.splatterizer.helpers.ParticleSpawnHelper;
import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import com.beckadam.splatterizer.SplatterizerMod;

public class ClientProxy extends CommonProxy {

    @Override
    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "ClientProxy.AttackEntityFromHandler");
        ParticleType particleType = ParticleMathHelper.getParticleTypeForEntity(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        ParticleSpawnHelper.splatter(
                particleType,
                ParticleMathHelper.getParticlePosition(entity),
                ParticleMathHelper.getParticleVelocity(entity.getPositionVector(), source)
        );
    }

    @Override
    public void sendMessageParticle(int dimension, ParticleType type, Vec3d position, Vec3d direction) {}

}