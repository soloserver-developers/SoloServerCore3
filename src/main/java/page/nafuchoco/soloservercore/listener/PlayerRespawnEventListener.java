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

package page.nafuchoco.soloservercore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import page.nafuchoco.soloservercore.SpawnPointLoader;

public class PlayerRespawnEventListener implements Listener {
    private final SpawnPointLoader loader;

    public PlayerRespawnEventListener(SpawnPointLoader loader) {
        this.loader = loader;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        // Bedスポーンの場合はバイパス
        if (!event.isBedSpawn())
            event.setRespawnLocation(loader.getSpawn(event.getPlayer()));
    }
}
