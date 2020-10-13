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

package page.nafuchoco.soloservercore.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveToNewWorldEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final World oldWorld;
    private final World newWorld;

    public PlayerMoveToNewWorldEvent(Player player, World oldWorld, World newWorld) {
        this.player = player;
        this.oldWorld = oldWorld;
        this.newWorld = newWorld;
    }

    public Player getPlayer() {
        return player;
    }

    public World getOldWorld() {
        return oldWorld;
    }

    public World getNewWorld() {
        return newWorld;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
