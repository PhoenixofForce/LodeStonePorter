package lodestone.teleporter;

import lodestone.ChatUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeleporterHandler {

    private final List<Teleporter> teleporter = new ArrayList<>();

    public TeleporterHandler() { }

    public List<Teleporter> getTeleporter() {
        return teleporter;
    }

    public void addTeleporter(Teleporter teleporter) {
        this.teleporter.add(teleporter);
    }

    public Optional<Teleporter> deleteTeleporter(Location l) {
        for(int i = teleporter.size() - 1; i >= 0; i--) {
            if(teleporter.get(i).location().equals(l))
                return Optional.of(teleporter.remove(i));
        }

        return Optional.empty();
    }

    public Teleporter getByIndex(int i) {
        return teleporter.get(i);
    }

    public Optional<Teleporter> getByLocation(Location loc) {
        return teleporter.stream().filter(tp -> tp.location().equals(loc)).findFirst();
    }

    public boolean contains(Location loc) {
        return teleporter.stream().anyMatch(l -> l.location().equals(loc));
    }

    public int getAmount() {
        return teleporter.size();
    }

    public void save(FileConfiguration config) {
        config.set("tpCount", getAmount());

        for(int i = 0; i < getAmount(); i++) {
            config.set("location_" + i, getByIndex(i).location());
            config.set("item_" + i, getByIndex(i).displayItem());
            config.set("owner_" + i, getByIndex(i).owner());
        }
    }

    public void load(FileConfiguration config) {
        int count = (int) config.get("tpCount", 0);

        for(int i = 0; i < count; i++) {
            Location l = (Location) config.get("location_" + i);
            ItemStack s = (ItemStack) config.get("item_" + i);
            String o = (String) config.get("owner_" + i);

            addTeleporter(new Teleporter(s, l, o));
        }
    }
}
