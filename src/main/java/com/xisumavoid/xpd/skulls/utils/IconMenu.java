package com.xisumavoid.xpd.skulls.utils;

import com.xisumavoid.xpd.skulls.Skulls;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;

public class IconMenu implements Listener {

    private String name;
    private final int size;
    private OptionClickEventHandler handler;
    private Player player;
    private String[] optionNames;
    private ItemStack[] optionIcons;
    private final Skulls plugin;
    private Inventory inventory;

    public IconMenu(String name, int size, Skulls plugin, OptionClickEventHandler handler) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public IconMenu setOption(int position, ItemStack icon, String name, String... info) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void setSpecificTo(Player player) {
        this.player = player;
    }

    public boolean isSpecific() {
        return player != null;
    }

    public void open(Player player) {
        if (inventory == null) {
            inventory = Bukkit.createInventory(player, size, name);
            for (int i = 0; i < optionIcons.length; i++) {
                if (optionIcons[i] != null) {
                    inventory.setItem(i, optionIcons[i]);
                }
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        handler = null;
        optionNames = null;
        optionIcons = null;
    }

    public ItemStack getItemByName(String name) {
        int index = ArrayUtils.indexOf(optionNames, name);
        if (index < 0 || index >= optionIcons.length) {
            return null;
        }
        return optionIcons[index];
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals(name) && (player == null || event.getWhoClicked() == player)) {
            event.setCancelled(true);
            if (event.getClick() != ClickType.LEFT) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot], optionIcons[slot]);
                handler.onOptionClick(e);
                ((Player) event.getWhoClicked()).updateInventory();
                if (e.willClose()) {
                    final Player p = (Player) event.getWhoClicked();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            p.closeInventory();
                        }
                    });
                }
                if (e.willDestroy()) {
                    destroy();
                }
            }
        }
    }

    public interface OptionClickEventHandler {

        public void onOptionClick(OptionClickEvent event);
    }

    public class OptionClickEvent {

        private final Player player;
        private final int position;
        private final String name;
        private boolean close;
        private boolean destroy;
        private final ItemStack item;

        public OptionClickEvent(Player player, int position, String name, ItemStack item) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = false;
            this.item = item;
        }

        public Player getPlayer() {
            return player;
        }

        public int getPosition() {
            return position;
        }

        public String getName() {
            return name;
        }

        public boolean willClose() {
            return close;
        }

        public boolean willDestroy() {
            return destroy;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> list = new ArrayList<>();
        for (String l : lore) {
            list.add(ChatColor.translateAlternateColorCodes('&', l));
        }
        im.setLore(list);
        item.setItemMeta(im);
        return item;
    }

}
