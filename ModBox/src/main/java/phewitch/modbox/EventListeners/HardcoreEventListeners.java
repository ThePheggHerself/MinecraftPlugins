package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import phewitch.modbox.Classes.HardcoreManager;

public class HardcoreEventListeners implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        var plr = event.getPlayer();
        boolean enabled = HardcoreManager.HardcoreEnabled(plr);

        if(enabled){
            for(var item : plr.getInventory())
            {
                if(item == null)
                    continue;
                plr.getWorld().dropItem(plr.getLocation(), item);
            }
            plr.getInventory().clear();
            plr.setLevel(0);
            plr.setExp(0);
            plr.setGameMode(GameMode.SPECTATOR);

            plr.sendMessage(Component.text("You died and have been forced into spectator"));

            var comp = Component.text()
                        .append(plr.displayName().color(NamedTextColor.GREEN))
                                .append(Component.text(" has died. You can revive them with ").color(NamedTextColor.YELLOW))
                    .append(Component.text("1 Totem Of Undying").color(NamedTextColor.BLUE))
                    .append(Component.text(", ").color(NamedTextColor.YELLOW))
                    .append(Component.text("1 Diamond Block").color(NamedTextColor.BLUE))
                    .append(Component.text(" and ").color(NamedTextColor.YELLOW))
                    .append(Component.text("15 Experience Levels").color(NamedTextColor.BLUE));

            Bukkit.getServer().broadcast(comp.build());

            event.setCancelled(true);
        }

        event.deathMessage(Component.text()
                .append(plr.displayName().color(NamedTextColor.GREEN))
                .append(Component.text(" died").color(NamedTextColor.YELLOW)).build());
    }
}
