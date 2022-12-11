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

package dev.nafusoft.soloservercore.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.nafusoft.soloservercore.SoloServerApi;
import lombok.val;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OfflineSSCPlayer implements SSCPlayer {
    private final UUID id;
    private final String spawnLocation;
    private final PlayersTeam joinedTeam;
    private final String fixedHomeLocation;

    private final boolean peacefulMode;

    public OfflineSSCPlayer(@NotNull UUID id,
                            @NotNull String spawnLocation,
                            @Nullable UUID joinedTeamId,
                            @Nullable String fixedHomeLocation,
                            boolean peacefulMode) {
        this.id = id;
        this.spawnLocation = spawnLocation;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;
        this.fixedHomeLocation = fixedHomeLocation;
        this.peacefulMode = peacefulMode;
    }

    public OfflineSSCPlayer(@NotNull UUID id,
                            @NotNull Location location,
                            @Nullable UUID joinedTeamId,
                            @Nullable Location fixedHomeLocation,
                            boolean peacefulMode) {
        this.id = id;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;

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

    @Override
    public boolean isPeacefulMode() {
        return peacefulMode;
    }
}
