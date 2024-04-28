package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import phewitch.modbox.Classes.PlayerData;
import phewitch.modbox.ModBox;

public class UpdateTablist {

    public static String ServerName = null;

    public static void Update(){
        PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);

        for (Player plr : Bukkit.getOnlinePlayers()) {
            var metadata = adapter.getMetaData(plr);

            var comp =Component.text()
                    .append(PlayerData.getPrefixComponent(plr, metadata, false))
                    .append(PlayerData.getNameComponent(plr, metadata))
                    .append(Component.text(" " + plr.getPing() + "ms").color(NamedTextColor.AQUA))
                    .build();

            plr.playerListName(comp);

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
