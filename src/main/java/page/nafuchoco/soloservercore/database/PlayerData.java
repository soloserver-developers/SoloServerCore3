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

package page.nafuchoco.soloservercore.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class PlayerData {
    private static final Gson gson = new Gson();

    private UUID id;
    private String playerName;
    private String spawnLocation;
    private Date lastJoined;
    private UUID joinedTeam;

    public PlayerData(@NotNull UUID id, @NotNull String playerName, @NotNull String spawnLocation, @NotNull Date lastJoined, @Nullable UUID joinedTeam) {
        this.id = id;
        this.playerName = playerName;
        this.spawnLocation = spawnLocation;
        this.lastJoined = lastJoined;
        this.joinedTeam = joinedTeam;
    }

    public PlayerData(@NotNull UUID id, @NotNull String playerName, @NotNull Location location, @NotNull Date lastJoined, @Nullable UUID joinedTeam) {
        this.id = id;
        this.playerName = playerName;
        this.lastJoined = lastJoined;
        this.joinedTeam = joinedTeam;

        JsonObject locationJson = new JsonObject();
        locationJson.addProperty("World", location.getWorld().getName());
        locationJson.addProperty("X", location.getBlockX());
        locationJson.addProperty("Y", location.getBlockY());
        locationJson.addProperty("Z", location.getBlockZ());
        this.spawnLocation = new Gson().toJson(locationJson);
    }

    public UUID getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getSpawnLocation() {
        return spawnLocation;
    }

    public Location getSpawnLocationLocation() {
        JsonObject locationJson = gson.fromJson(spawnLocation, JsonObject.class);
        String world = locationJson.get("World").getAsString();
        double x = locationJson.get("X").getAsDouble();
        double y = locationJson.get("Y").getAsDouble();
        double z = locationJson.get("Z").getAsDouble();
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public Date getLastJoined() {
        return lastJoined;
    }

    public UUID getJoinedTeam() {
        return joinedTeam;
    }
}
