package info.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import info.game.DataMessage;
import info.game.RequestMessage;
import info.menu.Menu;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import com.almasb.fxgl.FXGLLogger;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityType;
import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.net.Server;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsEntity;
import com.almasb.fxgl.physics.PhysicsManager;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BreakoutApp extends GameApplication implements Runnable
{
    private Assets assets;
    private Boolean flagaBall = true;
    private Boolean flagaBall2 = true;
    private Entity background;
    private Button button1, button2;
    private IntegerProperty score_1 = new SimpleIntegerProperty();
    private IntegerProperty score_2 = new SimpleIntegerProperty();
    private PhysicsEntity desk1, desk2, ball1, ball2, brick;
    Stage gameStage;
    private String single = new String();
    private boolean isHost = false;
    private boolean isConnected = false;
    boolean playOnline = false;
    private Server server = new Server();
    private Client client = new Client("127.0.0.1");
    private Map<KeyCode, Boolean> keys = new HashMap<>();
    private Queue<RequestMessage> requestQueue = new ConcurrentLinkedQueue<>();
    private Queue<DataMessage> updateQueue = new ConcurrentLinkedQueue<>();

    private enum Type implements EntityType
    {
        BALL, BALL2, BRICK, DESK, SCREEN, BORDER, BACKGROUND;
    }

    public BreakoutApp(String single, Stage gameStage)
    {
        super();
        this.single = single;
        this.gameStage = gameStage;
    }

    public BreakoutApp(boolean isHost, Stage gameStage)
    {
        super();
        this.isHost = isHost;
        this.gameStage = gameStage;
    }

    @Override
    protected void initSettings(GameSettings settings)
    {
        settings.setTitle("Block Blaster");
        settings.setVersion("1.0");
        settings.setWidth(640);
        settings.setHeight(960);
        settings.setIntroEnabled(false);
    }

    @Override
    protected void initAssets() throws Exception
    {
        assets = assetManager.cache();
        assets.logCached();
    }

    private void initBackGround()
    {
        background = new Entity(Type.BACKGROUND);
        background.setPosition(0, 0);
        background.setGraphics(assets.getTexture("background.png"));

        addEntities(background);
    }

    private void initNetworking()
    {
        if(isHost)
        {
            server.addParser(RequestMessage.class, data -> requestQueue.offer(data));
            server.addParser(DataMessage.class, data -> updateQueue.offer(data));
            server.addParser(String.class, data -> isConnected = true);
            server.start();
        }
        else
        {
            client.addParser(DataMessage.class, data -> updateQueue.offer(data));

            try
            {
                client.connect();
                client.send("Hi");
            }
            catch(Exception e)
            {
                log.severe(FXGLLogger.errorTraceAsString(e));
                exit();
            }
        }
    }

    @Override
    protected void initGame()
    {
        physicsManager.setGravity(0, 0);

        if(!single.equals("single"))
        {
            initNetworking();
        }

        initBackGround();
        initScreenBounds();
        initBalls();
        initBricks();
        initDesks();

        physicsManager.addCollisionHandler(new CollisionHandler(Type.BALL, Type.BRICK)
        {

            @Override
            public void onCollisionBegin(Entity a, Entity b)
            {
                removeEntity(b);
                score_1.set(score_1.get() + 100);
            }

            @Override
            public void onCollision(Entity a, Entity b){}

            @Override
            public void onCollisionEnd(Entity a, Entity b){}

        });

        physicsManager.addCollisionHandler(new CollisionHandler(Type.BALL, Type.BORDER)
        {

            @Override
            public void onCollisionBegin(Entity a, Entity b)
            {
                removeEntity(a);
                if(flagaBall) flagaBall = false;
                else flagaBall2 = false;
            }

            @Override
            public void onCollision(Entity a, Entity b){}

            @Override
            public void onCollisionEnd(Entity a, Entity b){}
        });

        if(!single.equals("single"))
        {
            physicsManager.addCollisionHandler(new CollisionHandler(Type.BALL2, Type.BORDER)
            {

                @Override
                public void onCollisionBegin(Entity a, Entity b)
                {
                    removeEntity(a);
                    if(flagaBall) flagaBall = false;
                    else flagaBall2 = false;
                }

                @Override
                public void onCollision(Entity a, Entity b){}

                @Override
                public void onCollisionEnd(Entity a, Entity b){}
            });

            physicsManager.addCollisionHandler(new CollisionHandler(Type.BALL2, Type.BRICK)
            {

                @Override
                public void onCollisionBegin(Entity a, Entity b)
                {
                    removeEntity(b);
                    score_2.set(score_2.get() + 100);
                }

                @Override
                public void onCollision(Entity a, Entity b){}

                @Override
                public void onCollisionEnd(Entity a, Entity b){}

            });
        }
    }

    private void initScreenBounds()
    {
        PhysicsEntity top = new PhysicsEntity(Type.BORDER);
        top.setPosition(0, 30);
        top.setGraphics(new Rectangle(getWidth(), 10));
        top.setCollidable(true);

        PhysicsEntity bottom = new PhysicsEntity(Type.BORDER);
        bottom.setPosition(0, getHeight());
        bottom.setGraphics(new Rectangle(getWidth(), 10));
        bottom.setCollidable(true);

        PhysicsEntity left = new PhysicsEntity(Type.SCREEN);
        left.setPosition(-10, 0);
        left.setGraphics(new Rectangle(10, getHeight()));

        PhysicsEntity right = new PhysicsEntity(Type.SCREEN);
        right.setPosition(getWidth(), 0);
        right.setGraphics(new Rectangle(10, getHeight()));

        addEntities(top, bottom, left, right);

    }

    private void initDesks()
    {
        desk1 = new PhysicsEntity(Type.DESK);
        desk1.setPosition(getWidth()/2 - 128/2, getHeight() - 25);
        desk1.setGraphics(assets.getTexture("desk.png"));
        desk1.setBodyType(BodyType.DYNAMIC);

        if(!single.equals("single"))
        {
            desk2 = new PhysicsEntity(Type.DESK);
            desk2.setPosition(getWidth()/2 - 128/2, 40);
            desk2.setGraphics(assets.getTexture("desk2.png"));
            desk2.setBodyType(BodyType.DYNAMIC);

            addEntities(desk1, desk2);
        }
        else
            addEntities(desk1);
    }

    private void initBricks()
    {
        for(int i = 0; i < 48; i++)
        {
            brick = new PhysicsEntity(Type.BRICK);
            if(!single.equals("single"))
            {
                brick.setPosition((i%16) * 40, ((i/16) + 10) * 40);
            }
            else
                brick.setPosition((i%16) * 40, ((i/16) + 1) * 40);

            brick.setGraphics(assets.getTexture("brick.png"));
            brick.setCollidable(true);

            addEntities(brick);
        }

    }

    @Override
    protected void initUI(Pane uiRoot)
    {

        Text scorePlayer_1 = new Text();
        scorePlayer_1.setTranslateY(20);
        scorePlayer_1.setTranslateX(50);
        scorePlayer_1.setFill(Color.WHITE);
        scorePlayer_1.setFont(Font.font(18));
        scorePlayer_1.textProperty().bind(score_1.asString());

        Text scorePlayer_1_Label = new Text();
        scorePlayer_1_Label.setTranslateY(20);
        scorePlayer_1_Label.setFill(Color.WHITE);
        scorePlayer_1_Label.setFont(Font.font(18));
        scorePlayer_1_Label.setText("Score: ");
        uiRoot.getChildren().addAll(scorePlayer_1_Label, scorePlayer_1);

        if(!single.equals("single"))
        {
            Text scorePlayer_2 = new Text();
            scorePlayer_2.setTranslateY(20);
            scorePlayer_2.setTranslateX(540);
            scorePlayer_2.setFont(Font.font(18));
            scorePlayer_2.setText("Score player 2: ");
            scorePlayer_2.textProperty().bind(score_2.asString());
            uiRoot.getChildren().add(scorePlayer_2);
        }
    }

    @Override
    protected void initInput()
    {
        if(isHost || single.equals("single"))
        {
            inputManager.addKeyPressBinding(KeyCode.A, () -> {
                desk1.setLinearVelocity(-7, 0);
            });

            inputManager.addKeyPressBinding(KeyCode.D, () -> {

                desk1.setLinearVelocity(7, 0);
            });
        }
        else
        {
            initKeys(KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ESCAPE, KeyCode.ENTER);
        }
    }

    private void initKeys(KeyCode... codes)
    {
        for(KeyCode k : codes)
        {
            keys.put(k, false);
            this.inputManager.addKeyPressBinding(k, () -> {
                keys.put(k, true);
            });
        }
    }

    private void initBalls()
    {
        FixtureDef fd = new FixtureDef();
        fd.restitution = 0.8f;
        fd.shape = new CircleShape();
        fd.shape.setRadius(PhysicsManager.toMeters(15));

        ball1 = new PhysicsEntity(Type.BALL);
        ball1.setPosition(getWidth()/2 -30/2, getHeight()/2 + 120);
        ball1.setGraphics(assets.getTexture("ball.png"));
        ball1.setBodyType(BodyType.DYNAMIC);
        ball1.setCollidable(true);
        ball1.setFixtureDef(fd);

        if(!single.equals("single"))
        {
            ball2 = new PhysicsEntity(Type.BALL2);
            ball2.setPosition(getWidth()/2 -30/2, getHeight()/2 - 200);
            ball2.setGraphics(assets.getTexture("ball2.png"));
            ball2.setBodyType(BodyType.DYNAMIC);
            ball2.setCollidable(true);
            ball2.setFixtureDef(fd);

            addEntities(ball1, ball2);
        }
        else
            addEntities(ball1);

        ball1.setLinearVelocity(0, 5);

        if(!single.equals("single"))
            ball2.setLinearVelocity(0, -5);
    }

    @Override
    protected void onUpdate()
    {

        if(!playOnline && !single.equals("single"))
        {
            Executor exec = Executors.newSingleThreadExecutor();
            exec.execute(new InitStart(this, isHost));
        }


        Point2D vDesk1 = desk1.getLinearVelocity();
        Point2D v1 = ball1.getLinearVelocity();
        double xBall1 = v1.getX();
        double yBall1 = v1.getY();
        desk1.setLinearVelocity(0, 0);

        if(Math.abs(v1.getY()) < 5)
        {
            double x = v1.getX();
            double signY = Math.signum(v1.getY());
            ball1.setLinearVelocity(x, signY * 5);
        }

        if(!single.equals("single"))
        {
            Point2D vDesk2 = desk2.getLinearVelocity();
            Point2D v2 = ball2.getLinearVelocity();
            double xBall2 = v2.getX();
            double yBall2 = v2.getY();
            desk2.setLinearVelocity(0, 0);

            if(Math.abs(v2.getY()) < 5)
            {
                double x = v2.getX();
                double signY = Math.signum(v2.getY());
                ball2.setLinearVelocity(x, signY * 5);
            }

            if(isHost)
            {
                if(!isConnected)
                {
                    return;
                }
                DataMessage data1 = updateQueue.poll();

                if(data1 != null)
                {
                    ball2.setLinearVelocity(data1.x4, data1.y4);
                }
                RequestMessage data = requestQueue.poll();
                if(data != null)
                {
                    for(KeyCode key : data.keys)
                    {
                        if(key == KeyCode.LEFT)
                        {
                            desk2.setLinearVelocity(-7, 0);
                        }
                        else if(key == KeyCode.RIGHT)
                        {
                            desk2.setLinearVelocity(7, 0);
                        }
                        else if(key == KeyCode.ENTER)
                        {
                            this.playOnline = true;
                        }
                        else if(key == KeyCode.ESCAPE)
                        {
                            exit();
                        }
                    }
                }

                try
                {
                    double xDesk1 = vDesk1.getX();
                    double yDesk1 = vDesk1.getY();
                    double xDesk2 = vDesk2.getX();
                    double yDesk2 = vDesk2.getY();

                    server.send(new DataMessage(xDesk1, yDesk1, xDesk2, yDesk2, xBall1, yBall1));
                }
                catch(Exception e)
                {
                    log.warning("Failed to send message: "+e.getMessage());
                }
            }
            else if(!isHost)
            {
                Runnable thread1 = new Runnable ()
                {
                    public void run ()
                    {
                        DataMessage data = updateQueue.poll();

                        if(data != null)
                        {
                            desk1.setLinearVelocity(data.x1, data.y1);
                            desk2.setLinearVelocity(data.x2, data.y2);
                            ball1.setLinearVelocity(data.x3, data.y3);
                        }
                    }
                };

                Runnable thread2 = new Runnable ()
                {
                    public void run ()
                    {
                        KeyCode[] codes = keys.keySet().stream().filter(k -> keys.get(k)).collect(Collectors.toList()).toArray(new KeyCode[0]);

                        try
                        {
                            client.send(new RequestMessage(codes));
                            client.send(new DataMessage(xBall2, yBall2));

                            if(keys.get(KeyCode.ESCAPE))
                            {
                                exit();
                            }
                        }
                        catch(Exception e)
                        {
                            log.warning("Failed to send message: "+e.getMessage());
                        }
                        keys.forEach((key, value) -> keys.put(key, false));
                    }
                };

                thread2.run();
                thread1.run();
            }
        }

        if((!flagaBall && !flagaBall2) || score_1.getValue() == 4800)
        {
            flagaBall = flagaBall2 = true;
            Rectangle rect = new Rectangle (100, 40, 100, 100);
            rect.setArcHeight(50);
            rect.setArcWidth(50);
            rect.setFill(Color.VIOLET);

            FadeTransition ft = new FadeTransition(Duration.millis(3000), background);
            ft.setNode(button1);
            ft.setFromValue(1.0);
            ft.setToValue(0.3);
            ft.play();
            initChoice();
        }
    }

    public void initChoice()
    {
        button1 = new Button();
        button1.setLayoutX(getWidth()/3*2);
        button1.setLayoutY(getHeight()/2);
        button1.setPrefSize(120, 60);
        button1.setText("Restart");
        button1.setOnMouseClicked(event ->
        {
            onRestart();
        });

        button2 = new Button();
        button2.setLayoutX(getWidth()/3*1);
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

    private void onRestart()
    {
        BreakoutApp gameApp = new BreakoutApp(this.isHost, this.gameStage);
        try
        {
            gameApp.start(new Stage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.mainStage.hide();
    }

    private void onMainMenu()
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

    @Override
    public void run()
    {
        try
        {
            super.start(gameStage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}