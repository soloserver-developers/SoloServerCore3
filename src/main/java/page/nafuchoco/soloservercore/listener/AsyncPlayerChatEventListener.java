package page.nafuchoco.soloservercore.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import page.nafuchoco.soloservercore.database.PlayerAndTeamsBridge;
import page.nafuchoco.soloservercore.database.TeamsPlayerData;

public class AsyncPlayerChatEventListener implements Listener {
    private final PlayerAndTeamsBridge playerAndTeamsBridge;

    public AsyncPlayerChatEventListener(PlayerAndTeamsBridge playerAndTeamsBridge) {
        this.playerAndTeamsBridge = playerAndTeamsBridge;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        TeamsPlayerData teamsPlayerData = playerAndTeamsBridge.getPlayerData(event.getPlayer());
        if (teamsPlayerData != null) {
            String message = event.getPlayer().getDisplayName() + " >> " +
                    ChatColor.translateAlternateColorCodes('&', event.getMessage());

            event.getPlayer().sendMessage(message);
            teamsPlayerData.getMembers().forEach(member -> {
                Player player = Bukkit.getPlayer(member);
                if (player != null) {
                    player.sendMessage(message);
                }
            });
        }
        event.setCancelled(true);
    }
}
