package phewitch.pheatures.Handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import phewitch.pheatures.Pheatures;

public class UpdateTablist {

    public UpdateTablist(){
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimer(Pheatures.Instance, () -> UpdateTablist(), 20L  /*<-- the initial delay */, 20L * 5L /*<-- the interval */);
    }

    public void UpdateTablist(){
        for (Player plr : Bukkit.getOnlinePlayers()) {
            var namePrefixComponent = ChatAndNotifications.GetPrefixWithNameComponent(plr, null);

            plr.playerListName(namePrefixComponent.append(Component.text(" " + plr.getPing() + "ms").color(NamedTextColor.AQUA)));
            plr.sendPlayerListHeader(Component.text("You lost the game"));
            plr.sendPlayerListFooter(Component.text("discord.gg/dragoninn").color(NamedTextColor.GOLD));

        }
    }

}
