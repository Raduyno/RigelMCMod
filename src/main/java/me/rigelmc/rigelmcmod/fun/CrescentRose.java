package me.rigelmc.rigelmcmod.fun;

import me.rigelmc.rigelmcmod.FreedomService;
import me.rigelmc.rigelmcmod.RigelMCMod;
import me.rigelmc.rigelmcmod.config.ConfigEntry;
import me.rigelmc.rigelmcmod.shop.ShopData;
import me.rigelmc.rigelmcmod.util.FUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect.Type;

@SuppressWarnings("LocalVariableHidesMemberVariable")
public class CrescentRose extends FreedomService
{
    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    public List<Integer> bullets = new ArrayList<>();
    public final long cooldownTime = 30;
    public final int use_price = ConfigEntry.SHOP_CRESCENT_ROSE_USE_PRICE.getInteger();

    public CrescentRose(RigelMCMod plugin)
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
    
    @EventHandler(priority=EventPriority.HIGH)
    public void onBulletImpact(ProjectileHitEvent event)
    {
    	if (event.getEntity() instanceof Arrow)
    	{
            if (bullets.contains(event.getEntity().getEntityId()))
            {
                Arrow bullet = (Arrow)event.getEntity();
                bullets.remove((Integer)bullet.getEntityId());

                if (event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity)
                {
                    LivingEntity target = (LivingEntity)event.getHitEntity();
                    // You never know, strange shit happens and it might not be a player
                    if (bullet.getShooter() != null && bullet.getShooter() instanceof Player)
                    {
                        /**Player shooter = (Player)bullet.getShooter();
                        if (event.getHitEntity() instanceof Player)
                        {
                            if (plugin.al.isAdmin((Player)event.getHitEntity()))
                            {
                                FUtil.playerMsg(shooter, "Sorry, but you can't attack admins with Crescent Rose!", ChatColor.RED);
                                return;
                            }
                        }**/
                        Player shooter = (Player)bullet.getShooter();
                        if (event.getHitEntity() instanceof Player)
                        {
                            Player p = (Player)event.getHitEntity();
                            if (p.getGameMode().equals(GameMode.CREATIVE) && !FUtil.isExecutive(shooter.getName()))
                            {
                                return;
                            }
                        }
                        if (event.getHitEntity() instanceof Player)
                        {
                            Player p = (Player)event.getHitEntity();
                            if (p.getGameMode().equals(GameMode.CREATIVE) && !FUtil.isExecutive(shooter.getName()))
                            {
                                return;
                            }
                        }
                        Location l = target.getLocation();
                        target.setHealth(0);
                        final Firework fw = (Firework) l.getWorld().spawn(l, Firework.class);
                        FireworkMeta fm = fw.getFireworkMeta();
                        fm.addEffect(FireworkEffect.builder().trail(true).with(Type.BALL_LARGE).withColor(Color.RED).build());
                        fm.setPower(0);
                        fw.setFireworkMeta(fm);
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                fw.detonate();
                            }
                        }.runTaskLater(plugin, 2L);
                    }
               }
            }
    	}
    }
    
    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerRightClick(PlayerInteractEvent event)
    {
    	if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    	{
            Player p = event.getPlayer();
            if (p.getInventory().getItemInMainHand().equals(getCrescentRose()))
            {
                if (!FUtil.isExecutive(p.getName()))
                {
                    ShopData sd = plugin.sh.getData(p);
                    if (plugin.sl.canAfford(use_price, sd.getCoins()))
                    {
                        sd.setCoins(sd.getCoins() - use_price);
                    }
                    else
                    {
                        int coins_needed = use_price - sd.getCoins();
                        FUtil.playerMsg(p, ChatColor.RED + "You only have " + ChatColor.DARK_RED + sd.getCoins() + ChatColor.RED + " coins. You need " + ChatColor.DARK_RED + coins_needed + ChatColor.RED + " more coins to use Crescent Rose!");
                        return;
                    }
                }
                if (cooldowns.containsKey(p.getName()))
                {
                    long secondsLeft = ((cooldowns.get(p.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0)
                    {
                        FUtil.playerMsg(p, "You can't use the Crescent Rose for another " + secondsLeft + " seconds!", ChatColor.RED);
                        return;
                    }
                }
                Arrow bullet = p.launchProjectile(Arrow.class, p.getLocation().getDirection());
                bullets.add(bullet.getEntityId());
                bullet.setVelocity(bullet.getVelocity().normalize().multiply(50));
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 2.5f);

                // Executives don't need a cool down :^)
                if (!FUtil.isExecutive(p.getName()))
                {
                    cooldowns.put(p.getName(), System.currentTimeMillis());
                }
            }
    	}
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event)
    {
    	Entity attacker = event.getDamager();
    	Entity target = event.getEntity();
    	if (attacker instanceof Player && target instanceof LivingEntity)
    	{
            Player p = (Player)attacker;
            ItemStack i = p.getInventory().getItemInMainHand();
            if (i != null && i.equals(getCrescentRose()))
            {	 
                ShopData sd = plugin.sh.getData(p);
                if (sd.isCrescentRose())
                {
                    if (!FUtil.isExecutive(p.getName()))
                    {
                        if (plugin.sl.canAfford(use_price, sd.getCoins()))
                        {
                            sd.setCoins(sd.getCoins() - use_price);
                        }
                        else
                        {
                            int coins_needed = use_price - sd.getCoins();
                            FUtil.playerMsg(p, ChatColor.RED + "You only have " + ChatColor.DARK_RED + sd.getCoins() + ChatColor.RED + " coins. You need " + ChatColor.DARK_RED + coins_needed + ChatColor.RED + " more coins to use Crescent Rose!");
                            return;
                        }
                    }
                    if (cooldowns.containsKey(p.getName()))
                    {
                        long secondsLeft = ((cooldowns.get(p.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if (secondsLeft > 0)
                        {
                            FUtil.playerMsg(p, "You can't use the Crescent Rose for another " + secondsLeft + " seconds!", ChatColor.RED);
                            return;
                        }
                    }
                    if (target instanceof Player)
                    {	
                        if (!plugin.al.isAdmin(p) && plugin.al.isAdmin((Player)target))
                        {
                            FUtil.playerMsg(p, "Sorry, but you can't attack admins with Crescent Rose!", ChatColor.RED);
                            return;
                        }
                    }
                    // Executives don't need a cool down :^)
                    if (!FUtil.isExecutive(p.getName()))
                    {
                        cooldowns.put(p.getName(), System.currentTimeMillis());
                    }

                    // Play attack sound
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 100F, 0.1F);

                    // Deliver the final blow
                    LivingEntity t = (LivingEntity)target;
                    t.setHealth(0);
                }
            }
    	}
    }
    
    public ItemStack getCrescentRose()
    {
    	ItemStack NEEDED_A_RWBY_REFERENCE = new ItemStack(Material.DIAMOND_HOE);
    	ItemMeta datMeta = NEEDED_A_RWBY_REFERENCE.getItemMeta();
    	datMeta.setDisplayName(ChatColor.DARK_RED + "Crescent Rose");
    	List<String> lore = new ArrayList();
    	lore.add(ChatColor.RED + "Totally didn't steal this from Ruby");
    	lore.add(ChatColor.RED + "(I really needed a RWBY reference)");
    	lore.add(ChatColor.GOLD + "WARNING: THIS WEAPON IS OVER POWERED");
    	lore.add(ChatColor.GOLD + "AND BY OVER POWERED I MEAN INSTANT KILL");
    	lore.add(ChatColor.YELLOW + "It costs " + ChatColor.RED + use_price + ChatColor.YELLOW + " coins per use in order to use this item.");
    	datMeta.setLore(lore);
    	datMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 42069, true);
    	datMeta.addEnchant(Enchantment.DAMAGE_ALL, 42069, true);
    	datMeta.setUnbreakable(true);
    	NEEDED_A_RWBY_REFERENCE.setItemMeta(datMeta);
    	return NEEDED_A_RWBY_REFERENCE;
    }
}
