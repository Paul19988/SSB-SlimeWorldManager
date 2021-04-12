package com.bgsoftware.ssbslimeworldmanager;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SlimeUtils {

    private static final Map<String, SlimeWorld> islandWorlds = new HashMap<>();

    private static final SlimePlugin slimePlugin;
    private static WorldData defaultWorldData;
    private static WorldData defaultNetherWorldData;
    private static WorldData defaultEndWorldData;
    private static SlimeLoader slimeLoader;

    static {
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        slimeLoader = slimePlugin.getLoader(SSBSlimeWorldManager.getConfLoader());
    }

    private SlimeUtils() { }

    public static void init() {
        try {
            if(!slimeLoader.worldExists("SuperiorWorld")) {
                String worldName = "SuperiorWorld";
                WorldData worldData = new WorldData();

                worldData.setDataSource(SSBSlimeWorldManager.getConfLoader());
                worldData.setDifficulty("normal");
                worldData.setAllowAnimals(false);
                worldData.setAllowMonsters(false);
                worldData.setPvp(false);
                worldData.setLoadOnStartup(true);

                slimePlugin.createEmptyWorld(slimeLoader, worldName, false, worldData.toPropertyMap());

                WorldsConfig config = ConfigManager.getWorldConfig();
                config.getWorlds().put(worldName, worldData);
                config.save();
            }
        } catch(IOException | WorldAlreadyExistsException e) {
            e.printStackTrace();
        }
        defaultWorldData = buildDefaultWorldData();
        defaultNetherWorldData = buildDefaultNetherWorldData();
        defaultEndWorldData = buildDefaultEndWorldData();
    }

    public static void unloadAllWorlds(){
        try{
            slimePlugin.getLoader(defaultWorldData.getDataSource()).listWorlds().forEach(worldName -> {
                if(isIslandWorldName(worldName) && Bukkit.getWorld(worldName) != null)
                    unloadWorld(worldName);
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static SlimeWorld loadAndGetWorld(Island island, World.Environment environment){
        return loadAndGetWorld(island.getUniqueId(), environment);
    }

    public static SlimeWorld loadAndGetWorld(UUID islandUUID, World.Environment environment){
        return loadAndGetWorld(getWorldName(islandUUID, environment), environment);
    }

    public static SlimeWorld loadAndGetWorld(String worldName, World.Environment environment){
        SlimeWorld slimeWorld = islandWorlds.get(worldName);

        if(slimeWorld == null){
            WorldData worldData = ConfigManager.getWorldConfig().getWorlds().get(worldName);

            try {
                // No world was found, creating a new world.
                if (worldData == null) {
                    if(worldName.toLowerCase().contains("nether")) {
                        SlimePropertyMap slimePropertyMap = defaultNetherWorldData.toPropertyMap();
                        slimePropertyMap.setString(SlimeProperties.ENVIRONMENT, environment.name().toUpperCase());
                        slimeWorld = slimePlugin.createEmptyWorld(slimePlugin.getLoader(defaultNetherWorldData.getDataSource()), worldName, defaultNetherWorldData.isReadOnly(), slimePropertyMap);

                        // Saving the world
                        WorldsConfig config = ConfigManager.getWorldConfig();
                        config.getWorlds().put(worldName, defaultNetherWorldData);
                        config.save();
                    }else if(worldName.toLowerCase().contains("the_end")) {
                        SlimePropertyMap slimePropertyMap = defaultEndWorldData.toPropertyMap();
                        slimePropertyMap.setString(SlimeProperties.ENVIRONMENT, environment.name().toUpperCase());
                        slimeWorld = slimePlugin.createEmptyWorld(slimePlugin.getLoader(defaultEndWorldData.getDataSource()), worldName, defaultEndWorldData.isReadOnly(), slimePropertyMap);

                        // Saving the world
                        WorldsConfig config = ConfigManager.getWorldConfig();
                        config.getWorlds().put(worldName, defaultEndWorldData);
                        config.save();
                    }else{
                        SlimePropertyMap slimePropertyMap = defaultWorldData.toPropertyMap();
                        slimePropertyMap.setString(SlimeProperties.ENVIRONMENT, environment.name().toUpperCase());
                        slimeWorld = slimePlugin.createEmptyWorld(slimePlugin.getLoader(defaultWorldData.getDataSource()), worldName, defaultWorldData.isReadOnly(), slimePropertyMap);

                        // Saving the world
                        WorldsConfig config = ConfigManager.getWorldConfig();
                        config.getWorlds().put(worldName, defaultWorldData);
                        config.save();
                    }
                } else {
                    slimeWorld = slimePlugin.loadWorld(slimePlugin.getLoader(worldData.getDataSource()),
                            worldName, worldData.isReadOnly(), worldData.toPropertyMap());
                }

                islandWorlds.put(worldName, slimeWorld);
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        if(Bukkit.getWorld(worldName) == null)
            slimePlugin.generateWorld(slimeWorld);

        return slimeWorld;
    }

    public static void deleteWorld(Island island, World.Environment environment){
        String worldName = getWorldName(island, environment);

        WorldData worldData = ConfigManager.getWorldConfig().getWorlds().get(worldName);

        unloadWorld(worldName);

        try {
            slimePlugin.getLoader(worldData.getDataSource()).deleteWorld(worldName);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean isIslandsWorld(String worldName){
        String[] nameSections = worldName.split("_");
        try{
            return SuperiorSkyblockAPI.getGrid().getIslandByUUID(UUID.fromString(nameSections[1])) != null;
        }catch (Exception ex) {
            return false;
        }
    }

    public static void unloadWorld(String worldName){
        SlimeWorld slimeWorld = islandWorlds.remove(worldName);
        if(slimeWorld != null) {
            try {
                slimeWorld.getLoader().saveWorld(worldName, ((CraftSlimeWorld) slimeWorld).serialize(), true);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        if(Bukkit.getWorld(worldName).getPlayers().isEmpty()) {
            Bukkit.unloadWorld(worldName, true);
        }else{

        }
    }

    public static String getWorldName(Island island, World.Environment environment){
        return getWorldName(island.getUniqueId(), environment);
    }

    public static String getWorldName(UUID islandUUID, World.Environment environment){
        return "island_" + islandUUID + "_" + environment.name().toLowerCase();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isIslandWorldName(String worldName){
        String[] nameSections = worldName.split("_");
        try{
            UUID.fromString(nameSections[0]);
            World.Environment.valueOf(nameSections[1]);
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    private static WorldData buildDefaultWorldData(){
        WorldData worldData = new WorldData();

        worldData.setDataSource(SSBSlimeWorldManager.getConfLoader());
        worldData.setEnvironment("NORMAL");
        worldData.setDifficulty("normal");
        worldData.setLoadOnStartup(false);

        return worldData;
    }

    private static WorldData buildDefaultNetherWorldData(){
        WorldData worldData = new WorldData();

        worldData.setDataSource(SSBSlimeWorldManager.getConfLoader());
        worldData.setEnvironment("NETHER");
        worldData.setDefaultBiome("minecraft:nether_wastes");
        worldData.setDifficulty("normal");
        worldData.setLoadOnStartup(false);

        return worldData;
    }

    private static WorldData buildDefaultEndWorldData(){
        WorldData worldData = new WorldData();

        worldData.setDataSource(SSBSlimeWorldManager.getConfLoader());
        worldData.setEnvironment("THE_END");
        worldData.setDefaultBiome("minecraft:the_end");
        worldData.setDifficulty("normal");
        worldData.setLoadOnStartup(false);

        return worldData;
    }

    public static boolean isWorldLoaded(String worldName) {
        try {
            System.out.println("Checking lock world: " + worldName);
            return slimeLoader.isWorldLocked(worldName);
        } catch(UnknownWorldException | IOException e) {
            System.out.println("World: " + worldName + " does not exist.");
            return false;
        }
    }

    public static boolean isWorldPopulated(String worldName) {
        if(isWorldLoaded(worldName)) {
            if(Bukkit.getWorld(worldName).getPlayers().size() <= 0) {
                return false;
            } else {
                System.out.println("World: " + worldName + " still contains players so cannot unload.");
                return true;
            }
        }else{
            return false;
        }
    }

    public static String getWorldNameFull(String worldUUID, Environment environment) {
        switch(environment) {
            case NORMAL:
                return "island_" + worldUUID + "_normal";
            case NETHER:
                return "island_" + worldUUID + "_nether";
            case THE_END:
                return "island_" + worldUUID + "_the_end";
            default:
                return "";
        }
    }

}
