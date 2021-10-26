/*
 * Copyright 2021 NAFU_at
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
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import page.nafuchoco.soloservercore.SoloServerApi;

public class PeacefulModeEventListener implements Listener {

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer.isPeacefulMode()) {
                if (player.getHealth() < 20.0) {
                    val effect = new PotionEffect(PotionEffectType.HEAL, 200, 1, false, false, false);
                    player.addPotionEffect(effect);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer.isPeacefulMode()) {
                boolean cancelled = event.isCancelled();
                if (event.getDamager() instanceof Monster monster) {
                    cancelled = true;

                    monster.setTarget(null); // 攻撃対象から外す
                } else if (event.getDamager() instanceof TNTPrimed) {
                    cancelled = true;
                }

                event.setCancelled(cancelled);
            }
        }
    }

    @EventHandler
    public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        PotionEffectType type = event.getNewEffect().getType();
        if (PotionEffectType.POISON.equals(type)
                || PotionEffectType.WITHER.equals(type)
                || PotionEffectType.HUNGER.equals(type)
                || PotionEffectType.CONFUSION.equals(type)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
            if (sscPlayer.isPeacefulMode()) {
                if (event.getFoodLevel() < 20) { // 空腹度の回復
                    val effect = new PotionEffect(PotionEffectType.SATURATION, 200, 1, false, false, false);
                    player.addPotionEffect(effect);
                }
            }
        }
    }
}
