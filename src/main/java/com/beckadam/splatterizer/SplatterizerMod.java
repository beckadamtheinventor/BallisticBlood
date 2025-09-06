package com.beckadam.splatterizer;

import com.beckadam.splatterizer.handlers.LivingEntityHurtHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.beckadam.splatterizer.handlers.ModRegistry;
import com.beckadam.splatterizer.proxy.CommonProxy;

@Mod(modid = SplatterizerMod.MODID, version = SplatterizerMod.VERSION, name = SplatterizerMod.NAME, dependencies = "required-after:fermiumbooter")
public class SplatterizerMod {
    public static final String MODID = "splatterizer";
    public static final String VERSION = "0.0.1";
    public static final String NAME = "Splatterizer";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;

    @SidedProxy(clientSide = "com.beckadam.splatterizer.proxy.ClientProxy", serverSide = "com.beckadam.splatterizer.proxy.CommonProxy")
    public static CommonProxy PROXY;
	
	@Instance(MODID)
	public static SplatterizerMod instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModRegistry.init();
        LivingEntityHurtHandler.init(MinecraftForge.EVENT_BUS);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        completedLoading = true;
    }
}