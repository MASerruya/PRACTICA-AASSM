import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.time.LocalDateTime;
                        
/** class that implements the Model of Covid City application */
public class SpreadModel extends GridWorldModel {

	// Constants for the grid objects
	public static final int JOB = 16;
	public static final int BAR = 32;
	public static final int HOSPITAL = 64;
	public static final int HOME = 128; 
	// Week days
	public static final int L = 0;
	public static final int M = 1;
	public static final int X = 2;
	public static final int J = 3;
	public static final int V = 4;
	public static final int S = 5;
	public static final int D = 6;
	// Cases to check if it is someday between L & V or between S & D.
	public static final int WEEK 	= 7;
	public static final int WEEKEND = 8;
	// A day in the system is turned into 30 seconds.
	public static final int DAY	= 30;
	
	public static final int NUMBER_OF_YOUNG = 2;
	public static final int NUMBER_OF_ADULT = 2;
	
	// Grid size
	public static int GSize = 30;
	
	// Object location
	Location lJob = new Location(4,0);
	Location lBar = new Location(28,7);
	Location lHospital = new Location(22,18);
    Location lHome  = new Location(GSize-1,GSize-1);
	
	
	public SpreadModel() {
        // create a GSize grid with n mobile agents
        super(GSize, GSize, 6);
		                
		// Agents location
		setAgPos(0, 13, 10);
		
		setAgPos(1, 3, 3);
		
		setAgPos(2, 12, 10);
		setAgPos(3, 11, 10); 
		/*
		setAgPos(4, 1, 1);
		setAgPos(5, 0, 0);
		*/
	
		// initial location of the objects
        add(JOB, lJob);
        add(BAR, lBar);
        add(HOSPITAL, lHospital);
        add(HOME, lHome);
    }
	
	
	boolean moveTowards(Location dest, int id) {
        Location lAgent = getAgPos(id);
        
		// X coord
		if (lAgent.x < dest.x){
			lAgent.x++;
		}else if (lAgent.x > dest.x){
			lAgent.x--;
		}
		
		// Y coord
        if (lAgent.y < dest.y){
			lAgent.y++;
        }else if (lAgent.y > dest.y){   
			lAgent.y--;
		}
		
        setAgPos(id, lAgent); // move the person in the grid
		
		//UpdateGrid()
		
                                              

                     
	
		
		   
		// repaint the locations
        if (view != null) {                             
            view.update(lJob.x,lJob.y);
            view.update(lBar.x,lBar.y);
			view.update(lHospital.x,lHospital.y);
			view.update(lHome.x,lHome.y);
        }
        return true;
    }


	boolean isDay(int day)
	{
		LocalDateTime now = LocalDateTime.now();
		//Get the current number of seconds consumed of the current hour.
		int seconds = now.getMinute() * 60 + now.getSecond();
		//Get the actual date within the system.
		int current_day = (seconds / DAY) % 7;

		//Check if the current day is a week day.
		if (day == WEEK)
		{
			return ((day >= L) && (day <= V));
		}
		//Check if the current day is a weekend day.
		else if (day == WEEKEND)
		{
			return ((day == S) || (day == D));
		}

		//Check if we are in some day of the week.
		return (day == current_day);
	}

}
