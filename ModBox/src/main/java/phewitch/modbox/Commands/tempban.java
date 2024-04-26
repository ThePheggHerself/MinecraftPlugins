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
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IBypassCommand;
import phewitch.modbox.ModBox;

import java.util.List;

public class tempban extends CustomCommand implements IBypassCommand {

    public tempban(@NotNull String name) {
        super(name);
    }

    @Override
    public String getBypassPermission() {
        return ModBox.Instance.getName() + "." + "banBypass";
    }

    @Override
    public String[] getArguments() {
        return new String[] { "player", "duration", "reason" };
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var plr = Bukkit.getPlayer(args[0]);
        var reason = getReasonFromArgs(args, 2);
        var length = getDuration(args[1]);

        if (plr.hasPermission(getBypassPermission())) {
            sender.sendMessage(Component.text()
                    .append(Component.text("This player has the ").color(NamedTextColor.RED))
                    .append(Component.text(getBypassPermission()).color(NamedTextColor.GOLD))
                    .append(Component.text(" permission and cannot be banned").color(NamedTextColor.RED)));
            return false;
        }

        BanInfo banInfo = new BanInfo(plr, sender instanceof Player admin ? admin : null, reason, length);
        BanManager.banPlayer(banInfo, plr);

        sender.sendMessage(banInfo.getBanMessage());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }
}
