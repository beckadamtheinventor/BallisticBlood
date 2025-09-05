package com.beckadam.splatterizer.handlers;

import net.minecraftforge.fml.common.Mod;
import com.beckadam.splatterizer.SplatterizerMod;

@Mod.EventBusSubscriber(modid = SplatterizerMod.MODID)
public class ModRegistry {

        public static void init() {
            PacketHandler.init();
        }

}