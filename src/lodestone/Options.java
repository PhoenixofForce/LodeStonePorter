package lodestone;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Options {

    public static boolean DROP_ITEM_ON_BREAK = true;
    public static boolean ONLY_ALLOW_OWNER_TO_BREAK = true;

    public static boolean ALLOW_INTERDIMENSIONAL_TRAVEL = true;
    public static int MIN_TELEPORT_DISTANCE = 0;
    public static int MAX_TELEPORT_DISTANCE = -1;

    public static boolean ALLOW_TP_IN_OVERWORLD = true;
    public static boolean ALLOW_TP_IN_NETHER = true;
    public static boolean ALLOW_TP_IN_END = true;
    public static boolean ALLOW_TP_IN_CUSTOM = false;

    public static float TP_COMMAND_COST = 0.25f;
    public static boolean ALLOW_TP_COMMAND = true;

    public static boolean PRIVATE_TP = false;

    public static boolean PAY_FOR_TELEPORT = true;
    public static Material CURRENCY = Material.GOLD_INGOT;
    public static int MIN_PRICE = 1;
    public static int MAX_PRICE = 10;
    public static int PRICE_STARTS_AT_DISTANCE = 200;
    public static int PRICE_ENDS_AT_DISTANCE = 10000;
    public static int INTERDIMENSIONAL_TRAVEL_COST = 20;

    public static FreeModel FREE_MODEL = FreeModel.FIRST_TP_EVER;
    public static double FREE_MODIFIER = 0.0;

    public static void loadFromConfig(FileConfiguration config) {
        DROP_ITEM_ON_BREAK = (boolean) config.get("dropItemOnBreak", DROP_ITEM_ON_BREAK);
        ONLY_ALLOW_OWNER_TO_BREAK = (boolean) config.get("onlyAllowOwnerToBreak", ONLY_ALLOW_OWNER_TO_BREAK);

        ALLOW_INTERDIMENSIONAL_TRAVEL = (boolean) config.get("interdimensionalTravel", ALLOW_INTERDIMENSIONAL_TRAVEL);
        MIN_TELEPORT_DISTANCE = (int) config.get("minTPDistance", MIN_TELEPORT_DISTANCE);
        MAX_TELEPORT_DISTANCE = (int) config.get("maxTPDistance", MAX_TELEPORT_DISTANCE);

        ALLOW_TP_IN_OVERWORLD = (boolean) config.get("tpsOverworld", ALLOW_TP_IN_OVERWORLD);
        ALLOW_TP_IN_NETHER = (boolean) config.get("tpsNether", ALLOW_TP_IN_NETHER);
        ALLOW_TP_IN_END = (boolean) config.get("tpsEnd", ALLOW_TP_IN_END);
        ALLOW_TP_IN_CUSTOM = (boolean) config.get("tpsCustom", ALLOW_TP_IN_CUSTOM);

        TP_COMMAND_COST = (float) ((double) config.get("tpCommandCost", TP_COMMAND_COST));
        ALLOW_TP_COMMAND = (boolean) config.get("tpCommand", ALLOW_TP_COMMAND);

        PRIVATE_TP = (boolean) config.get("privateTps", PRIVATE_TP);

        PAY_FOR_TELEPORT = (boolean) config.get("payForTP", PAY_FOR_TELEPORT);
        CURRENCY = Material.valueOf((String) config.get("currency", CURRENCY));
        MIN_PRICE = (int) config.get("minPrice", MIN_PRICE);
        MAX_PRICE = (int) config.get("maxPrice", MAX_PRICE);
        PRICE_STARTS_AT_DISTANCE = (int) config.get("priceStartDistance", PRICE_STARTS_AT_DISTANCE);
        PRICE_ENDS_AT_DISTANCE = (int) config.get("priceEndDistance", PRICE_ENDS_AT_DISTANCE);
        INTERDIMENSIONAL_TRAVEL_COST = (int) config.get("interdimensionalCost", INTERDIMENSIONAL_TRAVEL_COST);

        FREE_MODEL = FreeModel.valueOf((String) config.get("freeModel", "NONE"));
        FREE_MODIFIER = (double) config.get("freeModifier", FREE_MODIFIER);
    }

    public static void saveToConfig(FileConfiguration config) {
        config.set("dropItemOnBreak", DROP_ITEM_ON_BREAK);
        config.set("onlyAllowOwnerToBreak", ONLY_ALLOW_OWNER_TO_BREAK);

        config.set("interdimensionalTravel", ALLOW_INTERDIMENSIONAL_TRAVEL);
        config.set("minTPDistance", MIN_TELEPORT_DISTANCE);
        config.set("maxTPDistance", MAX_TELEPORT_DISTANCE);

        config.set("tpsOverworld", ALLOW_TP_IN_OVERWORLD);
        config.set("tpsNether", ALLOW_TP_IN_NETHER);
        config.set("tpsEnd", ALLOW_TP_IN_END);
        config.set("tpsCustom", ALLOW_TP_IN_CUSTOM);

        config.set("tpCommandCost", TP_COMMAND_COST);
        config.set("tpsCustom", ALLOW_TP_COMMAND);

        config.set("privateTps", PRIVATE_TP);

        config.set("payForTP", PAY_FOR_TELEPORT);
        config.set("currency", CURRENCY.toString());
        config.set("minPrice", MIN_PRICE);
        config.set("maxPrice", MAX_PRICE);

        config.set("priceStartDistance", PRICE_STARTS_AT_DISTANCE);
        config.set("priceEndDistance", PRICE_ENDS_AT_DISTANCE);
        config.set("interdimensionalCost", INTERDIMENSIONAL_TRAVEL_COST);

        config.set("freeModel", FREE_MODEL.toString());
        config.set("freeModifier", FREE_MODIFIER);
    }

}
