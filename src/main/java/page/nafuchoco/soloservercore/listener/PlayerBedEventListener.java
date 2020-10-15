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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

public class PlayerBedEnterEventListener implements Listener {
    private final PluginSettingsManager settingsManager;

    public PlayerBedEnterEventListener(PluginSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        if (!event.isCancelled()) {
            event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "世界のどこかにいるまだ起きている誰かが眠るのを待っています...");
            int count = 0;
            for (Player player : event.getPlayer().getWorld().getPlayers()) {
                if (!player.equals(event.getPlayer()) && event.getPlayer().getWorld().equals(player.getWorld()) && !player.isSleeping()) {
                    player.sendMessage(ChatColor.DARK_GRAY + "世界のどこかにいる誰かが貴方が眠りにつくのを待っています...");
                    count++;
                }
            }

            if (settingsManager.isBroadcastBedCount()
                    && (event.getPlayer().getWorld().getTime() >= 12542 || event.getPlayer().getWorld().hasStorm())) {
                for (Player player : event.getPlayer().getWorld().getPlayers()) {
                    player.sendMessage(ChatColor.GRAY + "あと" + count + "人がベッドに入ると朝になります。");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        int count = 0;
        for (Player player : event.getPlayer().getWorld().getPlayers()) {
            if (!player.equals(event.getPlayer()) && event.getPlayer().getWorld().equals(player.getWorld()) && !player.isSleeping())
                count++;
        }

        if (settingsManager.isBroadcastBedCount()
                && (event.getPlayer().getWorld().getTime() >= 12542 || event.getPlayer().getWorld().hasStorm())) {
            for (Player player : event.getPlayer().getWorld().getPlayers()) {
                player.sendMessage(ChatColor.GRAY + "あと" + count + "人がベッドに入ると朝になります。");
            }
        }
    }
}
