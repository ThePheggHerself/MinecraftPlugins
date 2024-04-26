package phewitch.modboxvelocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import phewitch.modboxvelocity.Classes.BanManager;
import phewitch.modboxvelocity.Classes.Data.BanInfo;
import phewitch.modboxvelocity.Classes.SqlManager;
import phewitch.modboxvelocity.Commands.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Plugin(
        id = "modboxvelocity",
        name = "ModBoxVelocity",
        version = "1.0.0-SNAPSHOT"
)
public class ModBoxVelocity {
    public static ProxyServer server;
    public static Logger logger;

    @Inject
    public ModBoxVelocity(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        ModBoxVelocity.server = server;
        ModBoxVelocity.logger = logger;


        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(directory.resolve("config.yml")).build();
            CommentedConfigurationNode root;
            root = loader.load();

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                new SqlManager(
                        root.node("host").getString("localhost"),
                        root.node("port").getInt(3306),
                        root.node("database").getString("minecraft"),
                        root.node("user").getString("minecraft"),
                        root.node("password").getString("totallycoolpassword")
                );

            } catch (Exception e) {
                logger.error("Error loading MySQL driver!\n" + e);
                server.getPluginManager().getPlugin("modboxvelocity").ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
                return;
            }

            loader.save(root);
        } catch (Exception e) {
            logger.error("Could not create config file. Plugin will shut down!\n" + e);
            server.getPluginManager().getPlugin("modboxvelocity").ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
            return;
        }

        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    public static String GetReasonFromArgs(String[] args, String stringDefault) {
        List<String> argList = new LinkedList<String>(Arrays.asList(args));
        argList.remove(0);
        var str = String.join(" ", argList);

        if (str.isBlank())
            return stringDefault;
        else return str;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(Channels.Announcement);
        server.getChannelRegistrar().register(Channels.GlobalChat);
        server.getChannelRegistrar().register(Channels.ServerStatus);

        RegisterCommands();
    }

    public void RegisterCommands() {
        var cmdManager = server.getCommandManager();

        cmdManager.register(cmdManager.metaBuilder("announce").plugin(this).build(), CmdAnnounce.createBrigadierCommand(server));
        cmdManager.register(cmdManager.metaBuilder("ban").plugin(this).build(), CmdBan.createBrigadierCommand(server));
        cmdManager.register(cmdManager.metaBuilder("unban").plugin(this).build(), CmdUnban.createBrigadierCommand(server));
        cmdManager.register(cmdManager.metaBuilder("checkban").plugin(this).build(), CmdCheckBan.createBrigadierCommand(server));
    }

    @Subscribe
    public void onPlayerConnect(LoginEvent event) {
        if (!event.getResult().isAllowed())
            return;

        var banInfo = BanManager.getBan(event.getPlayer());
        if (banInfo.exists() && !banInfo.isUnbanned()) {

            logger.info("BAN FOUND: NAME " + banInfo.Username + " ID: " + banInfo.UUID + " ADDRESS: " + banInfo.IpAddress);

            event.setResult(ComponentResult.denied(banInfo.getBanMessage()));

            server.getScheduler().buildTask(this, () -> {
                logger.info("IP CHECKER: " + event.getPlayer().getUniqueId() + " AND " + banInfo.UUID + " " + banInfo.UUID.equals(event.getPlayer().getUniqueId().toString()));

                if (!banInfo.UUID.equals(event.getPlayer().getUniqueId().toString())) {
                    logger.info("Ban found for Address " + event.getPlayer().getRemoteAddress() + "! "
                            + event.getPlayer().getUsername() + " (" + event.getPlayer().getUniqueId() + ") will also be banned!");

                    BanManager.banPlayer(new BanInfo(banInfo, event), null);
                }
            }).delay(1L, TimeUnit.SECONDS).schedule();
        }
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        logger.info("New message inbound " + event.getSource() + " " + event.getIdentifier());

        if (!(event.getSource() instanceof ServerConnection origin)) {
            return;
        }
        // Ensure the identifier is what you expect before trying to handle the data
        if (event.getIdentifier() == Channels.GlobalChat)
            ChatMessage(event, origin);
        else if (event.getIdentifier() == Channels.Announcement)
            Announcement(event, origin);
        else if (event.getIdentifier() == Channels.ServerStatus) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(origin.getServer().getServerInfo().getName());

            origin.sendPluginMessage(Channels.ServerStatus, out.toByteArray());
        }
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

    public static class Channels {
        public static final MinecraftChannelIdentifier Announcement = MinecraftChannelIdentifier.from("phewitch:velocityannouncements");
        public static final MinecraftChannelIdentifier GlobalChat = MinecraftChannelIdentifier.from("phewitch:chatmessage");
        public static final MinecraftChannelIdentifier ServerStatus = MinecraftChannelIdentifier.from("phewitch:serverstatus");
    }
}
