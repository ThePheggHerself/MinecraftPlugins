package phewitch.velocitytesting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import phewitch.velocitytesting.Commands.CmdAnnounce;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;


@Plugin(
        id = "velocitypheatures",
        name = "VelocityPheatures",
        version = BuildConstants.VERSION
)
public class VelocityTesting {
    public static class Channels{
        public static final MinecraftChannelIdentifier Announcement = MinecraftChannelIdentifier.from("phewitch:velocityannouncements");
        public static final MinecraftChannelIdentifier GlobalChat = MinecraftChannelIdentifier.from("phewitch:chatmessage");
        public static final MinecraftChannelIdentifier ServerStatus = MinecraftChannelIdentifier.from("phewitch:serverstatus");
    }

    public static ProxyServer server;
    public static Logger logger;
    public static YamlDocument config;

    @Inject
    public VelocityTesting(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;

        try{
            config = YamlDocument.create(new File(directory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());

            config.update();
            config.save();
        }
        catch (Exception e){
            logger.error("Could not create config file. Plugin will shut down!\n" + e.toString());
            server.getPluginManager().getPlugin("velocitypheatures").ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(Channels.Announcement);
        server.getChannelRegistrar().register(Channels.GlobalChat);
        server.getChannelRegistrar().register(Channels.ServerStatus);

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
        if (event.getIdentifier() == Channels.GlobalChat)
            ChatMessage(event, origin);
        else if (event.getIdentifier() == Channels.Announcement)
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
                var success = server.sendPluginMessage(Channels.GlobalChat, out.toByteArray());
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
            var success = server.sendPluginMessage(Channels.Announcement, out.toByteArray());
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
