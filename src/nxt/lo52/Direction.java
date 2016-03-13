package nxt.lo52;

public class Direction
{
	public int directionPrise;
	public boolean intersection;
	
	public Direction()
	{
		directionPrise = Consts.FORWARD;
		intersection = false;
	}
	
	public String directionToStr()
	{
		String str = new String();
		
		switch(directionPrise) {
			case Consts.FORWARD: str = "F"; break;
			case Consts.LEFT: str = "L"; break;
			case Consts.RIGHT: str = "R"; break;
		}
		
		return str;
	}
}
