package phewitch.modbox.EventListeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
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

        var msg = Component.text()
                .append(PlayerData.getPrefixComponent(event.getPlayer(), null))
                .append(PlayerData.getNameComponent(event.getPlayer(), null))
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

}
