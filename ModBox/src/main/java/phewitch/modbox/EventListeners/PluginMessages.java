package phewitch.modbox.EventListeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.ModBox;

public class PluginMessages implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (channel.equals(ModBox.Channels.GlobalChat)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            var json = in.readUTF();

            var msg = JSONComponentSerializer.json().deserialize(json);
            Bukkit.getServer().broadcast(msg);
        }
        if (channel.equals(ModBox.Channels.Announcement)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            Bukkit.getServer().broadcast(JSONComponentSerializer.json().deserialize(in.readUTF()));

            for (Player plr : Bukkit.getServer().getOnlinePlayers()) {
                plr.playNote(plr.getLocation(), Instrument.BELL, new Note(4));
            }
        }
        if(channel.equals(ModBox.Channels.ServerStatus)){
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

            var e = in.readUTF();

            UpdateTablist.ServerName = e;

            UpdateTablist.Update();
        }
    }
}
