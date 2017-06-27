package me.rigelmc.rigelmcmod;

import me.rigelmc.rigelmcmod.player.FPlayer;
import me.rigelmc.rigelmcmod.util.FSync;
import me.rigelmc.rigelmcmod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AntiSpam extends FreedomService
{

    public static final int MSG_PER_CYCLE = 8;
    public static final int TICKS_PER_CYCLE = 2 * 10;
    //
    public BukkitTask cycleTask = null;

    public AntiSpam(RigelMCMod plugin)
    {
        super(plugin);
    }

    @Override
    protected void onStart()
    {
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                cycle();
            }
        }.runTaskTimer(plugin, TICKS_PER_CYCLE, TICKS_PER_CYCLE);
    }

    @Override
    protected void onStop()
    {
        FUtil.cancel(cycleTask);
    }

    private void cycle()
    {
        for (Player player : server.getOnlinePlayers())
        {
            final FPlayer playerdata = plugin.pl.getPlayer(player);

            // TODO: Move each to their own section
            playerdata.resetMsgCount();
            playerdata.resetBlockDestroyCount();
            playerdata.resetBlockPlaceCount();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        String message = event.getMessage().trim();

        final FPlayer playerdata = plugin.pl.getPlayerSync(player);

        // Check for spam
        if (playerdata.incrementAndGetMsgCount() > MSG_PER_CYCLE && !plugin.al.isAdmin(player))
        {
            FSync.bcastMsg(player.getName() + " was automatically kicked for spamming chat.", ChatColor.RED);
            FSync.autoEject(player, "Kicked for spamming chat.");

            playerdata.resetMsgCount();

            event.setCancelled(true);
            return;
        }

        // Check for message repeat
        if (playerdata.getLastMessage().equalsIgnoreCase(message) && !plugin.al.isAdmin(player))
        {
            FSync.playerMsg(player, "Please do not repeat messages.");
            event.setCancelled(true);
            return;
        }

        playerdata.setLastMessage(message);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        final Player player = event.getPlayer();
        final FPlayer fPlayer = plugin.pl.getPlayer(player);
        fPlayer.setLastCommand(command);

        if (fPlayer.allCommandsBlocked())
        {
            FUtil.playerMsg(player, "Your commands have been blocked by an admin.", ChatColor.RED);
            event.setCancelled(true);
            return;
        }

        if (fPlayer.incrementAndGetMsgCount() > MSG_PER_CYCLE && !plugin.al.isAdmin(player))
        {
            FUtil.bcastMsg(player.getName() + " was automatically kicked for spamming commands.", ChatColor.RED);
            plugin.bm.eject(player, "Kicked for spamming commands.");

            fPlayer.resetMsgCount();
            event.setCancelled(true);
        }
    }

}
