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

package dev.nafusoft.soloservercore.event.team;

import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.data.InGameSSCPlayer;
import dev.nafusoft.soloservercore.data.PlayersTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class PlayersTeamEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final PlayersTeam playersTeam;
    private final InGameSSCPlayer player;

    protected PlayersTeamEvent(PlayersTeam playersTeam, Player player) {
        this.playersTeam = playersTeam;
        this.player = SoloServerApi.getInstance().getSSCPlayer(player);
    }

    protected PlayersTeamEvent(PlayersTeam playersTeam, InGameSSCPlayer player) {
        this.playersTeam = playersTeam;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayersTeam getPlayersTeam() {
        return playersTeam;
    }

    public InGameSSCPlayer getPlayer() {
        return player;
    }

    public Player getBukkitPlayer() {
        return player.getPlayer();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
