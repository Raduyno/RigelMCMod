package me.rigelmc.rigelmcmod.command;

import me.rigelmc.rigelmcmod.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.NON_OP, source = SourceType.BOTH)
@CommandParameters(description = "Shows how to apply for admin", usage = "/<command>", aliases = "ai")
public class Command_admininfo extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        msg("How to apply for admin on the RigelMCMod server:", ChatColor.YELLOW);
        msg(" - Do not ask for admin in game,", ChatColor.AQUA);
        msg(" - Be helpful within the server,", ChatColor.DARK_AQUA);
        msg(" - Report those breaking the rules,", ChatColor.AQUA);
        msg(" - And apply on our forums at this link:", ChatColor.DARK_AQUA);
        msg(" - http://forum.rigelmc.ga/forumdisplay.php?fid=13", ChatColor.AQUA;
        msg(" - Do not apply for admin if you cannot be active!", ChatColor.DARK_AQUA);
        return true;
    }
}
