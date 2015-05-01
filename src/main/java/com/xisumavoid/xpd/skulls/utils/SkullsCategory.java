package com.xisumavoid.xpd.skulls.utils;

import com.xisumavoid.xpd.skulls.Skulls;
import com.xisumavoid.xpd.skulls.utils.IconMenu.OptionClickEventHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONArray;

/**
 *
 * @author Autom
 */
public class SkullsCategory {

    private final String categoryName;
    private final List<IconMenu> pages = new ArrayList<>();
    private final Set<String> names = new HashSet<>();
    private final Skulls plugin;
    private int slot = 0;

    public SkullsCategory(Skulls skulls, String categoryName) {
        this.plugin = skulls;
        this.categoryName = categoryName;
        if (plugin.getConfig().getBoolean("updateonstartup")) {
            updateSkulls();
        }
        loadSkulls();
    }

    public void addSkull(String name, UUID owner, String value) {
        final int rowsPerPage = plugin.getConfig().getInt("rowsperpage");
        final int size = pages.size();
        if (slot == 0) {
            for (IconMenu page : pages) {
                String menuName = page.getName().split("/")[0] + "/" + (size + 1) + " " + categoryName;
                if (menuName.length() > 32) {
                    menuName = menuName.substring(0, 31);
                }
                page.setName(menuName);
            }

            String menuName = "Page " + (size + 1) + "/" + (size + 1) + " " + categoryName;
            if (menuName.length() > 32) {
                menuName = menuName.substring(0, 31);
            }
            IconMenu iconMenu = new IconMenu(menuName, (rowsPerPage + 1) * 9, plugin, new OptionClickEventHandler() {

                @Override
                public void onOptionClick(final IconMenu.OptionClickEvent event) {
                    event.setWillClose(true);
                    if (event.getPosition() / 9 != rowsPerPage) {
                        event.getPlayer().getInventory().addItem(event.getItem());
                        event.getPlayer().sendMessage(ChatColor.GREEN + "Here's the skull");
                        return;
                    }
                    if (event.getPosition() == rowsPerPage * 9) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                pages.get(size - 1).open(event.getPlayer());
                            }
                        }, plugin.getConfig().getLong("delay"));
                        return;
                    }
                    if (event.getPosition() == ((rowsPerPage + 1) * 9) - 1) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                pages.get(size + 1).open(event.getPlayer());
                            }
                        }, plugin.getConfig().getLong("delay"));
                    }
                }
            });

            if (!pages.isEmpty()) {
                ItemStack skull1 = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
                SkullMeta skullMeta1 = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
                skullMeta1.setOwner("MHF_ArrowRight");
                skull1.setItemMeta(skullMeta1);
                pages.get(size - 1).setOption(((rowsPerPage + 1) * 9) - 1, skull1, "Next", "");

                ItemStack skull2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
                SkullMeta skullMeta2 = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
                skullMeta2.setOwner("MHF_ArrowLeft");
                skull2.setItemMeta(skullMeta2);
                iconMenu.setOption(rowsPerPage * 9, skull2, "Previous", "");
            }
            pages.add(iconMenu);
        }

        pages.get(pages.size() - 1).setOption(slot, plugin.getSkullsUtils().createSkull(name, value, owner), name, "");
        slot = (slot + 1) % (rowsPerPage * 9);
        names.add(name);
    }

    public void openPage(int page, final Player player) {
        if (page >= pages.size() || page < 0) {
            player.sendMessage(ChatColor.RED + "Invalid page number");
            return;
        }
        pages.get(page).open(player);
    }

    public Set<String> getNames() {
        return names;
    }

    public ItemStack getSkull(String skullName) {
        for (IconMenu page : pages) {
            ItemStack item = page.getItemByName(skullName);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public void loadSkulls() {
        if (categoryName.equalsIgnoreCase("everything")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    ConfigurationSection section = plugin.getConfig().getConfigurationSection("categories");
                    Set<String> set = section.getKeys(true);
                    for (String category : set) {
                        if (category.equalsIgnoreCase("everything")) {
                            continue;
                        }
                        File file = new File(plugin.getDataFolder(), category + ".json");
                        if (!file.exists()) {
                            updateSkulls();
                        }
                        String jsonString = "";
                        try (Scanner scanner = new Scanner(file)) {
                            while (scanner.hasNextLine()) {
                                jsonString += scanner.nextLine();
                            }
                        } catch (FileNotFoundException ex) {
                            plugin.getLogger().log(Level.SEVERE, "Couldn''t find {0}.json", category);
                        }
                        JSONArray json = new JSONArray(jsonString);
                        int i;
                        for (i = 0; i < json.length(); i++) {
                            String name = json.getJSONObject(i).getString("name");
                            UUID skullOwner = UUID.fromString(json.getJSONObject(i).getString("skullowner"));
                            String value = json.getJSONObject(i).getString("value");
                            addSkull(name, skullOwner, value);
                        }
                    }
                }
            }, 20);
            return;
        }
        File file = new File(plugin.getDataFolder(), categoryName + ".json");
        if (!file.exists()) {
            updateSkulls();
        }
        String jsonString = "";
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                jsonString += scanner.nextLine();
            }
        } catch (FileNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn''t find {0}.json", categoryName);
        }
        JSONArray json = new JSONArray(jsonString);
        int i;
        for (i = 0; i < json.length(); i++) {
            String name = json.getJSONObject(i).getString("name");
            UUID skullOwner = UUID.fromString(json.getJSONObject(i).getString("skullowner"));
            String value = json.getJSONObject(i).getString("value");
            addSkull(name, skullOwner, value);
        }
        plugin.getLogger().log(Level.INFO, "Loaded {0} skulls in category {1}", new Object[]{i, categoryName});
    }

    public void unloadSkulls() {
        for (IconMenu page : pages) {
            page.destroy();
        }
        pages.clear();
    }

    public void updateSkulls() {
        if (categoryName.equalsIgnoreCase("everything")) {
            return;
        }
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File file = new File(plugin.getDataFolder(), categoryName + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't find {0}.json", categoryName);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage("[XPD-Skulls] " + ChatColor.GREEN + "Updating " + categoryName + ".json!");
        JSONArray json = plugin.getSkullsUtils().fromUrl("http://heads.freshcoal.com/mainapi.php?query=" + categoryName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(json.toString());
            writer.flush();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't write to {0}.json", categoryName);
        }
    }
}
