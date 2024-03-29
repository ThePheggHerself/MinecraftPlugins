package phewitch.modbox.EventListeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandEventListener implements Listener {

    @EventHandler
    public void OnCommand(PlayerCommandPreprocessEvent event){
        Bukkit.getLogger().info(event.getMessage());
    }

}
