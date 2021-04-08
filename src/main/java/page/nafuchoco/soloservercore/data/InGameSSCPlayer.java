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

package page.nafuchoco.soloservercore.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerApi;

import java.util.UUID;

public class InGameSSCPlayer implements SSCPlayer {
    private final UUID id;
    private final String spawnLocation;
    private PlayersTeam joinedTeam;

    private final Player player;
    private long latestMoveTime;

    public InGameSSCPlayer(@NotNull UUID id, @NotNull String spawnLocation, @Nullable UUID joinedTeamId, @NotNull Player player) {
        this.id = id;
        this.spawnLocation = spawnLocation;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;
        this.player = player;
    }

    public InGameSSCPlayer(@NotNull OfflineSSCPlayer offlineSSCPlayer, @NotNull Player player) {
        this.id = offlineSSCPlayer.getId();
        this.spawnLocation = offlineSSCPlayer.getSpawnLocation();
        this.joinedTeam = (offlineSSCPlayer.getJoinedTeamId() != null) ?
                SoloServerApi.getInstance().getPlayersTeam(offlineSSCPlayer.getJoinedTeamId()) : null;
        this.player = player;
    }

    @Override
    public @NotNull UUID getId() {
        return id;
    }

    @Override
    public @NotNull String getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public @Nullable PlayersTeam getJoinedTeam() {
        return joinedTeam;
    }

    public void setJoinedTeam(PlayersTeam joinedTeam) { // TODO: 2021/04/07 パブリックにしたくない。
        this.joinedTeam = joinedTeam;
    }

    public Player getPlayer() {
        return player;
    }

    public long getLatestMoveTime() {
        return latestMoveTime;
    }

    void setLatestMoveTime(long latestMoveTime) {
        this.latestMoveTime = latestMoveTime;
    }
}
