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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface SSCPlayer {

    /**
     * プレイヤーのIDを返します。
     *
     * @return プレイヤーのID
     */
    @NotNull UUID getId();

    /**
     * プレイヤーに割り当てられたスポーン地点の座標をJson形式で返します。
     *
     * @return プレイヤーに割り当てられたスポーン地点の座標Json
     */
    @NotNull String getSpawnLocation();

    /**
     * プレイヤーに割り当てられたスポーン地点のLocationオブジェクトを返します。
     *
     * @return レイヤーに割り当てられたスポーン地点のLocationオブジェクト
     */
    @NotNull
    default Location getSpawnLocationObject() {
        val locationJson = new Gson().fromJson(getSpawnLocation(), JsonObject.class);
        val world = locationJson.get("World").getAsString();
        val x = locationJson.get("X").getAsDouble();
        val y = locationJson.get("Y").getAsDouble();
        val z = locationJson.get("Z").getAsDouble();
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    /**
     * プレイヤーが所属しているチームを返します。
     *
     * @return プレイヤーが所属しているチーム
     */
    @Nullable PlayersTeam getJoinedTeam();

    /**
     * プレイヤーが所属しているチームのIDを返します。
     *
     * @return プレイヤーが所属しているチームのID
     */
    @Nullable
    default UUID getJoinedTeamId() {
        return getJoinedTeam() != null ? getJoinedTeam().getId() : null;
    }

    /**
     * 固定されたホーム地点の座標をJson形式で返します。
     *
     * @return 固定されたホーム地点の座標Json
     */
    @Nullable String getFixedHomeLocation();

    /**
     * 固定されたホーム地点のLocationオブジェクトを返します。
     *
     * @return 固定されたホーム地点のLocationオブジェクト
     */
    @Nullable
    default Location getFixedHomeLocationObject() {
        val locationJson = new Gson().fromJson(getFixedHomeLocation(), JsonObject.class);
        if (locationJson != null) {
            val world = locationJson.get("World").getAsString();
            val x = locationJson.get("X").getAsDouble();
            val y = locationJson.get("Y").getAsDouble();
            val z = locationJson.get("Z").getAsDouble();
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
        return null;
    }

    /**
     * ピースフルモードの状態を返します。
     *
     * @return ピースフルモードの状態
     */
    boolean isPeacefulMode();
}
