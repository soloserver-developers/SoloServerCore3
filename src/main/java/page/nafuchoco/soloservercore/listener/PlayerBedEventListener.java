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

import org.apache.commons.lang.text.ExtendedMessageFormat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import page.nafuchoco.soloservercore.database.InGamePlayerData;
import page.nafuchoco.soloservercore.database.PlayerDataStore;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerBedEventListener implements Listener {
    private final Map<Player, Date> cooldownMap;
    private final PluginSettingsManager settingsManager;
    private final PlayerDataStore playerDataStore;

    public PlayerBedEventListener(PluginSettingsManager settingsManager, PlayerDataStore playerDataStore) {
        this.settingsManager = settingsManager;
        this.playerDataStore = playerDataStore;
        cooldownMap = new LinkedHashMap<>();
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        if (!event.isCancelled() && !checkCooldown(event.getPlayer())) {
            if (settingsManager.isBroadcastBedCount()
                    && event.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
                long count = event.getBed().getWorld().getPlayers().stream()
                        .filter(player -> !isAfk(player))
                        .filter(player -> !player.isSleeping())
                        .filter(player -> !player.equals(event.getPlayer()))
                        .peek(player -> player.sendMessage(ChatColor.DARK_GRAY + "世界のどこかにいる誰かが貴方が眠りにつくのを待っています..."))
                        .count();
                if (count > 0) {
                    event.getBed().getWorld().getPlayers().forEach(
                            player -> player.sendMessage(ChatColor.GRAY
                                    + ExtendedMessageFormat.format("あと{0}人がベッドに入ると朝になります。", count)));
                    event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "世界のどこかにいるまだ起きている誰かが眠るのを待っています...");
                } else if (settingsManager.isUseAfkCount()
                        && event.getBed().getWorld().getPlayers().stream().filter(player -> isAfk(player)).count() > 0) {
                    event.getPlayer().getWorld().setTime(0);
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 5);
            cooldownMap.put(event.getPlayer(), calendar.getTime());
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        if (!checkCooldown(event.getPlayer())
                && settingsManager.isBroadcastBedCount()
                && (event.getPlayer().getWorld().getTime() >= 12542 || event.getPlayer().getWorld().hasStorm())) {
            long count = event.getBed().getWorld().getPlayers().stream()
                    .filter(player -> !player.isSleeping())
                    .filter(player -> !player.equals(event.getPlayer()))
                    .count();
            event.getBed().getWorld().getPlayers().forEach(
                    player -> player.sendMessage(ChatColor.GRAY
                            + ExtendedMessageFormat.format("あと{0}人がベッドに入ると朝になります。", count)));
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
            boolean cooldown = date.after(new Date());
            if (!cooldown)
                cooldownMap.remove(player);
            return cooldown;
        }
        return false;
    }

    private boolean isAfk(Player player) {
        if (settingsManager.isUseAfkCount()) {
            InGamePlayerData playerData = (InGamePlayerData) playerDataStore.getPlayerData(player);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(playerData.getLatestMoveTime()));

            calendar.add(Calendar.MINUTE, settingsManager.getAfkTimeThreshold());

            return new Date().after(calendar.getTime());
        } else {
            return false;
        }
    }
}
