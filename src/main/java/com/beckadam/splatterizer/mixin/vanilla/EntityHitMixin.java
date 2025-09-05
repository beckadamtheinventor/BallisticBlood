package com.beckadam.splatterizer.mixin.vanilla;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import com.beckadam.splatterizer.SplatterizerMod;

@Mixin(Entity.class)
public abstract class EntityHitMixin {

    @Shadow
    public abstract boolean getIsInvulnerable();

    @Inject(
            method = "attackEntityFrom",
            at = @At("HEAD"),
            order = InjectionInfo.InjectorOrder.LATE
    )
    public void splatterizer_vanillaEntity_AttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!getIsInvulnerable()) {
            SplatterizerMod.PROXY.AttackEntityFromHandler(source, amount);
        }
    }
}
