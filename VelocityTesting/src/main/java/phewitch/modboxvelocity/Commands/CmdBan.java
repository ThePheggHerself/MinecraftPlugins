package phewitch.modboxvelocity.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import phewitch.modboxvelocity.Classes.BanManager;
import phewitch.modboxvelocity.Classes.Data.BanInfo;
import phewitch.modboxvelocity.ModBoxVelocity;

public class CmdBan {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> banNode = LiteralArgumentBuilder.<CommandSource>literal("ban")
                .requires(source -> source.hasPermission("mbv.ban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            proxy.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername(),
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                                    )
                            ));
                            // If you do not need to add a tooltip to the hint
                            // or your command is intended only for versions lower than Minecraft 1.13,
                            // you can omit adding the tooltip, since for older clients,
                            // the tooltip will not be displayed.
                            builder.suggest("all");
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    var reason = context.getArgument("reason", String.class);
                                    var plrStr = context.getArgument("player", String.class);
                                    var tmpPlr = ModBoxVelocity.server.getPlayer(plrStr);

                                    if(tmpPlr.isEmpty()){
                                        if(context.getSource() instanceof Player admin){
                                            BanManager.banPlayer(admin.getUsername(), admin.getUniqueId().toString(), plrStr, reason);
                                        }
                                        else {
                                            BanManager.banPlayer("Server Console", "CONSOLE", plrStr, reason);
                                        }
                                    }
                                    else {
                                        if(context.getSource() instanceof Player admin){
                                            BanManager.banPlayer(new BanInfo(tmpPlr.get(), admin, reason), tmpPlr.get());
                                        }
                                        else {
                                            BanManager.banPlayer(new BanInfo(tmpPlr.get(), reason), tmpPlr.get());
                                        }
                                    }
                                    context.getSource().sendMessage(Component.text(plrStr, NamedTextColor.RED)
                                            .append(Component.text(" was banned from the server!").color(NamedTextColor.YELLOW)));

                                    return Command.SINGLE_SUCCESS;
                                })))
                        .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(banNode);
    }
}
