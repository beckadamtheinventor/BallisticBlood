package com.beckadam.splatterizer.handlers;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import com.beckadam.splatterizer.SplatterizerMod;

@Mod.EventBusSubscriber(modid = SplatterizerMod.MODID, value = Side.CLIENT)
public class ClientModRegistry {

    public static void init() {
    }
}