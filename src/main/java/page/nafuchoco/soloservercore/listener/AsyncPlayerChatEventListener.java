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

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.SoloServerCore;

import java.util.Objects;

public class AsyncPlayerChatEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        val message = event.getPlayer().getDisplayName() + " >> " +
                ChatColor.translateAlternateColorCodes('&', event.getMessage());
        SoloServerCore.getInstance().getLogger().info(message);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("soloservercore.chat.bypass"))
                .forEach(p -> p.sendMessage(ChatColor.GRAY + "[Chat] " + message));

        val joinedTeam = SoloServerApi.getInstance().getPlayersTeam(event.getPlayer());
        if (joinedTeam != null) {

            val owner = Bukkit.getPlayer(joinedTeam.getOwner());
            if (owner != null)
                owner.sendMessage(message);

            joinedTeam.getMembers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.sendMessage(message));
        }
        event.setCancelled(true);
    }
}
