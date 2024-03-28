package phewitch.pheatures.DataClasses;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPRequest {

    public static Map<UUID, TPRequest> PendingRequests = new HashMap<UUID, TPRequest>();

    public UUID RequestTarget;
    public UUID RequestSender;
    public Long RequestTime;

    public int Timeout = 30;
    public Location Location;
    public RequestType Type;


    public TPRequest(Player sender, Player target, RequestType type){
        RequestTarget = target.getUniqueId();
        RequestSender = sender.getUniqueId();
        Location = null;
        RequestTime = System.currentTimeMillis() * 1000;
        Type = type;
    }

    public TPRequest(Player sender, Player target, RequestType type, Location location){
        RequestTarget = target.getUniqueId();
        RequestSender = sender.getUniqueId();
        Location = location;
        RequestTime = System.currentTimeMillis() * 1000;
        Type = type;
    }

    public boolean hasExpired(){
        return RequestTime - (System.currentTimeMillis() * 1000) > Timeout;
    }

    public void SetTimeout(int timeout){
        Timeout = timeout;
    }

    public enum RequestType{
        SenderToTarget,
        TargetToSender,
        SenderToLocation,
        TargetToLocation
    }
}
