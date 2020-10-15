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
import org.bukkit.event.player.PlayerQuitEvent;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerBedEventListener implements Listener {
    private final Map<Player, Date> cooldownMap;
    private final PluginSettingsManager settingsManager;

    public PlayerBedEventListener(PluginSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        cooldownMap = new LinkedHashMap<>();
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        if (!event.isCancelled() && !checkCooldown(event.getPlayer())) {
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

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 5);
            cooldownMap.put(event.getPlayer(), calendar.getTime());
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        if (!checkCooldown(event.getPlayer())) {
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


    // Clear cache.
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        cooldownMap.remove(event.getPlayer());
    }

    private boolean checkCooldown(Player player) {
        Date date = cooldownMap.get(player);
        if (date != null) {
            boolean cooldown = date.before(new Date());
            if (!cooldown)
                cooldownMap.remove(player);
            return cooldown;
        }
        return false;
    }
}
