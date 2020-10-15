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

package page.nafuchoco.soloservercore;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import page.nafuchoco.soloservercore.database.PlayerData;
import page.nafuchoco.soloservercore.team.PlayersTeam;

import java.util.UUID;

public final class SoloServerApi {
    private final SoloServerCore soloServerCore;

    public static SoloServerApi getSoloServerApi() {
        SoloServerCore core = SoloServerCore.getInstance();
        if (core == null)
            return null;
        return new SoloServerApi(core);
    }

    private SoloServerApi(SoloServerCore soloServerCore) {
        this.soloServerCore = soloServerCore;
    }

    public Location getPlayerSpawn(Player player) {
        return getPlayerSpawn(player.getUniqueId());
    }

    public Location getPlayerSpawn(UUID player) {
        return soloServerCore.getSpawnPointLoader().getSpawn(player);
    }

    public UUID getPlayerJoinedTeamUUID(UUID player) {
        PlayerData playerData = soloServerCore.getPlayersTable().getPlayerData(player);
        if (playerData != null)
            return playerData.getJoinedTeam();
        return null;
    }

    public PlayersTeam getPlayerJoinedTeam(UUID player) {
        UUID uuid = getPlayerJoinedTeamUUID(player);
        if (uuid != null)
            return soloServerCore.getPlayersTeamsTable().getPlayersTeam(uuid);
        return null;
    }
}
