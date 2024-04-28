package phewitch.modbox;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.modbox.Classes.SqlManager;
import phewitch.modbox.Commands.CommandBase.CustomCommand;
import phewitch.modbox.Commands.CommandBase.IPlayerOnlyCommand;
import phewitch.modbox.EventListeners.*;

import java.text.SimpleDateFormat;
import java.util.*;

public final class ModBox extends JavaPlugin {
    public static ModBox Instance;
    public static ProtocolManager protocolManager;
    public static LuckPerms LuckPermsAPI;
    public static Map<String, CustomCommand> CommandMap = new HashMap<>();

    public static String formatSeconds(int secs) {
        Date d = new Date(secs * 1000L);
        SimpleDateFormat df = new SimpleDateFormat("mm:ss"); // HH for 0-23
        return df.format(d);
    }

    @Override
    public void onEnable() {
        Instance = this;
        var logger = this.getLogger();
        protocolManager = ProtocolLibrary.getProtocolManager();

        @Nullable RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPermsAPI = provider.getProvider();
        }

        logger.info("Checking config and SQL Connection");
        if (!RegisterConfig()) {
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

        logger.info("Registering events");
        if (!RegisterEvents()) {
            logger.warning("Unable to register events. Plugin will not be loaded");
            setEnabled(false);
            return;
        }

        logger.info("Registering recipes");
        if (!RegisterRecipes()) {
            logger.warning("Unable to register recipes. Plugin will not be loaded");
            setEnabled(false);
            return;
        }



        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimer(ModBox.Instance, UpdateTablist::Update, 0L  /*<-- the initial delay */, 20L * 1 /*<-- the interval */);
    }

    public boolean RegisterConfig() {
        var config = this.getConfig();

        if (!config.contains("plugin-version")) {
            config.options().copyDefaults(true);
            saveConfig();
        }

        var dbConfig = config.getConfigurationSection("database").getValues(false);

        new SqlManager(dbConfig.get("host").toString(),
                Integer.parseInt(dbConfig.get("port").toString()),
                dbConfig.get("database").toString(),
                dbConfig.get("user").toString(),
                dbConfig.get("password").toString()
        );

        return SqlManager.Instance.checkConnection();
    }

    public boolean RegisterPluginChannels() {

        var messageHandler = new PluginMessages();

        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.Announcement);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.Announcement, messageHandler);

        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.GlobalChat);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.GlobalChat, messageHandler);

        return true;
    }

    public boolean RegisterEvents() {
        getServer().getPluginManager().registerEvents(new JoiningAndLeaving(), this);
        getServer().getPluginManager().registerEvents(new ChatNotifications(), this);
        getServer().getPluginManager().registerEvents(new CommandEventListener(), this);
        getServer().getPluginManager().registerEvents(new PortalEvents(), this);
        getServer().getPluginManager().registerEvents(new HardcoreEventListeners(), this);
        return true;
    }

    public boolean RegisterRecipes(){
        var key = new NamespacedKey(this, "cookedbread");
        Recipe recipe = new FurnaceRecipe(key , new ItemStack(Material.BREAD, 1), Material.WHEAT, 0, 10 * 20);
        Bukkit.addRecipe(recipe);

        key = new NamespacedKey(this, "cookedleather");
        recipe = new FurnaceRecipe(key , new ItemStack(Material.LEATHER, 1), Material.ROTTEN_FLESH, 0, 40 * 20);
        Bukkit.addRecipe(recipe);

        return true;
    }

    public boolean RegisterEnchants(){
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command baseCommand, @NotNull String alias, @NotNull String[] args) {
        var command = getCustomCommand(sender, baseCommand, alias, args);

        return super.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command baseCommand, final String label, final String[] args) {
        var command = getCustomCommand(sender, baseCommand, label, args);

        if ((command instanceof IPlayerOnlyCommand) && !(sender instanceof Player plr)) {
            sender.sendMessage(Component.text("Only players can run this command").color(NamedTextColor.RED));
            return false;
        }

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

    public CustomCommand getCustomCommand(@NotNull CommandSender sender, @NotNull Command baseCommand, @NotNull String alias, @NotNull String[] args) {
        try {

            if (CommandMap.containsKey(baseCommand.getName()))
                return CommandMap.get(baseCommand.getName());

            try {
                getLogger().info("Registering command: " + baseCommand.getName());
                var customCommand = (CustomCommand) this.getClassLoader().loadClass("phewitch.modbox.Commands." + baseCommand.getName())
                        .getDeclaredConstructor(String.class).newInstance(baseCommand.getName());

                CommandMap.put(baseCommand.getName(), customCommand);
                baseCommand.register(Bukkit.getCommandMap());
                var cmd = getCommand(baseCommand.getName());
                cmd.setTabCompleter(customCommand);
                cmd.setExecutor(this::onCommand);

                return customCommand;

            } catch (Exception e) {
                this.getLogger().warning(e.toString());
                return null;
            }
        } catch (Exception e) {
            this.getLogger().warning(e.toString());
            return null;
        }
    }

    public static class Channels {
        public static final String Announcement = "phewitch:velocityannouncements";
        public static final String GlobalChat = "phewitch:chatmessage";
        public static final String ServerStatus = "phewitch:serverstatus";
    }

    public static String beautifyString(String string){
        var arr = string.split("_| ", -2);
        ArrayList<String> arr2 = new ArrayList<>(){};
        for (var str : arr)
            arr2.add(str.substring(0, 1).toUpperCase() + str.substring(1));
        return String.join (" ", arr);
    }

    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    public final static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

}
