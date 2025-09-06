package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.SplatterizerMod;
import com.beckadam.splatterizer.helpers.ParticleMathHelper;
import com.beckadam.splatterizer.message.MessageParticleHandler;
import com.beckadam.splatterizer.particles.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Level;

public class CommonProxy {
    public static final SimpleNetworkWrapper networkWrapperInstance = NetworkRegistry.INSTANCE.newSimpleChannel(SplatterizerMod.MODID);

    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "CommonProxy.AttackEntityFromHandler");
        ParticleType particleType = ParticleMathHelper.getParticleTypeForEntity(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        SplatterizerMod.PROXY.sendMessageParticle(
                entity.dimension, particleType,
                ParticleMathHelper.getParticlePosition(entity),
                ParticleMathHelper.getParticleVelocity(entity.getPositionVector(), source)
        );
    }

    public void sendMessageParticle(int dimension, ParticleType type, Vec3d position, Vec3d direction) {
        MessageParticleHandler.MessageParticleFX message =
                new MessageParticleHandler.MessageParticleFX(type, position, direction);
        networkWrapperInstance.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, position.x, position.y, position.z, 128.0));
    }
}