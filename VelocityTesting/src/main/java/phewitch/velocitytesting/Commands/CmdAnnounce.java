package phewitch.velocitytesting.Commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import phewitch.velocitytesting.VelocityTesting;

public class CmdAnnounce implements SimpleCommand {
    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("You need to provide a message").color(NamedTextColor.RED));
            return;
        }

        var message = String.join(" ", args);

        var msg = Component.text()
                .append(Component.text("[").color(NamedTextColor.BLUE))
                .append(Component.text("Announcement").color(NamedTextColor.GOLD))
                .append(Component.text("] ").color(NamedTextColor.BLUE))
                .append(Component.text(message).color(NamedTextColor.YELLOW));

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(JSONComponentSerializer.json().serialize(msg.build()));

        for (RegisteredServer server : VelocityTesting.server.getAllServers()) {
            var success = server.sendPluginMessage(VelocityTesting.Channels.Announcement, out.toByteArray());
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("velocity.announcement");
    }
}
