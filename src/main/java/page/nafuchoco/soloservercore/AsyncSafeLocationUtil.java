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

package page.nafuchoco.soloservercore;

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

            if (SoloServerApi.getInstance().isDebug())
                SoloServerCore.getInstance().getLogger().info("[Debug] Start Search: " + spawnWorld.getName() + ", " + x + ", " + z);

            Chunk chunk = null;
            try {
                chunk = PaperLib.getChunkAtAsync(spawnWorld, x, z, true).get();
            } catch (InterruptedException | ExecutionException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Error!", e);
            }
            
            do {
                if (SoloServerApi.getInstance().isDebug())
                    SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Searching Y: {0}", y);

                if (!isUnsafe(spawnWorld, x, y, z))
                    location = new Location(chunk.getWorld(), x, y + 1, z);

                y--;
                if (y <= 60) {
                    if (SoloServerApi.getInstance().isDebug())
                        SoloServerCore.getInstance().getLogger().info("[Debug] Safe location were not found.");
                    break;
                }
            } while (location == null);
            if (SoloServerApi.getInstance().isDebug())
                SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] While Ended. Y: {0}", y);
        } while (location == null);

        return location;
    }

    public static boolean isUnsafe(World world, int x, int y, int z) {
        return isDamagingBlock(world, x, y, z) || !isAboveAir(world, x, y, z);
    }

    public static boolean isDamagingBlock(World world, int x, int y, int z) {
        if (SoloServerApi.getInstance().isDebug())
            SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Damage Block Checking...:");
        val block = world.getBlockAt(x, y, z);
        boolean result = block.getType().equals(Material.AIR)
                || DAMAGING_TYPES.contains(block.getType())
                || FLUID_TYPES.contains(block.getType());
        if (SoloServerApi.getInstance().isDebug())
            SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Damage Block Check result.: {0}", result);
        return result;
    }

    public static boolean isAboveAir(World world, int x, int y, int z) {
        if (SoloServerApi.getInstance().isDebug())
            SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Air Checking...:");
        boolean result = world.getBlockAt(x, y + 2, z).getType().equals(Material.AIR);
        if (SoloServerApi.getInstance().isDebug())
            SoloServerCore.getInstance().getLogger().log(Level.INFO, "[Debug] Air Check result.: {0}", result);
        return result;
    }
}
