/*
 * Copyright 2022 Nafu Satsuki
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

package dev.nafusoft.soloservercore.listener;

import dev.nafusoft.soloservercore.MessageManager;
import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.SoloServerCore;
import dev.nafusoft.soloservercore.data.TempSSCPlayer;
import dev.nafusoft.soloservercore.database.PluginSettingsManager;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
            if (settingsManager.isBroadcastBedCount()
                    && event.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
                val count = event.getBed().getWorld().getPlayers().stream()
                        .filter(player -> !isAfk(player))
                        .filter(player -> !player.isSleeping())
                        .filter(player -> !player.equals(event.getPlayer()))
                        .peek(player -> player.sendMessage(SoloServerCore.getMessage(player, "system.sleeping.waiting.you")))
                        .count();
                if (count > 0) {
                    event.getPlayer().sendMessage(SoloServerCore.getMessage(event.getPlayer(), "system.sleeping.waiting"));
                } else if (settingsManager.isUseAfkCount()
                        && event.getBed().getWorld().getPlayers().stream().anyMatch(this::isAfk)) {
                    event.getPlayer().getWorld().setTime(0);
                }
            }

            val calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 5);
            cooldownMap.put(event.getPlayer(), calendar.getTime());
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        if (!checkCooldown(event.getPlayer())
                && settingsManager.isBroadcastBedCount()
                && (event.getPlayer().getWorld().getTime() >= 12542 || event.getPlayer().getWorld().isThundering())) {
            val count = event.getBed().getWorld().getPlayers().stream()
                    .filter(player -> !player.isSleeping())
                    .filter(player -> !player.equals(event.getPlayer()))
                    .count();
            event.getBed().getWorld().getPlayers().forEach(
                    player -> player.sendActionBar(MessageManager.format(SoloServerCore.getMessage(player, "system.sleeping.count"), count)));
        }
    }


    // Clear cache.
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        cooldownMap.remove(event.getPlayer());
    }

    private boolean checkCooldown(Player player) {
        val date = cooldownMap.get(player);
        if (date != null) {
            val cooldown = date.after(new Date());
            if (!cooldown)
                cooldownMap.remove(player);
            return cooldown;
        }
        return false;
    }

    private boolean isAfk(Player player) {
        if (settingsManager.isUseAfkCount()) {
            val playerData = SoloServerApi.getInstance().getSSCPlayer(player);
            if (playerData instanceof TempSSCPlayer)
                return true;

            val calendar = Calendar.getInstance();
            calendar.setTime(new Date(playerData.getLatestMoveTime()));

            calendar.add(Calendar.MINUTE, settingsManager.getAfkTimeThreshold());

            return new Date().after(calendar.getTime());
        } else {
            return false;
        }
    }
}
