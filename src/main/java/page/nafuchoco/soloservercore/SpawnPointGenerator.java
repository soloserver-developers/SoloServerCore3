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
import org.bukkit.Material;
import org.bukkit.World;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class SpawnPointGenerator {
    private final List<World> worlds;
    private final int generateRange;

    public SpawnPointGenerator(List<World> spawnWorlds, int generateRange) {
        this.worlds = spawnWorlds;
        this.generateRange = generateRange;
    }

    public SpawnPointGenerator(int generateRange) {
        this.worlds = new ArrayList<>();
        this.generateRange = generateRange;
    }

    public void addSpawnWorld(World world) {
        worlds.add(world);
    }

    public void generatePoint(SpawnPointLoader loader) {
        Bukkit.getScheduler().runTask(SoloServerCore.getInstance(), () -> {
            SecureRandom secureRandom = new SecureRandom();
            World world = worlds.get(secureRandom.nextInt(worlds.size()));
            int x;
            int y = 96;
            int z;

            do {
                x = secureRandom.nextInt(generateRange * 2) - generateRange;
                z = secureRandom.nextInt(generateRange * 2) - generateRange;

                Location location = SpawnPointGenerator.this.searchSafeLocation(world, x, y, z);
                if (location != null) {
                    loader.addSpawnLocation(location);
                    loader.initPoint();
                    break;
                }
            } while (true);
        });
    }

    private Location searchSafeLocation(World world, int x, int y, int z) {
        do {
            Location point1 = new Location(world, x, y, z);
            point1.getChunk().load(true);

            if (!world.getBlockAt(point1).getType().equals(Material.AIR)) {
                Location point2 = new Location(world, x, y + 2, z);
                if (world.getBlockAt(point2).getType().equals(Material.AIR))
                    return new Location(world, x, y + 1, z);
            } else {
                y--;
                if (y <= 5)
                    return null;
            }
        } while (true);
    }
}
