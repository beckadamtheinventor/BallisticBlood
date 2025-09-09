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
        public Map<ResourceLocation, Integer> entitySplatterTypeMap = null;

        @Config.Comment("name=texture.png,size,gravity,velocity,blendMode,impactEmissionRate,projectileEmissionRate,decalEmissionRate,emissionVelocity")
        @Config.Name("Particle Configuration")
        public String[] particleConfig = new String[] {
                "BLOOD=textures/particle/blood_particle.png,1,1,1,MULTIPLY,4,0,4,1",
                "DUST=textures/particle/dust_particle.png,1,0.2,0.4,NORMAL,8,0,0,0.1",
                "ASH=textures/particle/ash_particle.png,1,0.2,0.4,NORMAL,8,0,0,0.1",
                "SLIME=textures/particle/slime_particle.png,1,1,1.2,LIGHTEN,4,0,4,1",
                "ENDER=textures/particle/ender_particle.png,1,1,1,SRC_ALPHA:SRC_COLOR,4,0,4,0.5",
        };

    }

	public static class ClientConfig {

        @Config.Name("Lifetime of splatter particles in ticks")
        public int particleLifetime = 120*20;

        @Config.Name("Splatter particle fade start time in ticks")
        public int particleFadeStart = 90*20;

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
        public int particleSpreadCount = 8;

        @Config.Name("Maximum primary particles per splatter")
        public int particleSpreadMax = 24;

        @Config.Name("Maximum secondary particles per splatter")
        public int particleSubMax = 31;

        @Config.Name("Extra particles per heart of damage")
        public float extraParticlesPerHeartOfDamage = 0.25f;

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
        public final String blendMode;
        public final int impactEmissionRate;
        public final int projectileEmissionRate;
        public final int decalEmissionRate;
        public final float emissionVelocity;

        public ParticleConfig(
                int typeNum, String typeName, String tex, float size, float gravity, float velocity, String blendMode,
                int impactEmissionRate, int projectileEmissionRate, int decalEmissionRate, float emissionVelocity
        ) {
            this.type = typeNum;
            this.typeName = typeName;
            this.texture = new ResourceLocation(SplatterizerMod.MODID, tex);
            this.gravity = gravity;
            this.velocity = velocity;
            this.size = size;
            this.blendMode = blendMode;
            this.impactEmissionRate = impactEmissionRate;
            this.projectileEmissionRate = projectileEmissionRate;
            this.decalEmissionRate = decalEmissionRate;
            this.emissionVelocity = emissionVelocity;
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
                a2 = a[1].split(",");
            }
            if (a.length != 2 || a2.length < 9) {
                SplatterizerMod.LOGGER.log(Level.WARN, "Invalid particle type config: \"" + s + "\"");
                continue;
            }
            int num = SplatterizerMod.particleTypes.add(a[0]);
            ParticleConfig conf = new ParticleConfig(
                    num, a[0], a2[0], Float.parseFloat(a2[1]), Float.parseFloat(a2[2]), Float.parseFloat(a2[3]), a2[4],
                    Integer.parseInt(a2[5]), Integer.parseInt(a2[6]), Integer.parseInt(a2[7]), Float.parseFloat(a2[8])
            );
            particleConfigMap.put(a[0], conf);
            particleConfigIntMap.put(num, conf);
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
    }
}