package com.beckadam.splatterizer.handlers;

import com.beckadam.splatterizer.particles.ParticleType;
import fermiumbooter.annotations.MixinConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.beckadam.splatterizer.SplatterizerMod;
import org.apache.logging.log4j.Level;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Config(modid = SplatterizerMod.MODID)
public class ForgeConfigHandler {
	
	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	public static class ServerConfig {
        @Config.Name("Entity splatter types")
        public String[] entitySplatterTypes = new String[] {
                "minecraft:skeleton=DUST",
                "minecraft:skeleton_horse=DUST",
                "minecraft:wither=ASH",
                "minecraft:wither_skeleton=ASH",
                "minecraft:slime=SLIME",
                "minecraft:enderman=ENDER",
                "minecraft:endermite=ENDER",
                "minecraft:shulker=ENDER",
        };

        @Config.Name("Default splatter type")
        public String entitySplatterTypeDefault = "BLOOD";

        @Config.Ignore
        public Map<ResourceLocation, String> entitySplatterTypeMap = null;
    }

	public static class ClientConfig {

        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 60*20;

        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 30*20;

        @Config.Name("Size of splatter spread")
        public float particleSpreadSize = 2.0f;

        @Config.Name("Size of each individual splatter particle in meters")
        public float particleSize = 0.2f;

        @Config.Name("Particle velocity multiplier")
        public float particleVelocityMultiplier = 1.0f;

        @Config.Name("Particle Spread Variance")
        public float particleSpreadVariance = 1.0f;


        @Config.Name("Enable/Disable splatter particles")
        public boolean enableSplatterParticles = true;

        @Config.Comment("Particles emitted is scaled by the damage of the attack and the health of the entity")
        @Config.Name("Number of particles to emit each time a splatter is triggered")
        public int particleSpreadCount = 7;

        @Config.Name("Maximum particles per splatter")
        public int particleSpreadMax = 31;

        @Config.Name("Sub-particles per splatter particle emission")
        public int particleSubCount = 3;

        @Config.Name("Ticks per sub-particle emission")
        public int particleSubEmissionRate = 4;

        @Config.Name("Maximum sub-particles per splatter particle")
        public int particleSubMax = 15;

        @Config.Name("Extra particles per heart of damage")
        public float extraParticlesPerHeartOfDamage = 0.25f;

        @Config.Name("Blood Sub-particle base velocity")
        public float bloodSubParticleBaseVelocity = 0.2f;

        @Config.Name("Ash Sub-particle base velocity")
        public float ashSubParticleBaseVelocity = 0.05f;

        @Config.Name("Dust Sub-particle base velocity")
        public float dustSubParticleBaseVelocity = 0.025f;

        @Config.Name("Slime Sub-particle base velocity")
        public float slimeSubParticleBaseVelocity = 0.3f;

        @Config.Name("Ender splatter Sub-particle base velocity")
        public float enderSubParticleBaseVelocity = 0.1f;


    }

	@Mod.EventBusSubscriber(modid = SplatterizerMod.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(SplatterizerMod.MODID)) {
				ConfigManager.sync(SplatterizerMod.MODID, Config.Type.INSTANCE);
                ParseSplatterTypes();
			}
		}
	}

    public static void ParseSplatterTypes() {
        if (server.entitySplatterTypeMap == null) {
            server.entitySplatterTypeMap = new HashMap<>();
        } else {
            server.entitySplatterTypeMap.clear();
        }
        for (String s : server.entitySplatterTypes) {
            String[] a = s.split("=", 2);
            if (a.length >= 2) {
                server.entitySplatterTypeMap.put(new ResourceLocation(a[0]), a[1]);
                SplatterizerMod.LOGGER.log(Level.INFO, a[0] + " = " + a[1]);
            } else {
                SplatterizerMod.LOGGER.log(Level.WARN, "Invalid splatter type mapping: \"" + s + "\"");
            }
        }
    }
}