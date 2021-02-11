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

package page.nafuchoco.soloservercore.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStore {
    private final Map<UUID, PlayerData> dataStore = new HashMap<>();

    private final PlayersTable playersTable;

    public PlayerDataStore(PlayersTable playersTable) {
        this.playersTable = playersTable;
    }

    public PlayerData getPlayerData(@NotNull Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(@NotNull UUID uuid) {
        PlayerData playerData = dataStore.get(uuid);
        if (Bukkit.getPlayer(uuid) == null) {
            return playerData == null ? dataStore.put(uuid, playersTable.getPlayerData(uuid)) : playerData;
        } else {
            if (playerData == null) {
                // PlayerがInGameの場合Nullの可能性はない。
                return new InGamePlayerData(playersTable.getPlayerData(uuid));
            } else if (playerData instanceof PlayerData) {
                return new InGamePlayerData(playerData);
            } else {
                return playerData;
            }
        }
    }
}
