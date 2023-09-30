package lodestone;

import lodestone.teleporter.Teleporter;
import org.bukkit.entity.Player;

import java.util.Optional;


public enum FreeModel {

    FIRST_TP_EVER((tp, player) -> tp.location().equals(Main.teleportHandler.getByIndex(0).location())),
    FIRST_TP_OF_PLAYER((tp, player) -> {
        Optional<Teleporter> firstTP = Main.teleportHandler.getTeleporter().stream()
                .filter(e -> e.owner().equals(player.getUniqueId().toString()))
                .findFirst();

        return firstTP.isPresent() && tp.location().equals(firstTP.get().location());
    }),
    NONE((tp, player) -> false);

    private RequirementsCheck requirementsCheck;
    FreeModel(RequirementsCheck requirementsCheck) {
        this.requirementsCheck = requirementsCheck;
    }

    public boolean meetsCheck(Teleporter tp, Player player) {
        return requirementsCheck.meets(tp, player);
    }


    public interface RequirementsCheck {
        boolean meets(Teleporter teleporter, Player player);
    }
}
