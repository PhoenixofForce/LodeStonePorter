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
		if(location.getWorld() == null || this.location.getWorld() == null) return location.getWorld() == this.location.getWorld();
		return location.getWorld().getEnvironment() == this.location.getWorld().getEnvironment();
	}

}
