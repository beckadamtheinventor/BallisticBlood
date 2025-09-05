package com.beckadam.splatterizer.handlers;

import com.beckadam.splatterizer.SplatterizerMod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingEntityHurtHandler {
    @SubscribeEvent
    public static void livingHurtEvent(LivingHurtEvent event) {
        SplatterizerMod.PROXY.AttackEntityFromHandler(event.getSource(), event.getAmount());
    }

    public static void init(EventBus eventBus) {
        eventBus.register(LivingEntityHurtHandler.class);
    }
}
