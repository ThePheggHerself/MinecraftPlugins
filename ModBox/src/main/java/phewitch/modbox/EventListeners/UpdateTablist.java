package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import phewitch.modbox.Classes.PlayerData;
import phewitch.modbox.ModBox;

public class UpdateTablist {

    public static String ServerName = null;

    public static void Update(){
        for (Player plr : Bukkit.getOnlinePlayers()) {
            var comp = PlayerData.getPrefixComponent(plr, null);
            comp.append(PlayerData.getNameComponent(plr, null));

            plr.playerListName(comp.append(Component.text(" " + plr.getPing() + "ms").color(NamedTextColor.AQUA)));

            comp = Component.text()
                    .append(Component.text("Welcome to The Dragon Inn").color(NamedTextColor.AQUA))
                    .append(Component.text("\nCurrent server: ").color(NamedTextColor.GRAY))
                    .append(Component.text(ModBox.Instance.getConfig().get("server-name").toString() + "\n").color(NamedTextColor.GREEN))
                    .build();

            plr.sendPlayerListHeader(comp);

            comp = Component.text()
                    .append(Component.text("\nJoin our discord at \n").color(NamedTextColor.GRAY))
                    .append(Component.text("discord.gg/dragoninn").color(NamedTextColor.GOLD))
                    .build();

            plr.sendPlayerListFooter(comp);
        }
    }

}
