package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.eclipse.sisu.Priority;
import phewitch.modbox.Classes.BanManager;
import phewitch.modbox.Classes.Data.BanInfo;
import phewitch.modbox.Classes.PlayerData;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.ModBox;

import java.text.SimpleDateFormat;

public class JoiningAndLeaving implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        var banInfo = BanManager.getBan(event);
        if (banInfo.exists() && !banInfo.isUnbanned()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banInfo.getBanMessage());

            Bukkit.getScheduler().runTaskLater(ModBox.Instance, new Runnable() {
                @Override
                public void run() {
                    if(!banInfo.UUID.equals(event.getPlayerProfile().getId().toString())){
                        Bukkit.getLogger().info("Ban found for Address " + event.getAddress() + "! "
                                + event.getPlayerProfile().getName() + " (" + event.getPlayerProfile().getId() + ") will also be banned!");
                        BanManager.banPlayer(new BanInfo(banInfo, event), null);
                    }
                }
            }, 10L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        if (offlinePlayer.getFirstPlayed() == 0) {
            Component message = Component.text()
                    .append(player.displayName().color(NamedTextColor.GOLD))
                    .append(Component.text(" joined for the first time. Awesome \\o/\nEnjoy your stay here.").color(NamedTextColor.GOLD)).build();

            event.joinMessage(message);

            message = Component.text()
                    .append(Component.text("Welcome to our server. We hope you enjoy your stay here. Don't forget to join our discord at: ").color(NamedTextColor.GREEN))
                    .append(Component.text("discord.gg/dragoninn").color(NamedTextColor.GOLD)).build();

            player.sendMessage(message);
        } else {
            Component message = Component.text()
                    .append(Component.text("Welcome back ").color(NamedTextColor.GREEN))
                    .append(PlayerData.getNameComponent(event.getPlayer(), null)).build();

            event.joinMessage(message);
        }

        UpdateTablist.Update();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var message = Component.text()
                .append(Component.text("Goodbye ").color(NamedTextColor.RED))
                .append(PlayerData.getNameComponent(event.getPlayer(), null))
                .append(Component.text(", Come back soon!").color(NamedTextColor.RED))
                .build();

        event.quitMessage(message);
    }
}
