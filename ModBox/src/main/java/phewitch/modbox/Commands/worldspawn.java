package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IBypassCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;

public class worldspawn extends CustomCommand implements IPlayerOnlyCommand, IBypassCommand {
    public worldspawn(@NotNull String name) { super(name); }

    @Override
    public @Nullable String getPermission() {

        return "modbox.spawn";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
        var plr = (Player) sender;

        if((plr.getGameMode() == GameMode.SURVIVAL || plr.getGameMode() == GameMode.ADVENTURE) && !plr.hasPermission((getBypassPermission()))) {
            var plrXp = plr.getLevel();
            if (plrXp < 1) {
                sender.sendMessage(Component.text("You do not have the required XP to teleport").color(NamedTextColor.RED));
                return false;
            }
            plr.setLevel((plrXp - 1));
        }

        plr.teleport(plr.getWorld().getSpawnLocation());
        sender.sendMessage(Component.text("You have been teleported to the world spawn").color(NamedTextColor.GOLD));
        return true;
    }

    @Override
    public String getBypassPermission() {
        return "modbox.spawn.bypass";
    }
}
