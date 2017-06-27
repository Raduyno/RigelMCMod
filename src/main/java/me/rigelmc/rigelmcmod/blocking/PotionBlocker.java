package me.rigelmc.rigelmcmod.blocking;

import me.rigelmc.rigelmcmod.FreedomService;
import me.rigelmc.rigelmcmod.RigelMCMod;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PotionBlocker extends FreedomService
{

    public static final int POTION_BLOCK_RADIUS_SQUARED = 20 * 20;

    public PotionBlocker(RigelMCMod plugin)
    {
        super(plugin);
    }

    @Override
    protected void onStart()
    {
    }

    @Override
    protected void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onThrowPotion(PotionSplashEvent event)
    {
        ProjectileSource source = event.getEntity().getShooter();

        if (!(source instanceof Player))
        {
            event.setCancelled(true);
            return;
        }

        Player thrower = (Player) source;

        if (plugin.al.isAdmin(thrower))
        {
            return;
        }

        for (Player player : thrower.getWorld().getPlayers())
        {
            if (thrower.getLocation().distanceSquared(player.getLocation()) < POTION_BLOCK_RADIUS_SQUARED)
            {
                thrower.sendMessage(ChatColor.RED + "You cannot use splash potions close to other players.");
                event.setCancelled(true);
                return;
            }
        }

    }

}
