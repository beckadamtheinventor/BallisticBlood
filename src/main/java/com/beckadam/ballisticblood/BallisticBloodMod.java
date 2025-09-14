package com.beckadam.ballisticblood;

import com.beckadam.ballisticblood.handlers.AttackEntityFromHandler;
import com.beckadam.ballisticblood.handlers.BallisticBloodCommandHandler;
import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.particles.ParticleManager;
import com.beckadam.ballisticblood.particles.ParticleTypeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.beckadam.ballisticblood.proxy.CommonProxy;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = BallisticBloodMod.MODID, version = BallisticBloodMod.VERSION, name = BallisticBloodMod.NAME)
public class BallisticBloodMod {
    public static final String MODID = "ballisticblood";
    public static final String VERSION = "0.0.2";
    public static final String NAME = "Ballistic Blood";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;
    public static final ParticleTypeManager particleTypes = new ParticleTypeManager();

    @SidedProxy(clientSide = "com.beckadam.ballisticblood.proxy.ClientProxy", serverSide = "com.beckadam.ballisticblood.proxy.CommonProxy")
    public static CommonProxy PROXY;
	
	@Instance(MODID)
	public static BallisticBloodMod instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AttackEntityFromHandler.register(MinecraftForge.EVENT_BUS);
//        if (event.getSide() == Side.CLIENT) {
//            RenderWorldLastEventHandler.register(MinecraftForge.EVENT_BUS);
//        }
        PROXY.init();
        if (event.getSide() == Side.CLIENT) {
            BallisticBloodCommandHandler.register(MinecraftForge.EVENT_BUS);
            ParticleManager.register(MinecraftForge.EVENT_BUS);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ForgeConfigHandler.ParseSplatterizerConfig();
        BallisticBloodMod.PROXY.LoadTextures();
        completedLoading = true;
    }
}