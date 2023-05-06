package lodestone.teleporter;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public record Teleporter (
		ItemStack displayItem,
		Location location,
		String owner
	) {

}
