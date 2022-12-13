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

package dev.nafusoft.soloservercore;

import io.papermc.lib.PaperLib;
import lombok.val;
import org.bukkit.*;

import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class AsyncSafeLocationUtil {
    private static final Set<Material> DAMAGING_TYPES = Set.of(
            Material.CACTUS,
            Material.CAMPFIRE,
            Material.FIRE,
            Material.MAGMA_BLOCK,
            Material.SOUL_CAMPFIRE,
            Material.SOUL_FIRE,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE);
    private static final Set<Material> FLUID_TYPES = Set.of(
            Material.WATER,
            Material.LAVA);


    public static Location generateNewRandomLocation() {
        // Generate new location
        val generateRange = SoloServerCore.getInstance().getCoreConfig().getInitConfig().generateLocationRange();
        val spawnWorld = Bukkit.getWorld(SoloServerCore.getInstance().getCoreConfig().getInitConfig().getSpawnWorld());
        val secureRandom = new SecureRandom();

        int x;
        int y;
        int z;
        Location location = null;

        do {
            x = secureRandom.nextInt(generateRange * 2) - generateRange;
            z = secureRandom.nextInt(generateRange * 2) - generateRange;
            y = 120;

            Chunk chunk = null;
            try {
                chunk = PaperLib.getChunkAtAsync(spawnWorld, x, z, true).get();
            } catch (InterruptedException | ExecutionException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Error!", e);
            }

            do {
                if (!isUnsafe(spawnWorld, x, y, z))
                    location = new Location(chunk.getWorld(), x, y + 1, z);

                y--;
                if (y <= 60)
                    break;
            } while (location == null);
        } while (location == null);

        return location;
    }

    public static boolean isUnsafe(World world, int x, int y, int z) {
        return isDamagingBlock(world, x, y, z) || !isAboveAir(world, x, y, z);
    }

    public static boolean isDamagingBlock(World world, int x, int y, int z) {
        val block = world.getBlockAt(x, y, z);
        boolean result = block.getType().equals(Material.AIR)
                || DAMAGING_TYPES.contains(block.getType())
                || FLUID_TYPES.contains(block.getType());
        return result;
    }

    public static boolean isAboveAir(World world, int x, int y, int z) {
        boolean result = world.getBlockAt(x, y + 2, z).getType().equals(Material.AIR);
        return result;
    }
}
