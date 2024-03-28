package phewitch.pheatures.Handlers;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import phewitch.pheatures.Pheatures;

public class ChatWelcomeAndTablist implements Listener {
    // Listen for the AsyncChatEvent
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {

        if(event.isCancelled())
            return;

        var msg = Component.text()
                .append(GetPrefixWithNameComponent(event.getPlayer(), null))
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(event.message().color(NamedTextColor.WHITE))
                .build();

        var json = JSONComponentSerializer.json().serialize(msg);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(json);

        event.getPlayer().sendPluginMessage(Pheatures.Instance, Pheatures.IdfGlobalChat, out.toByteArray());
        Bukkit.getServer().broadcast(msg);
        event.setCancelled(true);
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

        var namePrefixComponent = GetPrefixWithNameComponent(player, null);
        player.playerListName(namePrefixComponent);
        player.sendPlayerListHeader(Component.text("You lost the game"));
        player.sendPlayerListFooter(Component.text("discord.gg/dragoninn").color(NamedTextColor.GOLD));
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
            PlayerAdapter<Player> adapter = Pheatures.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(metadata.getPrefix() + " ");
    }

    public Component GetPrefixWithNameComponent(Player player, @Nullable CachedMetaData metadata) {
        if (metadata == null) {
            PlayerAdapter<Player> adapter = Pheatures.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        var prefix = LegacyComponentSerializer.legacyAmpersand().deserialize(metadata.getPrefix());

        return Component.text()
                .append(prefix)
                .append(Component.text().append(player.displayName()).color(GetNameColour(player, metadata)))
                .build();
    }

    public TextColor GetNameColour(Player player, @Nullable CachedMetaData metadata) {
        if (metadata == null) {
            PlayerAdapter<Player> adapter = Pheatures.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }


        var colourData = metadata.getMetaValue("namecolour");

        if(colourData != null)
            return TextColor.fromCSSHexString(colourData);
        else return NamedTextColor.WHITE;
    }
}
