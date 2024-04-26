package phewitch.modboxvelocity.Classes;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;
import phewitch.modboxvelocity.Classes.Data.BanInfo;
import phewitch.modboxvelocity.ModBoxVelocity;

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

        if (player == null) {
            var tmpPlr = ModBoxVelocity.server.getPlayer(info.UUID);
            if(tmpPlr.isPresent())
                player = tmpPlr.get();
        }


        if(player != null)
            player.disconnect(Component.text("\nYou have been banned from the server.\nReason: ").color(NamedTextColor.RED)
                    .append(Component.text(info.Reason).color(NamedTextColor.YELLOW)));
    }

    public static void banPlayer(String adminName, String adminID, String targetUUID, String reason) {
        var query = "INSERT INTO player_bans (player_uuid, reason, ban_timestamp, admin_name, admin_uuid) VALUES (?,?,?,?,?)";

        Long timestamp = System.currentTimeMillis();

        var parameters = new String[]{
                targetUUID,
                reason,
                timestamp.toString(),
                adminName,
                adminID
        };

        SqlManager.Instance.writeToDatabase(query, parameters);
    }

    public static void unbanPlayer(BanInfo info, @Nullable Player admin) {
        var query = "UPDATE player_bans SET unban_timestamp = ?, unban_uuid = ?, unban_reason = ? WHERE ID = ?";

        var parameters = new String[]{
                info.UnbannedTimestamp.toString(),
                info.UnbanUUID,
                info.UnbanReason,
                info.BanID
        };

        SqlManager.Instance.writeToDatabase(query, parameters);
    }

    public static BanInfo getBan(Player player) {
        var query = "SELECT * FROM player_bans WHERE player_uuid = ? OR player_ip = ? ORDER BY ID DESC";
        var result = SqlManager.Instance.getFromDatabase(query,
                new String[]{player.getUniqueId().toString(), player.getRemoteAddress().toString()});

        return new BanInfo(result);
    }

    public static BanInfo getBan(String searchTerm) {
        var query = "SELECT * FROM player_bans WHERE player_name = ? OR player_uuid = ? OR player_ip = ? ORDER BY ID DESC";
        var result = SqlManager.Instance.getFromDatabase(query,
                new String[]{searchTerm, searchTerm, searchTerm});

        return new BanInfo(result);
    }
}
