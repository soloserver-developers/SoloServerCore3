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

package page.nafuchoco.soloservercore.event.team;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.data.TeamMessage;

public class PlayersTeamMessageCreateEvent extends PlayersTeamEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final TeamMessage teamMessage;
    private boolean cancelled;

    public PlayersTeamMessageCreateEvent(PlayersTeam playersTeam, Player player, TeamMessage message) {
        super(playersTeam, player);
        teamMessage = message;
    }

    public PlayersTeamMessageCreateEvent(PlayersTeam playersTeam, InGameSSCPlayer player, TeamMessage message) {
        super(playersTeam, player);
        teamMessage = message;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public TeamMessage getCreateTeamMessage() {
        return teamMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
