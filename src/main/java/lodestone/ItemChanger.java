package lodestone;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemChanger {

    public static ItemStack addEnchantmentGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LURE, 1);

        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack removeEnchantmentGlow(ItemStack item) {
        item.removeEnchantment(Enchantment.LURE);

        ItemMeta meta = item.getItemMeta();
        meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }

    public static String getName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        String name = meta.hasDisplayName()? meta.getDisplayName():
                (meta.hasLocalizedName()? meta.getLocalizedName():
                        Strings.capitalize(item.getType().toString().replace("_", " "))
                );

        return name;
    }

    public static ItemStack changeName(ItemStack item, Function<String, String> nameChanger) {
        ItemMeta meta = item.getItemMeta();

        String name = meta.hasDisplayName()? meta.getDisplayName():
                        (meta.hasLocalizedName()? meta.getLocalizedName():
                                Strings.capitalize(item.getType().toString().replace("_", " "))
                        );

        name = nameChanger.apply(name);
        meta.setDisplayName(name);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack changeLore(ItemStack item, int loreLine, Function<String, String> loreChanger) {
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        lore.set(loreLine, loreChanger.apply(lore.get(loreLine)));
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack addLore(ItemStack item, String line) {
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        if(lore == null) lore = new ArrayList<>();
        lore.add(line);
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

}
