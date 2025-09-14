package com.beckadam.ballisticblood.handlers;

import com.beckadam.ballisticblood.BallisticBloodMod;
import com.beckadam.ballisticblood.particles.ParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BallisticBloodCommandHandler implements ICommand {

    public static void register(EventBus bus) {
        ClientCommandHandler.instance.registerCommand(new BallisticBloodCommandHandler());
    }

    protected BallisticBloodCommandHandler() {}

    @SubscribeEvent
    public static void onClientMessage(ClientChatEvent event) {
        executeCommand(Minecraft.getMinecraft().player, event.getMessage());
    }

    public static void executeCommand(ICommandSender sender, String message) {
        message = message.trim();
        boolean usedSlash = message.startsWith("/");
        if (usedSlash) {
            message = message.substring(1);
        }

        String[] temp = message.split(" ");
        String commandName = temp[0];
        if (!(usedSlash && commandName.equals("ballisticblood"))) {
            return;
        }
        if (temp.length > 1) {
            String[] args = new String[temp.length - 1];
            System.arraycopy(temp, 1, args, 0, args.length);


        }
        sender.sendMessage(new TextComponentString("/ballisticblood reload|disable|enable|toggle|clear"));
    }

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
            ForgeConfigHandler.ParseSplatterizerConfig();
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
            ParticleManager.instance.clear();
            sender.sendMessage(new TextComponentTranslation("command.ballisticblood.cleared"));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer minecraftServer, ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings, @Nullable BlockPos blockPos) {
        if (strings.length == 0) {
            return Arrays.asList(new String[]{
                    "reload", "disable", "enable", "toggle", "clear",
            });
        }
        return Collections.emptyList();
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
