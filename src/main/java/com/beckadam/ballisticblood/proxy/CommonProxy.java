package com.beckadam.ballisticblood.proxy;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import com.beckadam.ballisticblood.message.MessageParticleHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
    public static final SimpleNetworkWrapper networkWrapperInstance = NetworkRegistry.INSTANCE.newSimpleChannel(BallisticBloodMod.MODID);

    public void init() {
        networkWrapperInstance.registerMessage(
                MessageParticleHandler.MessageParticle.Handler.class,
                MessageParticleHandler.MessageParticle.class,
                0, Side.CLIENT
        );
    }
    public void LoadTextures() {
        ForgeConfigHandler.needsTextureLoad = false;
    }

    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "CommonProxy.AttackEntityFromHandler");
        int particleType = CommonHelper.GetParticleTypeForEntity(entity);
        Entity sourceEntity = source.getImmediateSource();
        if (sourceEntity == null) {
            sourceEntity = source.getTrueSource();
        }
        if (sourceEntity != null) {
            // Spawn particles of particleType using position, velocity (scaled by damage amount)
            BallisticBloodMod.PROXY.sendMessageParticle(
                    entity.dimension, particleType,
                    CommonHelper.GetParticlePosition(entity, source),
                    CommonHelper.GetParticleVelocity(entity.getPositionVector(), source),
                    amount
            );
        }
    }

    public void sendMessageParticle(int dimension, int type, Vec3d position, Vec3d direction, float damage) {
        MessageParticleHandler.MessageParticle message =
                new MessageParticleHandler.MessageParticle(type, position, direction, damage);
        networkWrapperInstance.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, position.x, position.y, position.z, 128.0));
    }
}