package lodestone.teleporter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record Teleporter (
		ItemStack displayItem,
		Location location,
		String owner
	) {

	public boolean isOwner(Player p) {
		return owner.equals(p.getUniqueId().toString());
	}

	public boolean sameDimension(Location l) {
		return l.getWorld().getEnvironment() == location.getWorld().getEnvironment();
	}

}
