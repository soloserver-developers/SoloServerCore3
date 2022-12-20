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

package dev.nafusoft.soloservercore;

import dev.nafusoft.soloservercore.data.SSCPlayer;
import dev.nafusoft.soloservercore.data.TempSSCPlayer;
import lombok.val;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AsyncLoginManager {
    private static final Map<UUID, CompletableFuture<LoginResult>> loggingInPlayers = new ConcurrentHashMap<>();

    public static void login(Player player) {
        loggingInPlayers.putIfAbsent(player.getUniqueId(), CompletableFuture.supplyAsync(() -> {
            LoginResult result = null;
            SSCPlayer sscPlayer = SoloServerCore.getInstance().getPlayersTable().getPlayerData(player.getUniqueId());

            if (sscPlayer != null) {
                if (sscPlayer.getSpawnLocationObject().getWorld() != null) {
                    result = new LoginResult(ResultStatus.JOINED, "");
                } else {
                    // 旧プレイヤーデータの削除
                    try {
                        SoloServerCore.getInstance().getPlayersTable().deletePlayer(sscPlayer);
                    } catch (SQLException e) {
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to delete player data.", e);
                    }

                    // チーム情報を確認し所属している場合は脱退
                    if (sscPlayer.getJoinedTeam() != null)
                        sscPlayer.getJoinedTeam().leaveTeam(player);

                    if (SoloServerCore.getInstance().getPluginSettingsManager().isReteleportResetAll()) {
                        player.getInventory().clear();
                        player.getEnderChest().clear();
                        player.setLevel(0);
                        player.setExp(0F);
                        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
                        player.setFireTicks(0);
                        player.setHealth(20D);
                        player.setFoodLevel(20);
                        player.setSaturation(20F);
                    }
                }
            }

            if (result == null) {
                val location = AsyncSafeLocationUtil.generateNewRandomLocation();

                if (location != null) {
                    try {
                        SoloServerCore.getInstance().getLogger().info("Generated new location for " + player.getName() + ": " + location);
                        ((TempSSCPlayer) SoloServerApi.getInstance().getSSCPlayer(player)).setGeneratedLocation(location);
                        result = new LoginResult(ResultStatus.FIRST_JOINED, "");
                    } catch (Exception e) {
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to generate new location.", e);
                    }
                } else {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "There is no stock of teleport coordinates. Please execute regeneration.");

                    result = new LoginResult(ResultStatus.FAILED, "System is in preparation.");
                }
            }
            return result;
        }));
    }

    public static CompletableFuture<LoginResult> getLoginResult(Player player) {
        val future = loggingInPlayers.remove(player.getUniqueId());
        SoloServerCore.getInstance().getLogger().info("Login result for " + player.getName() + ": " + future);
        return future;
    }


    public enum ResultStatus {
        JOINED, FIRST_JOINED, FAILED
    }

    public record LoginResult(ResultStatus status,
                              String message) {
    }
}
