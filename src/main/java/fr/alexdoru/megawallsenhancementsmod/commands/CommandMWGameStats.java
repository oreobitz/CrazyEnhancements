package fr.alexdoru.megawallsenhancementsmod.commands;

import fr.alexdoru.megawallsenhancementsmod.events.MWGameStatsEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil.addChatMessage;
import static fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil.getTagMW;

public class CommandMWGameStats extends CommandBase {

    @Override
    public String getCommandName() {
        return "mwgamestats";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mwgamestats";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (MWGameStatsEvent.getGameStats() != null) {
            addChatMessage(MWGameStatsEvent.getGameStats().getGameStatMessage(MWGameStatsEvent.getFormattedname()));
        } else {
            addChatMessage(new ChatComponentText(getTagMW() + EnumChatFormatting.RED + "No game stats available"));
        }

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

}
