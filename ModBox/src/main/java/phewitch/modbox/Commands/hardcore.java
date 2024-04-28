package phewitch.modbox.Commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Classes.HardcoreManager;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;
import phewitch.modbox.ModBox;

import java.util.logging.Logger;

public class hardcore extends CustomCommand implements IPlayerOnlyCommand {

    public hardcore(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player plr) {
            if(plr.getGameMode() != GameMode.SURVIVAL)
            {
                sender.sendMessage(Component.text("You must be in survival to run this command").color(NamedTextColor.YELLOW));
                return false;
            }

            boolean enabled = HardcoreManager.HardcoreEnabled(plr);
            HardcoreManager.ToggleHardcore(plr, !enabled);

            enabled = HardcoreManager.HardcoreEnabled(plr);
            sender.sendMessage(Component.text("Hardcore is now ").color(NamedTextColor.YELLOW)
                    .append(Component.text(enabled ? "ENABLED" : "DISABLED").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));

            return true;
        } else {
            sender.sendMessage(Component.text("You must be a player to use this command").color(NamedTextColor.RED));
            return false;
        }
    }
}
