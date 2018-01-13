package info.game;

import javafx.stage.Stage;

/**
 * Created by Duduś on 2018-01-13.
 */
public class SingleMode extends BreakoutEngine implements Runnable{

    public SingleMode(Stage gameStage) {
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
