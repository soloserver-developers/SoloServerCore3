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

package page.nafuchoco.soloservercore;

import lombok.val;
import org.bukkit.entity.Player;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AsyncLoginManager {

    public static CompletableFuture<LoginResult> login(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            LoginResult result;
            if (SoloServerCore.getInstance().getPlayersTable().getPlayerData(player.getUniqueId()) == null) {
                val location = AsyncSafeLocationUtil.generateNewRandomLocation();

                if (location != null) {
                    val sscPlayer = new InGameSSCPlayer(player.getUniqueId(),
                            location,
                            null,
                            player,
                            true,
                            null,
                            false);
                    try {
                        SoloServerApi.getInstance().registerSSCPlayer(sscPlayer);
                        result = new LoginResult(ResultStatus.FIRST_JOINED, "");
                    } catch (SQLException | NullPointerException exception) {
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the player data.\n" +
                                "New data will be regenerated next time.", exception);

                        result = new LoginResult(ResultStatus.FAILED, "The login process was interrupted due to a system problem.");
                    }
                } else {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "There is no stock of teleport coordinates. Please execute regeneration.");

                    result = new LoginResult(ResultStatus.FAILED, "System is in preparation.");
                }
            } else {
                result = new LoginResult(ResultStatus.JOINED, "");
            }
            return result;
        });
    }


    public enum ResultStatus {
        JOINED, FIRST_JOINED, FAILED
    }

    public record LoginResult(ResultStatus status,
                              String message) {
    }
}
