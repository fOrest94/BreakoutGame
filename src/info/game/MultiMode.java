package info.game;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsEntity;
import javafx.stage.Stage;
import org.jbox2d.dynamics.BodyType;

/**
 * Created by Duduś on 2018-01-13.
 */
public class MultiMode extends BreakoutEngine implements Runnable {


    public MultiMode(Stage gameStage) {
        super(gameStage);
    }

    @Override
    public void run() {
        try {
            super.start(gameStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
