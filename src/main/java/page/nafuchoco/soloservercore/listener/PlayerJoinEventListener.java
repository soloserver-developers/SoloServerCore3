/*
 * Copyright 2020 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.soloservercore.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerJoinEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage("");

        InGameSSCPlayer sscPlayer = SoloServerApi.getInstance().getSSCPlayer(event.getPlayer());
        if (sscPlayer.isFirstJoined()) {
            // MVとの競合に対する対策
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SoloServerCore.getInstance(),
                    () -> {
                        Location location = sscPlayer.getSpawnLocationObject();
                        event.getPlayer().teleport(location);
                        SoloServerCore.getInstance().getLogger().log(Level.INFO,
                                event.getPlayer().getName() +
                                        " has been successfully teleported to " +
                                        location.getBlockX() + ", " +
                                        location.getBlockY() + ", " +
                                        location.getBlockZ());
                    }, 10L);
            event.getPlayer().setCompassTarget(sscPlayer.getSpawnLocationObject());
        }

        PlayersTeam joinedTeam = SoloServerApi.getInstance().getPlayersTeam(event.getPlayer());
        List<UUID> member;
        if (joinedTeam != null) {
            member = new ArrayList<>(joinedTeam.getMembers());
            member.add(joinedTeam.getOwner());
        } else {
            member = new ArrayList<>();
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.forEach(player -> {
            if (!player.equals(event.getPlayer()) && !member.contains(player.getUniqueId())) {
                event.getPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                player.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
            }
        });

        if (member.contains(event.getPlayer().getUniqueId()))
            member.forEach(m -> {
                Player player = Bukkit.getPlayer(m);
                if (player != null && !player.equals(event.getPlayer()))
                    player.sendMessage(ChatColor.AQUA + "[Teams]" + event.getPlayer().getDisplayName() + "がログインしました。");
            });
    }
}
