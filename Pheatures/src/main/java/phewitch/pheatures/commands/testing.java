package phewitch.pheatures.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.pheatures.DataClasses.CustomCommand;

import java.util.ArrayList;
import java.util.List;

public class testing extends CustomCommand {

    @Override
    public String[] getArguments() {
        return new String[] { "player", "amount" };
    }

    public testing(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(Component.text("ORANGE").color(NamedTextColor.GREEN));

        try {
            var plr = Bukkit.getPlayer(args[0]);
            var damage = Integer.parseInt(args[1]);

            plr.damage(damage);
        }
        catch (Exception e){
            Bukkit.getLogger().warning(e.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return Bukkit.getOnlinePlayers().stream().map(player -> player.getDisplayName()).toList();
        }
        else {
            return new ArrayList<String>() {{
                add("1");
                add("5");
                add("10");
                add("50");
                add("100");
            }};
        }
    }
}
