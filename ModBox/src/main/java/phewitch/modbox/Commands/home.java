package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import phewitch.modbox.Commands.CommandBase.BypassCommand;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IBypassCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;

public class home extends CustomCommand implements IPlayerOnlyCommand, IBypassCommand {
    public home(@NotNull String name) { super(name); }

    @Override
    public @Nullable String getPermission() {
        return "modbox.home";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
        var plr = (Player) sender;

        if(plr.getRespawnLocation() == null)
        {
            sender.sendMessage(Component.text("You do not have a home set. Try sleeping in a bed").color(NamedTextColor.RED));
            return false;
        }

        if((plr.getGameMode() == GameMode.SURVIVAL || plr.getGameMode() == GameMode.ADVENTURE) && !plr.hasPermission(getBypassPermission())) {
            if (!plr.getInventory().contains(Material.ENDER_PEARL)) {
                sender.sendMessage(Component.text("You do not have the required item to teleport").color(NamedTextColor.RED));
                return false;
            }
            var itemStack = new ItemStack((Material.ENDER_PEARL));
            itemStack.setAmount(1);
            plr.getInventory().removeItem(itemStack);
        }

        plr.teleport(plr.getRespawnLocation());
        sender.sendMessage(Component.text("You have been teleported to your home").color(NamedTextColor.GOLD));
        return true;
    }

    @Override
    public String getBypassPermission() {
        return "modbox.home.bypass";
    }
}
