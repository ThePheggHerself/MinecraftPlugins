package phewitch.modbox.Commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Commands.CommandBase.CustomCommand;

public class creative extends CustomCommand {
    public creative(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ((Player)sender).setGameMode(GameMode.CREATIVE);
        return true;
    }
}
