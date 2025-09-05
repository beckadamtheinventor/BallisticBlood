package com.beckadam.splatterizer.handlers;


import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import com.beckadam.splatterizer.SplatterizerMod;

public class PacketHandler {

    public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(SplatterizerMod.MODID);

    public static void init() {
        instance.registerMessage(MessageParticleHandler.MessageParticleFX.Handler.class, MessageParticleHandler.MessageParticleFX.class, 0, Side.CLIENT);
    }
}