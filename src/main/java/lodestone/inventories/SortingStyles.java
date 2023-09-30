package lodestone.inventories;

import lodestone.ItemChanger;
import lodestone.teleporter.Teleporter;
import lodestone.teleporter.TeleporterHandler;
import org.bukkit.entity.Player;

public enum SortingStyles {

    CREATION_DATE((p, tp1, tp2) -> 0),
    DISTANCE((p, tp1, tp2) -> Double.compare(TeleporterHandler.calculateDistance(tp1, p), TeleporterHandler.calculateDistance(tp2, p))),
    PLAYER_NAME((p, tp1, tp2) -> tp1.owner().compareTo(tp2.owner())),
    TELEPORTER_NAME((p, tp1, tp2) -> ItemChanger.getName(tp1.displayItem()).compareTo(ItemChanger.getName(tp2.displayItem())));

    private SortingFunctionFunction sorter;
    SortingStyles(SortingFunctionFunction sorter) {
        this.sorter = sorter;
    }

    public int sort(Player player, Teleporter tp1, Teleporter tp2) {
        return sorter.sort(player, tp1, tp2);
    }

    public SortingStyles next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public interface SortingFunctionFunction {
        int sort(Player player, Teleporter tp1, Teleporter tp2);
    }

}
