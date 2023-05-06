package lodestone.inventories;

import lodestone.ItemChanger;
import lodestone.Main;
import lodestone.teleporter.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TeleporterSelectGUI implements GUI {

    private Player player;
    private int offset;
    public TeleporterSelectGUI(Player player, int offsetIn) {
        this.player = player;
        this.offset = Math.max(0, offsetIn);
        this.offset = Math.min(offset, Math.max(0, Main.teleportHandler.getAmount() - 7));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();

        int slot = e.getSlot();
        if(slot < 0) return;

        if(slot == 0) Main.openTeleportSelector(player, offset - 8);
        else if(slot == 8) Main.openTeleportSelector(player, offset + 8);

        else {
            int teleporterIndex = offset + slot - 1;
            player.teleport(
                    Main.teleportHandler.getByIndex(teleporterIndex).location().clone()
                        .add(0.5, 1, 0.5)
                        .setDirection(player.getLocation().getDirection())
            );
        }
    }

    @Override
    public Inventory getInventory() {
        var tps = Main.teleportHandler;

        //TODO: item for scrolling
        //TODO: display page in title

        Inventory inv = Bukkit.createInventory(this, 9, ChatColor.DARK_AQUA +  "Select Teleporter");
        for(int i = 1; i < 8; i++) {
            int index = i - 1 + offset;
            if(index >= tps.getAmount()) break;
            Teleporter currentTeleporter = tps.getByIndex(index);

            String distanceToPlayer = "???";
            if(currentTeleporter.location().getWorld().getEnvironment() == player.getLocation().getWorld().getEnvironment()) {
                distanceToPlayer = "" + ((int) currentTeleporter.location().distance(player.getLocation()));
            }

            ItemStack item = currentTeleporter.displayItem().clone();
            ItemChanger.addLore(item, ChatColor.GRAY + distanceToPlayer + " blocks away");

            inv.setItem(i, item);
        }


        return inv;
    }
}
