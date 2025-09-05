package com.beckadam.splatterizer.proxy;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CommonProxy {

    public void SpawnParticle(Particle particle) {}
    public void AttackEntityFromHandler(DamageSource source, float amount) {}

}