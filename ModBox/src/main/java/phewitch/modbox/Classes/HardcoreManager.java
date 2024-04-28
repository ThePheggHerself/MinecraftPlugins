package phewitch.modbox.Classes;

import org.bukkit.entity.Player;

public class HardcoreManager {
    public static boolean HardcoreEnabled(Player player){
        var result = SqlManager.Instance.getFromDatabase("SELECT * FROM player_hardcore WHERE player_uuid = ?", new String[]{
               player.getUniqueId().toString()
        });

        return result != null && Boolean.parseBoolean(SqlManager.getResult(result, "enabled"));
    }

    public static void ToggleHardcore(Player player, Boolean enabled){
        var time = ((Long) System.currentTimeMillis()).toString();
        var state = enabled.toString();

        SqlManager.Instance.writeToDatabase("INSERT INTO player_hardcore (player_uuid, update_time, enabled) VALUES (?,?,?)" +
                        "ON DUPLICATE KEY UPDATE update_time = ?, enabled = ?",
                new String[]{
                        player.getUniqueId().toString(),
                        time,
                        state,
                        time,
                        state,
                });
    }
}
