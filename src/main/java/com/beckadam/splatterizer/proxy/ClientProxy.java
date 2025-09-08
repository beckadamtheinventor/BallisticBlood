package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.helpers.ParticleHelper;
import com.beckadam.splatterizer.helpers.ParticleClientHelper;
import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class ClientProxy extends CommonProxy {

    @Override
    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "ClientProxy.AttackEntityFromHandler");
        ParticleType particleType = ParticleHelper.getParticleTypeForEntity(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        ParticleClientHelper.splatter(
                particleType,
                ParticleHelper.getParticlePosition(entity, source.getImmediateSource()),
                ParticleHelper.getParticleVelocity(entity.getPositionVector(), source),
                amount
        );
    }

    @Override
    public void sendMessageParticle(int dimension, ParticleType type, Vec3d position, Vec3d direction, float damage) {}

}