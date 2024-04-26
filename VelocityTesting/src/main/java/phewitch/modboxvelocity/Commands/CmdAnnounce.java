package phewitch.modboxvelocity.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import phewitch.modboxvelocity.ModBoxVelocity;

public class CmdAnnounce {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> banNode = LiteralArgumentBuilder.<CommandSource>literal("announce")
                .requires(source -> source.hasPermission("mbv.announce"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                        .executes(context -> {
                            var msg = Component.text()
                                    .append(Component.text("[").color(NamedTextColor.BLUE))
                                    .append(Component.text("Announcement").color(NamedTextColor.GOLD))
                                    .append(Component.text("] ").color(NamedTextColor.BLUE))
                                    .append(Component.text(context.getArgument("reason", String.class)).color(NamedTextColor.YELLOW));

                            for(var plr : ModBoxVelocity.server.getAllPlayers()){
                                plr.sendMessage(msg);
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(banNode);
    }
}
