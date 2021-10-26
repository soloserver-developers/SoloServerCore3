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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerApi;

import java.util.UUID;

public class InGameSSCPlayer implements SSCPlayer {
    private final UUID id;
    private final String spawnLocation;
    private PlayersTeam joinedTeam;
    private String fixedHomeLocation;

    private final Player player;
    private long latestMoveTime;
    private final boolean firstJoined;

    public InGameSSCPlayer(@NotNull UUID id,
                           @NotNull String spawnLocation,
                           @Nullable UUID joinedTeamId,
                           @NotNull Player player,
                           @NotNull boolean firstJoined,
                           @Nullable String fixedHomeLocation) {
        this.id = id;
        this.spawnLocation = spawnLocation;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;
        this.player = player;
        this.firstJoined = firstJoined;
        this.fixedHomeLocation = fixedHomeLocation;
    }

    public InGameSSCPlayer(@NotNull UUID id,
                           @NotNull Location location,
                           @Nullable UUID joinedTeamId,
                           @NotNull Player player,
                           @NotNull boolean firstJoined,
                           @Nullable Location fixedHomeLocation) {
        this.id = id;

        val locationJson = new JsonObject();
        locationJson.addProperty("World", location.getWorld().getName());
        locationJson.addProperty("X", location.getBlockX());
        locationJson.addProperty("Y", location.getBlockY());
        locationJson.addProperty("Z", location.getBlockZ());
        this.spawnLocation = new Gson().toJson(locationJson);

        if (fixedHomeLocation != null) {
            val fixedHomeJson = new JsonObject();
            fixedHomeJson.addProperty("World", fixedHomeLocation.getWorld().getName());
            fixedHomeJson.addProperty("X", fixedHomeLocation.getBlockX());
            fixedHomeJson.addProperty("Y", fixedHomeLocation.getBlockY());
            fixedHomeJson.addProperty("Z", fixedHomeLocation.getBlockZ());
            this.fixedHomeLocation = new Gson().toJson(fixedHomeJson);
        } else {
            this.fixedHomeLocation = null;
        }
        this.peacefulMode = peacefulMode;

        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;
        this.player = player;
        this.firstJoined = firstJoined;
    }

    public InGameSSCPlayer(@NotNull OfflineSSCPlayer offlineSSCPlayer, @NotNull Player player, @NotNull boolean firstJoined) {
        this.id = offlineSSCPlayer.getId();
        this.spawnLocation = offlineSSCPlayer.getSpawnLocation();
        this.joinedTeam = (offlineSSCPlayer.getJoinedTeamId() != null) ?
                SoloServerApi.getInstance().getPlayersTeam(offlineSSCPlayer.getJoinedTeamId()) : null;
        this.player = player;
        this.firstJoined = firstJoined;
        this.fixedHomeLocation = offlineSSCPlayer.getFixedHomeLocation();
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

    @Override
    public @Nullable String getFixedHomeLocation() {
        return fixedHomeLocation;
    }

    public boolean isFirstJoined() {
        return firstJoined;
    }

    public void setJoinedTeam(PlayersTeam joinedTeam) { // TODO: 2021/04/07 パブリックにしたくない。
        this.joinedTeam = joinedTeam;
    }

    public void setFixedHomeLocation(@Nullable Location fixedHomeLocation) {
        if (fixedHomeLocation != null) {
            val fixedHomeJson = new JsonObject();
            fixedHomeJson.addProperty("World", fixedHomeLocation.getWorld().getName());
            fixedHomeJson.addProperty("X", fixedHomeLocation.getBlockX());
            fixedHomeJson.addProperty("Y", fixedHomeLocation.getBlockY());
            fixedHomeJson.addProperty("Z", fixedHomeLocation.getBlockZ());
            this.fixedHomeLocation = new Gson().toJson(fixedHomeJson);
        } else {
            this.fixedHomeLocation = null;
        }
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
