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
	   - [ ] load booleans from config
	   - [ ] add min teleport distance, max teleport distance
	   - [ ] add min and max cost, currency
	   - [ ] success effect
	   - [ ] config if cross dimension teleport is allowed, price always max
	   - [ ] allow teleporter in nether, overworld, end, custom
	   - [ ] permissions for creating teleporter, breaking all teleporter
	   - [ ] teleport selector with items for scrolling, display page in title, inventory larger?
	   - [ ] config only show teleporter in same dimension
	   - [ ] config only show teleporter created by player
	   - [ ] chat messages
	   - [ ] particles for active teleporter
	 */

	public static boolean DROP_ITEM_ON_BREAK = true;
	public static boolean ONLY_ALLOW_OWNER_TO_BREAK = true;

	public static final TeleporterHandler teleportHandler = new TeleporterHandler();
	
	@Override
	public void onEnable(){
		List<String> authors = getDescription().getAuthors();
		ChatUtil.PLUGIN_NAME = getDescription().getName();

		Bukkit.getLogger().log(Level.INFO,  "___" + ChatUtil.PLUGIN_NAME + " v" + getDescription().getVersion() + "_".repeat(10));
		Bukkit.getLogger().log(Level.INFO, "  Created by PhoenixofForce");
		if(authors.size() > 1) Bukkit.getLogger().log(Level.INFO, "  Developed by " + authors.stream().reduce("", (s1, s2) -> s1 + ", " + s2).substring(2));

		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new EventListener(teleportHandler), this);
		loadConfigFile();
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		saveConfigFile();
	}
	
	public void loadConfigFile() {
    	FileConfiguration config = getConfig();
		teleportHandler.load(config);
    }
	
	public void saveConfigFile() {
		FileConfiguration config = getConfig();
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
		if((!player.isSneaking() || playerHoldItem.getAmount() <= 0) && teleporterExists) {
			openTeleportSelector(player, 0);
			return;
		}
		if(!player.isSneaking() || playerHoldItem.getAmount() <= 0 || teleporterExists) return;

		//Exit when holding compass
		boolean playerHoldingCompass = playerHoldItem.getType() == Material.COMPASS;
		if(playerHoldingCompass) return;

		World.Environment dimension = clickedLocation.getWorld().getEnvironment();
		String dimensionString = Strings.capitalize(dimension.name().replace("_", " "));

		ChatColor dimensionColor = switch (dimension) {
			case NORMAL -> ChatColor.GREEN;
			case NETHER -> ChatColor.DARK_RED;
			case THE_END -> ChatColor.DARK_AQUA;
			default -> ChatColor.DARK_PURPLE;
		};

		dimensionString = dimensionColor.toString() + ChatColor.BOLD + dimensionString + ChatColor.RESET;

		//Creating teleporter
		ItemStack displayItem = playerHoldItem.clone();
		displayItem.setAmount(1);

		ItemChanger.changeName(displayItem, name -> ChatColor.AQUA + (ChatColor.stripColor(name)));
		ItemChanger.addEnchantmentGlow(displayItem);
		ItemChanger.setLore(displayItem, List.of(
				dimensionString + ChatColor.GRAY + "@(" + clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ() + ")",
				ChatColor.GRAY + "Point created by " + ChatColor.ITALIC + player.getDisplayName()
		));

		playerHoldItem.setAmount(playerHoldItem.getAmount() - 1);
		if(playerHoldItem.getAmount() == 0) player.getInventory().setItemInMainHand(null);

		teleportHandler.addTeleporter(new Teleporter(displayItem, event.getClickedBlock().getLocation(), player.getUniqueId().toString()));
		event.setCancelled(true);
		//TODO: play reward effect
	}

	public static void openTeleportSelector(Player player, int offset) {
		player.openInventory(new TeleporterSelectGUI(player, offset).getInventory());
	}
}