package com.beckadam.splatterizer.proxy;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.helpers.ParticleHelper;
import com.beckadam.splatterizer.helpers.ParticleClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class ClientProxy extends CommonProxy {

    @Override
    public void LoadTextures() {
        ForgeConfigHandler.ParseSplatterizerConfig();
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        for (ForgeConfigHandler.ParticleConfig cfg : ForgeConfigHandler.particleConfigIntMap.values()) {
            tm.loadTexture(cfg.texture, new SimpleTexture(cfg.texture));
        }
    }
    @Override
    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "ClientProxy.AttackEntityFromHandler");
        int particleType = ParticleHelper.GetParticleTypeForEntity(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        Entity sourceEntity = source.getImmediateSource();
        if (sourceEntity == null) {
            sourceEntity = source.getTrueSource();
        }
        if (sourceEntity != null) {
            ParticleClientHelper.splatter(
                    particleType,
                    ParticleHelper.GetParticlePosition(entity, source),
                    ParticleHelper.GetParticleVelocity(entity.getPositionVector(), source),
                    amount
            );
        }
    }

    @Override
    public void sendMessageParticle(int dimension, int type, Vec3d position, Vec3d direction, float damage) {}

}