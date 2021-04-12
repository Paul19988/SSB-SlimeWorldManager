package com.bgsoftware.ssbslimeworldmanager;

import com.bgsoftware.ssbslimeworldmanager.listeners.ConnectionListener;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class SSBSlimeWorldManager extends JavaPlugin implements Listener {

    public static SSBSlimeWorldManager plugin;
    private static String loader;
    private boolean autoLoad;
    private boolean autoUnload;

    @Override
    public void onEnable() {
        plugin = this;

        if(!getConfig().isString("loader")) {
            getConfig().set("loader", "mysql");
            saveConfig();
            loader = "mysql";
        }else{
            loader = getConfig().getString("loader");
        }

        if(!getConfig().isBoolean("autoload")) {
            getConfig().set("autoload", true);
            saveConfig();
            autoLoad = true;
        }else{
            autoLoad = getConfig().getBoolean("autoload");
        }

        if(!getConfig().isBoolean("autounload")) {
            getConfig().set("autounload", true);
            saveConfig();
            autoUnload = true;
        }else{
            autoUnload = getConfig().getBoolean("autounload");
        }

        SlimeUtils.init();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
    }

    @Override
    public void onDisable() {
        SlimeUtils.unloadAllWorlds();
    }

    @EventHandler
    public void onSSBInit(PluginInitializeEvent e){
        e.getPlugin().getProviders().setWorldsProvider(SSBWorldManager.createManager());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onIslandDelete(IslandDisbandEvent e){
        Arrays.stream(World.Environment.values()).forEach(environment -> SlimeUtils.deleteWorld(e.getIsland(), environment));
    }

    public static String getConfLoader() {
        return loader;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    public boolean isAutoUnload() {
        return autoUnload;
    }

    public static SSBSlimeWorldManager getInstance() {
        return SSBSlimeWorldManager.getPlugin(SSBSlimeWorldManager.class);
    }
}
