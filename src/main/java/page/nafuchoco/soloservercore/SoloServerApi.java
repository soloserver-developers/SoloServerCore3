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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.OfflineSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SoloServerApi {
    private final SoloServerCore soloServerCore;
    private final Map<Player, InGameSSCPlayer> playerStore;

    public static SoloServerApi getInstance() {
        SoloServerCore core = SoloServerCore.getInstance();
        if (core == null)
            return null;
        return new SoloServerApi(core);
    }

    private SoloServerApi(SoloServerCore soloServerCore) {
        this.soloServerCore = soloServerCore;
        playerStore = new HashMap<>();
    }

    /**
     * SoloServerCore固有のオンラインプレイヤーデータを返します。
     *
     * @param player プレイヤーデータを取得したいプレイヤー
     * @return SoloServerCore固有のプレイヤーデータクラス
     */
    @NotNull
    public InGameSSCPlayer getSSCPlayer(@NotNull Player player) {
        InGameSSCPlayer sscPlayer = playerStore.get(player);
        if (sscPlayer == null) {
            UUID id = player.getUniqueId();
            OfflineSSCPlayer offlineSSCPlayer = getOfflineSSCPlayer(id);
            if (offlineSSCPlayer == null)
                throw new IllegalStateException();
            sscPlayer = new InGameSSCPlayer(offlineSSCPlayer, player);
        }
        return sscPlayer;
    }

    /**
     * SoloServerCore固有のプレイヤーデータを返します。
     *
     * @param uuid プレイヤーデータを取得したいプレイヤーのUUID
     * @return SoloServerCore固有のプレイヤーデータクラス もしくは null
     */
    @Nullable
    public OfflineSSCPlayer getOfflineSSCPlayer(@NotNull UUID uuid) {
        return soloServerCore.getPlayersTable().getPlayerData(uuid);
    }

    /**
     * 指定したプレイヤーが所有するPlayersTeamを検索します。
     *
     * @param owner 検索するプレイヤーのID
     * @return PlayersTeam もしくは null
     */
    @Nullable
    public PlayersTeam searchTeamFromOwner(@NotNull UUID owner) {
        UUID teamId = soloServerCore.getPlayersTeamsTable().searchTeamFromOwner(owner);
        if (teamId != null)
            return getPlayersTeam(teamId);
        return null;
    }

    /**
     * PlayersTeamを返します。
     *
     * @param id 取得したいPlayersTeamのID
     * @return PlayersTeam もしくは null
     */
    @Nullable
    public PlayersTeam getPlayersTeam(@NotNull UUID id) {
        return soloServerCore.getPlayersTeamsTable().getPlayersTeam(id);
    }

    /**
     * 指定したプレイヤーが所属するPlayersTeamを返します。
     *
     * @param player 取得したいプレイヤー
     * @return プレイヤーが所属するPlayersTeam もしくは null
     */
    @Nullable
    public PlayersTeam getPlayersTeam(@NotNull Player player) {
        return getSSCPlayer(player).getJoinedTeam();
    }


    void dropStoreData(Player player) {
        playerStore.remove(player);
    }
}
