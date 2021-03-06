package ru.hubsmc.hubsarena;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.hubsmc.hubsarena.heroes.*;
import ru.hubsmc.hubsarena.util.PlayerUtils;
import ru.hubsmc.hubscore.PluginUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.hubsmc.hubscore.module.values.api.API.addDollars;

public class ArenaKeeper {

    private File dataFile;
    private YamlConfiguration dataFileConfig;

    private Map<Player, Hero> heroMap;

    public ArenaKeeper(File dataFile) {
        this.heroMap = new HashMap<>();
        this.dataFile = dataFile;
        this.dataFileConfig = YamlConfiguration.loadConfiguration(dataFile);
    }


    /*
     *  Heroes
     */
    public enum Heroes {
        ARCHER, KNIGHT, PYRO, BERSERK, ENDER_GLEK, SPIRIT, FLIER, COWBOY;

        public static Heroes getHeroByName(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public void pickHero(Player player, Heroes heroes) {
        switch (heroes) {
            case ARCHER:
                heroMap.put(player, new Archer(player));
                break;

            case KNIGHT:
                heroMap.put(player, new Knight(player));
                break;

            case PYRO:
                heroMap.put(player, new Pyro(player));
                break;

            case BERSERK:
                heroMap.put(player, new Berserk(player));
                break;

            case SPIRIT:
                heroMap.put(player, new Spirit(player));
                break;

            case FLIER:
                heroMap.put(player, new Flier(player));
                break;

            case COWBOY:
                heroMap.put(player, new Cowboy(player));
                break;

            case ENDER_GLEK:
                heroMap.put(player, new EnderGlek(player));
                break;

            default: {
                break;
            }
        }
        PluginUtils.getHubsPlayer(player).updateCustomVars(heroMap.get(player).getName(), "", "", "");
    }

    public Hero getHero(Player player) {
        return heroMap.get(player);
    }



    /*
     *  Battle
     */
    public void sendPlayerToBattlefield(Player player) {
        heroMap.get(player).joinTheBattlefield();
    }



    // Need to realize
    public boolean hasPlayerAccessToHero(Player player, Heroes hero) {
        return true;
    }

    public boolean isPlayerInBattlefield(Player player) {
        return heroMap.containsKey(player) && heroMap.get(player).isInBattle();
    }



    public void sendPlayerToLobby(Player player) {
        if (isPlayerInBattlefield(player)) {
            heroMap.get(player).leaveTheBattlefield();
        }
        player.teleport(HubsArena.LOBBY_LOCATION);
        player.getInventory().clear();
        PlayerUtils.curePotionEffects(player);
        player.getInventory().setItem(0, HubsArena.ITEM_MENU);
    }



    public void onJoinToArena(Player player) {
        sendPlayerToLobby(player);
        loadPlayer(player);
    }



    /*
     *  Rewards
     */
    private int GetReward(EntityType type) {
        switch (type) {
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case SPIDER:
                return 10;

            case SKELETON:
            case WITCH:
            case VINDICATOR:
            case ENDERMAN:
                return 15;

            case HUSK:
                return 20;

            case CAVE_SPIDER:
            case PHANTOM:
                return 25;

            case ILLUSIONER:
                return 30;

            case RAVAGER:
                return 35;

            default:
                return 5;
        }
    }

    public void rewardPlayerForMobKill(Player player, EntityType type) {
        addDollars(player, GetReward(type));
    }

    public void rewardPlayerForKill(Player player) {
        addDollars(player, 50);
    }



    /*
     *  Data storing
     */
    public void loadPlayer(Player player) {
        if (dataFileConfig.contains(player.getUniqueId().toString())) {
            pickHero(player, Heroes.valueOf(dataFileConfig.getString(player.getUniqueId().toString())));
        } else {
            pickHero(player, Heroes.KNIGHT);
        }
    }

    public void loadPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (dataFileConfig.contains(player.getUniqueId().toString())) {
                pickHero(player, Heroes.valueOf(dataFileConfig.getString(player.getUniqueId().toString())));
            } else {
                pickHero(player, Heroes.ARCHER);
            }
        }
    }

    public void savePlayer(Player player) {
        dataFileConfig.set(player.getUniqueId().toString(), heroMap.get(player).getHero().toString());
        heroMap.remove(player);
        saveData();
    }

    public void savePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            dataFileConfig.set(player.getUniqueId().toString(), heroMap.get(player).getHero().toString());
        }
        heroMap.clear();
        saveData();
    }

    private void saveData() {
        try {
            dataFileConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}