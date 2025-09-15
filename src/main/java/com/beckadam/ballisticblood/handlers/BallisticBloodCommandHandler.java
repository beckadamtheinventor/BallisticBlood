package com.beckadam.ballisticblood.handlers;

import com.beckadam.ballisticblood.BallisticBloodMod;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BallisticBloodCommandHandler implements ICommand {

    public static void register() {
        ClientCommandHandler.instance.registerCommand(new BallisticBloodCommandHandler());
    }

    protected BallisticBloodCommandHandler() {}

    @Override
    public String getName() {
        return "ballisticblood";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/ballisticblood reload|disable|enable|toggle|clear";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender sender, String[] strings) throws CommandException {
        if (strings.length < 1) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }
        String action = strings[0];
        if (action.equals("reload")) {
            ConfigManager.load(BallisticBloodMod.MODID, Config.Type.INSTANCE);
            ForgeConfigHandler.ParseConfig();
            sender.sendMessage(new TextComponentTranslation("command.ballisticblood.reloaded"));
        } else if (action.equals("disable")) {
            ForgeConfigHandler.client.enableSplatterParticles = false;
            sender.sendMessage(new TextComponentTranslation("command.ballisticblood.disabled"));
        } else if (action.equals("enable")) {
            ForgeConfigHandler.client.enableSplatterParticles = true;
            sender.sendMessage(new TextComponentTranslation("command.ballisticblood.enabled"));
        } else if (action.equals("toggle")) {
            ForgeConfigHandler.client.enableSplatterParticles = !ForgeConfigHandler.client.enableSplatterParticles;
            sender.sendMessage(new TextComponentTranslation(
                    ForgeConfigHandler.client.enableSplatterParticles ?
                            "command.ballisticblood.enabled" :
                            "command.ballisticblood.disabled"
            ));
        } else if (action.equals("clear")) {
            Minecraft.getMinecraft().effectRenderer.clearEffects(Minecraft.getMinecraft().world);
            sender.sendMessage(new TextComponentTranslation("command.ballisticblood.cleared"));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer minecraftServer, ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings, @Nullable BlockPos blockPos) {
        return Arrays.asList(new String[]{
                "reload", "disable", "enable", "toggle", "clear",
        });
//        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
