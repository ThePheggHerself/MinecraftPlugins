package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.Classes.BanManager;
import phewitch.modbox.Classes.Data.BanInfo;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.Commands.CommandBase.CustomCommand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class unban extends CustomCommand {

    public unban(@NotNull String name) {
        super(name);
    }

    @Override
    public String[] getArguments() {
        return new String[] { "name", "reason" };
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var query = "SELECT * FROM player_bans WHERE player_name = ? OR player_uuid = ? OR player_ip = ? ORDER BY ID DESC";
        var result = SqlManager.Instance.getFromDatabase(query,
                new String[]{args[0], args[0], args[0]});

        var banInfo = new BanInfo(result);
        if(!banInfo.exists() || banInfo.isUnbanned())
        {
            sender.sendMessage(Component.text()
                    .append(Component.text("This player is not banned").color(NamedTextColor.RED)).build());
            return false;
        }

        var reason = getReasonFromArgs(args);
        banInfo.unban(reason, sender instanceof Player ? (Player)sender : null);

        sender.sendMessage(Component.text()
                .append(Component.text(args[0]).color(NamedTextColor.YELLOW))
                .append(Component.text(" has been unbanned from the server!").color(NamedTextColor.GREEN)));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }
}
