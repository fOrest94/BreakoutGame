package info.game;

public class DataMessage implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;
	public double x1, y1, x2, y2, x3, y3, x4, y4;
	
	public DataMessage(double x1, double y1, double x2, double y2, double x3, double y3) 
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;
	}
	
	public DataMessage(double x, double y) 
	{
			this.x4 = x;
			this.y4 = y;
	}
}
