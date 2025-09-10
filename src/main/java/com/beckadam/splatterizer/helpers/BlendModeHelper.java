package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.SplatterizerMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlendModeHelper {
    public static GlStateManager.SourceFactor getSourceFactor(String mode) {
        if (mode.equalsIgnoreCase("NORMAL")) {
            return GlStateManager.SourceFactor.SRC_ALPHA;
        } else if (mode.equalsIgnoreCase("MULTIPLY")) {
            return GlStateManager.SourceFactor.DST_COLOR;
        } else if (mode.equalsIgnoreCase("LIGHTEN")) {
            return GlStateManager.SourceFactor.ONE;
        } else {
            try {
                return GlStateManager.SourceFactor.valueOf(mode);
            } catch (Exception e) {
                SplatterizerMod.LOGGER.log(Level.WARN,
                        "Unknown/Invalid blend source factor: \"" + mode+ "\"");
                return null;
            }
        }
    }
    public static GlStateManager.DestFactor getDestFactor(String mode) {
        if (mode.equalsIgnoreCase("NORMAL")) {
            return GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
        } else if (mode.equalsIgnoreCase("MULTIPLY")) {
            return GlStateManager.DestFactor.SRC_COLOR;
        } else if (mode.equalsIgnoreCase("LIGHTEN")) {
            return GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR;
        } else {
            try {
                return GlStateManager.DestFactor.valueOf(mode);
            } catch (Exception e) {
                SplatterizerMod.LOGGER.log(Level.WARN,
                        "Unknown/Invalid blend dest factor: \"" +mode + "\"");
                return null;
            }
        }
    }
    public static boolean getShouldLight(String mode) {
        return !mode.equalsIgnoreCase("BRIGHT");
    }
    public static int getBlendFunction(String mode) {
        // same modes as net.minecraft.client.renderer.stringToBlendFunction
        if (mode.equalsIgnoreCase("ADD")) {
            return 32774;
        } else if (mode.equalsIgnoreCase("SUBTRACT")) {
            return 32778;
        } else if (mode.equalsIgnoreCase("REVERSE_SUBTRACT")) {
            return 32779;
        } else if (mode.equalsIgnoreCase("REVERSESUBTRACT")) {
            return 32779;
        } else if (mode.equalsIgnoreCase("MIN")) {
            return 32775;
        } else {
            // I'm not going to bother converting these Unicode characters to integer values, let the compiler do it
            return mode.equalsIgnoreCase("MAX") ? '耈' : '耆';
        }
    }
}
