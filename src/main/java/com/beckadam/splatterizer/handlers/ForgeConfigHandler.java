package com.beckadam.splatterizer.handlers;

import fermiumbooter.annotations.MixinConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.beckadam.splatterizer.SplatterizerMod;

@Config(modid = SplatterizerMod.MODID)
public class ForgeConfigHandler {
	
	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	public static class ServerConfig {

    }

	public static class ClientConfig {

        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 60*20;

        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 30*20;

        @Config.Name("Size of splatter spread relative to velocity")
        public float particleSpreadSize = 2.0f;

        @Config.Name("Size of each individual splatter particle in meters")
        public float particleSize = 0.1f;

        @Config.Name("Particle velocity multiplier")
        public float particleVelocityMultiplier = 1.0f;

        @Config.Name("Particle Spread Variance")
        public float particleSpreadVariance = 1.0f;


        @Config.Name("Enable/Disable splatter particles")
        public boolean enableSplatterParticles = true;

        @Config.Name("Number of particles to emit each time a splatter is triggered")
        public int particleSpreadCount = 15;


    }

	@Mod.EventBusSubscriber(modid = SplatterizerMod.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(SplatterizerMod.MODID)) {
				ConfigManager.sync(SplatterizerMod.MODID, Config.Type.INSTANCE);
			}
		}
	}
}