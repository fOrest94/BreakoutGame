package info.game;

import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsEntity;
import com.almasb.fxgl.physics.PhysicsManager;
import info.menu.Menu;
import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import java.util.Random;

/**
 * Created by Duduś on 2018-01-13.
 */
public class BreakoutEngine extends GameApplication {

    private Assets assets;
    protected IntegerProperty score = new SimpleIntegerProperty();
    private PhysicsEntity desk, ball, brick;
    private Entity background;
    private double ballAcceleration = 5;
    private Button button1, button2;
    protected Stage gameStage;
    private boolean gameOver = false;

    public BreakoutEngine(Stage gameStage) {

        this.gameStage = gameStage;
    }

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setTitle("Block Blaster");
        settings.setWidth(640);
        settings.setHeight(960);
        settings.setIntroEnabled(false);
    }

    @Override
    protected void initAssets() throws Exception {

        assets = assetManager.cache();
        assets.logCached();
    }

    @Override
    protected void initGame() {

        physicsManager.setGravity(0, 0);

        initBackGround();
        initScreenBounds();
        initBalls();
        initBricks();
        initDesks();

        physicsManager.addCollisionHandler(new CollisionHandler(ObjectType.BALL, ObjectType.BRICK) {

            @Override
            public void onCollisionBegin(Entity a, Entity b) {
                removeEntity(b);
                setBallAcceleration(getBallAcceleration()+0.35);
                score.set(score.get() + 100);
            }

            @Override
            public void onCollision(Entity a, Entity b) {
            }

            @Override
            public void onCollisionEnd(Entity a, Entity b) {
            }

        });

        physicsManager.addCollisionHandler(new CollisionHandler(ObjectType.BALL, ObjectType.BORDER_COLLIDABLE) {
            @Override
            public void onCollisionBegin(Entity a, Entity b) {
                removeEntity(a);
                gameOverView();
            }

            @Override
            public void onCollision(Entity a, Entity b) {
            }

            @Override
            public void onCollisionEnd(Entity a, Entity b) {
            }
        });
    }

    public void gameOverView(){

        Rectangle rect = new Rectangle (100, 40, 100, 100);
        rect.setArcHeight(50);
        rect.setArcWidth(50);
        rect.setFill(Color.VIOLET);



        FadeTransition ft = new FadeTransition(Duration.millis(3000));
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setCycleCount(4);
        ft.setAutoReverse(true);

        ft.play();
        initChoice();
    }

    public void initChoice()
    {
        button1 = new Button();
        button1.setLayoutX(getWidth()/4-60);
        button1.setLayoutY(getHeight()/2);
        button1.setPrefSize(120, 60);
        button1.setText("Restart");
        button1.setOnMouseClicked(event ->
        {
            onRestart();
        });

        button2 = new Button();
        button2.setLayoutX(getWidth()/4*3-60);
        button2.setLayoutY(getHeight()/2);
        button2.setPrefSize(120, 60);
        button2.setText("Get back to main menu");
        button2.setOnMouseClicked(event ->
        {
            onMainMenu();
        });

        Pane pane = new Pane();
        pane.getChildren().addAll(button1, button2);
        this.addUINode(pane);
    }

    protected void onRestart()
    {
        SingleMode singleMode = new SingleMode(this.gameStage);
        try
        {
            singleMode.start(singleMode.gameStage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void onMainMenu()
    {
        Menu mainMenu = new Menu();
        try
        {
            mainMenu.start(new Stage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.mainStage.hide();
    }

    protected void initBackGround() {

        background = new Entity(ObjectType.BACKGROUND);
        background.setPosition(0, 0);
        background.setGraphics(assets.getTexture("background.png"));
        addEntities(background);
    }

    protected void initScreenBounds() {

        PhysicsEntity top = new PhysicsEntity(ObjectType.BORDER);
        top.setPosition(0, 30);
        top.setGraphics(new Rectangle(getWidth(), 10));
        top.setCollidable(false);

        PhysicsEntity bottom = new PhysicsEntity(ObjectType.BORDER_COLLIDABLE);
        bottom.setPosition(0, getHeight());
        bottom.setGraphics(new Rectangle(getWidth(), 10));
        bottom.setCollidable(true);

        PhysicsEntity left = new PhysicsEntity(ObjectType.SCREEN);
        left.setPosition(-10, 0);
        left.setGraphics(new Rectangle(10, getHeight()));

        PhysicsEntity right = new PhysicsEntity(ObjectType.SCREEN);
        right.setPosition(getWidth(), 0);
        right.setGraphics(new Rectangle(10, getHeight()));

        addEntities(top, bottom, left, right);
    }

    protected void initDesks() {

        desk = new PhysicsEntity(ObjectType.DESK);
        desk.setPosition(getWidth() / 2 - 128 / 2, getHeight() - 25);
        desk.setGraphics(assets.getTexture("desk.png"));
        desk.setBodyType(BodyType.DYNAMIC);

        addEntities(desk);
    }

    protected void initBricks() {

        Random generator = new Random();
        boolean randomValue;
        int l=0;
        for (int i = 1; i < 132; i++) {
            randomValue = generator.nextBoolean();

            if (randomValue){
                l++;
                customBridge((i % 16) * 40, ((i / 16) + 1) * 40);}
        }
        System.out.println(l);
    }

    protected void customBridge(int x, int y) {

        brick = new PhysicsEntity(ObjectType.BRICK);
        brick.setPosition(x, y);
        brick.setGraphics(assets.getTexture("brick.png"));
        brick.setCollidable(true);
        addEntities(brick);
    }

    protected void initBalls() {

        FixtureDef fd = new FixtureDef();
        fd.restitution = 0.8f;
        fd.shape = new CircleShape();
        fd.shape.setRadius(PhysicsManager.toMeters(15));

        ball = new PhysicsEntity(ObjectType.BALL);
        ball.setPosition(getWidth() / 2 - 30 / 2, getHeight() / 2 + 120);
        ball.setGraphics(assets.getTexture("ball.png"));
        ball.setBodyType(BodyType.DYNAMIC);
        ball.setCollidable(true);
        ball.setFixtureDef(fd);
        addEntities(ball);
        ball.setLinearVelocity(0, 5);
    }

    @Override
    protected void initUI(Pane uiRoot) {

        Text scorePlayer_1 = new Text();
        scorePlayer_1.setTranslateY(20);
        scorePlayer_1.setTranslateX(50);
        scorePlayer_1.setFill(Color.WHITE);
        scorePlayer_1.setFont(Font.font(18));
        scorePlayer_1.textProperty().bind(score.asString());

        Text scorePlayer_1_Label = new Text();
        scorePlayer_1_Label.setTranslateY(20);
        scorePlayer_1_Label.setFill(Color.WHITE);
        scorePlayer_1_Label.setFont(Font.font(18));
        scorePlayer_1_Label.setText("Score: ");
        uiRoot.getChildren().addAll(scorePlayer_1_Label, scorePlayer_1);
    }

    @Override
    protected void initInput() {

        inputManager.addKeyPressBinding(KeyCode.A, () -> {
            desk.setLinearVelocity(-7, 0);
        });

        inputManager.addKeyPressBinding(KeyCode.D, () -> {

            desk.setLinearVelocity(7, 0);
        });
    }

    @Override
    protected void onUpdate() {

        Point2D v1 = ball.getLinearVelocity();
        desk.setLinearVelocity(0, 0);

        if (Math.abs(v1.getY()) < getBallAcceleration()) {
            double x = v1.getX();
            double signY = Math.signum(v1.getY());
            ball.setLinearVelocity(x, signY * getBallAcceleration());
        }
    }

    protected double getBallAcceleration() {return ballAcceleration;}

    protected void setBallAcceleration(double ballAcceleration) {
        this.ballAcceleration = ballAcceleration;
    }
}
