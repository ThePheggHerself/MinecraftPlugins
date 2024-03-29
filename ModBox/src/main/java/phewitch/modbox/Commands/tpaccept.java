package phewitch.modbox.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import phewitch.modbox.Classes.Data.TPRequest;
import phewitch.modbox.ModBox;

//@ICommandInfo(
//        Name = "tpaccept",
//        Description = "Accepts a pending TP request",
//        Permission = "",
//        Alias = {}
//)
public class tpaccept {

//    protected tpaccept(@NotNull String name) {
//        super(name);
//    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player plr) {
            if (!TPRequest.PendingRequests.containsKey(plr.getUniqueId())){
                sender.sendMessage(Component.text("You do not have a pending TPA request!").color(NamedTextColor.RED));
                return false;
            }

            var request = TPRequest.PendingRequests.get(plr.getUniqueId());
            var reqSender = Bukkit.getPlayer(request.RequestSender);
            var reqTarget = Bukkit.getPlayer(request.RequestTarget);

            ModBox.Instance.getLogger().info(request.Type.name());

            if(request.Type == TPRequest.RequestType.SenderToTarget){
                reqSender.teleport(reqTarget);
            }
            else if(request.Type == TPRequest.RequestType.TargetToSender){
                reqTarget.teleport(reqSender);
            }
            else if(request.Type == TPRequest.RequestType.TargetToLocation){
                reqTarget.teleport(request.Location);
            }
            else if(request.Type == TPRequest.RequestType.SenderToLocation){
                reqSender.teleport(request.Location);
            }

            reqSender.sendMessage(Component.text("Your TPA request was accepted").color(NamedTextColor.GREEN));
            TPRequest.PendingRequests.remove(plr.getUniqueId());
            return true;
        }
        else {
            sender.sendMessage(Component.text("You must be a player to use this command").color(NamedTextColor.RED));
            return false;
        }
    }
}
