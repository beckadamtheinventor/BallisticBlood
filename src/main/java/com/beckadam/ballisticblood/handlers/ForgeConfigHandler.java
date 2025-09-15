package com.beckadam.ballisticblood.handlers;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.JsonUtils;
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
                        "\"gravity\":1,\"velocity\":1,\"spray_rate\":4,\"spray_velocity\":0.25,\"blend\":\"SRC_ALPHA SRC_COLOR\"}",
                "{\"name\":\"DUST\",\"texture\":\"ballisticblood:textures/particle/dust_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_rate\":0,\"spray_velocity\":0.02,\"blend\":\"NORMAL\",\"lighting\":false}",
                "{\"name\":\"ASH\",\"texture\":\"ballisticblood:textures/particle/ash_particle.png\",\"size\":1," +
                        "\"gravity\":0.1,\"velocity\":0.4,\"spray_rate\":0,\"spray_velocity\":0.02,\"blend\":\"NORMAL\",\"lighting\":false}",
                "{\"name\":\"SLIME\",\"texture\":\"ballisticblood:textures/particle/slime_particle.png\",\"size\":0.8," +
                        "\"gravity\":2,\"velocity\":1.2,\"spray_rate\":4,\"spray_velocity\":0.3," +
                        "\"blend\":\"ONE ONE_MINUS_SRC_ALPHA\",\"blend_op\":\"MAX\",\"lighting\":false," +
                        "\"color_multiplier\":2.0}",
                "{\"name\":\"ENDER\",\"texture\":\"ballisticblood:textures/particle/ender_particle.png\",\"size\":1," +
                        "\"gravity\":1,\"velocity\":1,\"spray_rate\":4,\"spray_velocity\":0.25," +
                        "\"blend\":\"SRC_ALPHA SRC_COLOR\"}",
        };

    }

	public static class ClientConfig {
        @Config.Comment("Ensures only this many projectile particles will ever be drawn at a time.\nOlder particles will be replaced with newer ones.")
        @Config.Name("Maximum projectile particles")
        public int maximumProjectileParticles = 200;

        @Config.Comment("Experimental method to reduce the likelihood of decal particles being partially midair")
        @Config.Name("Enable experimental overhang clipping method")
        public boolean enableExperimentalOverhangClipping = false;

        @Config.Comment("The number of vertices of a decal quad to allow midair before making the particle fall")
        @Config.Name("Floating vertex fall threshold")
        @Config.RangeInt(min=0, max=4)
        public int floatingVertexFallThreshold = 1;

        @Config.Comment("(advanced) set the surface offset multiplier for decal particles landed on surfaces")
        @Config.Name("Decal surface offset mutliplier")
        public float decalSurfaceOffsetMultiplier = 0.01f;

        @Config.Comment("Rough distance to move decal textures when considered midair. Set to 0 to disable this mechanic.")
        @Config.Name("Decal pop-off multiplier")
        public float particlePopOffMultiplier = 0.1f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nThis is the lifetime of the initial hit particle emitter.\nMake sure this is greater than or equal to the other lifetimes.")
        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 120*20;

        @Config.Comment("Measured in blocks.")
        @Config.Name("Maximum distance to render splatter particles at")
        public double particleRenderDistance = 20.0;

        @Config.Ignore
        public double particleRenderDistanceCubed = 1.0;

        @Config.Name("Radius of splatter spread in quarter-circles")
        public float particleSpreadSize = 1.0f;

        @Config.Comment("Measured in blocks per second. 9.81 is Earth gravity. Multiplied by other values to get final value.")
        @Config.Name("Gravity multiplier for all particles")
        public float particleGravityBase = 0.981f;

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

        @Config.Comment("Multiplied by other values to get final value.")
        @Config.Name("Spray particle size")
        public float sprayParticleSize = 1.0f;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.")
        @Config.Name("Spray particle lifetime in ticks")
        public int sprayParticleLifetime = 3*20;

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.\nNote that this doesn't work for all blend modes!")
        @Config.Name("Spray particle fade start in ticks")
        public int sprayParticleFadeStart = 20;

        @Config.Comment("If this is false, no splatter particles will be rendered!")
        @Config.Name("Enable splatter particles")
        public boolean enableSplatterParticles = true;

        @Config.Comment("Projectile particles emitted is this number plus the damage times the particles per heart.\nNote that this value can be negative to increase the minimum damage required to cause splatter particles.")
        @Config.Name("Number of projectile particles to emit for each hit")
        public int particleSpreadCount = -1;

        @Config.Comment("Absolute maximum number of projectile particles to spawn per hit")
        @Config.Name("Maximum projectile particles per splatter")
        public int particleSpreadMax = 24;

        @Config.Comment("Total number of spray particles to emit from the hit position")
        @Config.Name("Total number of spray particles to spawn per hit")
        public int sprayParticleCount = 10;

        @Config.Comment("Projectile particles emitted is the spread count plus the damage times this value")
        @Config.Name("Extra projectile particles per heart of damage")
        public float extraParticlesPerHeartOfDamage = 1.0f;
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

    public static class ParticleConfig {
        public final int type;
        public final String typeName;
        public final float velocity;
        public final float sprayVelocity;
        public final float gravity;
        public final float size;
        public final ResourceLocation texture;
        public final String[] blendMode;
        public final boolean lighting;
        public final String blendOp;
        public final float colorMultiplier;
        public final float alphaMultiplier;

        public ParticleConfig(String config) throws RuntimeException {
            JsonObject json = new JsonParser().parse(config).getAsJsonObject();
            if (!JsonUtils.hasField(json, "name")) {
                throw new RuntimeException("missing name in particle config!");
            }
            if (!JsonUtils.hasField(json, "texture")) {
                throw new RuntimeException("missing texture in particle config!");
            }
            typeName = JsonUtils.getString(json, "name");
            String tex = JsonUtils.getString(json, "texture");
            if (!tex.contains(":")) {
                throw new RuntimeException("missing mod id in texture resource location!");
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
                sprayVelocity = JsonUtils.getFloat(json, "spray_velocity");
            } else {
                sprayVelocity = 1.0f;
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
            type = BallisticBloodMod.particleTypes.add(typeName);
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
        for (String s : server.particleConfig) {
            try {
                ParticleConfig conf = new ParticleConfig(s);
                particleConfigMap.put(conf.typeName, conf);
                particleConfigIntMap.put(conf.type, conf);
                BallisticBloodMod.LOGGER.log(Level.INFO, s);
            } catch (Exception err) {
                BallisticBloodMod.LOGGER.log(Level.ERROR, "Failed to parse particle type config: \"" + s + "\"");
            }
        }
        if (server.entitySplatterTypeMap == null) {
            server.entitySplatterTypeMap = new HashMap<>();
        } else {
            server.entitySplatterTypeMap.clear();
        }
        for (String s : server.entitySplatterTypes) {
            String[] a = s.split("=", 2);
            if (a.length == 2) {
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