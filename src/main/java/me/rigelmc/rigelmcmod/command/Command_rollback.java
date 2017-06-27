package me.rigelmc.rigelmcmod.command;

import me.rigelmc.rigelmcmod.rank.Rank;
import me.rigelmc.rigelmcmod.util.FUtil;
import me.rigelmc.rigelmcmod.player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SUPER_ADMIN, source = SourceType.BOTH, blockHostConsole = true)
@CommandParameters(description = "Issues a rollback on a player", usage = "/<command> <name> | undo <name>", aliases = "rb")
public class Command_rollback extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!plugin.cpb.isEnabled())
        {
            msg("CoreProtect is either not installed or is not enabled.", ChatColor.RED);
        }
        if (args.length == 0 || args.length > 2)
        {
            return false;
        }
        
        final Player player;
        String playerName;
        
        if (args.length == 2 && args[0].equalsIgnoreCase("undo"))
        {
            playerName = args[1];
        }
        else
        {
            playerName = args[0];
        }
        
        player = getPlayer(playerName);
        
        if (player != null)
        {
            playerName = player.getName();
        }
        else
        {
            final PlayerData entry = plugin.pl.getData(args[0]);

            if (entry == null)
            {
                msg("Can't find that user. If target is not logged in, make sure that you spelled the name exactly.");
                return true;
            }
            playerName = entry.getUsername();
        }

        if (args.length == 1)
        {
            FUtil.adminAction(sender.getName(), "Rolling back player: " + playerName, false);
            plugin.cpb.rollback(playerName);
            msg("If this rollback was a mistake, use /rollback " + playerName + " undo to reverse the rollback.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("undo"))
        {
            FUtil.adminAction(sender.getName(), "Reverting rollback for player: " + playerName, false);
            plugin.cpb.undoRollback(playerName);
            return true;
        }
        return false;
    }
}
