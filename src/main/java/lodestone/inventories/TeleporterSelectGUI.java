package lodestone.inventories;

import lodestone.ChatUtil;
import lodestone.ItemChanger;
import lodestone.Main;
import lodestone.Options;
import lodestone.teleporter.Teleporter;
import lodestone.teleporter.TeleporterHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class TeleporterSelectGUI implements GUI {

    private final int PAGE_BACK_INDEX = 36;
    private final int PAGE_NEXT_INDEX = 37;
    private final int SORTING_INDEX = 39;
    private final int PLAYER_FILTER_INDEX = 40;

    private List<Teleporter> teleporters;

    private SortingStyles sortingStyle;
    private Player player;
    private int page;
    private boolean openedFromCommand;
    private int currentPlayerFilter = -1;
    private int dimensionFilter = -1;   //NONE, SAME, OVERWORLD, NETHER, END, CUSTOM

    //TODO: filter by dimension (availables and same dimension)
    //TODO: filter only tps with enough money

    public TeleporterSelectGUI(TeleporterSelectGUI clone) {
        this(clone.player, clone.page, clone.sortingStyle, clone.openedFromCommand, clone.currentPlayerFilter);
    }

    public TeleporterSelectGUI(Player player, int page, SortingStyles sortingStyles, boolean openedFromCommand, int currentPlayerFilter) {
        this.player = player;
        this.openedFromCommand = openedFromCommand;
        this.currentPlayerFilter = currentPlayerFilter;

        teleporters = Main.teleportHandler.getTeleporter();
        this.sortingStyle = sortingStyles;

        teleporters = teleporters.stream()
                .filter(tp ->
                        !Options.PRIVATE_TP || tp.isOwner(player)
                )
                .filter(tp ->
                        Options.ALLOW_INTERDIMENSIONAL_TRAVEL || tp.sameDimension(player.getLocation())
                )
                .filter(tp ->
                        currentPlayerFilter == -1 ||
                                Main.teleportHandler.getCreatorList().get(currentPlayerFilter).getUniqueId().toString().equals(tp.owner())
                )
                .sorted((o1, o2) -> sortingStyle.sort(player, o1, o2))
                .toList();

        this.page = Math.max(0, page);
        this.page = Math.min(page, getMaxPage());
    }

    //TODO: break this up as well
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

        if(slot == PAGE_BACK_INDEX && page > 0) Main.openTeleportSelector(player, page-1, sortingStyle, openedFromCommand, currentPlayerFilter);
        else if(slot == PAGE_NEXT_INDEX && page < getMaxPage()) Main.openTeleportSelector(player, page+1, sortingStyle, openedFromCommand, currentPlayerFilter);
        else if(slot == SORTING_INDEX) Main.openTeleportSelector(player, page, sortingStyle.next(), openedFromCommand, currentPlayerFilter);
        else if (slot == PLAYER_FILTER_INDEX) {
            List<OfflinePlayer> playerNames = Main.teleportHandler.getCreatorList();
            if(event.isShiftClick()) {

                currentPlayerFilter = -1;
                for(int i = 0; i < playerNames.size(); i++) {
                    String s = playerNames.get(i).getName();
                    if(s != null && s.equals(player.getName())) {
                        currentPlayerFilter = i;
                        break;
                    }
                }
            }

           else if(event.isRightClick()) {
                currentPlayerFilter = -1;
            }

            else if(event.isLeftClick()) {
                currentPlayerFilter += 1;
                System.out.println(currentPlayerFilter + " " + playerNames.size());
                if(currentPlayerFilter >= playerNames.size()) currentPlayerFilter = -1;
                System.out.println(currentPlayerFilter + " " + playerNames.size());
                System.out.println("---------------");
            }

            player.openInventory(new TeleporterSelectGUI(this).getInventory());
        }

        if(slot >= 3 * 9) return;

        int teleporterIndex = (page * 3 * 9) + slot;
        if(teleporterIndex >= teleporters.size()) return;
        Teleporter selectedTP = teleporters.get(teleporterIndex);

        boolean playerAndTPSameDimension = selectedTP.sameDimension(player.getLocation());
        if(!playerAndTPSameDimension && !Options.ALLOW_INTERDIMENSIONAL_TRAVEL) return;

        double distance = TeleporterHandler.calculateDistance(selectedTP, player);

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

            if(player.getGameMode() != GameMode.CREATIVE && price > 0) {
                player.getInventory().removeItem(new ItemStack(Options.CURRENCY, price));
            }
        }

        player.teleport(
                selectedTP.location().clone()
                    .add(0.5, 1, 0.5)
                    .setDirection(player.getLocation().getDirection())
        );
    }

    //TODO: Break this up. getXYItem(...)
    @Override
    public Inventory getInventory() {
        int offset = page * (3 * 9);
        Inventory inventory = Bukkit.createInventory(this, 9 * 5, ChatColor.DARK_GRAY +  "Select Teleporter (" + (page+1) + "/" + (getMaxPage() + 1) + ")");

        //Insert teleporter items
        for(int i = 0; i < 27; i++) {
            int index = i + offset;
            if(index >= teleporters.size()) break;
            Teleporter currentTeleporter = teleporters.get(index);

            String distanceToPlayer = "???";
            if(currentTeleporter.sameDimension(player.getLocation())) {
                int distance = (int) currentTeleporter.location().distance(player.getLocation());
                distanceToPlayer = String.valueOf(distance);
                distanceToPlayer += " blocks away";

                if(distance < Options.MIN_TELEPORT_DISTANCE) distanceToPlayer = "Too close";
                if(distance > Options.MAX_TELEPORT_DISTANCE && Options.MAX_TELEPORT_DISTANCE >= 0) distanceToPlayer = "Too far";
            }

            ItemStack teleportIcon = currentTeleporter.displayItem().clone();
            ItemChanger.addLore(teleportIcon, ChatColor.GRAY + distanceToPlayer);
            if(Options.PAY_FOR_TELEPORT && !(player.hasPermission("ignoreCosts") || player.getGameMode() == GameMode.CREATIVE)) {
                int goldInPlayersInventory = countCurrencyInPlayerInventory();
                int price = calculatePrice(currentTeleporter);
                ChatColor color = goldInPlayersInventory < price? ChatColor.RED: ChatColor.GREEN;
                ItemChanger.addLore(teleportIcon, ChatColor.BOLD.toString() + color + "Costs " + price + " " + ItemChanger.getName(new ItemStack(Options.CURRENCY)));
            }

            inventory.setItem(i, teleportIcon);
        }

        if(page > 0) {  //Previous Page Item
            ItemStack backItem = new ItemStack(Material.ARROW);
            ItemChanger.changeName(backItem, name -> ChatColor.DARK_RED + "Page back");
            inventory.setItem(PAGE_BACK_INDEX, backItem);
        }

        if(page < getMaxPage()) {   //Next Page Item
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemChanger.changeName(nextItem, name -> ChatColor.DARK_RED + "Next Page");
            inventory.setItem(PAGE_NEXT_INDEX, nextItem);
        }

        {//Sort Item
            ItemStack sortingItem = new ItemStack(Material.HOPPER);
            ItemChanger.changeName(sortingItem, name -> ChatColor.AQUA + "Sort");
            for(SortingStyles style: SortingStyles.values()) {
                ChatColor color = style == sortingStyle? ChatColor.WHITE: ChatColor.GRAY;
                String prefix = style == sortingStyle? "> ": "";

                String styleString = style.toString().charAt(0) + style.toString().substring(1).toLowerCase();
                styleString = styleString.replace("_", " ");

                ItemChanger.addLore(sortingItem, color + prefix + styleString);
            }

            inventory.setItem(SORTING_INDEX, sortingItem);
        }

        {//Filter Player Item
            ItemStack playerFilterItem = new ItemStack(Material.PLAYER_HEAD);
            String playerName = "";

            if(currentPlayerFilter == -1) {
                playerName = "None";
            } else {
                List<OfflinePlayer> player = Main.teleportHandler.getCreatorList();
                playerName = player.get(currentPlayerFilter).getName();
                SkullMeta meta = (SkullMeta) playerFilterItem.getItemMeta();
                meta.setOwningPlayer(player.get(currentPlayerFilter));
                playerFilterItem.setItemMeta(meta);
            }

            ItemChanger.changeName(playerFilterItem, name -> ChatColor.AQUA + "Filter by Player");
            ItemChanger.addLore(playerFilterItem, ChatColor.GRAY + "Current Player: " + ChatColor.BOLD + playerName);
            ItemChanger.addLore(playerFilterItem, ChatColor.DARK_GRAY + "Left Click for next Player");
            ItemChanger.addLore(playerFilterItem, ChatColor.DARK_GRAY + "Shift Click for You");
            ItemChanger.addLore(playerFilterItem, ChatColor.DARK_GRAY + "Right Click to clear");

            inventory.setItem(PLAYER_FILTER_INDEX, playerFilterItem);
        }

        return inventory;
    }

    private int getMaxPage() {
        return teleporters.size() / (3 * 9);
    }

    private int calculatePrice(Teleporter tp) {
        double distance = TeleporterHandler.calculateDistance(tp, player);
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
                double modifier = Options.TP_COMMAND_COST;

                if(modifier > 0 && modifier <= 1) {
                    price = (int) Math.ceil((1.0f + modifier) * price);
                } if(modifier > 1) {
                    price = (int) Math.ceil(modifier + price);
                }
            }
        }

        if (Options.FREE_MODEL.meetsCheck(tp, player)) {
            if(price > 0) {
                price = (int) Math.round(price * Options.FREE_MODIFIER);
                if(openedFromCommand) price += Options.ABSOLUTE_TP_COMMAND_COST_FOR_FREE_MODEL;
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
