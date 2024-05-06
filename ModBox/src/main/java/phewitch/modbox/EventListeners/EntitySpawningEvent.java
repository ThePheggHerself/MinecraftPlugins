package phewitch.modbox.EventListeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawningEvent implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event){
        if(event.isCancelled() || event.getEntity().getType() != EntityType.GLOW_SQUID)
            return;

        event.setCancelled(true);

//        for (int x = -5; x <= 5; x++)
//            for (int y = -5; y <= 5; y++)
//                for (int z = -5; z <= 5; z++) {
//                    if (event.getLocation().getBlock().getRelative(x, y, z).getType() == Material.SPAWNER)
//                    {
//                        event.setCancelled(true);
//                        return;
//                    }
//                }
    }
}
