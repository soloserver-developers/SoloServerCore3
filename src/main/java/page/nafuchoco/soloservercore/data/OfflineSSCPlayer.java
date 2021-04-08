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
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerApi;

import java.util.UUID;

public class OfflineSSCPlayer implements SSCPlayer {
    private static final Gson gson = new Gson();

    private final UUID id;
    private final String spawnLocation;
    private final PlayersTeam joinedTeam;

    public OfflineSSCPlayer(@NotNull UUID id, @NotNull String spawnLocation, @Nullable UUID joinedTeamId) {
        this.id = id;
        this.spawnLocation = spawnLocation;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;
    }

    public OfflineSSCPlayer(@NotNull UUID id, @NotNull Location location, @Nullable UUID joinedTeamId) {
        this.id = id;
        this.joinedTeam = (joinedTeamId != null) ? SoloServerApi.getInstance().getPlayersTeam(joinedTeamId) : null;

        JsonObject locationJson = new JsonObject();
        locationJson.addProperty("World", location.getWorld().getName());
        locationJson.addProperty("X", location.getBlockX());
        locationJson.addProperty("Y", location.getBlockY());
        locationJson.addProperty("Z", location.getBlockZ());
        this.spawnLocation = new Gson().toJson(locationJson);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public @Nullable PlayersTeam getJoinedTeam() {
        return joinedTeam;
    }
}
