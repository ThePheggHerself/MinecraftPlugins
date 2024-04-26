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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import phewitch.modboxvelocity.Classes.BanManager;

import java.text.SimpleDateFormat;

public class CmdCheckBan {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> banNode = LiteralArgumentBuilder.<CommandSource>literal("checkban")
                .requires(source -> source.hasPermission("mbv.checkban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .executes(context -> {
                            var plrStr = context.getArgument("player", String.class);

                            if (plrStr.equalsIgnoreCase("offline player")) {
                                context.getSource().sendMessage(Component.text("You cannot search for a ban with that term").color(NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            var banInfo = BanManager.getBan(plrStr);
                            if (!banInfo.exists()) {
                                context.getSource().sendMessage(Component.text()
                                        .append(Component.text("Unable to find a ban for ").color(NamedTextColor.YELLOW)
                                                .append(Component.text(plrStr).color(NamedTextColor.RED))).build());
                                return Command.SINGLE_SUCCESS;
                            }

                            var msg = Component.text()
                                    .append(Component.text("Ban found!\nName: ").color(NamedTextColor.GREEN))
                                    .append(Component.text(banInfo.Username).color(NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(Component.text(banInfo.UUID))))
                                    .append(Component.text("\nAdmin: ").color(NamedTextColor.GREEN))
                                    .append(Component.text(banInfo.IssuerUsername).color(NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(Component.text(banInfo.IssuerUUID))))
                                    .append(Component.text("\nTime: ").color(NamedTextColor.GREEN))
                                    .append(Component.text(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(banInfo.CreationTimestamp)).color(NamedTextColor.YELLOW))
                                    .append(Component.text("\nReason: ").color(NamedTextColor.GREEN))
                                    .append(Component.text(banInfo.Reason).color(NamedTextColor.YELLOW));

                            if (!banInfo.isPermBan()) {
                                msg.append(Component.text("\nExpiration: ").color(NamedTextColor.GREEN));
                                msg.append(Component.text(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(banInfo.getExpireDate())).color(NamedTextColor.GREEN));
                            }

                            if (banInfo.isUnbanned()) {
                                msg.append(Component.text("\nUnbanned: ").color(NamedTextColor.GREEN));
                                msg.append(Component.text(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(banInfo.UnbannedTimestamp)).color(NamedTextColor.YELLOW));

                                msg.append(Component.text("\nUnbanned by: ").color(NamedTextColor.GREEN));
                                msg.append(Component.text(banInfo.UnbanUUID).color(NamedTextColor.YELLOW));

                                msg.append(Component.text("\nUnban reason: ").color(NamedTextColor.GREEN));
                                msg.append(Component.text(banInfo.UnbanReason).color(NamedTextColor.YELLOW));
                            }

                            context.getSource().sendMessage(msg.build());

                            return Command.SINGLE_SUCCESS;
                        }))
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(banNode);
    }
}
