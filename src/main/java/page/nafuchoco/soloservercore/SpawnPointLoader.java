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
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.data.SSCPlayer;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SpawnPointLoader {
    private final Random random = new Random();

    private final PluginSettingsManager settingsManager;
    private final SpawnPointGenerator generator;
    private final List<Location> points;
    private boolean done;
    private int stockSpawnPoint;

    public SpawnPointLoader(PluginSettingsManager settingsManager, SpawnPointGenerator generator) {
        this.settingsManager = settingsManager;
        this.generator = generator;
        points = new ArrayList<>();

        stockSpawnPoint = settingsManager.getStockSpawnPoint();
    }

    public void initPoint(boolean init) {
        done = false;
        Bukkit.getScheduler().runTask(SoloServerCore.getInstance(), () -> {
            if (points.size() < stockSpawnPoint) {
                SoloServerCore.getInstance().getLogger().info("Generating Spawn Point... [" + (points.size() + 1) + "/" + stockSpawnPoint + "]");
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
        Location location = points.get(random.nextInt(points.size()));
        points.remove(location);
        return location;
    }

    public Location getSpawn(Player player) {
        return getSpawn(player.getUniqueId());
    }

    public Location getSpawn(UUID uuid) {
        if (settingsManager.isTeamSpawnCollect()) {
            SSCPlayer sscPlayer = SoloServerApi.getInstance().getOfflineSSCPlayer(uuid);
            if (sscPlayer.getJoinedTeam() != null) {
                PlayersTeam joinedTeam = SoloServerApi.getInstance().getPlayersTeam(sscPlayer.getJoinedTeam());
                SSCPlayer ownerPlayer = SoloServerApi.getInstance().getOfflineSSCPlayer(joinedTeam.getOwner());
                return ownerPlayer.getSpawnLocationObject();
            }
        }

        SSCPlayer sscPlayer = SoloServerApi.getInstance().getOfflineSSCPlayer(uuid);
        return sscPlayer.getSpawnLocationObject();
    }

    public int getPointRemaining() {
        return points.size();
    }
}
