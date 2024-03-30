package phewitch.modbox.Classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.Classes.Data.BanInfo;

public class BanManager {
    public static void banPlayer(BanInfo info, @Nullable Player player) {
        var query = "INSERT INTO player_bans (player_name, player_uuid, player_ip, reason, ban_timestamp, admin_name, admin_uuid) VALUES (?,?,?,?,?,?,?)";

        var parameters = new String[]{
                info.Username,
                info.UUID,
                info.IpAddress,
                info.Reason,
                info.CreationTimestamp.toString(),
                info.IssuerUsername,
                info.IssuerUUID
        };

        SqlManager.Instance.writeToDatabase(query, parameters);

        if (player == null)
            player = Bukkit.getPlayer(info.UUID);

        if(player != null && player.isOnline())
            player.kick(Component.text("\nYou have been banned from the server.\nReason: ").color(NamedTextColor.RED)
                    .append(Component.text(info.Reason).color(NamedTextColor.YELLOW)));
    }

    public static void unbanPlayer(BanInfo info, @Nullable Player admin) {
        var query = "UPDATE player_bans SET unban_timestamp = ?, unban_uuid = ?, unban_reason = ? WHERE ID = ?";

        Bukkit.getLogger().info("111111");

        var parameters = new String[]{
                info.UnbannedTimestamp.toString(),
                info.UnbanUUID,
                info.UnbanReason,
                info.BanID
        };

        Bukkit.getLogger().info("222222222");

        SqlManager.Instance.writeToDatabase(query, parameters);

        Bukkit.getLogger().info("3333333333333");
    }

    public static BanInfo getBan(Player player) {
        var query = "SELECT * FROM player_bans WHERE player_uuid = ? OR player_ip = ? ORDER BY ID DESC";
        var result = SqlManager.Instance.getFromDatabase(query,
                new String[]{player.getUniqueId().toString(), player.getAddress().toString()});

        return new BanInfo(result);
    }

    public static BanInfo getBan(AsyncPlayerPreLoginEvent event) {
        var query = "SELECT * FROM player_bans WHERE player_uuid = ? OR player_ip = ? ORDER BY ID DESC";
        var result = SqlManager.Instance.getFromDatabase(query,
                new String[]{event.getPlayerProfile().getId().toString(), event.getAddress().toString()});

        return new BanInfo(result);
    }
}
