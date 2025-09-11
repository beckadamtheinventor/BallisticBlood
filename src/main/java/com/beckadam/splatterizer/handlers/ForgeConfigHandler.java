package com.beckadam.splatterizer.handlers;


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

        @Config.Comment("Difficult to edit manually, but this defines all the available splatter types and various per-type values.\nThere will probably be a web UI for this at some point.")
        @Config.Name("Splatter Type List")
        public String[] particleConfig = new String[] {
                "BLOOD=textures/particle/blood_particle.png,1,1,1,4,0.25,MULTIPLY,,MIN",
                "DUST=textures/particle/dust_particle.png,1,0.1,0.4,0,0.02,NORMAL,,,UNLIT",
                "ASH=textures/particle/ash_particle.png,1,0.1,0.4,0,0.02,NORMAL",
                "SLIME=textures/particle/slime_particle.png,0.8,2,1.2,4,0.3,ONE,ONE_MINUS_SRC_COLOR,MAX,UNLIT",
                "ENDER=textures/particle/ender_particle.png,1,1,1,4,0.25,SRC_ALPHA,SRC_COLOR",
        };

    }

	public static class ClientConfig {

        @Config.Comment("Measured in ticks. There are 20 ticks in a second. This is the lifetime of the initial hit particle emitter")
        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 120*20;

        @Config.Comment("This doesn't work for all blend modes!")
        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 100*20;

        @Config.Name("Radius of splatter spread in quarter-circles")
        public float particleSpreadSize = 1.0f;

        @Config.Comment("Measured in blocks per second squared. Default value (9.81) is Earth gravity. Multiplied by other values to get final value.")
        @Config.Name("Gravity multiplier for all particles")
        public float particleGravityBase = 9.81f;

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

        @Config.Comment("Measured in ticks. There are 20 ticks in a second.")
        @Config.Name("Projectile particle lifetime in ticks")
        public int projectileParticleLifetime = 15*20;

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
        public final float velocity;
        public final float gravity;
        public final float size;
        public final String typeName;
        public final ResourceLocation texture;
        public final int type;
        public final String[] blendMode;
        public final int impactEmissionRate;
        public final float impactEmissionVelocity;

        public ParticleConfig(
                int typeNum, String typeName, String tex, float size, float gravity, float velocity,
                int impactEmissionRate, float impactEmissionVelocity, String blendMode
        ) {
            this.type = typeNum;
            this.typeName = typeName;
            this.texture = new ResourceLocation(SplatterizerMod.MODID, tex);
            this.gravity = gravity;
            this.velocity = velocity;
            this.size = size;
            this.blendMode = new String[] {"","","",""};
            this.impactEmissionRate = impactEmissionRate;
            this.impactEmissionVelocity = impactEmissionVelocity;
            String[] modes = blendMode.split(",");
            if (modes.length == 1) {
                this.blendMode[0] = this.blendMode[1] = modes[0];
            } else if (modes.length >= 2) {
                this.blendMode[0] = modes[0];
                if (modes[1].isEmpty()) {
                    this.blendMode[1] = modes[0];
                } else {
                    this.blendMode[1] = modes[1];
                }
                if (modes.length >= 3) {
                    this.blendMode[2] = modes[2];
                }
                if (modes.length >= 4) {
                    this.blendMode[3] = modes[3];
                }
            }
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
            String[] a = s.split("=", 2);
            String[] a2 = null;
            if (a.length == 2) {
                a2 = a[1].split(",", 7);
            }
            if (a.length != 2 || a2.length < 7) {
                SplatterizerMod.LOGGER.log(Level.WARN, "Invalid particle type config: \"" + s + "\"");
                continue;
            }
            int num = SplatterizerMod.particleTypes.add(a[0]);
            try {
                ParticleConfig conf = new ParticleConfig(
                        num, a[0], a2[0], Float.parseFloat(a2[1]), Float.parseFloat(a2[2]), Float.parseFloat(a2[3]),
                        Integer.parseInt(a2[4]), Float.parseFloat(a2[5]), a2[6]
                );
                particleConfigMap.put(a[0], conf);
                particleConfigIntMap.put(num, conf);
            } catch (Exception err) {
                SplatterizerMod.LOGGER.log(Level.ERROR, "Failed to parse particle type config: \"" + s + "\"");
                continue;
            }
            SplatterizerMod.LOGGER.log(Level.INFO, s);
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
                SplatterizerMod.LOGGER.log(Level.INFO, a[0] + " = " + a[1]);
            } else {
                SplatterizerMod.LOGGER.log(Level.WARN, "Invalid splatter type mapping: \"" + s + "\"");
            }
        }
        needsTextureLoad = true;
    }
}