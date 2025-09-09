package com.beckadam.splatterizer;

import com.beckadam.splatterizer.handlers.AttackEntityFromHandler;
import com.beckadam.splatterizer.message.MessageParticleHandler;
import com.beckadam.splatterizer.particles.ParticleTypeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.beckadam.splatterizer.proxy.CommonProxy;

import static com.beckadam.splatterizer.proxy.CommonProxy.networkWrapperInstance;

@Mod(modid = SplatterizerMod.MODID, version = SplatterizerMod.VERSION, name = SplatterizerMod.NAME)
public class SplatterizerMod {
    public static final String MODID = "splatterizer";
    public static final String VERSION = "0.0.1";
    public static final String NAME = "Splatterizer";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;
    public static final ParticleTypeManager particleTypes = new ParticleTypeManager();

    @SidedProxy(clientSide = "com.beckadam.splatterizer.proxy.ClientProxy", serverSide = "com.beckadam.splatterizer.proxy.CommonProxy")
    public static CommonProxy PROXY;
	
	@Instance(MODID)
	public static SplatterizerMod instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AttackEntityFromHandler.init(MinecraftForge.EVENT_BUS);
        networkWrapperInstance.registerMessage(
                MessageParticleHandler.MessageParticleFX.Handler.class,
                MessageParticleHandler.MessageParticleFX.class,
                0, Side.CLIENT
        );
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        completedLoading = true;
    }
}