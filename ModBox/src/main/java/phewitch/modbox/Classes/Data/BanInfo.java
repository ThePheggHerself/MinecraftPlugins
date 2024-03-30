package phewitch.modbox.Classes.Data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.Classes.BanManager;
import phewitch.modbox.Classes.SqlManager;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BanInfo {

    public String BanID;
    public String Username;
    public String UUID;
    public String IpAddress;

    public String Reason;
    public Long CreationTimestamp;
    public Long ExpirationSeconds;
    public Long UnbannedTimestamp;
    public String IssuerUsername;
    public String IssuerUUID;
    public String UnbanUUID;
    public String UnbanReason;
    ResultSet results;

    public BanInfo(ResultSet resultSet) {
        if (resultSet == null)
            return;

        results = resultSet;
        BanID = SqlManager.getResult(resultSet, "ID");

        Username = SqlManager.getResult(resultSet, "player_name");
        UUID = SqlManager.getResult(resultSet, "player_uuid");
        IpAddress = SqlManager.getResult(resultSet, "player_ip");

        IssuerUsername = SqlManager.getResult(resultSet, "admin_name");
        IssuerUUID = SqlManager.getResult(resultSet, "admin_uuid");

        Reason = SqlManager.getResult(resultSet, "reason");

        UnbanUUID = SqlManager.getResult(resultSet, "unban_uuid");
        UnbanReason = SqlManager.getResult(resultSet, "unban_reason");

        try {
            CreationTimestamp = Long.parseLong(SqlManager.getResult(resultSet, "ban_timestamp"));
            ExpirationSeconds = Long.parseLong(SqlManager.getResult(resultSet, "expire_timestamp"));
            UnbannedTimestamp = Long.parseLong(SqlManager.getResult(resultSet, "unban_timestamp"));
        } catch (Exception e) {
        }
    }

    public BanInfo(BanInfo info, AsyncPlayerPreLoginEvent event) {
        Username = event.getPlayerProfile().getName();
        UUID = event.getPlayerProfile().getId().toString();
        IpAddress = event.getAddress().toString();

        IssuerUsername = info.IssuerUsername;
        IssuerUUID = info.IssuerUUID;


        Reason = info.Reason;
        CreationTimestamp = info.CreationTimestamp;
        ExpirationSeconds = info.ExpirationSeconds;
    }

    public BanInfo(Player plr, String reason) {
        createBanInfo(plr, null, reason, null);
    }

    public BanInfo(Player plr, Player admin, String reason) {
        createBanInfo(plr, admin, reason, null);
    }

    public BanInfo(Player plr, String reason, Long expirationSeconds) {
        createBanInfo(plr, null, reason, expirationSeconds);
    }

    public BanInfo(Player plr, Player admin, String reason, Long expirationSeconds) {
        createBanInfo(plr, admin, reason, expirationSeconds);
    }


    private void createBanInfo(Player plr, @Nullable Player admin, String reason, @Nullable Long expirationSeconds) {
        Username = plr.getName();
        UUID = plr.getUniqueId().toString();
        IpAddress = plr.getAddress().getAddress().toString();

        if (admin != null) {
            IssuerUsername = admin.getName();
            IssuerUUID = admin.getUniqueId().toString();
        } else {
            IssuerUsername = "Server Console";
            IssuerUUID = "CONSOLE";
        }

        Reason = reason;
        CreationTimestamp = System.currentTimeMillis();

        if (expirationSeconds != null) {
            ExpirationSeconds = expirationSeconds;
        }
    }

    public Boolean exists() {
        return UUID != null;
    }

    public boolean isUnbanned() {
        return UnbanUUID != null || isBanExpired();
    }

    public boolean isPermBan() {
        return ExpirationSeconds == null;
    }

    public boolean isBanExpired() {
        if (ExpirationSeconds == null)
            return false;

        return ExpirationSeconds < System.currentTimeMillis();
    }

    public Date getExpireDate() {
        return new Date(ExpirationSeconds);
    }

    public void unban(String reason, @Nullable Player admin) {
        UnbanUUID = admin != null ? admin.getUniqueId().toString() : "CONSOLE";
        UnbanReason = reason;
        UnbannedTimestamp = System.currentTimeMillis();

        BanManager.unbanPlayer(this, admin);
    }

    public Component getBanMessage() {
        var msg = Component.text();

        if (isPermBan())
            msg.append(Component.text("\nYou are permanently banned from the server!\n\nReason: ").color(NamedTextColor.RED));
        else
            msg.append(Component.text("\nYou are banned from the server!\nExpiration: ").color(NamedTextColor.RED)
                    .append(Component.text(
                            new SimpleDateFormat("yyyy-MM-dd HH:mm").format(getExpireDate())).color(NamedTextColor.YELLOW))
                    .append(Component.text("\n\nReason:").color(NamedTextColor.RED)));
        msg.append(Component.text(Reason).color(NamedTextColor.YELLOW));

        return msg.build();
    }
}
