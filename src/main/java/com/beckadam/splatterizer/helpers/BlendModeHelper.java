package com.beckadam.splatterizer.helpers;

import com.beckadam.splatterizer.SplatterizerMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.*;

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
        return !(mode.equalsIgnoreCase("BRIGHT") || mode.equalsIgnoreCase("UNLIT"));
    }
    public static int getBlendFunction(String mode) {
        // same modes as net.minecraft.client.renderer.stringToBlendFunction
        if (mode.equalsIgnoreCase("ADD")) {
            return GL14.GL_FUNC_ADD;
        } else if (mode.equalsIgnoreCase("SUBTRACT")) {
            return GL14.GL_FUNC_SUBTRACT;
        } else if (mode.equalsIgnoreCase("REVERSE_SUBTRACT")) {
            return GL14.GL_FUNC_REVERSE_SUBTRACT;
        } else if (mode.equalsIgnoreCase("REVERSESUBTRACT")) {
            return GL14.GL_FUNC_REVERSE_SUBTRACT;
        } else if (mode.equalsIgnoreCase("MIN")) {
            return GL14.GL_MIN;
        } else if (mode.equalsIgnoreCase("MAX")) {
            return GL14.GL_MAX;
        } else {
            if (!mode.isEmpty()) {
                SplatterizerMod.LOGGER.log(Level.WARN, "Unknown/Invalid blend operation \"" + mode + "\"");
            }
            return GL14.GL_FUNC_ADD;
        }
    }
}
