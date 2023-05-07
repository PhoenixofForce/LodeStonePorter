package lodestone;

import java.util.List;
import java.util.logging.Level;

import lodestone.inventories.GUI;
import lodestone.inventories.TeleporterSelectGUI;
import lodestone.teleporter.Teleporter;
import lodestone.teleporter.TeleporterHandler;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	/* TODO:
	   - [ ] success effect
	   - [ ] teleport selector with items for scrolling, display page in title, inventory larger?
	   - [ ] particles for active teleporter
	   - [x] chat messages
	   - [x] load booleans from config
	   - [x] add min teleport distance, max teleport distance
	   - [x] add min and max cost, currency
	   - [x] config if cross dimension teleport is allowed, price always max
	   - [x] allow teleporter in nether, overworld, end, custom
	   - [x] permissions for creating teleporter, breaking all teleporter
	   - [x] config only show teleporter created by player
	   =>[ ] only allow to right click teleporter created by player?
	   - [x] check tps on load
	   - [x] allow interdimensional travel
	 */

	public static final TeleporterHandler teleportHandler = new TeleporterHandler();
	
	@Override
	public void onEnable(){
		List<String> authors = getDescription().getAuthors();
		ChatUtil.PLUGIN_NAME = getDescription().getName();

		Bukkit.getLogger().log(Level.INFO,  "___" + ChatUtil.PLUGIN_NAME + " v" + getDescription().getVersion() + "_".repeat(10));
		Bukkit.getLogger().log(Level.INFO, "  Created by PhoenixofForce");
		if(authors.size() > 1) Bukkit.getLogger().log(Level.INFO, "  Developed by " + authors.stream().reduce("", (s1, s2) -> s1 + ", " + s2).substring(2) + "\r\n");

		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new EventListener(teleportHandler), this);
		loadConfigFile();

		Bukkit.getLogger().log(Level.INFO,  "Found " + teleportHandler.getAmount() + " teleporters, checking for corruption...");

		//Check if teleporter still exist
		for(int i = teleportHandler.getAmount() - 1; i >= 0; i--) {
			Teleporter tp = teleportHandler.getByIndex(i);

			if(tp.location().getBlock().getType() != Material.LODESTONE) {
				teleportHandler.deleteTeleporter(tp.location());
				Bukkit.getLogger().log(Level.INFO,
						" - Teleporter " + ItemChanger.getName(tp.displayItem()) + " (" + tp.location().getBlockX() + ", " + tp.location().getBlockY() + ", " + tp.location().getBlockZ() + ") got corrupted and removed"
				);
			}
		}

		Bukkit.getLogger().log(Level.INFO,  "_".repeat(10) + ChatUtil.PLUGIN_NAME + " v" + getDescription().getVersion() + "_".repeat(3));
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		saveConfigFile();
	}
	
	public void loadConfigFile() {
    	FileConfiguration config = getConfig();

		Options.loadFromConfig(config);
		teleportHandler.load(config);
    }
	
	public void saveConfigFile() {
		FileConfiguration config = getConfig();

		Options.saveToConfig(config);
		teleportHandler.save(config);
	    super.saveConfig();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getClickedInventory() != null &&
			e.getClickedInventory().getHolder() != null &&
			e.getClickedInventory().getHolder() instanceof GUI gui) {

			gui.onInventoryClick(e);
		}
	}

	@EventHandler
	public void onPlayerRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
	    Action action = event.getAction();

		ItemStack playerHoldItem = player.getInventory().getItemInMainHand();

		//Exit when clicking something else than a lodestone
	    boolean clickedLodestone = action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.LODESTONE;
	    if (!clickedLodestone) return;
		Location clickedLocation = event.getClickedBlock().getLocation();

		//Open teleport menu
		boolean teleporterExists = teleportHandler.contains(clickedLocation);
		if(teleporterExists) {
			//Exit when player is not allowed to use teleporter
			if(!player.hasPermission("useTP")) {
				ChatUtil.sendErrorMessage(player, "You do not have enough permissions to use this teleporter.");
				return;
			}

			openTeleportSelector(player, 0);
			return;
		}
		if(!player.isSneaking() || playerHoldItem.getAmount() <= 0 || teleporterExists) return;

		//Exit when no permission for creating tp
		if(!(player.hasPermission("createTP") || player.isOp())) {
			ChatUtil.sendErrorMessage(player, "You do not have enough permissions to create a teleporter.");
			return;
		}

		//Exit when holding compass
		boolean playerHoldingCompass = playerHoldItem.getType() == Material.COMPASS;
		if(playerHoldingCompass) return;

		if(!isPlacingTPInThisDimensionAllowed(clickedLocation)) {
			ChatUtil.sendErrorMessage(player, "Creating teleporter in this dimension is not allowed.");
		}

		//Creating teleporter
		ItemStack displayItem = createTeleporterIcon(playerHoldItem.clone(), clickedLocation, player);

		playerHoldItem.setAmount(playerHoldItem.getAmount() - 1);
		if(playerHoldItem.getAmount() == 0) player.getInventory().setItemInMainHand(null);

		ChatUtil.sendMessage(player, "Successfully created teleporter");
		teleportHandler.addTeleporter(new Teleporter(displayItem, event.getClickedBlock().getLocation(), player.getUniqueId().toString()));
		event.setCancelled(true);
		//TODO: play reward effect
	}

	private ItemStack createTeleporterIcon(ItemStack item, Location clickedLocation, Player player) {
		String dimensionString = Strings.capitalize(clickedLocation.getWorld().getEnvironment().name());
		dimensionString = ChatUtil.getDimensionColor(clickedLocation).toString() + ChatColor.BOLD + dimensionString + ChatColor.RESET;

		ItemStack displayItem = item.clone();
		displayItem.setAmount(1);

		ItemChanger.changeName(displayItem, name -> ChatColor.AQUA + (ChatColor.stripColor(name)));
		ItemChanger.addEnchantmentGlow(displayItem);
		ItemChanger.setLore(displayItem, List.of(
				dimensionString + ChatColor.GRAY + "@(" + clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ() + ")",
				ChatColor.GRAY + "Point created by " + ChatColor.ITALIC + player.getDisplayName()
		));

		return displayItem;
	}

	private boolean isPlacingTPInThisDimensionAllowed(Location l) {
		World.Environment dimension = l.getWorld().getEnvironment();
		if(dimension == World.Environment.NORMAL && !Options.ALLOW_TP_IN_OVERWORLD) return false;
		if(dimension == World.Environment.NETHER && !Options.ALLOW_TP_IN_NETHER) return false;
		if(dimension == World.Environment.THE_END && !Options.ALLOW_TP_IN_END) return false;
		if(dimension == World.Environment.CUSTOM && !Options.ALLOW_TP_IN_CUSTOM) return false;
		return true;
	}

	public static void openTeleportSelector(Player player, int offset) {
		player.openInventory(new TeleporterSelectGUI(player, offset).getInventory());
	}
}