package com.beckadam.ballisticblood.handlers;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.beckadam.ballisticblood.BallisticBloodMod;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

@Config(modid = BallisticBloodMod.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

    @Config.Ignore
    public static HashMap<String, ParticleConfig> particleConfigMap;
    @Config.Ignore
    public static HashMap<Integer, ParticleConfig> particleConfigIntMap;
    @Config.Ignore
    public static boolean needsTextureLoad = true;



    public static class ServerConfig {
        @Config.Comment("entity=type eg. minecraft:pig=BLOOD")
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

        @Config.Comment("Splatter type for entities not specified")
        @Config.Name("Default splatter type")
        public String entitySplatterTypeDefault = "BLOOD";

        @Config.Ignore
        public Map<ResourceLocation, Integer> entitySplatterTypeMap = null;

        @Config.Comment("Difficult to edit manually. This defines all the splatter types and per-type values.\nThere will probably be a web UI for this at some point.")
        @Config.Name("Splatter Type List")
        public String[] particleConfig = new String[] {
                "{\"name\":\"BLOOD\",\"texture\":\"ballisticblood:textures/particle/blood_particle.png\",\"size\":1," +
                        "\"gravity\":1,\"velocity\":1,\"spray_velocity\":0.25,\"blend\":\"NORMAL\"}",
                "{\"name\":\"DUST\",\"texture\":\"ballisticblood:textures/particle/dust_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_velocity\":0.02,\"blend\":\"NORMAL\",\"lighting\":false," +
                        "\"tiling\":\"8,1\"}",
                "{\"name\":\"ASH\",\"texture\":\"ballisticblood:textures/particle/dust_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_velocity\":0.02,\"blend\":\"NORMAL\",\"lighting\":false," +
                        "\"tiling\":\"8,1\",\"color\":[0.1,0.1,0.1]}",
                "{\"name\":\"SLIME\",\"texture\":\"ballisticblood:textures/particle/slime_particle.png\",\"size\":0.8," +
                        "\"gravity\":2,\"velocity\":1.2,\"spray_velocity\":0.3," +
                        "\"blend\":\"ONE ONE_MINUS_SRC_ALPHA\",\"lighting\":false}",
                "{\"name\":\"ENDER\",\"texture\":\"ballisticblood:textures/particle/ender_particle.png\",\"size\":1," +
                        "\"gravity\":1,\"velocity\":1,\"spray_velocity\":0.25," +
                        "\"blend\":\"NORMAL\"}",
        };

    }

	public static class ClientConfig {
        @Config.Comment("Ensures only this many projectile particles will ever be drawn at a time.\nOlder particles will be replaced with newer ones.")
        @Config.Name("Maximum projectile particles")
        public int maximumProjectileParticles = 200;

        @Config.Comment("Experimental method to reduce the likelihood of decal particles being partially midair")
        @Config.Name("Enable experimental overhang clipping method")
        public boolean enableExperimentalOverhangClipping = true;

        @Config.Comment("The number of vertices of a decal quad to allow midair before making the particle fall")
        @Config.Name("Floating vertex fall threshold")
        @Config.RangeInt(min=0, max=4)
        public int floatingVertexFallThreshold = 1;

        @Config.Comment("(advanced) set the surface offset multiplier for decal particles landed on surfaces")
        @Config.Name("Decal surface offset mutliplier")
        public float decalSurfaceOffsetMultiplier = 0.001f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nThis is the lifetime of the initial hit particle emitter.\nMake sure this is greater than or equal to the other lifetimes.")
        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 120*20;

        @Config.Comment("Measured in blocks.")
        @Config.Name("Maximum distance to render splatter particles at")
        public double particleRenderDistance = 20.0;

        @Config.Ignore
        public double particleRenderDistanceCubed = 1.0;

        @Config.Name("Radius of projectile spread in quarter-circles")
        public float particleSpreadSize = 1.0f;

        @Config.Comment("Measured in blocks per second. 9.81 is Earth gravity. Multiplied by other values to get final value.")
        @Config.Name("Gravity multiplier for all particles")
        public float particleGravityBase = 0.981f;

        @Config.Comment("Measured in blocks. Multiplied by other values to get final value.")
        @Config.Name("Base size of particles")
        public float particleSize = 0.5f;

        @Config.Comment("Maximum multiplier to randomly offset projectile particle direction")
        @Config.Name("Projectile spread variance")
        public float particleSpreadVariance = 1.0f;

        @Config.Comment("Size of particles when they hit a wall. Multiplied by other values to get final value.")
        @Config.Name("Decal scale")
        public float decalScale = 2.0f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Projectile particle velocity")
        public float projectileParticleVelocity = 0.75f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Projectile particle gravity")
        public float projectileParticleGravity = 1.0f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Projectile particle size")
        public float projectileParticleSize = 1.0f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nNote that this lifetime is respected for decal particles produced by projectiles landing as well.")
        @Config.Name("Projectile particle lifetime in ticks")
        public int projectileParticleLifetime = 120*20;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nNote that this doesn't work for all blend modes!")
        @Config.Name("Projectile particle fade start in ticks")
        public int projectileParticleFadeStart = 100*20;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Spray particle velocity")
        public float sprayParticleVelocity = 0.1f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Spray particle gravity")
        public float sprayParticleGravity = 0.0f;

        @Config.Comment("If this is false, no splatter particles will be rendered!")
        @Config.Name("Enable splatter particles")
        public boolean enableSplatterParticles = true;

        @Config.Comment("Projectile particles emitted is this number plus the damage times the particles per heart.\nNote that this value can be negative to increase the minimum damage required to cause projectile particles.")
        @Config.Name("Additional projectile particles to spawn per heart of damage")
        public float projectileParticlesPerHeart = 1.0f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Spray particle size")
        public float sprayParticleSize = 1.0f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.")
        @Config.Name("Spray particle lifetime in ticks")
        public int sprayParticleLifetime = 3*20;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nNote that this doesn't work for all blend modes!")
        @Config.Name("Spray particle fade start in ticks")
        public int sprayParticleFadeStart = 40;

        @Config.Comment("Base number of projectile particles to emit from the hit position")
        @Config.Name("Base number of projectile particles to spawn per hit")
        public float projectileParticleBase = 1.0f;

        @Config.Comment("Maximum number of projectile particles to spawn per hit")
        @Config.Name("Maximum projectile particles per hit")
        public int projectileParticleMax = 24;

        @Config.Comment("Minimum number of projectile particles to spawn per hit")
        @Config.Name("Minimum projectile particles per hit")
        public int projectileParticleMin = 1;

        @Config.Comment("Minimum number of hearts to spawn projectile particles")
        @Config.Name("Minimum damage required to spawn projectile particles")
        public float projectileParticleMinDamage = 2.0f;

        @Config.Comment("Base number of spray particles to emit from the hit position")
        @Config.Name("Base number of spray particles to spawn per hit")
        public float sprayParticleBase = 10.0f;

        @Config.Comment("Additional spray particles to emit from the hit position")
        @Config.Name("Additional spray particles to spawn per heart of damage")
        public float sprayParticlePerHeart = 1.0f;

        @Config.Comment("Maximum spray particles to emit from the hit position")
        @Config.Name("Maximum number of spray particles to spawn per hit")
        public int sprayParticleMax = 10;

        @Config.Comment("Minimum spray particles to emit from the hit position")
        @Config.Name("Minimum number of spray particles to spawn per hit")
        public int sprayParticleMin = 10;

        @Config.Comment("Spray particles emitted is the spread count plus the damage times this value")
        @Config.Name("Minimum damage required to spawn spray particles")
        public float sprayParticleMinDamage = 2.0f;
    }

	@Mod.EventBusSubscriber(modid = BallisticBloodMod.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(BallisticBloodMod.MODID)) {
				ConfigManager.sync(BallisticBloodMod.MODID, Config.Type.INSTANCE);
                ParseConfig();
			}
		}
	}

    public static void ParseConfig() {
        if (particleConfigMap == null) {
            particleConfigMap = new HashMap<>();
        } else {
            particleConfigMap.clear();
        }
        if (particleConfigIntMap == null) {
            particleConfigIntMap = new HashMap<>();
        } else {
            particleConfigIntMap.clear();
        }
        client.particleRenderDistanceCubed = client.particleRenderDistance*client.particleRenderDistance*client.particleRenderDistance;
        BallisticBloodMod.particleTypes.init();
        StringBuilder loaded_types = new StringBuilder();
        for (String s : server.particleConfig) {
            try {
                ParticleConfig conf = new ParticleConfig(s);
                particleConfigMap.put(conf.typeName, conf);
                particleConfigIntMap.put(conf.type, conf);
                BallisticBloodMod.LOGGER.log(Level.INFO, s);
                loaded_types.append(conf.typeName).append(", ");
            } catch (Exception err) {
                BallisticBloodMod.LOGGER.log(Level.ERROR, "Failed to parse particle type config: \"" + s + "\"");
            }
        }
        loaded_types.delete(loaded_types.length()-2, loaded_types.length());
        BallisticBloodMod.LOGGER.log(Level.INFO, "types: " + loaded_types);
        if (server.entitySplatterTypeMap == null) {
            server.entitySplatterTypeMap = new HashMap<>();
        } else {
            server.entitySplatterTypeMap.clear();
        }
        for (String s : server.entitySplatterTypes) {
            String[] a = s.split("=", 2);
            if (a.length == 2 && a[0] != null && a[1] != null) {
                int num = BallisticBloodMod.particleTypes.get(a[1]);
                server.entitySplatterTypeMap.put(new ResourceLocation(a[0]), num);
                BallisticBloodMod.LOGGER.log(Level.INFO, a[0] + " = " + a[1]);
            } else {
                BallisticBloodMod.LOGGER.log(Level.WARN, "Invalid splatter type mapping: \"" + s + "\"");
            }
        }
        needsTextureLoad = true;
    }
}