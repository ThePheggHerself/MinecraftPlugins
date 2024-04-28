package phewitch.modbox.Classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.ModBox;

public class PlayerData {

    public static Component getPrefixComponent(Player player, @Nullable CachedMetaData metadata, Boolean trim) {

        if (metadata == null) {
            PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(trim ? metadata.getPrefix().trim() : metadata.getPrefix());
    }

    public static Component getNameComponent(Player player, @Nullable CachedMetaData metadata) {
        if (metadata == null) {
            PlayerAdapter<Player> adapter = ModBox.LuckPermsAPI.getPlayerAdapter(Player.class);
            metadata = adapter.getMetaData(player);
        }

        var colorMeta = metadata.getMetaValue("namecolour");
        var color = colorMeta == null ? NamedTextColor.WHITE : TextColor.fromCSSHexString(colorMeta);

        return player.displayName().color(color);
    }
}
