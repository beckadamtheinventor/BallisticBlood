package com.beckadam.splatterizer.handlers;

import com.beckadam.splatterizer.SplatterizerMod;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AttackEntityFromHandler {
    @SubscribeEvent
    public static void LivingDamageEvent(LivingDamageEvent event) {
        SplatterizerMod.PROXY.AttackEntityFromHandler(event.getEntity(), event.getSource(), event.getAmount());
    }

    public static void init(EventBus eventBus) {
        eventBus.register(AttackEntityFromHandler.class);
    }
}
