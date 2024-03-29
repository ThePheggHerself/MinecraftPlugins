package phewitch.pheatures;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.pheatures.DataClasses.CustomCommand;
import phewitch.pheatures.Handlers.ChatAndNotifications;
import phewitch.pheatures.Handlers.PluginMessages;
import phewitch.pheatures.Handlers.UpdateTablist;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class Pheatures extends JavaPlugin {
    public static Pheatures Instance;
    public static LuckPerms LuckPermsAPI;

    public static Map<String, CustomCommand> CommandMap = new HashMap<>();

    public static MysqlDataSource GetSQLConnection() {
        var config = Instance.getConfig();
        var dbConfig = config.getConfigurationSection("database").getValues(false);
        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

        dataSource.setServerName(dbConfig.get("host").toString());
        dataSource.setPort(Integer.parseInt(dbConfig.get("port").toString()));
        dataSource.setDatabaseName(dbConfig.get("database").toString());
        dataSource.setUser(dbConfig.get("user").toString());
        dataSource.setPassword(dbConfig.get("password").toString());

        return dataSource;
    }

    @Override
    public void onEnable() {
        Instance = this;
        var logger = this.getLogger();
        logger.info("Loading Pheatures plugin");

        @Nullable RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPermsAPI = provider.getProvider();
        }

        String response = "";

        logger.info("Loading config");
        RegisterConfig();

        logger.info("Checking SQL Connection");
        if (!CheckSQL()) {
            logger.warning("Unable to connect to database. Plugin will not be loaded");
            setEnabled(false);
            return;
        }

        logger.info("Registering plugin message channels");
        if (!RegisterPluginChannels()) {
            logger.warning("Unable to register commands. Plugin will not be loaded");
            setEnabled(false);
            return;
        }

        logger.info("Registering commands");
        if (!RegisterCommands()) {
            logger.warning("Unable to register commands. Plugin will not be loaded");
            setEnabled(false);
            return;
        }

        logger.info("Registering events");
        if (!RegisterEvents()) {
            logger.warning("Unable to register events. Plugin will not be loaded");
            setEnabled(false);
            return;
        }

        new UpdateTablist();


    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command baseCommand, @NotNull String label, @NotNull String[] args) {
        if (!CommandMap.containsKey(baseCommand.getName())) {
            try {
                getLogger().info("Registering command: " + baseCommand.getName());
                var customCommand = (CustomCommand) this.getClassLoader().loadClass("phewitch.pheatures.commands." + baseCommand.getName())
                        .getDeclaredConstructor(String.class).newInstance(baseCommand.getName());

                CommandMap.put(baseCommand.getName(), customCommand);
                baseCommand.register(Bukkit.getCommandMap());

            } catch (Exception e) {
                this.getLogger().warning(e.toString());
                return false;
            }
        }

        getLogger().info(CommandMap.containsKey(baseCommand.getName()) + " ");

        var command = CommandMap.get(baseCommand.getName());

        getLogger().info("AAAAAAAAAAAA");

        if (command.RequirePlayer && !(sender instanceof Player plr)) {
            sender.sendMessage(Component.text("Only players can run this command").color(NamedTextColor.RED));
            return false;
        }

        getLogger().info("BBBBB");

        var perm = command.getPermission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage(Component.text("You do not have permission to run this command").color(NamedTextColor.RED));
            return false;
        }
        var msg = command.hasValidArguments(args);

        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(Component.text(msg).color(NamedTextColor.YELLOW));
            return false;
        }

        return command.onCommand(sender, baseCommand, label, args);
    }

    public boolean RegisterConfig() {
        var config = this.getConfig();

        if (config.contains("plugin-version"))
            return true;

        config.options().copyDefaults(true);
        saveConfig();
        return true;
    }

    public boolean CheckSQL() {
        try {
            try (Connection conn = GetSQLConnection().getConnection()) {
                if (!conn.isValid(5)) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // This should be replaced with a propper logging solution. don't do this.
            return false;
        }
    }

    public boolean RegisterPluginChannels() {

        var messageHandler = new PluginMessages();

        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.Announcement);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.Announcement, messageHandler);

        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.GlobalChat);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.GlobalChat, messageHandler);

        return true;
    }

    public boolean RegisterCommands() {
        try {
            var path = "phewitch.pheatures.commands.";
            var list = PluginCommandYamlParser.parse(this);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean RegisterEvents() {
        getServer().getPluginManager().registerEvents(new ChatAndNotifications(), this);
        return true;
    }


    public static class Channels {
        public static final String Announcement = "phewitch:velocityannouncements";
        public static final String GlobalChat = "phewitch:chatmessage";
        public static final String ServerStatus = "phewitch:serverstatus";
    }
}
