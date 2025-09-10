package com.beckadam.splatterizer.handlers;

import com.beckadam.splatterizer.render.SplatterParticleRenderer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderWorldLastEventHandler {
    private static boolean loaded = false;
    @SubscribeEvent
    public static void RenderWorldLastEvent(RenderWorldLastEvent event) {
        if (!loaded) {
            SplatterParticleRenderer.init();
            loaded = true;
        }
        SplatterParticleRenderer.renderParticles(event.getPartialTicks());
    }
    public static void register(EventBus eventBus) {
        eventBus.register(RenderWorldLastEventHandler.class);
    }

}
