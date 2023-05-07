package lodestone;

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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class EventListener implements Listener {

    private TeleporterHandler teleportHandler;
    public EventListener(TeleporterHandler teleporterHandler) {
        this.teleportHandler = teleporterHandler;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for(int i = e.getBlocks().size() - 1; i >= 0; i--) {
            Block b = e.getBlocks().get(i);

            if(blockDestroyed(b.getLocation(), Optional.empty())) {
                e.getBlocks().remove(i);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for(int i = e.getBlocks().size() - 1; i >= 0; i--) {
            Block b = e.getBlocks().get(i);

            if(blockDestroyed(b.getLocation(), Optional.empty())) {
                e.getBlocks().remove(i);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(blockDestroyed(e.getBlock().getLocation(), Optional.of(e.getPlayer())));
    }

    @EventHandler
    public void onExplodeEvent(EntityExplodeEvent e) {
        for(int i = e.blockList().size() - 1; i >= 0; i--) {
            Block b = e.blockList().get(i);

            if(blockDestroyed(b.getLocation(), Optional.empty())) {
                e.blockList().remove(i);
            }
        }
    }

    /**
     *
     * @param location
     * @param player
     * @return true if the event should be cancelled
     */
    private boolean blockDestroyed(Location location, Optional<Player> player) {
        Optional<Teleporter> removedTp = teleportHandler.getByLocation(location);

        if(!removedTp.isPresent()) return false;

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

            location.getWorld().dropItemNaturally(location, droppedItem);
        }

        return false;
    }

}
