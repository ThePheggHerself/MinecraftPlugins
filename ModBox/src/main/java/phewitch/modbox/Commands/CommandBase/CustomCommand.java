package phewitch.modbox.Commands.CommandBase;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomCommand extends Command implements TabExecutor {

    protected CustomCommand(@NotNull String name) {
        super(name);
    }

    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Override
    public @NotNull String getUsage() {

        if (this.getArguments().length < 1)
            return "Usage: /" + this.getName();

        return "Usage: /" + this.getName() + " <" + String.join("> <", this.getArguments()) + ">";
    }

    public String hasValidArguments(String[] args) {
        var cmdArgs = this.getArguments();

        if (args.length < cmdArgs.length)
            return getUsage();

        var index = 0;
        for (String arg : cmdArgs) {
            switch (parseArgType(arg)) {
                case "player": {
                    if (Bukkit.getPlayer(args[index]) == null)
                        return "Unable to find player " + args[index];

                    break;
                }
                case "number": {
                    try {
                        Integer.parseInt(args[index]);
                    } catch (Exception e) {
                        return "Cannot parse " + args[index] + " into a number";
                    }

                    break;
                }
            }
            index++;
        }

        return null;
    }

    public String parseArgType(String arg) {
        switch (arg) {
            default:
            case "player": {
                return "player";
            }
            case "amount":
            case "number": {
                return "number";
            }
            case "x":
                return "coordx";
            case "y":
                return "coordy";
            case "z":
                return "coordz";
            case "reason":
            case "message":
            case "string":
                return "string";
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var cmdArgs = this.getArguments();
        var index = args.length - 1;

        Bukkit.getLogger().info("ORANGE " + args.length + " " + cmdArgs.length);

        if (args.length > cmdArgs.length)
            return new ArrayList<String>();

        var type = parseArgType(cmdArgs[index]);
        Bukkit.getLogger().info(cmdArgs[index] + " " + args.length + " " + type);

        switch (parseArgType(cmdArgs[index])) {
            default:
            case "player": {
                return Bukkit.getOnlinePlayers().stream().map(player -> player.getDisplayName()).toList();
            }
            case "number": {
                return new ArrayList<String>() {{
                    add("1");
                    add("5");
                    add("10");
                    add("15");
                    add("20");
                }};
            }
            case "coordx":{
                var response = new ArrayList<String>();
                if(sender instanceof Player plr){
                    response.add(Double.toString(plr.getLocation().x()));
                    response.add("~");
                }
                else {
                    response.add("0");
                    response.add("100");
                }
                return response;
            }
            case "coordy":{
                var response = new ArrayList<String>();
                if(sender instanceof Player plr){
                    response.add(Double.toString(plr.getLocation().y()));
                    response.add("~");
                }
                else {
                    response.add("0");
                    response.add("100");
                }
                return response;
            }
            case "coordz":{
                var response = new ArrayList<String>();
                if(sender instanceof Player plr){
                    response.add(Double.toString(plr.getLocation().z()));
                    response.add("~");
                }
                else {
                    response.add("0");
                    response.add("100");
                }
                return response;
            }
            case "string":
            {
                return new ArrayList<String>() {};
            }

        }


    }
}
