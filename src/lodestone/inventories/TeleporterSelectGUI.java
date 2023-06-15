package lodestone.inventories;

import lodestone.ChatUtil;
import lodestone.ItemChanger;
import lodestone.Main;
import lodestone.Options;
import lodestone.teleporter.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TeleporterSelectGUI implements GUI {

    private int PAGE_BACK_INDEX = 36;
    private int PAGE_NEXT_INDEX = 37;
    private int SORTING_INDEX = 39;

    private List<Teleporter> teleporters;

    private SortingStyles sortingStyle;
    private Player player;
    private int page;
    private boolean openedFromCommand;

    //TODO: allow for filter by player
    //TODO: filter by dimension (availables and same dimension)

    public TeleporterSelectGUI(Player player, int page, SortingStyles sortingStyles, boolean openedFromCommand) {
        this.player = player;
        this.openedFromCommand = openedFromCommand;

        teleporters = Main.teleportHandler.getTeleporter();
        this.sortingStyle = sortingStyles;

        teleporters = teleporters.stream()
                .filter(tp ->
                        !Options.PRIVATE_TP || tp.isOwner(player)
                )
                .filter(tp ->
                        Options.ALLOW_INTERDIMENSIONAL_TRAVEL || tp.sameDimension(player.getLocation())
                )
                .sorted((o1, o2) -> switch (sortingStyles) {
                    case CREATION_DATE -> 0;
                    case PLAYER_NAME -> o1.owner().compareTo(o2.owner());
                    case DISTANCE -> Double.compare(calculateDistance(o1), calculateDistance(o2));
                })
                .toList();

        this.page = Math.max(0, page);
        this.page = Math.min(page, getMaxPage());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        //TODO: only cancel when interacts with this inventory
        if(event.getClickedInventory() == null ||
                event.getClickedInventory().getHolder() == null ||
                event.getClickedInventory().getHolder() != this) {

            return;
        }

        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();
        if(slot < 0) return;

        if(slot == PAGE_BACK_INDEX && page > 0) Main.openTeleportSelector(player, page-1, sortingStyle, openedFromCommand);
        else if(slot == PAGE_NEXT_INDEX && page < getMaxPage()) Main.openTeleportSelector(player, page+1, sortingStyle, openedFromCommand);
        else if(slot == SORTING_INDEX) Main.openTeleportSelector(player, page, sortingStyle.next(), openedFromCommand);

        if(slot >= 3 * 9) return;

        int teleporterIndex = (page * 3 * 9) + slot;
        if(teleporterIndex >= teleporters.size()) return;
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

    @Override
    public Inventory getInventory() {
        int offset = page * (3 * 9);
        Inventory inventory = Bukkit.createInventory(this, 9 * 5, ChatColor.DARK_GRAY +  "Select Teleporter (" + (page+1) + "/" + (getMaxPage() + 1) + ")");
        for(int i = 0; i < 27; i++) {
            int index = i + offset;
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

            ItemStack teleportIcon = currentTeleporter.displayItem().clone();
            ItemChanger.addLore(teleportIcon, ChatColor.GRAY + distanceToPlayer);
            if(Options.PAY_FOR_TELEPORT && !(player.hasPermission("ignoreCosts") || player.getGameMode() == GameMode.CREATIVE))
                ItemChanger.addLore(teleportIcon, ChatColor.BOLD.toString() + ChatColor.RED + "Costs " + calculatePrice(currentTeleporter) + " " + ItemChanger.getName(new ItemStack(Options.CURRENCY)));

            inventory.setItem(i, teleportIcon);
        }

        if(page > 0) {
            ItemStack backItem = new ItemStack(Material.ARROW);
            ItemChanger.changeName(backItem, name -> ChatColor.DARK_RED + "Page back");
            inventory.setItem(PAGE_BACK_INDEX, backItem);
        }

        if(page < getMaxPage()) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemChanger.changeName(nextItem, name -> ChatColor.DARK_RED + "Next Page");
            inventory.setItem(PAGE_NEXT_INDEX, nextItem);
        }

        {
            ItemStack sortingItem = new ItemStack(Material.HOPPER);
            ItemChanger.changeName(sortingItem, name -> ChatColor.AQUA + "Sort");
            for(SortingStyles style: SortingStyles.values()) {
                ChatColor color = style == sortingStyle? ChatColor.WHITE: ChatColor.GRAY;
                String prefix = style == sortingStyle? "> ": "";

                ItemChanger.addLore(sortingItem, color + prefix + style.toString().charAt(0) + style.toString().substring(1).toLowerCase());
            }

            inventory.setItem(SORTING_INDEX, sortingItem);
        }

        return inventory;
    }

    private int getMaxPage() {
        return teleporters.size() / (3 * 9);
    }

    private double calculateDistance(Teleporter tp) {
        return tp.sameDimension(player.getLocation())?
                tp.location().distance(player.getLocation()):
                (Options.PRICE_ENDS_AT_DISTANCE);
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

            if(openedFromCommand) {
                float modifier = Options.TP_COMMAND_COST;

                if(modifier > 0 && modifier <= 1) {
                    price = (int) Math.ceil((1.0f + modifier) * price);
                } if(modifier > 1) {
                    price = (int) Math.ceil(modifier + price);
                }
            }
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
