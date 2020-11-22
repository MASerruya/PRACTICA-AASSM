import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/** class that implements the Model of Covid City application */
public class SpreadModel extends GridWorldModel {

	// Constants for the grid objects
	public static final int JOB = 16;
	public static final int BAR = 32;
	public static final int HOSPITAL = 64;
	public static final int HOME = 128;
	
	// Grid size
	public static int GSize = 30;
	
	// Object location
	Location lJob = new Location(4,0);
	Location lBar = new Location(7,7);
	Location lHospital = new Location(22,18);
    Location lHome  = new Location(GSize-1,GSize-1);
	
	
	public SpreadModel() {
        // create a GSize grid with n mobile agents
        super(GSize, GSize, 5);
		
		// Agent location
		setAgPos(0, 13, 10);
		setAgPos(1, 3, 3);
		setAgPos(2, 21, 21);
		setAgPos(3, 4, 4);
		setAgPos(4, 1, 1);
	
		// initial location of the objects
        add(JOB, lJob);
        add(BAR, lBar);
        add(HOSPITAL, lHospital);
        add(HOME, lHome);
    }
}
