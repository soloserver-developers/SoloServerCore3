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
import page.nafuchoco.soloservercore.SoloServerApi;

public class PlayerRespawnEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        // Bedスポーンの場合はバイパス
        if (!event.isBedSpawn()) {
            var location = SoloServerApi.getInstance().getSSCPlayer(event.getPlayer()).getFixedHomeLocationObject();
            if (location == null)
                location = SoloServerApi.getInstance().getPlayerSpawn(event.getPlayer());
            event.setRespawnLocation(location);
        }
    }
}
