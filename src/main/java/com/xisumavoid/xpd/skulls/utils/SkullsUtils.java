package com.xisumavoid.xpd.skulls.utils;

import com.xisumavoid.xpd.skulls.Skulls;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Autom
 */
public class SkullsUtils {

    private final Skulls plugin;
    private final Map<Integer, SkullsCategory> categories = new HashMap<>();

    public SkullsUtils(Skulls skulls) {
        this.plugin = skulls;
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("categories");
                Set<String> set = section.getKeys(true);
                for (String category : set) {
                    categories.put(section.getInt(category), new SkullsCategory(plugin, category));
                }
            }
        }, 20);

    }

    public ItemStack getSkull(String skullName) {
        for (SkullsCategory category : plugin.getSkullsUtils().getCategories().values()) {
            ItemStack skull = category.getSkull(skullName);
            if (skull != null) {
                return skull;
            }
        }
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
        skullMeta.setOwner(skullName);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public Map<Integer, SkullsCategory> getCategories() {
        return categories;
    }

    public ItemStack createSkull(String name, String value, UUID owner) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = new net.minecraft.server.v1_8_R3.ItemStack(net.minecraft.server.v1_8_R3.Items.SKULL, 1, 3);
        nmsStack.setTag(new NBTTagCompound());
        NBTTagCompound displayTag = new NBTTagCompound();
        NBTTagCompound entryTag = new NBTTagCompound();
        NBTTagCompound propertiesTag = new NBTTagCompound();
        NBTTagCompound skullOwnerTag = new NBTTagCompound();

        NBTTagList texturesList = new NBTTagList();

        displayTag.setString("Name", name);

        entryTag.setString("Value", value);

        texturesList.add(entryTag);

        propertiesTag.set("textures", texturesList);

        skullOwnerTag.setString("Id", owner.toString());
        skullOwnerTag.set("Properties", propertiesTag);

        nmsStack.getTag().set("display", displayTag);
        nmsStack.getTag().set("SkullOwner", skullOwnerTag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public JSONArray fromUrl(String urlString) {
        String all = readUrl(urlString);
        String body = all.substring(all.indexOf("<body>") + 6, all.indexOf("</body>")).trim();
        return new JSONArray(body);
    }

    private String readUrl(String urlString) {
        HttpURLConnection request = null;
        try {
            URL url = new URL(urlString);
            request = (HttpURLConnection) url.openConnection();

            request.setRequestMethod("POST");

            request.setDoOutput(true);
            request.setDoInput(true);
            request.setInstanceFollowRedirects(false);
            request.setRequestMethod("POST");
            request.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            request.setRequestProperty("charset", "utf-8");
            request.setUseCaches(false);

            int responseCode = request.getResponseCode();

            if (responseCode == 200) {
                String encoding = request.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;
                return IOUtils.toString(request.getInputStream(), encoding);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("URL not found");
        } finally {

            if (request != null) {
                request.disconnect();
            }
        }
        return null;
    }
}
