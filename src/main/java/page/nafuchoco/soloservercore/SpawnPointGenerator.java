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

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.security.SecureRandom;
import java.util.logging.Level;

public class SpawnPointGenerator {
    private final World world;
    private final int generateRange;

    public SpawnPointGenerator(World spawnWorld, int generateRange) {
        this.world = spawnWorld;
        this.generateRange = generateRange;
    }

    public void generatePoint(SpawnPointLoader loader, boolean init) {
        Bukkit.getScheduler().runTask(SoloServerCore.getInstance(), () -> {
            var secureRandom = new SecureRandom();
            int x;
            int y = 120;
            int z;

            do {
                x = secureRandom.nextInt(generateRange * 2) - generateRange;
                z = secureRandom.nextInt(generateRange * 2) - generateRange;

                if (SoloServerApi.getInstance().isDebug())
                    SoloServerCore.getInstance().getLogger().info("[Debug] Start Search: " + world.getName() + ", " + x + ", " + z);
                var location = SpawnPointGenerator.this.searchSafeLocation(world, x, y, z);
                if (location != null) {
                    loader.addSpawnLocation(location);
                    loader.initPoint(init);
                    break;
                }
            } while (true);
        });
    }

    private Location searchSafeLocation(World world, int x, int y, int z) {
        do {
            val point1 = new Location(world, x, y, z);
            point1.getChunk().load(true);

            if (SoloServerApi.getInstance().isDebug())
                SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Searching Y: {0}", y);
            if (!(world.getBlockAt(point1).getType().equals(Material.AIR) ||
                    world.getBlockAt(point1).getType().equals(Material.WATER) ||
                    world.getBlockAt(point1).getType().equals(Material.LAVA))) {
                val point2 = new Location(world, x, y + 2, z);
                if (world.getBlockAt(point2).getType().equals(Material.AIR))
                    return new Location(world, x, y + 1, z);
            }

            y--;
            if (y <= 60) {
                if (SoloServerApi.getInstance().isDebug())
                    SoloServerCore.getInstance().getLogger().info("[Debug] Safe location were not found.");
                return null;
            }
        } while (true);
    }
}
