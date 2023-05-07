package lodestone.inventories;

import lodestone.ChatUtil;
import lodestone.ItemChanger;
import lodestone.Main;
import lodestone.Options;
import lodestone.teleporter.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TeleporterSelectGUI implements GUI {

    private List<Teleporter> teleporters;

    private Player player;
    private int offset;
    public TeleporterSelectGUI(Player player, int offsetIn) {
        teleporters = Main.teleportHandler.getTeleporter();

        teleporters = teleporters.stream()
                .filter(tp ->
                        !Options.PRIVATE_TP || tp.isOwner(player)
                )
                .filter(tp ->
                        Options.ALLOW_INTERDIMENSIONAL_TRAVEL || tp.sameDimension(player.getLocation())
                )
                .toList();

        this.player = player;
        this.offset = Math.max(0, offsetIn);
        this.offset = Math.min(offset, Math.max(0, teleporters.size() - 7));
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
            Teleporter selectedTP = teleporters.get(teleporterIndex);

            boolean playerAndTPSameDimension = selectedTP.sameDimension(player.getLocation());
            if(!playerAndTPSameDimension && !Options.ALLOW_INTERDIMENSIONAL_TRAVEL) return;

            double distance = calculateDistance(selectedTP);

            boolean liesInAllowedWindow = distance >= Options.MIN_TELEPORT_DISTANCE && (Options.MAX_TELEPORT_DISTANCE <= 0 || distance <= Options.MAX_TELEPORT_DISTANCE);
            if(!liesInAllowedWindow) {
                ChatUtil.sendErrorMessage(player, "This teleport does not lie in the allowed range.");
            }

            if(Options.PAY_FOR_TELEPORT && !player.hasPermission("ignoreCosts")) {
                int price = calculatePrice(selectedTP);

                int holdCurrency = countCurrencyInPlayerInventory();
                if(holdCurrency < price && player.getGameMode() != GameMode.CREATIVE) {
                    ChatUtil.sendErrorMessage(player, "You do not have enough currency for this teleport.");
                    player.closeInventory();
                    return;
                }

                if(player.getGameMode() != GameMode.CREATIVE) player.getInventory().removeItem(new ItemStack(Options.CURRENCY, price));
            }

            player.teleport(
                    selectedTP.location().clone()
                        .add(0.5, 1, 0.5)
                        .setDirection(player.getLocation().getDirection())
            );
        }
    }

    @Override
    public Inventory getInventory() {
        //TODO: item for scrolling
        //TODO: display page in title

        Inventory inv = Bukkit.createInventory(this, 9, ChatColor.DARK_AQUA +  "Select Teleporter");
        for(int i = 1; i < 8; i++) {
            int index = i - 1 + offset;
            if(index >= teleporters.size()) break;
            Teleporter currentTeleporter = teleporters.get(index);

            String distanceToPlayer = "???";
            if(currentTeleporter.sameDimension(player.getLocation())) {
                int distance = (int) currentTeleporter.location().distance(player.getLocation());
                distanceToPlayer = "" + distance;
                distanceToPlayer += " blocks away";

                if(distance < Options.MIN_TELEPORT_DISTANCE) distanceToPlayer = "Too close";
                if(distance > Options.MAX_TELEPORT_DISTANCE && Options.MAX_TELEPORT_DISTANCE >= 0) distanceToPlayer = "Too far";
            }

            ItemStack item = currentTeleporter.displayItem().clone();
            ItemChanger.addLore(item, ChatColor.GRAY + distanceToPlayer);
            if(Options.PAY_FOR_TELEPORT && !(player.hasPermission("ignoreCosts") || player.getGameMode() == GameMode.CREATIVE))
                ItemChanger.addLore(item, ChatColor.BOLD.toString() + ChatColor.RED + "Costs " + calculatePrice(currentTeleporter) + " " + ItemChanger.getName(new ItemStack(Options.CURRENCY)));

            inv.setItem(i, item);
        }

        return inv;
    }

    private double calculateDistance(Teleporter tp) {
        return tp.sameDimension(player.getLocation())?
                tp.location().distance(player.getLocation()):
                (Options.PRICE_STARTS_AT_DISTANCE + Options.PRICE_ENDS_AT_DISTANCE) / 2.0;
    }

    private int calculatePrice(Teleporter tp) {
        double distance = calculateDistance(tp);
        int price = 0;
        if(Options.PAY_FOR_TELEPORT) {
            if(distance < Options.PRICE_STARTS_AT_DISTANCE) price = 0;
            else if(distance >= Options.PRICE_STARTS_AT_DISTANCE && distance <= Options.PRICE_ENDS_AT_DISTANCE) {
                double percentile = (distance - Options.PRICE_STARTS_AT_DISTANCE) / (Options.PRICE_ENDS_AT_DISTANCE - Options.PRICE_STARTS_AT_DISTANCE);
                price = (int) Math.round(percentile * (Options.MAX_PRICE - Options.MIN_PRICE) + Options.MIN_PRICE);
            }
            else price = Options.MAX_PRICE;

            if(!tp.sameDimension(player.getLocation())) price = Options.INTERDIMENSIONAL_TRAVEL_COST;
        }

        return price;
    }

    private int countCurrencyInPlayerInventory() {
        int count = 0;
        for(int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if(s != null && s.getType() == Options.CURRENCY) count += s.getAmount();
        }
        return count;
    }
}
