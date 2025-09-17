package com.beckadam.ballisticblood.handlers;


import com.beckadam.ballisticblood.BallisticBloodMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class ParticleConfig {
    public final int type;
    public final String typeName;
    public final float velocity;
    public final float sprayVelocity;
    public final float gravity;
    public final float size;
    public final ResourceLocation texture;
    public final String[] blendMode;
    public final boolean lighting;
    public final String blendOp;
    public final float colorMultiplierR;
    public final float colorMultiplierG;
    public final float colorMultiplierB;
    public final float alphaMultiplier;
    public final int[] tiling;
    public final int[] sprayRows;
    public final int[] projectileRows;
    public final int[] decalRows;

    public ParticleConfig(String config) throws RuntimeException {
        JsonObject json = new JsonParser().parse(config).getAsJsonObject();
        if (!JsonUtils.hasField(json, "name")) {
            throw new RuntimeException("missing name in particle config!");
        }
        if (!JsonUtils.hasField(json, "texture")) {
            throw new RuntimeException("missing texture in particle config!");
        }
        typeName = JsonUtils.getString(json, "name");
        String tex = JsonUtils.getString(json, "texture");
        if (!tex.contains(":")) {
            throw new RuntimeException("missing mod id in texture resource location!");
        }
        texture = new ResourceLocation(tex);
        if (JsonUtils.hasField(json, "size")) {
            size = JsonUtils.getFloat(json, "size");
        } else {
            size = 1.0f;
        }
        if (JsonUtils.hasField(json, "velocity")) {
            velocity = JsonUtils.getFloat(json, "velocity");
        } else {
            velocity = 1.0f;
        }
        if (JsonUtils.hasField(json, "gravity")) {
            gravity = JsonUtils.getFloat(json, "gravity");
        } else {
            gravity = 1.0f;
        }
        if (JsonUtils.hasField(json, "spray_velocity")) {
            sprayVelocity = JsonUtils.getFloat(json, "spray_velocity");
        } else {
            sprayVelocity = 1.0f;
        }
        tiling = new int[] {8, 8};
        if (JsonUtils.hasField(json, "tiling")) {
            String[] t = JsonUtils.getString(json, "tiling").split(",");
            if (t.length >= 1) {
                tiling[0] = Integer.parseInt(t[0]);
                if (t.length >= 2) {
                    tiling[1] = Integer.parseInt(t[1]);
                } else {
                    tiling[1] = tiling[0];
                }
            }
        }
        if (JsonUtils.hasField(json, "spray_rows")) {
            String[] t = JsonUtils.getString(json, "spray_rows").split(",");
            sprayRows = new int[t.length];
            for (int i=0; i<t.length; i++) {
                sprayRows[i] = Integer.parseInt(t[i]);
            }
        } else {
            sprayRows = new int[] {0, 1, 2, 3, 4};
        }
        if (JsonUtils.hasField(json, "projectile_rows")) {
            String[] t = JsonUtils.getString(json, "projectile_rows").split(",");
            projectileRows = new int[t.length];
            for (int i=0; i<t.length; i++) {
                projectileRows[i] = Integer.parseInt(t[i]);
            }
        } else {
            projectileRows = new int[] {5, 6};
        }
        if (JsonUtils.hasField(json, "decal_rows")) {
            String[] t = JsonUtils.getString(json, "decal_rows").split(",");
            decalRows = new int[t.length];
            for (int i=0; i<t.length; i++) {
                decalRows[i] = Integer.parseInt(t[i]);
            }
        } else {
            decalRows = new int[] {7};
        }

        blendMode = new String[2];
        blendMode[0] = blendMode[1] = "NORMAL";
        if (JsonUtils.hasField(json, "blend")) {
            String[] mode = JsonUtils.getString(json, "blend").split(" ");
            if (mode.length == 1) {
                blendMode[0] = blendMode[1] = mode[0];
            } else if (mode.length >= 2) {
                blendMode[0] = mode[0];
                blendMode[1] = mode[1];
            }
        } else {
            blendMode[0] = blendMode[1] = "NORMAL";
        }
        if (JsonUtils.hasField(json, "blend_op")) {
            blendOp = JsonUtils.getString(json, "blend_op");
        } else {
            blendOp = "ADD";
        }
        if (JsonUtils.hasField(json, "lighting")) {
            lighting = JsonUtils.getBoolean(json, "lighting");
        } else {
            lighting = true;
        }
        if (JsonUtils.hasField(json, "color")) {
            com.google.gson.JsonArray arr = JsonUtils.getJsonArray(json, "color");
            colorMultiplierR = arr.get(0).getAsFloat();
            colorMultiplierG = arr.get(1).getAsFloat();
            colorMultiplierB = arr.get(2).getAsFloat();
        } else {
            colorMultiplierR = colorMultiplierG = colorMultiplierB = 1.0f;
        }
        if (JsonUtils.hasField(json, "alpha")) {
            alphaMultiplier = JsonUtils.getFloat(json, "alpha");
        } else {
            alphaMultiplier = 1.0f;
        }
        type = BallisticBloodMod.particleTypes.add(typeName);
    }
}
