package phewitch.modbox.EventListeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import phewitch.modbox.Classes.PlayerData;
import phewitch.modbox.ModBox;

import java.text.SimpleDateFormat;

public class ChatNotifications implements Listener {

    // Listen for the AsyncChatEvent
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {

        if (event.isCancelled())
            return;

        var player = event.getPlayer();

        PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
        var metadata = adapter.getMetaData(player);

        var prefix = PlayerData.getPrefixComponent(player, metadata, false);
        var name = PlayerData.getNameComponent(player, metadata);

        var msg = Component.text()
                .append(prefix)
                .append(name)
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(event.message().color(NamedTextColor.WHITE))
                .build();

        Bukkit.getServer().broadcast(msg);

        prefix = PlayerData.getPrefixComponent(player, metadata, true);

        msg = Component.text()
                .append(name
                        .hoverEvent(HoverEvent.showText(prefix)))
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(event.message().color(NamedTextColor.WHITE))
                .build();

        var json = JSONComponentSerializer.json().serialize(msg);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(json);

        event.getPlayer().sendPluginMessage(ModBox.Instance, ModBox.Channels.GlobalChat, out.toByteArray());

        event.setCancelled(true);
    }

}
