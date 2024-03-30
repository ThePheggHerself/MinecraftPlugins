package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Classes.Data.TPRequest;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;

public class tpdeny extends CustomCommand implements IPlayerOnlyCommand {

    public tpdeny(@NotNull String name) {
        super(name);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var plr = (Player) sender;

        if (!TPRequest.PendingRequests.containsKey(plr.getUniqueId())) {
            sender.sendMessage(Component.text("You do not have a pending TPA request!").color(NamedTextColor.RED));
            return false;
        }

        var request = TPRequest.PendingRequests.get(plr.getUniqueId());
        var reqSender = Bukkit.getPlayer(request.RequestSender);

        reqSender.sendMessage(Component.text("Your TPA request has been denied").color(NamedTextColor.RED));
        plr.sendMessage(Component.text("You have denied the TPA request").color(NamedTextColor.RED));

        return true;

    }
}
