package com.xisumavoid.xpd.skulls.commands;

import com.xisumavoid.xpd.skulls.Skulls;
import com.xisumavoid.xpd.skulls.utils.IconMenu;
import com.xisumavoid.xpd.skulls.utils.IconMenu.OptionClickEventHandler;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONArray;

/**
 *
 * @author Autom
 */
public class SkullsCommand implements CommandExecutor {

    private final Skulls plugin;

    public SkullsCommand(Skulls plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("update") && sender.hasPermission("skulls.update")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GREEN + "Updating all categories...");
                for (Integer i : plugin.getSkullsUtils().getCategories().keySet()) {
                    plugin.getSkullsUtils().getCategories().get(i).updateSkulls();
                }
            } else {
                if (plugin.getConfig().isInt("categories." + args[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Updating all category: " + args[1] + "...");
                    plugin.getSkullsUtils().getCategories().get(plugin.getConfig().getInt("categories." + args[1])).updateSkulls();
                } else {
                    sendCategories(sender);
                    return true;
                }
            }

            sender.sendMessage(ChatColor.GOLD + "Now you need to reload this plugin!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can perform this command");
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("skulls.specialskull")) {

            if (args.length == 0) {
                if (plugin.getConfig().isInt("categories.everything")) {
                    plugin.getSkullsUtils().getCategories().get(plugin.getConfig().getInt("categories.everything")).openPage(0, player);
                    return true;
                }
            }

            if (args.length > 0) {
                if (args.length > 1 && args[0].equalsIgnoreCase("search") && player.hasPermission("skulls.search")) {
                    if (player.hasPermission("skulls.searchapi")) {
                        if (args[1].length() < 3) {
                            player.sendMessage(ChatColor.RED + "Use at least 3 characters");
                            return true;
                        }
                        IconMenu menu = new IconMenu("Pick a skull", 54, plugin, new OptionClickEventHandler() {

                            @Override
                            public void onOptionClick(IconMenu.OptionClickEvent event) {
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                                event.getPlayer().getInventory().addItem(event.getItem());
                                event.getPlayer().sendMessage(ChatColor.GREEN + "Here's the skull");
                            }
                        });
                        JSONArray jsonArray = plugin.getSkullsUtils().fromUrl("http://heads.freshcoal.com/api.php?query=" + args[1]);
                        for (int i = 0; i < Math.min(jsonArray.length(), 54); i++) {
                            String name = jsonArray.getJSONObject(i).getString("name");
                            UUID skullOwner = UUID.fromString(jsonArray.getJSONObject(i).getString("skullowner"));
                            String value = jsonArray.getJSONObject(i).getString("value");
                            menu.setOption(i, plugin.getSkullsUtils().createSkull(name, value, skullOwner), name, "");
                        }
                        menu.open(player);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("categories")) {
                    sendCategories(sender);
                    return true;
                }
                if (plugin.getConfig().isInt("categories." + args[0])) {
                    if (args.length > 2 && args[1].equalsIgnoreCase("page") && StringUtils.isNumeric(args[2])) {
                        plugin.getSkullsUtils().getCategories().get(plugin.getConfig().getInt("categories." + args[0])).openPage(Integer.parseInt(args[2]) - 1, player);
                    }
                    plugin.getSkullsUtils().getCategories().get(plugin.getConfig().getInt("categories." + args[0])).openPage(0, player);
                    return true;
                }
                player.getInventory().addItem(plugin.getSkullsUtils().getSkull(StringUtils.join(args, ' ', 0, args.length)));
                player.sendMessage(ChatColor.GREEN + "Here's the skull");
                return true;
            }
        }

        if (player.hasPermission("skulls.ownskull")) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
            SkullMeta skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
            skullMeta.setOwner(player.getName());
            skull.setItemMeta(skullMeta);
        }
        return false;
    }

    private void sendCategories(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "No category found with that name, possible categories: " + ChatColor.GOLD + StringUtils.join(plugin.getSkullsUtils().getCategories().keySet(), ", "));
    }
}
