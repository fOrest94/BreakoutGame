package info.menu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DecWindow extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		Pane root = new Pane();
		root.setPrefSize(800, 500);
		Scene scene = new Scene(root);
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
	    primaryStage.setOnCloseRequest(e -> {
           Platform.exit();
           System.exit(0);
        });
		
		Platform.runLater(() -> {
            try
            {
                //BreakoutApp gameApp = new BreakoutApp();
                //gameApp.start(new Stage());

            }
            catch (Exception e)
            {
                System.err.println(e);
            }
        });
		
	}
	
	private static class MenuButton extends StackPane
	{
		private Text text;
 
		public MenuButton(String name)
		{
			text = new Text(name);
			text.getFont();
			text.setFont(Font.font(20));
			text.setFill(Color.WHITE);
 
			Rectangle bg = new Rectangle(250, 30);
			bg.setOpacity(0.6);
			bg.setFill(Color.BLACK);
			bg.setEffect(new GaussianBlur(3.5));
 
			setAlignment(Pos.CENTER_LEFT);
			setRotate(-0.5);
			getChildren().addAll(bg, text);
 
			this.setOnMouseEntered(event -> {
				bg.setTranslateX(10);
				text.setTranslateX(10);
				bg.setFill(Color.WHITE);
				text.setFill(Color.BLACK);
			});
 
			this.setOnMouseExited(event -> {
				bg.setTranslateX(0);
				text.setTranslateX(0);
				bg.setFill(Color.BLACK);
				text.setFill(Color.WHITE);
			});
 
			DropShadow drop = new DropShadow(50, Color.WHITE);
			drop.setInput(new Glow());
 
			setOnMousePressed(event -> setEffect(drop));
			setOnMouseReleased(event -> setEffect(null));
		}
	}
	
}
