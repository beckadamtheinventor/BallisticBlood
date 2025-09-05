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

	@MixinConfig(name = SplatterizerMod.MODID) //Needed on config classes that contain MixinToggles for those mixins to be added
	public static class ServerConfig {
//		@Config.Comment("Enable Entity Hit Mixin")
//		@Config.Name("Enable Entity Hit Mixin")
//		@MixinConfig.MixinToggle(earlyMixin = "mixins.splatterizer.vanilla.json", defaultValue = true)
//		public boolean enableEntityHitMixin = true;
    }

	public static class ClientConfig {

        @Config.Comment("Enable/disable splatter particles entirely")
        @Config.Name("Enable/Disable splatter particles")
        public boolean enableSplatterParticles = true;

        @Config.Comment("Lifetime of splatter particles in ticks")
        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 5*60*20;

        @Config.Comment("Splatter particle fade start time in ticks")
        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 4*60*20;

        @Config.Comment("Number of particles to emit each time a splatter is triggered")
        @Config.Name("Number of particles to emit each time a splatter is triggered")
        public int particleSpreadCount = 15;

        @Config.Comment("Size of splatter spread in meters")
        @Config.Name("Size of splatter spread in meters")
        public double particleSpreadSize = 0.05;

        @Config.Comment("Size of each individual splatter particle in meters")
        @Config.Name("Size of each individual splatter particle in meters")
        public float particleSize = 0.05f;
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