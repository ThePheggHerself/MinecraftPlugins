package phewitch.modboxforge;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.Logging;

public class Events {
    @SubscribeEvent
    public void onPlayerConnect(ClientPlayerNetworkEvent.LoggingIn event)
    {
        ModBoxForge.LOGGER.info(event.getPlayer().getName() + " has joined");
    }
}
