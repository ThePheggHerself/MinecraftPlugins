package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Classes.Data.TPRequest;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;
import phewitch.modbox.ModBox;

public class tpa extends CustomCommand implements IPlayerOnlyCommand {
    public tpa(@NotNull String name) {
        super(name);
    }

    @Override
    public String[] getArguments() {
        return new String[]{"player"};
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var target = Bukkit.getPlayer(args[0]);
        var plr = (Player) sender;

        if (target.getUniqueId() == plr.getUniqueId()) {
            plr.sendMessage(Component.text("You cannot send a TPA request to yourself").color(NamedTextColor.RED));
            return false;
        }

        if (TPRequest.PendingRequests.containsKey(target.getUniqueId())) {
            plr.sendMessage(target.displayName().color(NamedTextColor.GOLD).append(Component.text(" already has a pending TPA request!")));
            return false;
        }

        var request = new TPRequest(plr, target, TPRequest.RequestType.SenderToTarget);
        TPRequest.PendingRequests.put(target.getUniqueId(), request);

        var tpaMessage = Component.text()
                .append(plr.displayName().color(NamedTextColor.GOLD))
                .append(Component.text(" wants to TP to you!\nYou have 30 seconds to ").color(NamedTextColor.YELLOW))
                .append(Component.text("Accept")
                        .color(NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Runs /tpaccept")))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept")))
                .append(Component.text(" or ").color(NamedTextColor.YELLOW))
                .append(Component.text("Deny")
                        .color(NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.text("Runs /tpdeny")))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny")));

        target.sendMessage(tpaMessage);
        plr.sendMessage(Component.text("Request sent!").color(NamedTextColor.YELLOW));

        Bukkit.getScheduler().scheduleSyncDelayedTask(ModBox.Instance, () -> {
            if (TPRequest.PendingRequests.containsKey(target.getUniqueId())) {
                TPRequest.PendingRequests.remove(target.getUniqueId());

                var msg = Component.text("Your pending TPA request has timed out").color(NamedTextColor.RED);
                plr.sendMessage(msg);
                target.sendMessage(msg);
            }
        }, 20L * 2);

        return true;
    }
}
