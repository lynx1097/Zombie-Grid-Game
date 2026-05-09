package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import model.characters.Explorer;
import model.characters.Fighter;
import model.characters.Hero;
import model.characters.Medic;
import model.characters.Zombie;
import model.collectibles.Supply;
import model.collectibles.Vaccine;
import model.world.Cell;
import model.world.CharacterCell;
import model.world.CollectibleCell;
import model.world.TrapCell;
import exceptions.InvalidTargetException;
import exceptions.NotEnoughActionsException;

/**
 * Central game engine. Holds all mutable game state as static fields and
 * exposes methods for turn management, entity spawning, win/lose detection,
 * and CSV hero loading.
 *
 * <p>{@code onMapUpdate} is a callback set by the View layer at startup so
 * that the engine can trigger a UI refresh without importing the View package.</p>
 */
public class Game {

    /** Callback invoked whenever the game map must be redrawn. Set by the GUI at startup. */
    public static Runnable onMapUpdate = () -> {};

    /** Heroes available to recruit (loaded from CSV, shrinks as heroes are selected or cured in). */
    public static ArrayList<Hero> availableHeroes = new ArrayList<>();

    /** Heroes currently active on the map. */
    public static ArrayList<Hero> heroes = new ArrayList<>();

    /** Zombies currently on the map. A new zombie spawns at the end of every turn. */
    public static ArrayList<Zombie> zombies = new ArrayList<>();

    /** The 15×15 game grid. Each cell is a {@link CharacterCell}, {@link CollectibleCell}, or {@link TrapCell}. */
    public static Cell[][] map = new Cell[GameConstants.GRID_SIZE][GameConstants.GRID_SIZE];

    // ── CSV loading ───────────────────────────────────────────────────────────

    /**
     * Parses a CSV hero roster file and populates {@link #availableHeroes}.
     *
     * <p>Each row has the format: {@code Name,TYPE,maxHp,maxActions,attackDamage}
     * where TYPE is {@code EXP}, {@code FIGH}, or {@code MED}.</p>
     *
     * @param filePath path to the CSV file (relative to the working directory)
     * @throws IOException if the file cannot be read
     */
    public static void loadHeroes(String filePath) throws IOException {
        availableHeroes = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine();
        while (line != null) {
            String[] sp = line.split(",");
            Hero h;
            if (sp[1].equals("EXP")) {
                h = new Explorer(sp[0], Integer.parseInt(sp[2]), Integer.parseInt(sp[4]), Integer.parseInt(sp[3]));
            } else if (sp[1].equals("FIGH")) {
                h = new Fighter(sp[0], Integer.parseInt(sp[2]), Integer.parseInt(sp[4]), Integer.parseInt(sp[3]));
            } else {
                h = new Medic(sp[0], Integer.parseInt(sp[2]), Integer.parseInt(sp[4]), Integer.parseInt(sp[3]));
            }
            availableHeroes.add(h);
            line = br.readLine();
        }
        br.close();
    }

    // ── Turn management ───────────────────────────────────────────────────────

    /**
     * Advances the game by one turn:
     * <ol>
     *   <li>Each zombie attacks an adjacent hero (if any).</li>
     *   <li>A new zombie spawns at a random empty cell.</li>
     *   <li>All cells are hidden (fog-of-war reset).</li>
     *   <li>Each active hero's action points are restored and visibility is recalculated.</li>
     *   <li>The map is redrawn via {@link #onMapUpdate}.</li>
     * </ol>
     *
     * @throws NotEnoughActionsException propagated from zombie attack logic
     * @throws InvalidTargetException    propagated from zombie attack logic
     */
    public static void endTurn() throws NotEnoughActionsException, InvalidTargetException {
        for (Zombie zombie : zombies) {
            zombie.attack();
            zombie.setTarget(null);
        }
        spawnNewZombie();
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++)
                map[i][j].setVisible(false);
        for (Hero hero : heroes) {
            hero.setActionsAvailable(hero.getMaxActions());
            hero.setTarget(null);
            hero.setSpecialAction(false);
            adjustVisibility(hero);
        }
        onMapUpdate.run();
    }

    // ── Visibility / fog-of-war ───────────────────────────────────────────────

    /**
     * Reveals the 3×3 Moore neighbourhood (Chebyshev distance ≤ 1) around {@code h}.
     *
     * @param h the hero whose surroundings are revealed
     */
    public static void adjustVisibility(Hero h) {
        Point p = h.getLocation();
        for (int i = -1; i <= 1; i++) {
            int cx = p.x + i;
            if (cx >= 0 && cx < map.length) {
                for (int j = -1; j <= 1; j++) {
                    int cy = p.y + j;
                    if (cy >= 0 && cy < map[cx].length) {
                        map[cx][cy].setVisible(true);
                    }
                }
            }
        }
    }

    // ── Spawning ──────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link Zombie} and places it at a random empty cell.
     */
    public static void spawnNewZombie() {
        Zombie z = new Zombie();
        zombies.add(z);
        int x, y;
        do {
            x = (int) (Math.random() * map.length);
            y = (int) (Math.random() * map[x].length);
        } while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
                || (map[x][y] instanceof CollectibleCell) || (map[x][y] instanceof TrapCell));
        z.setLocation(new Point(x, y));
        map[x][y] = new CharacterCell(z);
    }

    // ── Win / lose conditions ─────────────────────────────────────────────────

    /**
     * Returns {@code true} when the player wins: at least {@link GameConstants#HEROES_TO_WIN}
     * heroes are alive and no vaccines remain on the map or in hero inventories.
     *
     * @return {@code true} if the win condition is met
     */
    public static boolean checkWin() {
        int remaining = 0;
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++)
                if (map[i][j] instanceof CollectibleCell
                        && ((CollectibleCell) map[i][j]).getCollectible() instanceof Vaccine)
                    remaining++;
        for (Hero hero : heroes)
            remaining += hero.getVaccineInventory().size();
        return heroes.size() >= GameConstants.HEROES_TO_WIN && remaining == 0;
    }

    /**
     * Returns {@code true} when the game is lost: all heroes are dead, or all vaccines
     * have been consumed but the hero count cannot meet the win condition.
     *
     * @return {@code true} if the lose condition is met
     */
    public static boolean checkGameOver() {
        if (heroes.size() > 0) {
            for (int i = 0; i < map.length; i++)
                for (int j = 0; j < map[i].length; j++)
                    if (map[i][j] instanceof CollectibleCell
                            && ((CollectibleCell) map[i][j]).getCollectible() instanceof Vaccine)
                        return false;
            for (Hero hero : heroes)
                if (hero.getVaccineInventory().size() > 0) return false;
        }
        return true;
    }

    // ── Game initialisation ───────────────────────────────────────────────────

    /**
     * Initialises a new game with the selected starting hero.
     * Clears and resets the map, places the hero at (0,0), then spawns
     * zombies, vaccines, supplies, and traps.
     *
     * @param h the hero chosen by the player on the selection screen
     */
    public static void startGame(Hero h) {
        heroes.clear();
        zombies.clear();
        heroes.add(h);
        availableHeroes.remove(h);
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++)
                map[i][j] = new CharacterCell(null);

        ((CharacterCell) map[0][0]).setCharacter(h);
        h.setLocation(new Point(0, 0));

        spawnCollectibles();
        for (int i = 0; i < GameConstants.ZOMBIES_INITIAL; i++) spawnNewZombie();
        spawnTraps();
        adjustVisibility(h);
    }

    /** Places vaccines and supplies at random empty cells. */
    public static void spawnCollectibles() {
        for (int i = 0; i < GameConstants.VACCINES_COUNT; i++) {
            int x, y;
            do {
                x = (int) (Math.random() * map.length);
                y = (int) (Math.random() * map[x].length);
            } while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
                    || map[x][y] instanceof CollectibleCell || map[x][y] instanceof TrapCell);
            map[x][y] = new CollectibleCell(new Vaccine());
        }
        for (int i = 0; i < GameConstants.SUPPLIES_COUNT; i++) {
            int x, y;
            do {
                x = (int) (Math.random() * map.length);
                y = (int) (Math.random() * map[x].length);
            } while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
                    || map[x][y] instanceof CollectibleCell || map[x][y] instanceof TrapCell);
            map[x][y] = new CollectibleCell(new Supply());
        }
    }

    /** Places trap cells at random empty cells. */
    public static void spawnTraps() {
        for (int i = 0; i < GameConstants.TRAPS_COUNT; i++) {
            int x, y;
            do {
                x = (int) (Math.random() * map.length);
                y = (int) (Math.random() * map[x].length);
            } while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
                    || map[x][y] instanceof CollectibleCell || map[x][y] instanceof TrapCell);
            map[x][y] = new TrapCell();
        }
    }
}
