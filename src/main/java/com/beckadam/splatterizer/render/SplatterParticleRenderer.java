package com.beckadam.splatterizer.render;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.particles.SplatterParticleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SplatterParticleRenderer {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected static final List<SplatterParticleBase> particles = Collections.synchronizedList(new ArrayList<>());
    protected static IResourceManager resourceManager;
//    protected static ShaderGroup shaderGroup;

    public static void init() {
        resourceManager = mc.getResourceManager();
//        try {
//            shaderGroup = new ShaderGroup(
//                    mc.getTextureManager(), resourceManager,
//                    mc.getFramebuffer(), new ResourceLocation("splatterizer:shaders/particle.json")
//            );
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
    }

    public static void add(SplatterParticleBase particle) {
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
//        particles.add(particle);
    }

    public static void renderParticles(float partialTicks) {
        renderParticles(mc.player, partialTicks);
    }
    public static void renderParticles(Entity cameraEntity, float partialTicks) {
        if (particles.isEmpty()) {
            return;
        }

        float f = ((float)Math.PI / 180F);
        float f1 = MathHelper.cos(cameraEntity.rotationYaw * f);
        float f2 = MathHelper.sin(cameraEntity.rotationYaw * f);
        float f3 = -f2 * MathHelper.sin(cameraEntity.rotationPitch * f);
        float f4 = f1 * MathHelper.sin(cameraEntity.rotationPitch * f);
        float f5 = MathHelper.cos(cameraEntity.rotationPitch * f);

//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.disableBlend();
//        GlStateManager.disableDepth();
//        GlStateManager.disableAlpha();
//        GlStateManager.disableFog();
//        GlStateManager.disableLighting();
//        GlStateManager.disableColorMaterial();
//        GlStateManager.enableTexture2D();
//        GlStateManager.bindTexture(0);

//        GlStateManager.matrixMode(5890);
//        GlStateManager.pushMatrix();
//        GlStateManager.loadIdentity();

//        Shader particleShader = shaderGroup.listShaders.get(0);
//        Shader swapShader = shaderGroup.listShaders.get(1);

//        particleShader.framebufferIn.unbindFramebuffer();
//        float lvt_2_1_ = (float)particleShader.framebufferOut.framebufferTextureWidth;
//        float lvt_3_1_ = (float)particleShader.framebufferOut.framebufferTextureHeight;
//        GlStateManager.viewport(0, 0, (int)lvt_2_1_, (int)lvt_3_1_);
//
//        ShaderManager shaderManager = particleShader.getShaderManager();
//        shaderManager.addSamplerTexture("DiffuseSampler", particleShader.framebufferIn);
//
//        for(int lvt_4_1_ = 0; lvt_4_1_ < particleShader.listAuxFramebuffers.size(); ++lvt_4_1_) {
//            shaderManager.addSamplerTexture(particleShader.listAuxNames.get(lvt_4_1_), particleShader.listAuxFramebuffers.get(lvt_4_1_));
//            shaderManager.getShaderUniformOrDefault("AuxSize" + lvt_4_1_).set((float)(Integer)particleShader.listAuxWidths.get(lvt_4_1_), (float)(Integer)particleShader.listAuxHeights.get(lvt_4_1_));
//        }
//
//        shaderManager.getShaderUniformOrDefault("ProjMat").set(particleShader.projectionMatrix);
//        shaderManager.getShaderUniformOrDefault("InSize").set((float)particleShader.framebufferIn.framebufferTextureWidth, (float)particleShader.framebufferIn.framebufferTextureHeight);
//        shaderManager.getShaderUniformOrDefault("OutSize").set(lvt_2_1_, lvt_3_1_);
//        shaderManager.getShaderUniformOrDefault("Time").set(partialTicks);
//        shaderManager.getShaderUniformOrDefault("ScreenSize").set((float)mc.displayWidth, (float)mc.displayHeight);
//        shaderManager.useShader();
//        particleShader.framebufferOut.framebufferClear();
//        particleShader.framebufferOut.bindFramebuffer(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (SplatterParticleBase particle : particles) {
//            shaderManager.addSamplerTexture("ParticleAtlas", mc.getTextureManager().getTexture(particle.splatterParticleTexture));
            particle.renderParticle(bufferbuilder, cameraEntity, partialTicks, f1, f5, f2, f3, f4);
        }

        GlStateManager.disableBlend();

//        particleShader.manager.endShader();
//        particleShader.framebufferOut.unbindFramebuffer();
//        particleShader.framebufferIn.unbindFramebufferTexture();

//        for(Object aux : particleShader.listAuxFramebuffers) {
//            if (aux instanceof Framebuffer) {
//                ((Framebuffer)aux).unbindFramebufferTexture();
//            }
//        }

//        GlStateManager.popMatrix();
//        mc.getFramebuffer().bindFramebuffer(true);

//        swapShader.render(partialTicks);

        for (SplatterParticleBase particle : particles) {
            particle.onUpdate();
        }
    }

}
