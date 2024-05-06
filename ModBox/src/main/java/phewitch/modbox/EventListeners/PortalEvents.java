package phewitch.modbox.EventListeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalEvents implements Listener {

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.isCancelled())
            return;

        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            for (BlockState block : event.getBlocks()) {
                if (block.getType() == Material.OBSIDIAN) {
                    event.setCancelled(true);
                }
            }

            if (event.isCancelled()) ;
            event.getEntity().sendMessage(Component.text("An ancient power has blocked your portal from being created").color(NamedTextColor.DARK_RED));
        } else if (event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            for (BlockState block : event.getBlocks()) {
                if (block.getType() == Material.OBSIDIAN) {
                    block.setType(Material.REINFORCED_DEEPSLATE);
                }
            }
        }
    }

    @EventHandler
    public void createFireEvent(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getBlock().getType() != Material.FIRE || event.getBlockAgainst().getType() != Material.REINFORCED_DEEPSLATE)
            return;

        var world = event.getBlock().getWorld();
        var origin = event.getBlockAgainst().getLocation();
        var searchPoint = origin.clone();
        var upperLimit = origin.clone();
        Location leftLimit = null;
        Location rightLimit = null;
        for (Integer x = 0; x < 8; x++) {
            upperLimit.add(0, 1, 0);

            var checkBlock = world.getBlockAt(upperLimit);

            if (checkBlock.getType() == Material.REINFORCED_DEEPSLATE)
                break;
        }

        //Bukkit.getLogger().info("Searching for left limit");
        searchPoint.add(0, 1, 0);

        for (Integer x = 0; x < 21; x++) {
            var checkBlock = world.getBlockAt(searchPoint.add(1, 0, 0));
            //Bukkit.getLogger().info("X: " + x + " " + checkBlock.getType() + " " + checkBlock.getLocation());

            if (checkBlock.getType() == Material.REINFORCED_DEEPSLATE) {
                //Bukkit.getLogger().info("Left Limit Found: " + checkBlock.getLocation() + " " + checkBlock.getType());
                leftLimit = checkBlock.getLocation();
                break;
            }
        }
        if (leftLimit == null) {
            searchPoint = origin.clone();
            searchPoint.add(0, 1, 0);

            for (Integer z = 0; z < 21; z++) {
                {
                    var checkBlock = world.getBlockAt(searchPoint.add(0, 0, 1));
                    //Bukkit.getLogger().info("Z: " + z + " " + checkBlock.getType() + " " + checkBlock.getLocation());

                    if (checkBlock.getType() == Material.REINFORCED_DEEPSLATE) {
                        //Bukkit.getLogger().info("Left Limit Found: " + checkBlock.getLocation() + " " + checkBlock.getType());
                        leftLimit = checkBlock.getLocation();
                        break;
                    }

                }
            }
        }

        //Bukkit.getLogger().info("Searching for Right limit");
        searchPoint = origin.clone();
        searchPoint.add(0, 1, 0);

        for (Integer x = 0; x < 21; x++) {
            var checkBlock = world.getBlockAt(searchPoint.add(-1, 0, 0));
            //Bukkit.getLogger().info("X: " + -x + " " + checkBlock.getType() + " " + checkBlock.getLocation());

            if (checkBlock.getType() == Material.REINFORCED_DEEPSLATE) {
                //Bukkit.getLogger().info("Right Limit Found: " + checkBlock.getLocation() + " " + checkBlock.getType());
                rightLimit = checkBlock.getLocation();
                break;
            }
        }
        if (rightLimit == null) {
            searchPoint = origin.clone();
            searchPoint.add(0, 1, 0);

            for (Integer z = 0; z < 21; z++) {
                var checkBlock = world.getBlockAt(searchPoint.add(0, 0, -1));
                //Bukkit.getLogger().info("Z: " + -z + " " + checkBlock.getType() + " " + checkBlock.getLocation());

                if (checkBlock.getType() == Material.REINFORCED_DEEPSLATE) {
                    //Bukkit.getLogger().info("Right Limit Found: " + checkBlock.getLocation() + " " + checkBlock.getType());
                    rightLimit = checkBlock.getLocation();
                    break;
                }

            }
        }

        //Bukkit.getLogger().info("Upper: " + upperLimit.y() + " Origin: " + origin.y());
        Boolean isOnZ = rightLimit.x() == leftLimit.x();
        double width = isOnZ ? leftLimit.z() - rightLimit.z() : leftLimit.x() - rightLimit.x();
        double height = upperLimit.y() - origin.y();
        var replacePoint = new Location(world, rightLimit.x(), origin.y(), rightLimit.z());
        //Bukkit.getLogger().info("Width: " + width + " Height: " + height + " " + isOnZ);


        for (Integer y = 0; y < height + 1; y++) {
            for (Integer x = 0; x < width; x++) {
                if (isOnZ)
                    replacePoint.add(0, 0, 1);
                else
                    replacePoint.add(1, 0, 0);

                var replaceBlock = world.getBlockAt(replacePoint);

                if (replaceBlock.getType() == Material.AIR || replaceBlock.getType() == Material.FIRE || replaceBlock.getType() == Material.SCULK_VEIN) {
                    replaceBlock.setType(Material.NETHER_PORTAL);

                    var data = replaceBlock.getBlockData();
                    if(isOnZ) {
                        data.rotate(StructureRotation.CLOCKWISE_90);
                        replaceBlock.setBlockData(data);
                    }
                }
            }

            replacePoint.add(0, 1, 0);
            replacePoint = new Location(world, rightLimit.x(), replacePoint.y(), rightLimit.z());
        }
    }
}
