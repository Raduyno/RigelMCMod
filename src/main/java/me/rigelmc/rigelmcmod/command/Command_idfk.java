package me.rigelmc.rigelmcmod.command;

import me.rigelmc.rigelmcmod.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.Random;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "i dont fucking know, what even", usage = "thot if you dont know what this does then dont use it")
public class Command_idfk extends FreedomCommand
{
    
    public static final String[] DRUNK_LINES = new String[]{"i is thy best bitch in teh world xD xD xD",
            "ooga boga ooga boga ooga boga ooga boga", "jake u dog fukr", "nero is a nice black pussy",
            "robin has a perfect penis", "ABABABABABABABABABA ABAOBOABOBA", "agge is a fuckin egg",
            "explosive errors == explosive arrows", "sync; echo 3 > /proc/sys/vm/drop_caches", "yeet",
            "si"};
    
    private static final Random random = new Random();

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }
        
        switch (args[0])
        {
            case "killmepls":
                playerSender.setHealth(0);
                msg("ok u wanted die u now die");
                break;
            case "robin":
                msg("robin is a meme xD xD xD http://creeperseth.com/robinsmeme");
                msg("http://invitemeplease.rubyroseisgay.creeperseth.com/deth");
                break;
            case "succ":
                playerSender.chat("mmmmm *suckle* *suckle* daddy");
                break;
            case "drunk":
                msg(DRUNK_LINES[random.nextInt(DRUNK_LINES.length)]);
                break;
            case "zero":
                msg("WATCH ZERO KARA HAJIMERU MAHOU NO SHO (GRIMOIRE OF ZERO) TODAY! https://kissanime.ru/anime/zero-kara-hajimeru-mahou-no-sho");
                break;
            case "boom":
                msg("allahu akbar bitch");
                playerSender.getWorld().createExplosion(playerSender.getLocation(), 0F);
                break;
            case "rocket":
                playerSender.setVelocity(new Vector(0, 1000, 0));
                msg("*insert shooting stars meme here*");
                break;
            default:
                return false;
        }
        return true;
    }
}
