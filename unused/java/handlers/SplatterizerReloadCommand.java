package com.beckadam.ballisticblood.handlers;

import com.beckadam.ballisticblood.SplatterizerMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.server.command.ForgeCommand;

public class SplatterizerReloadCommand extends CommandBase {
    @Override
    public String getName() {
        return "splatterizer-reload";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/splatterizer-reload";
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings) throws CommandException {
        ConfigManager.load(SplatterizerMod.MODID, Config.Type.INSTANCE);
        ForgeConfigHandler.ParseSplatterizerConfig();
        iCommandSender.sendMessage(new TextComponentString("Splatterizer config reloaded!"));
    }
}
