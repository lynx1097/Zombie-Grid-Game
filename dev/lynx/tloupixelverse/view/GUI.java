package dev.lynx.tloupixelverse.view;

import dev.lynx.tloupixelverse.engine.Game;
import dev.lynx.tloupixelverse.exceptions.InvalidTargetException;
import dev.lynx.tloupixelverse.exceptions.MovementException;
import dev.lynx.tloupixelverse.exceptions.NoAvailableResourcesException;
import dev.lynx.tloupixelverse.exceptions.NotEnoughActionsException;
import dev.lynx.tloupixelverse.model.characters.Direction;
import dev.lynx.tloupixelverse.model.characters.Explorer;
import dev.lynx.tloupixelverse.model.characters.Fighter;
import dev.lynx.tloupixelverse.model.characters.Hero;
import dev.lynx.tloupixelverse.model.characters.Medic;
import dev.lynx.tloupixelverse.model.characters.Zombie;
import dev.lynx.tloupixelverse.model.collectibles.Supply;
import dev.lynx.tloupixelverse.model.world.CharacterCell;
import dev.lynx.tloupixelverse.model.world.CollectibleCell;
import dev.lynx.tloupixelverse.util.Point;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GUI extends Application {
    private static Button [][] map = new Button[15][15];
    private static GridPane mapGrid = new GridPane();
    private static BorderPane mainGameScreen = new BorderPane(mapGrid);
    private static HBox heroSelection = new HBox();
    private static StackPane gameRoot = new StackPane(mainGameScreen);
    private static Scene mainGameScene = new Scene(gameRoot);
    
    private static StackPane heroRoot = new StackPane(heroSelection);
    private static Scene heroSelectionScene = new Scene(heroRoot);
    private static Hero selectedHero;
    private static Hero selectedHeroTarget ;
    private static Zombie selectedZombie ;
    private static int clicks = 0;
    private static Object [][] heroes = new Object[8][2];
    private static Button endTurnButton =null;

    private static Label info = null;
    private static Stage primaryStage;
    private static Button useSpecial = new Button("Use Special Action");
    private Button healOther = new Button("Heal the selected hero");
    private static Button cure = new Button("Cure");
    private static Button attack = new Button("Attack");
    private static VBox box1 = new VBox();
    private static Media heroSelMusic = new Media("/dev/lynx/tloupixelverse/Sounds/HeroSel.mp3");
    private static Media gameMusic = new Media("/dev/lynx/tloupixelverse/Sounds/Game.mp3");
    private static MediaPlayer mainMusicPlayer = new MediaPlayer(heroSelMusic);
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    private static String iconPath(String name) {
        return "/dev/lynx/tloupixelverse/icons/Characters/" + name.replace(' ', '_') + ".png";
    }
    public static void showErrorPopup(String message) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Action Invalid");
        
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label label = new Label(message);
        label.setWrapText(true); // Ensures long messages don't get cut off
        
        Button okButton = new Button("OK");
        okButton.setMinSize(80, 30);
        okButton.setOnAction(e -> dialog.close());
        
        box.getChildren().addAll(label, okButton);
        dialog.setScene(new Scene(box, 400, 300));
        
        dialog.show();
        dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - dialog.getWidth()) / 2);
        dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - dialog.getHeight()) / 2);
    }
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        initializeHeroSelection();
        primaryStage.setScene(heroSelectionScene);
        primaryStage.show();

    }
    public static void loadHeroesIcons(){
        for (int i = 0 ; i<8 ; i++){
            heroes[i][0] = Game.availableHeroes.get(i).getName() ;
            heroes[i][1] = new ImageView(new Image(iconPath(Game.availableHeroes.get(i).getName())));
            ((ImageView)heroes[i][1]).setFitHeight(36);
            ((ImageView)heroes[i][1]).setFitWidth(36);
        }
    }

    public static void initializeHeroSelection()  {
        
        Game.loadHeroes();
        loadHeroesIcons();
        primaryStage.setTitle("Select your Hero");
        heroSelection.setAlignment(Pos.CENTER);
        mainMusicPlayer.stop();
        mainMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop the music!
        mainMusicPlayer.setVolume(0.7); // Optional: Adjust volume if needed
        mainMusicPlayer.play();
        for (int i =0;i<8;i++){
            Button button = new Button();
            button.setMaxSize(144,144);
            button.setMinSize(144,144);
            ((ImageView)heroes[i][1]).setFitWidth(144);
            ((ImageView)heroes[i][1]).setFitHeight(144);
            Hero m = Game.availableHeroes.get(i);
            String s = m instanceof Fighter ? "Fighter": m instanceof Explorer ? "Explorer" : "Medic";
            Tooltip heroInfo = new Tooltip(m.getName()+"\nMax HP : "+m.getMaxHp()+"\nRole : "+s+"\nAction Points : "+m.getMaxActions());
//            heroInfo.setShowDelay(Duration.millis(100));
            button.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));            
            button.setTooltip(heroInfo);
            button.setGraphic((Node) heroes[i][1]);
            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    selectedHero =m ;
                    Game.startGame(selectedHero);
                    initializeMainGameScreen();

                }
            });
            heroSelection.getChildren().add(button);
            primaryStage.setScene(heroSelectionScene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        }

    }
    public static void initializeMainGameScreen(){
        mainMusicPlayer.stop();
        mainMusicPlayer = new MediaPlayer(gameMusic);
        mainMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop the music!
        mainMusicPlayer.setVolume(0.5); // Optional: Adjust volume if needed
        mainMusicPlayer.play();        
        box1.setMinWidth(150);
        primaryStage.setTitle("The Last Of Us : Pixelverse");
        endTurnButton = new Button("End Turn");
        endTurnButton.setMinSize(150,50);
        mainGameScreen.setRight(box1);
        info = new Label();
        info.setMinSize((40*15)+150,70);
        info.setBackground(new Background(new BackgroundFill(Color.web("#FF6D60"), CornerRadii.EMPTY, Insets.EMPTY)));
        BorderPane pane2 = new BorderPane(info);
        info.setAlignment(Pos.CENTER);
        mainGameScreen.setBottom(pane2);
        mainGameScreen.setBackground(new Background(new BackgroundFill(Color.web("#ADE4DB"), CornerRadii.EMPTY, Insets.EMPTY)));        initializeMap();
        updateMap();
        primaryStage.setResizable(false);
        primaryStage.setScene(mainGameScene);
        primaryStage.centerOnScreen();



    }
    public static void updateMap(){

        for (int i = 0; i < 15; i++) {
            for (int j=0;j<15;j++){
                map[i][j].setGraphic(null);
                map[i][j].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                map[i][j].setOnMouseClicked(null);
                map[i][j].setOnMouseEntered(null);
            }
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (Game.map[i][j].isVisible()){
                    if(Game.map[i][j]instanceof CharacterCell) {

                        if (((CharacterCell) Game.map[i][j]).getCharacter() != null) {
                            ImageView view = null;
                            if (((CharacterCell) Game.map[i][j]).getCharacter() instanceof Hero) {
                                view = new ImageView(new Image(iconPath(((CharacterCell) Game.map[i][j]).getCharacter().getName())));
                            }else{
                                view = new ImageView(new Image("/dev/lynx/tloupixelverse/icons/Characters/Zombie.png"));
                            }
                            view.setFitHeight(36);
                            view.setFitWidth(36);
                            map[i][j].setGraphic(view);
                        }

                    }

                    if(Game.map[i][j] instanceof CollectibleCell){
                        ImageView view = null;
                        if(((CollectibleCell)Game.map[i][j]).getCollectible() instanceof Supply)
                            view = new ImageView(new Image("/dev/lynx/tloupixelverse/icons/Supply.png"));
                        else
                            view = new ImageView(new Image("/dev/lynx/tloupixelverse/icons/Vaccine.png"));
                        view.setFitHeight(36);
                        view.setFitWidth(36);
                        map[i][j].setGraphic(view);
                    }
                }
               else{
                    ImageView view = new ImageView(new Image("/dev/lynx/tloupixelverse/icons/invisible.png"));
                    view.setFitWidth(32);
                    view.setFitHeight(32);
                    map[i][j].setGraphic(view);
                    map[i][j].setBackground(new Background(new BackgroundFill(Color.web("#F7D060"), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        }
        if(Game.checkWin()||Game.checkGameOver())if(Game.checkWin()||Game.checkGameOver()){
            Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            dialog.setTitle("Game Ended");
            
            VBox box = new VBox(20);
            box.setAlignment(Pos.CENTER);
            box.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            
            String header = Game.checkWin() ? "You have dominated the zombies." : "Oops , you have been bamboozled.";
            Label label = new Label(header + "\n\nYou still wanna play?");
            label.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            Button replay = new Button("New Game ?");
            replay.setMinSize(100, 40);
            replay.setOnAction(e -> { 
                dialog.close(); 
                initializeHeroSelection(); 
            });
            
            Button exit = new Button("Exit");
            exit.setMinSize(100, 40);
            exit.setOnAction(e -> { 
                dialog.close(); 
                Platform.exit(); 
            });
            
            HBox buttonBox = new HBox(20, replay, exit);
            buttonBox.setAlignment(Pos.CENTER);
            
            box.getChildren().addAll(label, buttonBox);
            dialog.setScene(new Scene(box, 400, 300));
            
            dialog.show();
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - dialog.getHeight()) / 2);
        }
        for (Hero h:Game.heroes ) {
            initHeroButton(h);
        }
        for (Zombie z:Game.zombies) {
            initZombieButton(z);
        }
        updateHeroesInfo();

    }
    public static void initOtherButtons() {
        endTurnButton.setOnMouseClicked(event -> {
            try {
                Game.endTurn();
            } catch (NotEnoughActionsException | InvalidTargetException e) {
                showErrorPopup(e.getMessage());
            }
            clicks=0;
            selectedHero=null;
            selectedHeroTarget=null;
            selectedZombie=null;
            updateMap();
        });
        if(selectedHero!=null){
            cure.setMinSize(150,50);
            cure.setOnMouseClicked(event -> {
            try {
                selectedHero.cure();
                Game.adjustVisibility(Game.heroes.get(Game.heroes.size()-1));
            } catch (NotEnoughActionsException | InvalidTargetException | NoAvailableResourcesException e) {
                showErrorPopup(e.getMessage());
            }
            updateMap();
            });
            attack.setMinSize(150,50);
            attack.setOnMouseClicked(event -> {
            try {
                selectedHero.attack();
            } catch (NotEnoughActionsException | InvalidTargetException e) {
                showErrorPopup(e.getMessage());

            }
            updateMap();
            });
            useSpecial.setMinSize(150,50);
            useSpecial.setOnMouseClicked(event -> {
            try {
                if( selectedHero!=null) {
                    if (useSpecial.getText().equals("Heal Yourself")) {
                        selectedHero.setTarget(selectedHero);
                    }
                    else if (useSpecial.getText().equals("Heal this Hero"))
                        selectedHero.setTarget(selectedHeroTarget);
                    selectedHero.useSpecial();
                }
            } catch (InvalidTargetException | NoAvailableResourcesException e) {
                showErrorPopup(e.getMessage());
            }
            updateMap();
            });
        }

    }
    public static void updateHeroesInfo(){
        for (Hero h :Game.heroes ) {
            Point point = h.getLocation();
            String s = String.valueOf(h.getClass());
            s = s.replace("class dev.lynx.tloupixelverse.model.characters.","");
            Tooltip tooltip = new Tooltip(s+" "+h.getName()+"\nHP : "+h.getCurrentHp()+"\nActionPoints : "+h.getActionsAvailable()+"\nSupplies : "+h.getSupplyInventory().size()+"\nVaccines : "+h.getVaccineInventory().size());
//            tooltip.setShowDelay(Duration.millis(100));
           // map[point.x][point.y].setTooltip(tooltip);
            String finalS = s;
            map[point.x][point.y].setOnMouseEntered(event -> {
                info.setText(finalS +" "+h.getName()+"\nHP : "+h.getCurrentHp()+"                ActionPoints : "+h.getActionsAvailable()+"\nSupplies : "+h.getSupplyInventory().size()+"                Vaccines : "+h.getVaccineInventory().size());
            });
            map[point.x][point.y].setOnMouseExited(event -> {
                info.setText("");
            });
        }
        info.setTextFill(Color.WHITE);
        info.setFont(Font.font("Courier New", FontWeight.BOLD, FontPosture.findByName("Regular"),15));
    }

    public static void initHeroButton(Hero h){
        if(!box1.getChildren().contains(endTurnButton))box1.getChildren().add(endTurnButton);
        Point l = h.getLocation();
        int i=l.x,j=l.y;
        map[i][j].setOnMouseClicked(event -> {
            if (clicks==0){
                clicks++;
                selectedHero=h;
                if(selectedHero instanceof Medic){
                    useSpecial.setText("Heal Yourself");
                }
                if(!box1.getChildren().contains(useSpecial))box1.getChildren().add(useSpecial);
                mainGameScreen.setRight(box1);
                map[i][j].setBackground(new Background(new BackgroundFill(Color.web("#FFA500"), CornerRadii.EMPTY, Insets.EMPTY)));
                if(i+1>=0 && i+1 <=14){
                    map[i+1][j].setBackground(new Background(new BackgroundFill(Color.web("#FFC0CB"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i+1][j].setOnMouseClicked(event1 -> {
                        try { h.move(Direction.UP);clicks--;} catch (MovementException |NotEnoughActionsException e) {
                            showErrorPopup(e.getMessage());
                        }
                    });
                }
                if(i-1>=0 && i-1 <=14){
                    map[i-1][j].setBackground(new Background(new BackgroundFill(Color.web("#FFC0CB"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i-1][j].setOnMouseClicked(event1 -> {
                        try { h.move(Direction.DOWN);clicks--;} catch (MovementException |NotEnoughActionsException e) {
                            showErrorPopup(e.getMessage());
                        }
                    });
                }
                if(j+1>=0 && j+1 <=14){
                    map[i][j+1].setBackground(new Background(new BackgroundFill(Color.web("#FFC0CB"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i][j+1].setOnMouseClicked(event1 -> {
                        try { h.move(Direction.RIGHT);clicks--;} catch (MovementException |NotEnoughActionsException e) {
                            showErrorPopup(e.getMessage());
                    }
                    });
                }
                if(j-1>=0 && j-1 <=14){
                    map[i][j-1].setBackground(new Background(new BackgroundFill(Color.web("#FFC0CB"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i][j-1].setOnMouseClicked(event1 -> {
                        try { h.move(Direction.LEFT);clicks--;} catch (MovementException |NotEnoughActionsException e) {
                            showErrorPopup(e.getMessage());
                        }
                    });
                }

            } else if (clicks == 1 && !h.equals(selectedHero) && selectedHero!=null) {
                clicks++;
                selectedHeroTarget=h;
                selectedHero.setTarget(selectedHeroTarget);
                if(selectedHero.checkDistance()) {
                    map[i][j].setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                }else 
                    map[i][j].setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                if(selectedHero instanceof Medic){
                    useSpecial.setText("Heal this Hero");
                }
                if(!box1.getChildren().contains(useSpecial)) box1.getChildren().add(useSpecial);
            } else if (clicks==2 ||(clicks==1 && selectedHeroTarget==null)){
                clicks =0;
                selectedHero = null;
                selectedHeroTarget=null;
                if(box1.getChildren().contains(useSpecial))
                    box1.getChildren().remove(useSpecial);
                mainGameScreen.setRight(box1);
                map[i][j].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                if(i+1>=0 && i+1 <=14){
                    map[i+1][j].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i+1][j].setOnMouseClicked(null);
                }
                if(i-1>=0 && i-1 <=14){
                    map[i-1][j].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i-1][j].setOnMouseClicked(null);
                }
                if(j+1>=0 && j+1 <=14){
                    map[i][j+1].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i][j+1].setOnMouseClicked(null);
                }
                if(j-1>=0 && j-1 <=14){
                    map[i][j-1].setBackground(new Background(new BackgroundFill(Color.web("#F3E99F"), CornerRadii.EMPTY, Insets.EMPTY)));
                    map[i][j-1].setOnMouseClicked(null);
                }
            }
            initOtherButtons();
        });

    }
    public static void initZombieButton (Zombie z){
        Point l = z.getLocation();
        if(Game.map[l.x][l.y].isVisible()) {
            map[l.x][l.y].setOnMouseClicked(event -> {
            if(selectedHero!=null && clicks==1){
                clicks++;
                selectedZombie = z;
                selectedHero.setTarget(selectedZombie);
                if(selectedHero.checkDistance()) map[l.x][l.y].setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                else map[l.x][l.y].setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                if(!box1.getChildren().contains(attack)) box1.getChildren().add(attack);
                if(!box1.getChildren().contains(cure)) box1.getChildren().add(cure);
                mainGameScreen.setRight(box1);
            }
            else if (clicks==2){
                if(selectedHero!=null) selectedHero.setTarget(null);
                map[l.x][l.y].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                clicks--;
                box1.getChildren().remove(attack);
                box1.getChildren().remove(cure);
                mainGameScreen.setRight(box1);

            }
            });
            map[l.x][l.y].setOnMouseEntered(event -> {
                info.setText(z.getName()+"\nCurrent HP : "+z.getCurrentHp());
            });
            map[l.x][l.y].setOnMouseExited(event -> {
                info.setText("");
            });
        }
    }



    public static void initializeMap(){
        mapGrid.setMinSize(600,600);
        mapGrid.setMaxSize(600,600);
        for (int i = 0; i <15 ; i++) {
            for (int j = 0; j <15 ; j++) {
                Button button = new Button();
                button.setMinSize(40,40);
                button.setMaxSize(40,40);
                // Programmatic styling (see point 2 below)
                button.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                button.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                
                // Note: i and j swapped to transpose the grid natively instead of rotating
                mapGrid.add(button, j, i); 
                map[i][j] = button;
            }
        }
        mapGrid.setAlignment(Pos.CENTER);
        // REMOVED: mapGrid.setRotate(-90);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
