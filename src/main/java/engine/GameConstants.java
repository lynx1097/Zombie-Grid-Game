package engine;

/** Centralized game configuration values. Change here to affect the whole game. */
public final class GameConstants {

    private GameConstants() {}

    public static final int GRID_SIZE        = 15;
    public static final int CELL_SIZE_PX     = 40;
    public static final int CHAR_IMG_PX      = 36;
    public static final int PORTRAIT_IMG_PX  = 144;
    public static final int SIDEBAR_WIDTH    = 150;
    public static final int HERO_ROSTER_SIZE = 8;

    public static final int ZOMBIES_INITIAL  = 10;
    public static final int VACCINES_COUNT   = 5;
    public static final int SUPPLIES_COUNT   = 5;
    public static final int TRAPS_COUNT      = 5;
    public static final int HEROES_TO_WIN    = 5;
}
