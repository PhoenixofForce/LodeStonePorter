package lodestone.teleporter;

import lodestone.ChatUtil;
import lodestone.Options;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeleporterHandler {

    private final List<Teleporter> teleporter = new ArrayList<>();

    public TeleporterHandler() { }

    public List<Teleporter> getTeleporter() {
        return teleporter;
    }

    public void addTeleporter(Teleporter teleporter) {
        this.teleporter.add(teleporter);
        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(teleporter.owner().trim()));
        if(owner != null && !cache.contains(owner)) {
            cache.add(owner);
        }
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

    public void save(FileConfiguration configIgnored) {
        File dataFolder = new File("plugins/" + ChatUtil.PLUGIN_NAME + "/");
        File tpFile = new File(dataFolder, "tps.yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("tpCount", getAmount());

        for(int i = 0; i < getAmount(); i++) {
            config.set("location_" + i, getByIndex(i).location());
            config.set("item_" + i, getByIndex(i).displayItem());
            config.set("owner_" + i, getByIndex(i).owner());
        }
        try {
            config.save(tpFile);
        } catch (IOException e) {
            System.out.println("Couldnt save teleport data");
        }
    }

    public void load(FileConfiguration pluginConfig) {
        File dataFolder = new File("plugins/" + ChatUtil.PLUGIN_NAME + "/");
        File tpFile = new File(dataFolder, "tps.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(tpFile);

        if(pluginConfig.contains("tpCount")) {
            int count = (int) pluginConfig.get("tpCount", 0);
            pluginConfig.set("tpCount", null);

            for(int i = 0; i < count; i++) {
                Location location = (Location) pluginConfig.get("location_" + i);
                ItemStack itemDisplay = (ItemStack) pluginConfig.get("item_" + i);
                String owner = (String) pluginConfig.get("owner_" + i);

                pluginConfig.set("location_" + i, null);
                pluginConfig.set("item_" + i, null);
                pluginConfig.set("owner_" + i, null);

                addTeleporter(new Teleporter(itemDisplay, location, owner));
            }
        }

        if(config.contains("tpCount")) {
            int count = (int) config.get("tpCount", 0);

            for(int i = 0; i < count; i++) {
                Location location = (Location) config.get("location_" + i);
                ItemStack itemDisplay = (ItemStack) config.get("item_" + i);
                String owner = (String) config.get("owner_" + i);

                addTeleporter(new Teleporter(itemDisplay, location, owner));
            }
        }
    }

    private final List<OfflinePlayer> cache = new ArrayList<>();
    public List<OfflinePlayer> getCreatorList() {
        return cache;
    }

    public static double calculateDistance(Teleporter tp, Player player) {
        return tp.sameDimension(player.getLocation())?
                tp.location().distance(player.getLocation()):
                (Options.PRICE_ENDS_AT_DISTANCE);
    }
}
