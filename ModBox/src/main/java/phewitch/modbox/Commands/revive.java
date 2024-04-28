package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;

public class revive extends CustomCommand implements IPlayerOnlyCommand {
    public revive(@NotNull String name) {
        super(name);
    }

    @Override
    public String[] getArguments() {
        return new String[] {"player"};
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player plr) {
            var target = Bukkit.getPlayer(args[0]);

            if(plr.getGameMode() != GameMode.SURVIVAL)
            {
                sender.sendMessage(Component.text("You must be in survival to run this command").color(NamedTextColor.YELLOW));
                return false;
            }
            else if(target.getGameMode() != GameMode.SPECTATOR){
                sender.sendMessage(Component.text("That player is not dead").color(NamedTextColor.YELLOW));
                return false;
            }

            if(!plr.getInventory().contains(Material.TOTEM_OF_UNDYING) || !plr.getInventory().contains(Material.DIAMOND_BLOCK) || plr.getLevel() < 15){
                sender.sendMessage(Component.text("You lack the resources to revive a dead player. You need ").color(NamedTextColor.YELLOW)
                        .append(Component.text("1 Totem Of Undying").color(NamedTextColor.BLUE))
                        .append(Component.text(", ").color(NamedTextColor.YELLOW))
                        .append(Component.text("1 Diamond Block").color(NamedTextColor.BLUE))
                        .append(Component.text(" and ").color(NamedTextColor.YELLOW))
                        .append(Component.text("15 Experience Levels").color(NamedTextColor.BLUE)));
                return false;
            }

            var stack = new ItemStack(Material.TOTEM_OF_UNDYING);
            plr.getInventory().removeItem(stack);
            stack = new ItemStack(Material.DIAMOND_BLOCK);
            stack.setAmount(2);
            plr.getInventory().removeItem(stack);
            plr.setLevel(plr.getLevel() - 15);

            target.setGameMode(GameMode.SURVIVAL);
            var respawnPos = target.getRespawnLocation();
            target.teleport(respawnPos != null ? respawnPos : Bukkit.getWorlds().get(0).getSpawnLocation());

            sender.sendMessage(Component.text("You have successfully revived ").color(NamedTextColor.YELLOW)
                    .append(target.displayName().color(NamedTextColor.GREEN)));

            target.sendMessage(Component.text("You have been revived by ").color(NamedTextColor.YELLOW)
                    .append(plr.displayName().color(NamedTextColor.GREEN)));

            return true;
        } else {
            sender.sendMessage(Component.text("You must be a player to use this command").color(NamedTextColor.RED));
            return false;
        }
    }
}
