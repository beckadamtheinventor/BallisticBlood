package com.beckadam.splatterizer.helpers;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlendModeHelper {
    public static GlStateManager.SourceFactor getSourceFactor(String mode) {
        if (mode.equalsIgnoreCase("NORMAL")) {
            return GlStateManager.SourceFactor.SRC_ALPHA;
        } else if (mode.equalsIgnoreCase("MULTIPLY")) {
            return GlStateManager.SourceFactor.DST_COLOR;
        } else if (mode.equalsIgnoreCase("LIGHTEN")) {
            return GlStateManager.SourceFactor.ONE;
        } else if (mode.contains(":")) {
            String[] modes = mode.split(":");
            try {
                return GlStateManager.SourceFactor.valueOf(modes[0]);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    public static GlStateManager.DestFactor getDestFactor(String mode) {
        if (mode.equalsIgnoreCase("NORMAL")) {
            return GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
        } else if (mode.equalsIgnoreCase("MULTIPLY")) {
            return GlStateManager.DestFactor.SRC_COLOR;
        } else if (mode.equalsIgnoreCase("LIGHTEN")) {
            return GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR;
        } else if (mode.contains(":")) {
            String[] modes = mode.split(":");
            try {
                return GlStateManager.DestFactor.valueOf(modes[1]);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
