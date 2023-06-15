package lodestone.inventories;

public enum SortingStyles {

    CREATION_DATE,
    DISTANCE,
    PLAYER_NAME;

    public SortingStyles next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

}
