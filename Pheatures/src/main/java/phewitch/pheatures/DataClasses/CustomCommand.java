package phewitch.pheatures.DataClasses;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.pheatures.Pheatures;

import java.util.List;

public class CustomCommand extends Command implements TabExecutor {

    public Boolean RequirePlayer = true;

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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public @NotNull String getUsage() {

        if(this.getArguments().length < 1)
            return "Usage: /" + this.getName();

        return "Usage: /" + this.getName() + " <" + String.join("> <", this.getArguments()) + ">";
    }

    public String hasValidArguments(String[] args) {

        Pheatures.Instance.getLogger().info("ArgCheck: 11");

        var cmdArgs = this.getArguments();

        Pheatures.Instance.getLogger().info("ArgCheck: 22 " + cmdArgs.length);

        if (args.length < cmdArgs.length)
            return getUsage();

        Pheatures.Instance.getLogger().info("ArgCheck: 33");

        var index = 0;
        for (String arg : cmdArgs) {
            Pheatures.Instance.getLogger().info("Checking arg: " + arg + " " + args[index] + " " + index);

            switch (arg) {
                case "player": {
                    if (Bukkit.getPlayer(args[index]) == null)
                        return "Unable to find player " + args[index];

                    break;
                }
                case "amount":
                case "number": {
                    try{
                        Integer.parseInt(args[index]);
                    }
                    catch (Exception e)
                    {
                        return "Cannot parse " + args[index] + " into a number";
                    }

                    break;
                }
            }
            index++;
        }

        return null;
    }
}
