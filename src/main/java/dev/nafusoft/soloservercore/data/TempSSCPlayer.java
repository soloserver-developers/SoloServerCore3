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
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TempSSCPlayer extends InGameSSCPlayer {
    private Location generatedLocation;

    public TempSSCPlayer(@NotNull Player player) {
        super(player.getUniqueId(), getDefaultSpawnLocation(player), null, player, false, null, false);
    }

    public TempSSCPlayer(@NotNull OfflineSSCPlayer offlineSSCPlayer,
                         @NotNull Player player) {
        super(offlineSSCPlayer, player, false);
    }

    private static String getDefaultSpawnLocation(Player player) {
        val location = player.getWorld().getSpawnLocation();
        val locationJson = new JsonObject();
        locationJson.addProperty("World", location.getWorld().getName());
        locationJson.addProperty("X", location.getBlockX());
        locationJson.addProperty("Y", location.getBlockY());
        locationJson.addProperty("Z", location.getBlockZ());
        return new Gson().toJson(locationJson);
    }

    public void setGeneratedLocation(Location generatedLocation) {
        this.generatedLocation = generatedLocation;
    }

    @Override
    public @NotNull String getSpawnLocation() {
        if (generatedLocation != null) {
            val locationJson = new JsonObject();
            locationJson.addProperty("World", generatedLocation.getWorld().getName());
            locationJson.addProperty("X", generatedLocation.getBlockX());
            locationJson.addProperty("Y", generatedLocation.getBlockY());
            locationJson.addProperty("Z", generatedLocation.getBlockZ());
            return new Gson().toJson(locationJson);
        }
        return super.getSpawnLocation();
    }

    @Override
    public @NotNull Location getSpawnLocationObject() {
        if (generatedLocation != null)
            return generatedLocation;
        return super.getSpawnLocationObject();
    }
}
