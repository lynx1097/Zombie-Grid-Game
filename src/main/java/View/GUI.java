package View;

import engine.Game;
import engine.GameConstants;
import exceptions.InvalidTargetException;
import exceptions.MovementException;
import exceptions.NoAvailableResourcesException;
import exceptions.NotEnoughActionsException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.characters.*;
import model.collectibles.Supply;
import model.world.CharacterCell;
import model.world.CollectibleCell;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX Application class that owns the two game scenes:
 * the hero-selection screen and the main 15×15 grid gameplay screen.
 *
 * <p>All UI state is kept static so that the model layer can trigger
 * {@link #updateMap()} via the {@code Game.onMapUpdate} callback without
 * holding a direct reference to this class.</p>
 */
public class GUI extends Application {

    // ── Image cache ──────────────────────────────────────────────────────────
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    /**
     * Loads an image from the classpath, returning a cached copy on subsequent calls.
     *
     * @param path classpath-relative path, e.g. {@code "/icons/Characters/Marcus Rowe.png"}
     * @return the loaded {@link Image}
     */
    private static Image loadImage(String path) {
        return IMAGE_CACHE.computeIfAbsent(path,
                p -> new Image(GUI.class.getResourceAsStream(p)));
    }

    // ── UI components ────────────────────────────────────────────────────────
    private static final Button[][] map =
            new Button[GameConstants.GRID_SIZE][GameConstants.GRID_SIZE];
    private static final GridPane   mapGrid        = new GridPane();
    private static final BorderPane mainGameScreen = new BorderPane(mapGrid);
    private static final HBox       heroSelection  = new HBox();

    private static final Scene mainGameScene = new Scene(mainGameScreen);
    private static final Scene heroSelectionScene = new Scene(heroSelection,
            (double) GameConstants.HERO_ROSTER_SIZE * GameConstants.PORTRAIT_IMG_PX,
            GameConstants.PORTRAIT_IMG_PX);

    private static final Object[][] heroIcons =
            new Object[GameConstants.HERO_ROSTER_SIZE][2];

    private static final Button useSpecial = new Button("Use Special Action");
    private static final Button cure       = new Button("Cure");
    private static final Button attack     = new Button("Attack");
    private static final VBox   box1       = new VBox();

    private static Button endTurnButton = null;
    private static Label  info          = null;
    private static Stage  primaryStage;

    // ── Selection state ──────────────────────────────────────────────────────
    private static Hero   selectedHero       = null;
    private static Hero   selectedHeroTarget = null;
    private static Zombie selectedZombie     = null;
    private static int    clicks             = 0;

    // ── Application lifecycle ────────────────────────────────────────────────

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Game.onMapUpdate = GUI::updateMap;   // wire model → view callback
        initializeHeroSelection();
        primaryStage.setScene(heroSelectionScene);
        primaryStage.show();
    }

    // ── Hero selection screen ────────────────────────────────────────────────

    /** Loads hero portrait icons from the classpath into the {@code heroIcons} array. */
    public static void loadHeroesIcons() {
        for (int i = 0; i < GameConstants.HERO_ROSTER_SIZE; i++) {
            String name = Game.availableHeroes.get(i).getName();
            heroIcons[i][0] = name;
            ImageView iv = new ImageView(loadImage("/icons/Characters/" + name + ".png"));
            iv.setFitHeight(GameConstants.CHAR_IMG_PX);
            iv.setFitWidth(GameConstants.CHAR_IMG_PX);
            heroIcons[i][1] = iv;
        }
    }

    /**
     * Builds and displays the hero-selection scene.
     * Loads {@code Heroes.csv}, creates one button per hero, and switches to the
     * selection scene.
     */
    public static void initializeHeroSelection() {
        try {
            Game.loadHeroes("Heroes.csv");
        } catch (IOException e) {
            throw new RuntimeException("Could not load Heroes.csv", e);
        }
        heroSelection.getChildren().clear();
        loadHeroesIcons();
        primaryStage.setTitle("Zombie Survival Grid — Select your Hero");

        for (int i = 0; i < GameConstants.HERO_ROSTER_SIZE; i++) {
            Button button = new Button();
            button.setMaxSize(GameConstants.PORTRAIT_IMG_PX, GameConstants.PORTRAIT_IMG_PX);
            button.setMinSize(GameConstants.PORTRAIT_IMG_PX, GameConstants.PORTRAIT_IMG_PX);
            ((ImageView) heroIcons[i][1]).setFitWidth(GameConstants.PORTRAIT_IMG_PX);
            ((ImageView) heroIcons[i][1]).setFitHeight(GameConstants.PORTRAIT_IMG_PX);

            Hero m = Game.availableHeroes.get(i);
            String role = m instanceof Fighter ? "Fighter"
                    : m instanceof Explorer ? "Explorer" : "Medic";
            Tooltip tip = new Tooltip(m.getName()
                    + "\nMax HP : " + m.getMaxHp()
                    + "\nRole : " + role
                    + "\nAction Points : " + m.getMaxActions());
            tip.setShowDelay(Duration.millis(100));

            button.setStyle("-fx-border-radius: 0;-fx-border-width: 0; -fx-background-color: orange");
            button.setTooltip(tip);
            button.setGraphic((Node) heroIcons[i][1]);
            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    selectedHero = m;
                    Game.startGame(selectedHero);
                    initializeMainGameScreen();
                }
            });
            heroSelection.getChildren().add(button);
        }

        primaryStage.setScene(heroSelectionScene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // ── Main game screen ─────────────────────────────────────────────────────

    /**
     * Builds the main gameplay scene (grid + sidebar + status bar) and switches
     * the primary stage to it.
     */
    public static void initializeMainGameScreen() {
        box1.setMinWidth(GameConstants.SIDEBAR_WIDTH);
        primaryStage.setTitle("Zombie Survival Grid");

        endTurnButton = new Button("End Turn");
        endTurnButton.setMinSize(GameConstants.SIDEBAR_WIDTH, 50);
        mainGameScreen.setRight(box1);

        info = new Label();
        info.setMinSize(
                GameConstants.GRID_SIZE * GameConstants.CELL_SIZE_PX + GameConstants.SIDEBAR_WIDTH,
                70);
        info.setStyle("-fx-background-color: #FF6D60;");
        BorderPane pane2 = new BorderPane(info);
        info.setAlignment(Pos.CENTER);
        mainGameScreen.setBottom(pane2);
        mainGameScreen.setStyle("-fx-background-color: #ADE4DB;");

        initializeMap();
        updateMap();

        primaryStage.setResizable(false);
        primaryStage.setScene(mainGameScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Creates the 15×15 button grid and adds it to {@code mapGrid}.
     * Buttons are rotated 90° and the grid is rotated −90° to reconcile the
     * coordinate system with the game's (x, y) → (row, col) mapping.
     */
    public static void initializeMap() {
        int px = GameConstants.GRID_SIZE * GameConstants.CELL_SIZE_PX;
        mapGrid.setMinSize(px, px);
        mapGrid.setMaxSize(px, px);

        for (int i = 0; i < GameConstants.GRID_SIZE; i++) {
            for (int j = 0; j < GameConstants.GRID_SIZE; j++) {
                Button btn = new Button();
                btn.setMinSize(GameConstants.CELL_SIZE_PX, GameConstants.CELL_SIZE_PX);
                btn.setMaxSize(GameConstants.CELL_SIZE_PX, GameConstants.CELL_SIZE_PX);
                btn.setStyle("-fx-border-radius: 0;-fx-border-color: grey;-fx-background-color: white");
                btn.setRotate(90);
                mapGrid.add(btn, i, j);
                map[i][j] = btn;
            }
        }
        mapGrid.setAlignment(Pos.CENTER);
        mapGrid.setRotate(-90);
    }

    // ── Per-turn rendering ───────────────────────────────────────────────────

    /**
     * Redraws every cell on the grid to reflect the current {@link Game} state,
     * then re-attaches all click handlers. Also checks win/lose conditions and
     * shows the game-end dialog if either is true.
     *
     * <p>This method is called after every player action and at the end of each
     * turn. It is registered as {@code Game.onMapUpdate} at startup so that the
     * model layer can trigger it without importing this class.</p>
     */
    public static void updateMap() {
        // Reset all cells
        for (int i = 0; i < GameConstants.GRID_SIZE; i++) {
            for (int j = 0; j < GameConstants.GRID_SIZE; j++) {
                map[i][j].setGraphic(null);
                map[i][j].setStyle("-fx-background-color: #F3E99F;");
                map[i][j].setOnMouseClicked(null);
                map[i][j].setOnMouseEntered(null);
            }
        }

        // Render visible cells
        for (int i = 0; i < GameConstants.GRID_SIZE; i++) {
            for (int j = 0; j < GameConstants.GRID_SIZE; j++) {
                if (Game.map[i][j].isVisible()) {
                    if (Game.map[i][j] instanceof CharacterCell cc
                            && cc.getCharacter() != null) {
                        String imgPath = cc.getCharacter() instanceof Hero
                                ? "/icons/Characters/" + cc.getCharacter().getName() + ".png"
                                : "/icons/Characters/Zombie.png";
                        ImageView iv = new ImageView(loadImage(imgPath));
                        iv.setFitHeight(GameConstants.CHAR_IMG_PX);
                        iv.setFitWidth(GameConstants.CHAR_IMG_PX);
                        map[i][j].setGraphic(iv);
                    }
                    if (Game.map[i][j] instanceof CollectibleCell cc) {
                        String imgPath = cc.getCollectible() instanceof Supply
                                ? "/icons/Supply.png" : "/icons/Vaccine.png";
                        ImageView iv = new ImageView(loadImage(imgPath));
                        iv.setFitHeight(GameConstants.CHAR_IMG_PX);
                        iv.setFitWidth(GameConstants.CHAR_IMG_PX);
                        map[i][j].setGraphic(iv);
                    }
                } else {
                    ImageView iv = new ImageView(loadImage("/icons/invisible.png"));
                    iv.setFitWidth(GameConstants.CHAR_IMG_PX);
                    iv.setFitHeight(GameConstants.CHAR_IMG_PX);
                    map[i][j].setGraphic(iv);
                    map[i][j].setStyle("-fx-background-color: #F7D060;");
                }
            }
        }

        // Check end conditions before re-attaching handlers
        if (Game.checkWin() || Game.checkGameOver()) {
            primaryStage.setScene(null);
            ButtonType replay = new ButtonType("New Game?", ButtonBar.ButtonData.OK_DONE);
            ButtonType exit   = new ButtonType("Exit",      ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You still wanna play?", replay, exit);
            alert.setTitle("Game Ended");
            alert.setHeaderText(Game.checkWin()
                    ? "You have dominated the zombies!"
                    : "Oops — the zombies got you.");
            alert.showAndWait();
            if (alert.getResult() == replay) initializeHeroSelection();
            else Platform.exit();
            return;
        }

        for (Hero h : Game.heroes)   initHeroButton(h);
        for (Zombie z : Game.zombies) initZombieButton(z);
        updateHeroesInfo();
    }

    // ── Sidebar action buttons ────────────────────────────────────────────────

    /** Wires the End Turn, Attack, Cure, and Use Special sidebar buttons. */
    public static void initOtherButtons() {
        endTurnButton.setOnMouseClicked(event -> {
            try {
                Game.endTurn();
            } catch (NotEnoughActionsException | InvalidTargetException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
            clicks = 0;
            selectedHero = null;
            selectedHeroTarget = null;
            selectedZombie = null;
            updateMap();
        });

        if (selectedHero != null) {
            cure.setMinSize(GameConstants.SIDEBAR_WIDTH, 50);
            cure.setOnMouseClicked(event -> {
                try {
                    selectedHero.cure();
                    Game.adjustVisibility(Game.heroes.get(Game.heroes.size() - 1));
                } catch (NotEnoughActionsException | InvalidTargetException
                         | NoAvailableResourcesException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
                updateMap();
            });

            attack.setMinSize(GameConstants.SIDEBAR_WIDTH, 50);
            attack.setOnMouseClicked(event -> {
                try {
                    selectedHero.attack();
                } catch (NotEnoughActionsException | InvalidTargetException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
                updateMap();
            });

            useSpecial.setMinSize(GameConstants.SIDEBAR_WIDTH, 50);
            useSpecial.setOnMouseClicked(event -> {
                try {
                    if (selectedHero != null) {
                        if ("Heal Yourself".equals(useSpecial.getText()))
                            selectedHero.setTarget(selectedHero);
                        else if ("Heal this Hero".equals(useSpecial.getText()))
                            selectedHero.setTarget(selectedHeroTarget);
                        selectedHero.useSpecial();
                    }
                } catch (InvalidTargetException | NoAvailableResourcesException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
                updateMap();
            });
        }
    }

    /** Updates the bottom status bar with hover-triggered hero stats. */
    public static void updateHeroesInfo() {
        for (Hero h : Game.heroes) {
            Point pt   = h.getLocation();
            String role = h.getClass().getSimpleName();
            map[pt.x][pt.y].setOnMouseEntered(e ->
                    info.setText(role + " " + h.getName()
                            + "\nHP : " + h.getCurrentHp()
                            + "                ActionPoints : " + h.getActionsAvailable()
                            + "\nSupplies : " + h.getSupplyInventory().size()
                            + "                Vaccines : " + h.getVaccineInventory().size()));
            map[pt.x][pt.y].setOnMouseExited(e -> info.setText(""));
        }
        info.setTextFill(Color.WHITE);
        info.setFont(Font.font("Courier New", FontWeight.BOLD, FontPosture.REGULAR, 15));
    }

    // ── Cell click handlers ──────────────────────────────────────────────────

    /**
     * Attaches a three-phase click handler to the cell occupied by {@code h}:
     * <ol>
     *   <li>First click: select the hero; highlight adjacent movement targets.</li>
     *   <li>Second click: select an ally target (for heal/special) or deselect.</li>
     *   <li>Third click (or re-click): deselect and reset highlights.</li>
     * </ol>
     *
     * @param h the hero whose cell button is being wired
     */
    public static void initHeroButton(Hero h) {
        if (!box1.getChildren().contains(endTurnButton))
            box1.getChildren().add(endTurnButton);

        Point l = h.getLocation();
        int i = l.x, j = l.y;

        map[i][j].setOnMouseClicked(event -> {
            if (clicks == 0) {
                clicks++;
                selectedHero = h;
                if (selectedHero instanceof Medic) useSpecial.setText("Heal Yourself");
                if (!box1.getChildren().contains(useSpecial)) box1.getChildren().add(useSpecial);
                mainGameScreen.setRight(box1);
                map[i][j].setStyle("-fx-background-color: orange;");

                highlightAdjacent(i, j, Direction.UP,    i + 1, j);
                highlightAdjacent(i, j, Direction.DOWN,  i - 1, j);
                highlightAdjacent(i, j, Direction.RIGHT, i,     j + 1);
                highlightAdjacent(i, j, Direction.LEFT,  i,     j - 1);

            } else if (clicks == 1 && !h.equals(selectedHero) && selectedHero != null) {
                clicks++;
                selectedHeroTarget = h;
                selectedHero.setTarget(selectedHeroTarget);
                map[i][j].setStyle(selectedHero.checkDistance()
                        ? "-fx-background-color: green;" : "-fx-background-color: red;");
                if (selectedHero instanceof Medic) useSpecial.setText("Heal this Hero");
                if (!box1.getChildren().contains(useSpecial)) box1.getChildren().add(useSpecial);

            } else if (clicks == 2 || (clicks == 1 && selectedHeroTarget == null)) {
                clicks = 0;
                selectedHero = null;
                selectedHeroTarget = null;
                box1.getChildren().remove(useSpecial);
                mainGameScreen.setRight(box1);
                resetCell(i, j);
                resetCell(i + 1, j);
                resetCell(i - 1, j);
                resetCell(i, j + 1);
                resetCell(i, j - 1);
            }
            initOtherButtons();
        });
    }

    /**
     * Attaches click and hover handlers to the cell occupied by zombie {@code z}.
     * Only wired when the cell is visible (not in fog-of-war).
     *
     * @param z the zombie whose cell button is being wired
     */
    public static void initZombieButton(Zombie z) {
        Point l = z.getLocation();
        if (!Game.map[l.x][l.y].isVisible()) return;

        map[l.x][l.y].setOnMouseClicked(event -> {
            if (selectedHero != null && clicks == 1) {
                clicks++;
                selectedZombie = z;
                selectedHero.setTarget(selectedZombie);
                map[l.x][l.y].setStyle(selectedHero.checkDistance()
                        ? "-fx-background-color: green" : "-fx-background-color: red");
                if (!box1.getChildren().contains(attack)) box1.getChildren().add(attack);
                if (!box1.getChildren().contains(cure))   box1.getChildren().add(cure);
                mainGameScreen.setRight(box1);
            } else if (clicks == 2) {
                if (selectedHero != null) selectedHero.setTarget(null);
                map[l.x][l.y].setStyle("-fx-background-color: white;-fx-border-color: grey");
                clicks--;
                box1.getChildren().remove(attack);
                box1.getChildren().remove(cure);
                mainGameScreen.setRight(box1);
            }
        });
        map[l.x][l.y].setOnMouseEntered(e ->
                info.setText(z.getName() + "\nCurrent HP : " + z.getCurrentHp()));
        map[l.x][l.y].setOnMouseExited(e -> info.setText(""));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Highlights a valid adjacent cell pink and wires its movement click handler. */
    private static void highlightAdjacent(int fromI, int fromJ, Direction dir, int ni, int nj) {
        if (ni < 0 || ni >= GameConstants.GRID_SIZE || nj < 0 || nj >= GameConstants.GRID_SIZE)
            return;
        map[ni][nj].setStyle("-fx-background-color: pink; -fx-border-color: red");
        map[ni][nj].setOnMouseClicked(ev -> {
            try { selectedHero.move(dir); clicks--; }
            catch (MovementException | NotEnoughActionsException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        });
    }

    /** Resets a cell to the default empty-cell style and clears its click handler. */
    private static void resetCell(int i, int j) {
        if (i < 0 || i >= GameConstants.GRID_SIZE || j < 0 || j >= GameConstants.GRID_SIZE)
            return;
        map[i][j].setStyle("-fx-background-color: #F3E99F;");
        map[i][j].setOnMouseClicked(null);
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
