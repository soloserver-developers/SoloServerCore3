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

import java.util.UUID;

public class InGameSSCPlayer implements SSCPlayer {
    private final UUID id;
    private final String spawnLocation;
    private UUID joinedTeam;

    private final Player player;
    private long latestMoveTime;

    public InGameSSCPlayer(UUID id, String spawnLocation, UUID joinedTeam, Player player) {
        this.id = id;
        this.spawnLocation = spawnLocation;
        this.joinedTeam = joinedTeam;
        this.player = player;
    }

    public InGameSSCPlayer(OfflineSSCPlayer offlineSSCPlayer, Player player) {
        this.id = offlineSSCPlayer.getId();
        this.spawnLocation = offlineSSCPlayer.getSpawnLocation();
        this.joinedTeam = offlineSSCPlayer.getJoinedTeam();
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
    public @Nullable UUID getJoinedTeam() {
        return joinedTeam;
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
