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

import dev.nafusoft.soloservercore.MobHelper;
import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.data.TempSSCPlayer;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PeacefulModeEventListener implements Listener {

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                event.setCancelled(true);
            } else if (sscPlayer.isPeacefulMode()) {
                if (MobHelper.isOffensive(event.getEntity())
                        && (event.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER
                        || event.getReason() == EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY
                        || event.getReason() == EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                event.setCancelled(true);
            } else if (sscPlayer.isPeacefulMode()) {
                if (player.getHealth() - event.getDamage() < 20.0) {
                    val effect = new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false, false);
                    player.addPotionEffect(effect);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        boolean cancelled = event.isCancelled(); // 今後追加の可能性
        // プレイヤーに対する攻撃に関する処理
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                event.setCancelled(true);
            } else if (sscPlayer.isPeacefulMode()) {
                if (event.getDamager() instanceof TNTPrimed) // TNT爆破の無効化
                    cancelled = true;
            }
        } else if (event.getDamager() instanceof Player player
                && MobHelper.isHostile(event.getEntity())) { // プレイヤーによる攻撃に関する処理
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer.isPeacefulMode() && !player.hasPermission("soloservercore.peaceful.bypass"))
                cancelled = true;
        }

        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                event.setCancelled(true);
            } else if (sscPlayer.isPeacefulMode() && event.getNewEffect() != null) {
                val effectType = event.getNewEffect().getType();
                if (PotionEffectType.POISON.equals(effectType)
                        || PotionEffectType.WITHER.equals(effectType)
                        || PotionEffectType.HUNGER.equals(effectType)
                        || PotionEffectType.CONFUSION.equals(effectType)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                event.setCancelled(true);
            } else if (sscPlayer.isPeacefulMode()) {
                if (event.getFoodLevel() < 20) { // 空腹度の回復
                    val effect = new PotionEffect(PotionEffectType.SATURATION, 40, 0, false, false, false);
                    player.addPotionEffect(effect);
                }
            }
        }
    }
}
