package phewitch.velocitytesting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.slf4j.Logger;
import phewitch.velocitytesting.Commands.CmdAnnounce;


@Plugin(
        id = "velocitytesting",
        name = "VelocityTesting",
        version = BuildConstants.VERSION
)
public class VelocityTesting {
    public static final MinecraftChannelIdentifier IdfAnnouncement = MinecraftChannelIdentifier.from("phewitch:velocityannouncements");
    public static final MinecraftChannelIdentifier IdfGlobalChat = MinecraftChannelIdentifier.from("phewitch:chatmessage");
    public static final MinecraftChannelIdentifier IdfServerStatus = MinecraftChannelIdentifier.from("phewitch:serverstatus");
    public static ProxyServer server;
    public static Logger logger;
    //public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("bungeecord");

    @Inject
    public VelocityTesting(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(IdfAnnouncement);
        server.getChannelRegistrar().register(IdfGlobalChat);
        server.getChannelRegistrar().register(IdfServerStatus);

        RegisterCommands();
    }

    public void RegisterCommands(){
        var cmdManager = server.getCommandManager();

        cmdManager.register(cmdManager.metaBuilder("announce").aliases("announcement", "ga", "globalannouncement").plugin(this).build(), new CmdAnnounce());
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        logger.info("New message inbound " + event.getSource() + " " + event.getIdentifier());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        ServerConnection origin = (ServerConnection) event.getSource();
        // Ensure the identifier is what you expect before trying to handle the data
        if (event.getIdentifier() == IdfGlobalChat)
            ChatMessage(event, origin);
        else if (event.getIdentifier() == IdfAnnouncement)
            Announcement(event, origin);
    }

    public void ChatMessage(PluginMessageEvent event, ServerConnection origin) {
        var originInfo = origin.getServerInfo();
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        var str = in.readUTF();
        var oldMsg = JSONComponentSerializer.json().deserialize(str);
        var newMessage = ServerIdentifierFromEvent(originInfo).append(oldMsg);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(JSONComponentSerializer.json().serialize(newMessage));

        for (RegisteredServer server : server.getAllServers()) {
            var sInfo = server.getServerInfo();

            if (sInfo.getName() != originInfo.getName()) {
                var success = server.sendPluginMessage(IdfGlobalChat, out.toByteArray());
            }
        }
    }

    public void Announcement(PluginMessageEvent event, ServerConnection origin) {
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        var message = in.readUTF();
        var msg = Component.text()
                .append(Component.text("[").color(NamedTextColor.BLUE))
                .append(Component.text("Announcement").color(NamedTextColor.GOLD))
                .append(Component.text("] ").color(NamedTextColor.BLUE))
                .append(Component.text(message).color(NamedTextColor.YELLOW));

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(JSONComponentSerializer.json().serialize(msg.build()));

        for (RegisteredServer server : server.getAllServers()) {
            var success = server.sendPluginMessage(IdfAnnouncement, out.toByteArray());
        }
    }

    public Component ServerIdentifierFromEvent(ServerInfo server) {
        if (server.getName().equals("Survival"))
            return Component.text("[SV] -> ").color(NamedTextColor.DARK_GRAY).hoverEvent(HoverEvent.showText(Component.text(server.getName())));
        else if (server.getName().equals("SkyBlock"))
            return Component.text("[SB] -> ").color(NamedTextColor.DARK_GRAY).hoverEvent(HoverEvent.showText(Component.text(server.getName())));
        else if (server.getName().equals("AcidIsland"))
            return Component.text("[AI] -> ").color(NamedTextColor.DARK_GRAY).hoverEvent(HoverEvent.showText(Component.text(server.getName())));
        else if (server.getName().equals("Creative"))
            return Component.text("[CP] -> ").color(NamedTextColor.DARK_GRAY).hoverEvent(HoverEvent.showText(Component.text(server.getName())));

        return Component.text("[OT] -> ").color(NamedTextColor.DARK_GRAY).hoverEvent(HoverEvent.showText(Component.text(server.getName())));
    }
}
