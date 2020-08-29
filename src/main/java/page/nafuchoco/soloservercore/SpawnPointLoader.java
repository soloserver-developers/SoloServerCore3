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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import page.nafuchoco.soloservercore.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnPointLoader {
    private final PlayersTable playersTable;
    private final PlayerAndTeamsBridge playerAndTeamsBridge;
    private final PluginSettingsManager settingsManager;
    private final SpawnPointGenerator generator;

    private boolean done;
    private final List<Location> points;

    public SpawnPointLoader(PlayersTable playersTable, PlayerAndTeamsBridge playerAndTeamsBridge, PluginSettingsManager settingsManager, SpawnPointGenerator generator) {
        this.playersTable = playersTable;
        this.playerAndTeamsBridge = playerAndTeamsBridge;
        this.settingsManager = settingsManager;
        this.generator = generator;
        points = new ArrayList<>();
    }

    public void initPoint(boolean init) {
        done = false;
        Bukkit.getScheduler().runTask(SoloServerCore.getInstance(), () -> {
            if (points.size() < 100) {
                SoloServerCore.getInstance().getLogger().info("Generating Spawn Point... [" + (points.size() + 1) + "/100]");
                generator.generatePoint(this, init);
            } else {
                done = true;
                SoloServerCore.getInstance().getLogger().info("Generate Completed!");
            }

            if (init && done) {
                // Plugin Init on Main Thread.
                Bukkit.getScheduler().callSyncMethod(SoloServerCore.getInstance(), () -> {
                    SoloServerCore.getInstance().init();
                    return null;
                });
            }
        });
    }

    public boolean isDone() {
        return done;
    }

    protected void addSpawnLocation(Location location) {
        points.add(location);
    }

    public Location getNewLocation() {
        Random random = new Random();
        Location location = points.get(random.nextInt(points.size()) - 1);
        points.remove(location);
        return location;
    }

    public Location getSpawn(Player player) {
        if (settingsManager.isTeamSpawnCollect()) {
            TeamsPlayerData teamsPlayerData = playerAndTeamsBridge.getPlayerData(player);
            if (teamsPlayerData != null) { // プレイヤーデータが取得されている場合必然的にチームに所属している。
                PlayerData ownerData = playersTable.getPlayerData(teamsPlayerData.getTeamOwner());
                return ownerData.getSpawnLocationLocation();
            }
        }
        // いずれでもない場合プレイヤーデータのスポーンロケーションにテレポートさせる。
        PlayerData playerData = playersTable.getPlayerData(player);
        return playerData.getSpawnLocationLocation();
    }

    public int getPointRemaining() {
        return points.size();
    }
}
