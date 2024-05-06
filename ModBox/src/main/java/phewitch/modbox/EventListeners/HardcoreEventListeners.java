package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import phewitch.modbox.Classes.HardcoreManager;
import phewitch.modbox.Classes.PlayerData;

import java.util.ArrayList;
import java.util.Arrays;

public class HardcoreEventListeners implements Listener {

    public void onPlayerRespawn (PlayerRespawnEvent event){
        var plr = event.getPlayer();
        boolean enabled = HardcoreManager.HardcoreEnabled(plr);

        if(enabled)
            plr.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var plr = event.getPlayer();
        boolean enabled = HardcoreManager.HardcoreEnabled(plr);

        var msgString = PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(msgString.split(" ")));

        var msg = Component.text();

        for (var str : list) {
            var lpPlr = Bukkit.getServer().getPlayer(str);
            msg.append(Component.text(" ").color(NamedTextColor.YELLOW));
            if (lpPlr != null && lpPlr.isOnline()) {
                if(PlainTextComponentSerializer.plainText().serialize(lpPlr.name()).equalsIgnoreCase(str))
                    msg.append(PlayerData.getNameComponent(lpPlr, null));
                else
                    msg.append(Component.text(str).color(NamedTextColor.YELLOW));
            } else {
                msg.append(Component.text(str).color(NamedTextColor.YELLOW));
            }
        }

        if (enabled) {
            msg.append(Component.text("\nYou can revive them with ").color(NamedTextColor.YELLOW));
            msg.append(Component.text("1 Totem Of Undying").color(NamedTextColor.BLUE));
            msg.append(Component.text(", ").color(NamedTextColor.YELLOW));
            msg.append(Component.text("1 Diamond Block").color(NamedTextColor.BLUE));
            msg.append(Component.text(" and ").color(NamedTextColor.YELLOW));
            msg.append(Component.text("15 Experience Levels").color(NamedTextColor.BLUE));
        }
        event.deathMessage(msg.build());
    }
}
