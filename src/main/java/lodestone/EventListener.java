package lodestone;

import lodestone.inventories.GUI;
import lodestone.teleporter.Teleporter;
import lodestone.teleporter.TeleporterHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class EventListener implements Listener {

    private final TeleporterHandler teleportHandler;
    public EventListener(TeleporterHandler teleporterHandler) {
        this.teleportHandler = teleporterHandler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getInventory().getHolder() != null &&
            event.getInventory().getHolder() instanceof GUI gui) {

            gui.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(int i = event.getBlocks().size() - 1; i >= 0; i--) {
            Block block = event.getBlocks().get(i);

            if(blockDestroyed(block.getLocation(), Optional.empty())) {
                event.getBlocks().remove(i);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for(int i = event.getBlocks().size() - 1; i >= 0; i--) {
            Block block = event.getBlocks().get(i);

            if(blockDestroyed(block.getLocation(), Optional.empty())) {
                event.getBlocks().remove(i);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(blockDestroyed(event.getBlock().getLocation(), Optional.of(event.getPlayer()))) event.setCancelled(true);
    }

    @EventHandler
    public void onExplodeEvent(EntityExplodeEvent event) {
        for(int i = event.blockList().size() - 1; i >= 0; i--) {
            Block block = event.blockList().get(i);

            if(blockDestroyed(block.getLocation(), Optional.empty())) {
                event.blockList().remove(i);
            }
        }
    }

    private boolean blockDestroyed(Location location, Optional<Player> player) {
        Optional<Teleporter> removedTp = teleportHandler.getByLocation(location);

        if(removedTp.isEmpty()) return false;

        if(Options.ONLY_ALLOW_OWNER_TO_BREAK) {
            if(player.isPresent()) {
                boolean sameOwner = removedTp.get().isOwner(player.get());
                if(!(sameOwner || player.get().isOp() || player.get().hasPermission("breakAllTP"))) {
                    ChatUtil.sendErrorMessage(player.get(), "This teleporter belongs to a different player!");
                    return true;
                }
            } else {
                return true;
            }
        }

        teleportHandler.deleteTeleporter(location);
        if(Options.DROP_ITEM_ON_BREAK) {
            ItemStack droppedItem = removedTp.get().displayItem().clone();
            ItemChanger.setLore(droppedItem, new ArrayList<>());
            ItemChanger.removeEnchantmentGlow(droppedItem);
            ItemChanger.changeName(droppedItem, e -> null);

            if(location.getWorld() != null) {
                location.getWorld().dropItemNaturally(location, droppedItem);
            }
        }

        return false;
    }

}
