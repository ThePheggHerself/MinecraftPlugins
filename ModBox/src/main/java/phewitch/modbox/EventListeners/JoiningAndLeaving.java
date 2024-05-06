package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import phewitch.modbox.Classes.PlayerData;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.ModBox;

public class JoiningAndLeaving implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        var message = Component.text();

        if (offlinePlayer.getFirstPlayed() == 0) {
            message.append(Component.text("Welcome to our server. We hope you enjoy your stay here. Don't forget to join our discord at: ").color(NamedTextColor.GREEN));
            message.append(Component.text("discord.gg/dragoninn").color(NamedTextColor.GOLD)).build();

            player.sendMessage(message);

            message = Component.text();
            message.append(player.displayName().color(NamedTextColor.GOLD));
            message.append(Component.text(" joined for the first time. Awesome \\o/\nEnjoy your stay here.").color(NamedTextColor.GOLD)).build();
        } else {
            message.append(Component.text("Welcome back ").color(NamedTextColor.GREEN));
            message.append(PlayerData.getNameComponent(event.getPlayer(), null)).build();
        }

        event.joinMessage(message.build());

        UpdateTablist.Update();

        var serverName = ModBox.Instance.getConfig().get("server-name").toString();
        var timestamp = ((Long)System.currentTimeMillis()).toString();

        SqlManager.Instance.writeToDatabase("INSERT INTO player_cache (player_name, player_uuid, last_join, last_server, last_address) VALUES (?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE player_name = ?, last_join = ?, last_server = ?, last_address = ?",
                new String[]{
                        player.getName(),
                        player.getUniqueId().toString(),
                        timestamp,
                        serverName,
                        player.getAddress().getAddress().toString(),
                        player.getName(),
                        timestamp,
                        serverName,
                        player.getAddress().getAddress().toString()
                });
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
