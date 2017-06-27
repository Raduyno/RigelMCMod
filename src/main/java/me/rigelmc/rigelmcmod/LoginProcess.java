package me.rigelmc.rigelmcmod;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import me.rigelmc.rigelmcmod.command.FreedomCommand;
import me.rigelmc.rigelmcmod.config.ConfigEntry;
import me.rigelmc.rigelmcmod.util.FSync;
import me.rigelmc.rigelmcmod.util.FUtil;
import me.rigelmc.rigelmcmod.shop.ShopData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginProcess extends FreedomService
{

    public static final int DEFAULT_PORT = 25565;
    public static final int MIN_USERNAME_LENGTH = 2;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final Pattern USERNAME_REGEX = Pattern.compile("^[\\w\\d_]{3,20}$");
    //
    @Getter
    @Setter
    private static boolean lockdownEnabled = false;

    public LoginProcess(RigelMCMod plugin)
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

    /*
     * Banning and Permban checks are their respective services
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        final String ip = event.getAddress().getHostAddress().trim();
        final boolean isAdmin = plugin.al.getEntryByIp(ip) != null;

        // Check if the player is already online
        for (Player onlinePlayer : server.getOnlinePlayers())
        {
            if (!onlinePlayer.getName().equalsIgnoreCase(event.getName()))
            {
                continue;
            }

            if (isAdmin)
            {
                event.allow();
                FSync.playerKick(onlinePlayer, "An admin just logged in with the username you are using.");
                return;
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your username is already logged into this server.");
            return;
        }
        // Prevent imposters of famous people from joining because I'm tired of seeing them join as them and claim to be real
        if (ConfigEntry.FAMOUS_PLAYERS.getStringList().contains(event.getName().toLowerCase()))
        {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Stop trying to impose as famous people. Impersonation is illegal, I bet you didn't know that.");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final Player player = event.getPlayer();
        final String username = player.getName();
        final String ip = event.getAddress().getHostAddress().trim();

        // Check username length
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH)
        {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your username is an invalid length (must be between 3 and 20 characters long).");
            return;
        }

        // Check username characters
        if (!USERNAME_REGEX.matcher(username).find())
        {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your username contains invalid characters.");
            return;
        }

        // Check force-IP match
        if (ConfigEntry.FORCE_IP_ENABLED.getBoolean())
        {
            final String hostname = event.getHostname().replace("\u0000FML\u0000", ""); // Forge fix - https://github.com/TotalFreedom/TotalFreedomMod/issues/493
            final String connectAddress = ConfigEntry.SERVER_ADDRESS.getString();
            final int connectPort = server.getPort();

            if (!hostname.equalsIgnoreCase(connectAddress + ":" + connectPort) && !hostname.equalsIgnoreCase(connectAddress + ".:" + connectPort))
            {
                final int forceIpPort = ConfigEntry.FORCE_IP_PORT.getInteger();
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        ConfigEntry.FORCE_IP_KICKMSG.getString()
                        .replace("%address%", ConfigEntry.SERVER_ADDRESS.getString() + (forceIpPort == DEFAULT_PORT ? "" : ":" + forceIpPort)));
                return;
            }
        }

        // Check if player is admin
        // Not safe to use TFM_Util.isSuperAdmin(player) because player.getAddress() will return a null until after player login.
        final boolean isAdmin = plugin.al.getEntryByIp(ip) != null;

        // Validation below this point
        if (isAdmin) // Player is an admin
        {
            // Force-allow log in
            event.allow();

            int count = server.getOnlinePlayers().size();
            if (count >= server.getMaxPlayers())
            {
                for (Player onlinePlayer : server.getOnlinePlayers())
                {
                    if (!plugin.al.isAdmin(onlinePlayer))
                    {
                        onlinePlayer.kickPlayer("You have been kicked to free up room for an admin.");
                        count--;
                    }

                    if (count < server.getMaxPlayers())
                    {
                        break;
                    }
                }
            }

            if (count >= server.getMaxPlayers())
            {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The server is full and a player could not be kicked, sorry!");
                return;
            }

            return;
        }

        // Player is not an admin
        // Server full check
        if (server.getOnlinePlayers().size() >= server.getMaxPlayers())
        {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Sorry, but this server is full.");
            return;
        }

        // Admin-only mode
        if (ConfigEntry.ADMIN_ONLY_MODE.getBoolean())
        {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server is temporarily open to admins only.");
            return;
        }

        // Lockdown mode
        if (lockdownEnabled)
        {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server is currently in lockdown mode.");
            return;
        }

        // Whitelist
        if (plugin.si.isWhitelisted())
        {
            if (!plugin.si.getWhitelisted().contains(username.toLowerCase()))
            {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You are not whitelisted on this server.");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        final ShopData sd = plugin.sh.getData(player);
        
        // Op player on join if the player is not opped
        if (ConfigEntry.OP_ON_JOIN.getBoolean() && !player.isOp() && !plugin.al.isAdminImpostor(player))
        {
            player.setOp(true);
            player.sendMessage(FreedomCommand.YOU_ARE_OP);
        }
        
        // Has shop custom login message
        if (!plugin.al.isAdmin(player) && !plugin.al.isAdminImpostor(player) && sd.isCustomLoginMessage() && !sd.getLoginMessage().equalsIgnoreCase("none"))
        {
            FUtil.bcastMsg(plugin.sl.createLoginMessage(player, sd.getLoginMessage()));
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (ConfigEntry.ADMIN_ONLY_MODE.getBoolean())
                {
                    player.sendMessage(ChatColor.RED + "Server is currently closed to non-admins.");
                }

                if (lockdownEnabled)
                {
                    FUtil.playerMsg(player, "Warning: Server is currenty in lockdown-mode, new players will not be able to join!", ChatColor.RED);
                }
                
                if (plugin.al.isAdmin(player) && !ConfigEntry.ADMIN_LOGIN_MESSAGE.getList().isEmpty())
                {
                    List<String> messages = new ArrayList();
                    for (Object msg : ConfigEntry.ADMIN_LOGIN_MESSAGE.getList())
                    {
                        messages.add(FUtil.colorize((String) msg));
                    }
                    for (int i = 0; i < messages.size(); i++)
                    {
                        player.sendMessage(messages.get(i));
                    }
                }
            }
        }.runTaskLater(plugin, 20L * 1L);
    }
}
