package com.beckadam.ballisticblood.proxy;

import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.CommonHelper;
import com.beckadam.ballisticblood.helpers.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class ClientProxy extends CommonProxy {

    // TextureManager.getTexture returns null if the texture isn't found
    // according to the texture bind function, so I'm suppressing the warning
    // it produces
    @SuppressWarnings("ConstantValue")
    @Override
    public void LoadTextures() {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        for (ForgeConfigHandler.ParticleConfig cfg : ForgeConfigHandler.particleConfigIntMap.values()) {
            if (tm.getTexture(cfg.texture) == null) {
                tm.loadTexture(cfg.texture, new SimpleTexture(cfg.texture));
            }
        }
        ForgeConfigHandler.needsTextureLoad = false;
    }
    @Override
    public void AttackEntityFromHandler(Entity entity, DamageSource source, float amount) {
//        SplatterizerMod.LOGGER.log(Level.INFO, "ClientProxy.AttackEntityFromHandler");
        int particleType = CommonHelper.GetParticleTypeForEntity(entity);
        // Spawn particles of particleType using position, velocity (scaled by damage amount)
        ClientHelper.splatter(
                particleType,
                CommonHelper.GetParticlePosition(entity, source),
                CommonHelper.GetParticleVelocity(entity.getPositionVector(), source),
                amount
        );
    }

    @Override
    public void sendMessageParticle(int dimension, int type, Vec3d position, Vec3d direction, float damage) {}

}