package com.bgsoftware.ssbslimeworldmanager.listeners;

import com.bgsoftware.ssbslimeworldmanager.Environment;
import com.bgsoftware.ssbslimeworldmanager.SSBSlimeWorldManager;
import com.bgsoftware.ssbslimeworldmanager.SlimeUtils;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ConnectionListener implements Listener {

    @EventHandler
    private static void AsyncPreLoginEvent(AsyncPlayerPreLoginEvent e) {
        if(SSBSlimeWorldManager.getInstance().isAutoLoad()) {
            UUID uuid = e.getUniqueId();
            Island island = SuperiorSkyblockAPI.getPlayer(uuid).getIsland();
            Bukkit.broadcastMessage(uuid + ": " + island.getUniqueId().toString());
            Bukkit.getScheduler().runTaskLater(SSBSlimeWorldManager.getInstance(), () -> {
                if(!SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NORMAL))) {
                    SlimeUtils.loadAndGetWorld(island, World.Environment.NORMAL);
                }
                if(!SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NETHER))) {
                    SlimeUtils.loadAndGetWorld(island, World.Environment.NETHER);
                }
                if(!SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.THE_END))) {
                    SlimeUtils.loadAndGetWorld(island, World.Environment.THE_END);
                }
            }, 0);
        }
    }

    @EventHandler
    private static void quitEvent(PlayerQuitEvent e) {
        if(SSBSlimeWorldManager.getInstance().isAutoUnload()) {
            Player p = e.getPlayer();
            UUID uuid = p.getUniqueId();
            Island island = SuperiorSkyblockAPI.getPlayer(uuid).getIsland();
            unloadPlayerWorlds(island);
        }
    }

    @EventHandler
    private static void kickEvent(PlayerKickEvent e) {
        if(SSBSlimeWorldManager.getInstance().isAutoUnload()) {
            Player p = e.getPlayer();
            UUID uuid = p.getUniqueId();
            Island island = SuperiorSkyblockAPI.getPlayer(uuid).getIsland();
            unloadPlayerWorlds(island);
        }
    }

    private static void unloadPlayerWorlds(Island island) {
        Bukkit.getScheduler().runTaskLater(SSBSlimeWorldManager.getInstance(), () -> {
            if(SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NORMAL))
                    && SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NETHER))
                    && SlimeUtils.isWorldLoaded(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.THE_END))
                    && !SlimeUtils.isWorldPopulated(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NORMAL))
                    && !SlimeUtils.isWorldPopulated(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.NETHER))
                    && !SlimeUtils.isWorldPopulated(SlimeUtils.getWorldNameFull(island.getUniqueId().toString(), Environment.THE_END))) {
                SlimeUtils.unloadWorld("island_" + island.getUniqueId().toString() + "_normal");
                SlimeUtils.unloadWorld("island_" + island.getUniqueId().toString() + "_nether");
                SlimeUtils.unloadWorld("island_" + island.getUniqueId().toString() + "_the_end");
            }
        }, 50);
    }

}
