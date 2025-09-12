package com.beckadam.splatterizer.handlers;


import com.beckadam.splatterizer.particles.ParticleTypeManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.beckadam.splatterizer.SplatterizerMod;
import org.apache.logging.log4j.Level;

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

    @Config.Ignore
    public static HashMap<String, ParticleConfig> particleConfigMap;
    @Config.Ignore
    public static HashMap<Integer, ParticleConfig> particleConfigIntMap;
    @Config.Ignore
    public static boolean needsTextureLoad = true;

    public static class ServerConfig {
        @Config.Comment("entity=type eg. minecraft:player=ENDER")
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

        @Config.Comment("Difficult to edit manually, but this defines all the splatter types and per-type values.\nThere will probably be a web UI for this at some point.")
        @Config.Name("Splatter Type List")
        public String[] particleConfig = new String[] {
                "{\"name\":\"BLOOD\",\"texture\":\"splatterizer:textures/particle/blood_particle.png\",\"size\":1," +
                        "\"gravity\":1,\"velocity\":1,\"spray_rate\":4,\"spray_velocity\":0.25,\"blend\":\"NORMAL\"}",
                "{\"name\":\"DUST\",\"texture\":\"splatterizer:textures/particle/dust_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_rate\":0,\"spray_velocity\":0.02,\"blend\":\"NORMAL\",\"lighting\":false}",
                "{\"name\":\"ASH\",\"texture\":\"splatterizer:textures/particle/ash_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_rate\":0,\"spray_velocity\":0.02,\"blend\":\"NORMAL\"}",
                "{\"name\":\"SLIME\",\"texture\":\"splatterizer:textures/particle/slime_particle.png\",\"size\":0.8," +
                        "\"gravity\":2,\"velocity\":1.2,\"spray_rate\":4,\"spray_velocity\":0.3," +
                        "\"blend\":\"ONE ONE_MINUS_SRC_ALPHA\",\"blend_op\":\"MAX\",\"lighting\":false," +
                        "\"color_multiplier\":2.0}",
                "{\"name\":\"ENDER\",\"texture\":\"splatterizer:textures/particle/ender_particle.png\",\"size\":1," +
                        "\"gravity\":1,\"velocity\":1,\"spray_rate\":4,\"spray_velocity\":0.25," +
                        "\"blend\":\"SRC_ALPHA SRC_COLOR\"}",
        };

    }

	public static class ClientConfig {

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nThis is the lifetime of the initial hit particle emitter.\nMake sure this is greater than or equal to the other lifetimes.")
        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 120*20;

        @Config.Comment("This doesn't work for all blend modes!")
        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 100*20;

        @Config.Name("Radius of splatter spread in quarter-circles")
        public float particleSpreadSize = 1.0f;

        @Config.Comment("Measured in blocks per second squared. 9.81 is Earth gravity. Multiplied by other values to get final value.")
        @Config.Name("Gravity multiplier for all particles")
        public float particleGravityBase = 1.0f;

        @Config.Comment("Measured in blocks. Multiplied by other values to get final value.")
        @Config.Name("Base size of particles")
        public float particleSize = 0.5f;

        @Config.Comment("Maximum multiplier to randomly offset projectile particle direction")
        @Config.Name("Particle spread variance")
        public float particleSpreadVariance = 1.0f;

        @Config.Comment("Size of particles when they hit a wall. Multiplied by other values to get final value.")
        @Config.Name("Decal scale")
        public float decalScale = 2.0f;

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Projectile particle velocity")
        public float projectileParticleVelocity = 0.25f;

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

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Spray particle size")
        public float sprayParticleSize = 1.0f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.")
        @Config.Name("Spray particle lifetime in ticks")
        public int sprayParticleLifetime = 3*20;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nNote that this doesn't work for all blend modes!")
        @Config.Name("Spray particle fade start in ticks")
        public int sprayParticleFadeStart = 20;

        @Config.Name("Enable/Disable splatter particles entirely")
        public boolean enableSplatterParticles = true;

        @Config.Comment("Projectile particles emitted is this number plus the damage times the particles per heart")
        @Config.Name("Number of projectile particles to emit for each hit")
        public int particleSpreadCount = 1;

        @Config.Comment("Absolute maximum number of projectile particles to spawn per hit")
        @Config.Name("Maximum projectile particles per splatter")
        public int particleSpreadMax = 24;

        @Config.Comment("Total number of spray particles to emit from the hit position")
        @Config.Name("Total number of spray particles to spawn per hit")
        public int subParticleTotal = 10;

        @Config.Comment("Projectile particles emitted is the spread count plus the damage times this value")
        @Config.Name("Extra projectile particles per heart of damage")
        public float extraParticlesPerHeartOfDamage = 0.5f;
    }

	@Mod.EventBusSubscriber(modid = SplatterizerMod.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(SplatterizerMod.MODID)) {
				ConfigManager.sync(SplatterizerMod.MODID, Config.Type.INSTANCE);
                ParseSplatterizerConfig();
			}
		}
	}

    public static class ParticleConfig {
        public final int type;
        public final String typeName;
        public final float velocity;
        public final float gravity;
        public final float size;
        public final ResourceLocation texture;
        public final String[] blendMode;
        public final boolean lighting;
        public final String blendOp;
        public final float emissionRate;
        public final float emissionVelocity;
        public final float colorMultiplier;
        public final float alphaMultiplier;

        public ParticleConfig(String config) throws JsonException {
            JsonObject json = new JsonParser().parse(config).getAsJsonObject();
            if (!JsonUtils.hasField(json, "name")) {
                throw new JsonException("missing name in particle config!");
            }
            if (!JsonUtils.hasField(json, "texture")) {
                throw new JsonException("missing texture in particle config!");
            }
            typeName = JsonUtils.getString(json, "name");
            String tex = JsonUtils.getString(json, "texture");
            if (!tex.contains(":")) {
                throw new JsonException("missing mod id in texture resource location!");
            }
            texture = new ResourceLocation(tex);
            if (JsonUtils.hasField(json, "size")) {
                size = JsonUtils.getFloat(json, "size");
            } else {
                size = 1.0f;
            }
            if (JsonUtils.hasField(json, "velocity")) {
                velocity = JsonUtils.getFloat(json, "velocity");
            } else {
                velocity = 1.0f;
            }
            if (JsonUtils.hasField(json, "gravity")) {
                gravity = JsonUtils.getFloat(json, "gravity");
            } else {
                gravity = 1.0f;
            }
            if (JsonUtils.hasField(json, "spray_velocity")) {
                emissionVelocity = JsonUtils.getFloat(json, "spray_velocity");
            } else {
                emissionVelocity = 1.0f;
            }
            if (JsonUtils.hasField(json, "spray_rate")) {
                emissionRate = JsonUtils.getFloat(json, "spray_rate");
            } else {
                emissionRate = 0.0f;
            }
            blendMode = new String[2];
            blendMode[0] = blendMode[1] = "NORMAL";
            if (JsonUtils.hasField(json, "blend_mode")) {
                String[] mode = JsonUtils.getString(json, "blend_mode").split(" ");
                if (mode.length == 1) {
                    blendMode[0] = blendMode[1] = mode[0];
                } else if (mode.length >= 2) {
                    blendMode[0] = mode[0];
                    blendMode[1] = mode[1];
                }
            } else {
                blendMode[0] = blendMode[1] = "NORMAL";
            }
            if (JsonUtils.hasField(json, "blend_op")) {
                blendOp = JsonUtils.getString(json, "blend_op");
            } else {
                blendOp = "ADD";
            }
            if (JsonUtils.hasField(json, "lighting")) {
                lighting = JsonUtils.getBoolean(json, "lighting");
            } else {
                lighting = true;
            }
            if (JsonUtils.hasField(json, "color_multiplier")) {
                colorMultiplier = JsonUtils.getFloat(json, "color_multiplier");
            } else {
                colorMultiplier = 1.0f;
            }
            if (JsonUtils.hasField(json, "alpha_multiplier")) {
                alphaMultiplier = JsonUtils.getFloat(json, "alpha_multiplier");
            } else {
                alphaMultiplier = 1.0f;
            }
            type = SplatterizerMod.particleTypes.add(typeName);
        }
    }

    public static void ParseSplatterizerConfig() {
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
        SplatterizerMod.particleTypes.init();
        for (String s : server.particleConfig) {
            try {
                ParticleConfig conf = new ParticleConfig(s);
                particleConfigMap.put(conf.typeName, conf);
                particleConfigIntMap.put(conf.type, conf);
            } catch (Exception err) {
                SplatterizerMod.LOGGER.log(Level.ERROR, "Failed to parse particle type config: \"" + s + "\"");
                continue;
            }
//            SplatterizerMod.LOGGER.log(Level.INFO, s);
        }
        if (server.entitySplatterTypeMap == null) {
            server.entitySplatterTypeMap = new HashMap<>();
        } else {
            server.entitySplatterTypeMap.clear();
        }
        for (String s : server.entitySplatterTypes) {
            String[] a = s.split("=", 2);
            if (a.length == 2) {
                int num = SplatterizerMod.particleTypes.get(a[1]);
                server.entitySplatterTypeMap.put(new ResourceLocation(a[0]), num);
//                SplatterizerMod.LOGGER.log(Level.INFO, a[0] + " = " + a[1]);
            } else {
                SplatterizerMod.LOGGER.log(Level.WARN, "Invalid splatter type mapping: \"" + s + "\"");
            }
        }
        needsTextureLoad = true;
    }
}