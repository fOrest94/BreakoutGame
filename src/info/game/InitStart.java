package info.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
 
public class InitStart implements Runnable 
{
 
	BreakoutApp game;
	boolean type;
 
	public InitStart(BreakoutApp game, boolean type)
	{
		this.game = game;
		this.type = type;
	}

	@Override
	public void run() 
	{
		if(!game.playOnline && type == true)
		{
			game.pause();
	
			try 
			{
				ServerSocket serverSocket = new ServerSocket(8500);
				ExecutorService exe = Executors.newSingleThreadExecutor();
				Boolean check = false;
				while(!check)
				{
					Socket clientSocket = serverSocket.accept();
					if(clientSocket.isConnected()==true)
					{
						PauseThread gniazdo= new PauseThread(clientSocket);
						Future<Boolean> answer = exe.submit(gniazdo);
						
						check = (Boolean)answer.get();
						
						if(check)
						{
							game.resume();
							game.playOnline = true;
							serverSocket.close();
							exe.shutdown();
						}
					}
				}
			} 
			catch (Exception e) 
			{
				System.err.println(e);
			}
		}
		else if(!game.playOnline && type == false)
		{
			game.pause();
			Scene a = game.gameStage.getScene();
			
			a.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			      if(key.getCode()==KeyCode.ENTER) 
			      {
			    	  try
			    	  {
				    	  Socket socket = new Socket(InetAddress.getByName("localhost"), 8500);
				    	  PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
				    	  output.println("Start");		
				    	  socket.close();
				    	  output.close();
			    	  }
			    	  catch(Exception except)
			    	  {
			    		  System.out.println("Wystapil blad");
			    	  }
			    	  game.playOnline = true;
			    	  game.resume();
			      }
			});
		}
	}
	
	private class PauseThread implements Callable<Boolean>
	{
		Socket socket;
		
		public PauseThread(Socket socket) 
		{
			super();
			this.socket = socket;
		}

		@Override
		public Boolean call() throws Exception 
		{
			try 
			{
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String connect = input.readLine();
				System.out.println(connect);
				if(connect.equals("Start"))
				{
					return true;
				}
			} 
			catch (Exception e) 
			{
				System.out.print(e);
			}
			
			try 
			{
				socket.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return false;
		}
	}
}