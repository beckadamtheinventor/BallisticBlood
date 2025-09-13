package com.beckadam.ballisticblood.handlers;

import com.beckadam.ballisticblood.BallisticBloodMod;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AttackEntityFromHandler {
    @SubscribeEvent
    public static void LivingDamageEvent(LivingDamageEvent event) {
        BallisticBloodMod.PROXY.AttackEntityFromHandler(event.getEntity(), event.getSource(), event.getAmount());
    }

    public static void register(EventBus eventBus) {
        eventBus.register(AttackEntityFromHandler.class);
    }
}
