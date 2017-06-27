package me.rigelmc.rigelmcmod.command;

import java.io.File;
import me.rigelmc.rigelmcmod.rank.Rank;
import me.rigelmc.rigelmcmod.util.FUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SENIOR_ADMIN, source = SourceType.ONLY_CONSOLE, blockHostConsole = true)
@CommandParameters(description = "Removes essentials playerdata", usage = "/<command>")
public class Command_wipeessdata extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!server.getPluginManager().isPluginEnabled("UMC-Essentials"))
        {
            msg("UMC-Essentials is not enabled on this server");
            return true;
        }

        FUtil.adminAction(sender.getName(), "Wiping warps and essentials playerdata", true);

        FUtil.deleteFolder(new File(server.getPluginManager().getPlugin("UMC-Essentials").getDataFolder(), "userdata"));
        FUtil.deleteFolder(new File(server.getPluginManager().getPlugin("UMC-Essentials").getDataFolder(), "warps"));

        msg("All essentials data deleted.");
        return true;
    }
}
