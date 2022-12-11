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

import dev.nafusoft.soloservercore.SoloServerApi;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Date;

public class MoveTimeUpdater implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        // ブロック単位で移動した場合のみカウント
        if (!(event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY())) {
            val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(event.getPlayer());
            if (sscPlayer instanceof TempSSCPlayer)
                event.setCancelled(true);
            else
                sscPlayer.setLatestMoveTime(new Date().getTime());
        }
    }
}
