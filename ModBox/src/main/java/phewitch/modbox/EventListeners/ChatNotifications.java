package phewitch.modbox.EventListeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.ModBox;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class ChatNotifications implements Listener {
    public static Component GetPrefixWithNameComponent(Player player, @Nullable CachedMetaData metadata) {
        if (metadata == null) {
            PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        var prefix = LegacyComponentSerializer.legacyAmpersand().deserialize(metadata.getPrefix());

        return Component.text()
                .append(prefix)
                .append(Component.text().append(player.displayName()).color(GetNameColour(player, metadata)))
                .build();
    }

    public static TextColor GetNameColour(Player player, @Nullable CachedMetaData metadata) {
        if (metadata == null) {
            PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }


        var colourData = metadata.getMetaValue("namecolour");

        if (colourData != null)
            return TextColor.fromCSSHexString(colourData);
        else return NamedTextColor.WHITE;
    }

    // Listen for the AsyncChatEvent
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {

        if (event.isCancelled())
            return;

        var msg = Component.text()
                .append(GetPrefixWithNameComponent(event.getPlayer(), null))
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(event.message().color(NamedTextColor.WHITE))
                .build();

        var json = JSONComponentSerializer.json().serialize(msg);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(json);

        event.getPlayer().sendPluginMessage(ModBox.Instance, ModBox.Channels.GlobalChat, out.toByteArray());
        Bukkit.getServer().broadcast(msg);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        var sppComp = ModBox.Instance.getConfig().getBoolean("staffplusplus-compatibility");
        var query = "";

        if (sppComp)
            query = "SELECT * FROM sp_banned_players WHERE player_uuid = ?";
        else
            query = "SELECT * FROM banned_players WHERE player_uuid = ?";

        var result = SqlManager.Instance.getFromDatabase(query, new String[]{event.getPlayer().getUniqueId().toString()});

        if (result != null) {
            var unbannedUUID = SqlManager.getResult(result, "unbanned_by_uuid");

            Bukkit.getLogger().info("Unbanned? " + (unbannedUUID != null ? unbannedUUID : "NULL"));

            if (unbannedUUID == null) {
                var timestampString = SqlManager.getResult(result, "end_timestamp");

                if (timestampString == null) {
                    var reason = SqlManager.getResult(result, "reason");

                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Component.text()
                            .append(Component.text("\nYou are permanently banned from the server!\n\nReason: ").color(NamedTextColor.RED))
                            .append(Component.text(reason).color(NamedTextColor.YELLOW)).build());
                } else {
                    var timestamp = Long.parseLong(timestampString);
                    if (timestamp > System.currentTimeMillis()) {
                        var msg = Component.text("You are banned from the server!").color(NamedTextColor.RED);
                        var expirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));

                        event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                                Component.text("\nYou are banned from the server!\nExpiration: ").color(NamedTextColor.RED)
                                        .append(Component.text(expirationDate).color(NamedTextColor.YELLOW))
                                        .append(Component.text("\n\nReason:").color(NamedTextColor.RED))
                                        .append(Component.text(SqlManager.getResult(result, "reason")).color(NamedTextColor.YELLOW)));
                    }
                }
            }
        }
    }

    @EventHandler
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
                    .append(Component.text().append(player.displayName()).color(GetNameColour(player, null))).build();

            event.joinMessage(message);
        }

        UpdateTablist.Update();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var message = Component.text()
                .append(Component.text("Goodbye ").color(NamedTextColor.RED))
                .append(Component.text().append(player.displayName()).color(GetNameColour(player, null)))
                .append(Component.text(", Come back soon!").color(NamedTextColor.RED))
                .build();

        event.quitMessage(message);
    }

    public Component GetPrefixComponent(Player player, @Nullable CachedMetaData metadata) {

        if (metadata == null) {
            PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(metadata.getPrefix() + " ");
    }
}
