package lodestone.teleporter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record Teleporter (
		ItemStack displayItem,
		Location location,
		String owner
	) {

	public boolean isOwner(Player player) {
		return owner.equals(player.getUniqueId().toString());
	}

	public boolean sameDimension(Location location) {
		return location.getWorld().getEnvironment() == this.location.getWorld().getEnvironment();
	}

}
