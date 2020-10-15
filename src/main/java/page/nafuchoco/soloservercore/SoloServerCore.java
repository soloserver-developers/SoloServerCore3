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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.command.ReTeleportCommand;
import page.nafuchoco.soloservercore.command.SettingsCommand;
import page.nafuchoco.soloservercore.command.TeamCommand;
import page.nafuchoco.soloservercore.database.*;
import page.nafuchoco.soloservercore.listener.*;
import page.nafuchoco.soloservercore.listener.internal.PlayersTeamEventListener;
import page.nafuchoco.soloservercore.packet.ServerInfoPacketEventListener;

import java.sql.SQLException;
import java.util.logging.Level;

public final class SoloServerCore extends JavaPlugin implements Listener {
    private static SoloServerCore instance;
    private static SoloServerCoreConfig config;

    private static PluginSettingsTable pluginSettingsTable;
    private static PlayersTable playersTable;
    private static PlayersTeamsTable playersTeamsTable;

    private static PluginSettingsManager pluginSettingsManager;
    private static PlayerAndTeamsBridge playerAndTeamsBridge;
    private static SpawnPointLoader spawnPointLoader;
    private static ProtocolManager protocolManager;
    private static DatabaseConnector connector;
    private static CoreProtectAPI coreProtectAPI;

    public static SoloServerCore getInstance() {
        if (instance == null)
            instance = (SoloServerCore) Bukkit.getServer().getPluginManager().getPlugin("SoloServerCore");
        return instance;
    }

    public static SoloServerCoreConfig getCoreConfig() {
        if (config == null)
            config = new SoloServerCoreConfig();
        return config;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getCoreConfig().reloadConfig();

        getLogger().info("Start the initialization process. This may take some time.");

        // Database Int
        connector = new DatabaseConnector(getCoreConfig().getInitConfig().getDatabaseType(),
                getCoreConfig().getInitConfig().getAddress() + ":" + getCoreConfig().getInitConfig().getPort(),
                getCoreConfig().getInitConfig().getDatabase(),
                getCoreConfig().getInitConfig().getUsername(),
                getCoreConfig().getInitConfig().getPassword());
        pluginSettingsTable = new PluginSettingsTable("settings", connector);
        playersTable = new PlayersTable("players", connector);
        playersTeamsTable = new PlayersTeamsTable("teams", connector);
        try {
            pluginSettingsTable.createTable();
            playersTable.createTable();
            playersTeamsTable.createTable();
        } catch (SQLException throwables) {
            getInstance().getLogger().log(Level.WARNING, "An error occurred while initializing the database table.", throwables);
        }

        pluginSettingsManager = new PluginSettingsManager(pluginSettingsTable);
        playerAndTeamsBridge = new PlayerAndTeamsBridge(connector, playersTable, playersTeamsTable);

        getLogger().info("Pre-generate spawn points. This often seems to freeze the system, but for the most part it is normal.");
        World world = Bukkit.getWorld(config.getInitConfig().getSpawnWorld());
        spawnPointLoader = new SpawnPointLoader(playersTable,
                playerAndTeamsBridge,
                pluginSettingsManager,
                new SpawnPointGenerator(world, config.getInitConfig().getGenerateLocationRange()));
        spawnPointLoader.initPoint(true);
    }

    protected void init() {
        // ProtocolLib Init
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new ServerInfoPacketEventListener());

        // CoreProtect Init
        CoreProtect coreProtect = (CoreProtect) getServer().getPluginManager().getPlugin("CoreProtect");
        coreProtectAPI = coreProtect.getAPI();
        if (pluginSettingsManager.isCheckBlock())
            getServer().getPluginManager().registerEvents(new BlockEventListener(new CoreProtectClient(coreProtectAPI), pluginSettingsManager, playerAndTeamsBridge), this);

        getServer().getPluginManager().registerEvents(new PlayersTeamEventListener(playersTable, playersTeamsTable), this);
        getServer().getPluginManager().registerEvents(new PlayerBedEventListener(pluginSettingsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEventListener(spawnPointLoader), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginEventListener(playersTable, spawnPointLoader), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(playersTable, playerAndTeamsBridge), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatEventListener(playerAndTeamsBridge), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Command Register
        SettingsCommand settingsCommand = new SettingsCommand(pluginSettingsManager);
        TeamCommand teamCommand = new TeamCommand(playersTable, playersTeamsTable);
        ReTeleportCommand reTeleportCommand = new ReTeleportCommand(playersTable, playersTeamsTable, spawnPointLoader, Bukkit.getWorld(config.getInitConfig().getSpawnWorld()));
        getCommand("settings").setExecutor(settingsCommand);
        getCommand("settings").setTabCompleter(settingsCommand);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);
        getCommand("reteleport").setExecutor(reTeleportCommand);
        getCommand("reteleport").setTabCompleter(reTeleportCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (connector != null)
            connector.close();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("soloservercore." + command.getName())) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else switch (command.getName()) {
            case "status":
                sender.sendMessage("Remaining spawn points: " + spawnPointLoader.getPointRemaining());
                break;

            case "charge":
                Bukkit.broadcastMessage("[SSC] Start generate spawn points.\n" +
                        "The server may be stopped while it finishes.");
                spawnPointLoader.initPoint(false);
                Bukkit.broadcastMessage("[SSC] The generate spawn point is now complete.");
                break;

            case "spawn":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.teleport(spawnPointLoader.getSpawn(player));
                } else {
                    Bukkit.getLogger().info("This command must be executed in-game.");
                }
                break;

            case "home":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Location location = player.getBedSpawnLocation();
                    if (location == null)
                        location = spawnPointLoader.getSpawn(player);
                    player.teleport(location);
                } else {
                    Bukkit.getLogger().info("This command must be executed in-game.");
                }
                break;

            default:
                return false;
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage("");

        if (Bukkit.getOnlinePlayers().isEmpty())
            spawnPointLoader.initPoint(false);
    }

    PlayersTable getPlayersTable() {
        return playersTable;
    }

    PlayersTeamsTable getPlayersTeamsTable() {
        return playersTeamsTable;
    }

    PlayerAndTeamsBridge getPlayerAndTeamsBridge() {
        return playerAndTeamsBridge;
    }

    SpawnPointLoader getSpawnPointLoader() {
        return spawnPointLoader;
    }
}
