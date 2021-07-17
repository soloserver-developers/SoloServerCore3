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

import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.OfflineSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.database.DatabaseConnector;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SoloServerApi {
    private final SoloServerCore soloServerCore;
    private final Map<Player, InGameSSCPlayer> playerStore;
    private final Map<UUID, PlayersTeam> teamsStore;

    public static SoloServerApi getInstance() {
        return ApiInstanceHolder.INSTANCE;
    }

    private SoloServerApi(SoloServerCore soloServerCore) {
        this.soloServerCore = soloServerCore;
        playerStore = new HashMap<>();
        teamsStore = new HashMap<>();
    }

    public String getSpawnWorld() {
        return soloServerCore.getCoreConfig().getInitConfig().getSpawnWorld();
    }

    public boolean isDebug() {
        return soloServerCore.getCoreConfig().isDebug();
    }

    /**
     * @return SoloServerCoreに登録されているDB情報
     * @since v4.4
     */
    public DatabaseConnector getDatabaseConnector() {
        return new DatabaseConnector(soloServerCore.getCoreConfig().getInitConfig().getDatabaseType(),
                soloServerCore.getCoreConfig().getInitConfig().getAddress() + ":" + soloServerCore.getCoreConfig().getInitConfig().getPort(),
                soloServerCore.getCoreConfig().getInitConfig().getDatabase(),
                soloServerCore.getCoreConfig().getInitConfig().getUsername(),
                soloServerCore.getCoreConfig().getInitConfig().getPassword());
    }

    /**
     * SoloServerCore固有のオンラインプレイヤーデータを返します。
     *
     * @param player プレイヤーデータを取得したいプレイヤー
     * @return SoloServerCore固有のプレイヤーデータクラス
     */
    @NotNull
    public InGameSSCPlayer getSSCPlayer(@NotNull Player player) {
        var sscPlayer = playerStore.get(player);
        if (sscPlayer == null) {
            val id = player.getUniqueId();
            val offlineSSCPlayer = getOfflineSSCPlayer(id);
            assert offlineSSCPlayer != null;
            sscPlayer = new InGameSSCPlayer(offlineSSCPlayer, player, false);
            playerStore.put(player, sscPlayer);
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
     * @deprecated {@link #getPlayersTeam(Player)} に置き換えられたため非推奨に設定されました。
     */
    @Deprecated
    @Nullable
    public PlayersTeam searchTeamFromOwner(@NotNull UUID owner) {
        val teamId = soloServerCore.getPlayersTeamsTable().searchTeamFromOwner(owner);
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
        var playersTeam = teamsStore.get(id);
        if (playersTeam == null) {
            playersTeam = soloServerCore.getPlayersTeamsTable().getPlayersTeam(id);
            if (playersTeam != null)
                teamsStore.put(id, playersTeam);

            // Get Team message data
            playersTeam.setTeamMessages(soloServerCore.getMessagesTable().getAllMessage(playersTeam));
        }
        return playersTeam;
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

    /**
     * プラグイン設定を取得します。
     *
     * @param key 取得するプラグイン設定の名前
     * @return 取得されたプラグイン設定の値
     */
    public String getPluginSetting(@NotNull String key) {
        return soloServerCore.getPluginSettingsTable().getPluginSetting(key);
    }

    /**
     * プラグイン設定を登録します。
     *
     * @param key   登録するプラグイン設定の名前
     * @param value 登録するプラグイン設定の値
     * @throws SQLException プラグイン設定の保存中にエラーが発生した場合にスローされます。
     */
    public void setPluginSetting(@NotNull String key, @NotNull String value) throws SQLException {
        if (Arrays.stream(PluginSettingsManager.getSettingsKeys()).anyMatch(s -> s.equals(key)))
            throw new IllegalArgumentException("The settings name used by the system cannot be used.");
        soloServerCore.getPluginSettingsTable().setPluginSetting(key, value);
    }

    void registerSSCPlayer(InGameSSCPlayer sscPlayer) throws SQLException {
        soloServerCore.getPlayersTable().registerPlayer(sscPlayer);
        playerStore.put(sscPlayer.getPlayer(), sscPlayer);
    }

    void dropStoreData(Player player) {
        playerStore.remove(player);
    }

    private static class ApiInstanceHolder {
        private static final SoloServerApi INSTANCE;

        static {
            SoloServerCore core = SoloServerCore.getInstance();
            if (core == null)
                INSTANCE = null;
            else
                INSTANCE = new SoloServerApi(core);
        }
    }
}
