package phewitch.pheatures;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phewitch.pheatures.Commands.CmdTPA;
import phewitch.pheatures.Commands.CmdTPAccept;
import phewitch.pheatures.DataClasses.TPRequest;
import phewitch.pheatures.Handlers.ChatWelcomeAndTablist;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Pheatures extends JavaPlugin implements CommandExecutor, PluginMessageListener {

    public static Pheatures Instance;
    public static String IdfAnnouncement = "phewitch:velocityannouncements";
    public static String IdfGlobalChat = "phewitch:chatmessage";
    public static String IdfServerStatus = "phewitch:serverstatus";
    public static LuckPerms LuckPermsAPI;

    @Override
    public void onEnable() {
        Instance = this;
        RegisterPluginChannels();
        RegisterCommands();

        @Nullable RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPermsAPI = provider.getProvider();
        }

        getServer().getPluginManager().registerEvents(new ChatWelcomeAndTablist(), this);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeBoolean(true);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        //scheduler.runTaskTimer(this, () -> HandleTPARequestTimeout(), 20L * 10L /*<-- the initial delay */, 10L /*<-- the interval */);
    }

    public void HandleTPARequestTimeout(){
        ArrayList<UUID> toRemove = new ArrayList<>();
        for (var req : TPRequest.PendingRequests.entrySet()){
            if(req.getValue().hasExpired())
                toRemove.add(req.getKey());
        }

        for (UUID exp : toRemove) {
            TPRequest.PendingRequests.remove(exp);
        }
    }

    public void RegisterPluginChannels() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, IdfAnnouncement);
        getServer().getMessenger().registerIncomingPluginChannel(this, IdfAnnouncement, this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, IdfGlobalChat);
        getServer().getMessenger().registerIncomingPluginChannel(this, IdfGlobalChat, this);
    }

    public void RegisterCommands(){
        this.getCommand("tpa").setExecutor(new CmdTPA());
        this.getCommand("tpaccept").setExecutor(new CmdTPAccept());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if(channel.equals(IdfGlobalChat) || channel.equals(IdfServerStatus)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            var json = in.readUTF();

            var msg = JSONComponentSerializer.json().deserialize(json);
            this.getServer().broadcast(msg);
        }
        if (channel.equals(IdfAnnouncement)){
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            this.getServer().broadcast(JSONComponentSerializer.json().deserialize(in.readUTF()));

            for (Player plr : getServer().getOnlinePlayers()) {
                plr.playNote(plr.getLocation(), Instrument.BELL, new Note(4));
            }
        }
    }

    @Override
    public void onDisable() {
    }
}
