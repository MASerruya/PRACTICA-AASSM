import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import jason.mas2j.*;
import java.util.logging.Logger;
import java.time.LocalDateTime;

/*	Imports for periodical task schedulling */
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;
//DEBUG
import java.util.*;


public class SpreadEnv extends Environment {
    
    /****************** CONSTANTS ********************/

	// Model of the grid
	SpreadModel model;
	SpreadView view;
	
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

	/******** LITERALS ************************/
	 
	 //Location
	public static final Literal yab = Literal.parseLiteral("at(young,bar)"); 
	public static final Literal yaj = Literal.parseLiteral("at(young,job)"); 
	public static final Literal yahos = Literal.parseLiteral("at(young,hospital)");        
	public static final Literal yahom = Literal.parseLiteral("at(young,home)");        
	
	public static final Literal aab = Literal.parseLiteral("at(adult,bar)"); 
	public static final Literal aaj = Literal.parseLiteral("at(adult,job)"); 
	public static final Literal aahos = Literal.parseLiteral("at(adult,hospital)");     
	public static final Literal aahom = Literal.parseLiteral("at(adult,home)"); 

	//Weekdays and time
	
	public static final Literal dweek = Literal.parseLiteral("is_week");  
	public static final Literal dweekend = Literal.parseLiteral("is_weekend"); 
	public static final Literal newday = Literal.parseLiteral("new_day(young)"); 

	public static final Literal lu = Literal.parseLiteral("is_wday(LUN)");
	public static final Literal ma = Literal.parseLiteral("is_wday(MAR)");
	public static final Literal mi = Literal.parseLiteral("is_wday(MIER)");
	public static final Literal ju = Literal.parseLiteral("is_wday(JUE)");
	public static final Literal vi = Literal.parseLiteral("is_wday(VIE)");
	public static final Literal sa = Literal.parseLiteral("is_wday(SAB)");
	public static final Literal dom = Literal.parseLiteral("is_wday(DOM)");
	
	public static final Literal yinf = Literal.parseLiteral("is_infected(young)");

	private static MAS2JProject project;

	//Internal variable to keep track of the current day
	private int curr_day;


	/****************** ENV. METHODS ********************/
	
	/* Task to execute each day elapsed. Adds the newday perception */
    private Runnable dayelapsed = () -> {
		clearAllPercepts();
    	//Add newday perception for all agents
		
		curr_day = (curr_day + 1) % 7;
    	updatePercepts();
		
		/*has to be done for all agents*/
		addPercept("young1",newday); 
		addPercept("young2",newday);
		
		try {
          Thread.sleep(100);
        } catch (Exception e) {}        
		


    	//Debug log code, decide if remove in final version.
    	System.out.println("[DEBUG]: Newday belief added at -> " + new java.util.Date());

    	//DEBUG -- Print all percepts at this time
    	Collection<Literal> allperc = consultPercepts("young1");
    	System.out.println("PERCEPS FROM YOUNG1 " + " " + allperc);
    	
    };
	
	@Override
    public void init(String[] args) {
        model = new SpreadModel();

        if (args.length == 2 && args[0].equals("gui")) {
            view  = new SpreadView(model);
            model.setView(view);

            //Store initial parsed projects (to get a list of all initial agents)
            try{
            	jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileInputStream(args[1]));
            	project = parser.mas();
            }
            catch (Exception e){
            	//Catch exceptions in project initialization
            	e.printStackTrace();
            }
            

        }

		updatePercepts();

		//Set initial day to monday
		curr_day = V;
		addPercept(vi);
		addPercept(dweek);


		/* Creating a executor sevice to schedule a task that adds the newday belief each 'DAY' seconds elapsed */

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		//Schedule, dayelapsed->task to run, delay DAY to first execution, schedule each DAY seconds
        executorService.scheduleWithFixedDelay(dayelapsed, DAY, DAY, TimeUnit.SECONDS);
    }

	public void updatePercepts(String ag, String sid, int iid){        
		       		// clear the percepts of the agents    
		clearPercepts(ag);
		//clearPercepts("adult");
		//DEBUG
		System.out.println("Removed percepts: " + ag);    
		                         
		addPercept(get_weeklit(curr_day));  
		
		Location lagent = model.getAgPos(iid);  

        if (ag.startsWith("young")) {
						// add agent location to its percepts
			if (lagent.equals(model.lBar)) {
				addPercept("young" + sid, yab);
				System.out.println("Added lbar percept!");           
			}
			else if (lagent.equals(model.lJob)) {
				addPercept("young" + sid, yaj);
				System.out.println("Added ljob percept!");
			}                                                    
			else if (lagent.equals(model.lHospital)) {
				addPercept("young" + sid, yahos);
				System.out.println("Added lhosp percept!");
			}                  
			else if (lagent.equals(model.lHome)) {
				addPercept("young" + sid, yahom);
				System.out.println("Added lhome percept!");
			} 
		}else if (ag.startsWith("adult")){
          			// add agent location to its percepts
			if (lagent.equals(model.lBar)) {
				addPercept("adult" + sid, aab);              
			}
			else if (lagent.equals(model.lJob)) {
				addPercept("adult" + sid, aaj);
			}                                                
			else if (lagent.equals(model.lHospital)) {
				addPercept("adult" + sid, aahos);
			}                  
			else if (lagent.equals(model.lHome)) {
				addPercept("adult" + sid, aahom);
			}                                                  
		}

	}
	
	public void updatePercepts() {
		// clear the percepts of the agents    
		clearAllPercepts();
		//clearPercepts("adult");
		//DEBUG
		System.out.println("Removed all percepts!");
		
		addPercept(get_weeklit(curr_day));
			
		if(curr_day < S){
			addPercept(dweek);
		}
		else {
			System.out.println("WEEKEEEEEEEEEEEEEEEEEEEND");
			addPercept(dweekend); 
		}

		// get the Young location
		for(int i = 0; i < model.NUMBER_OF_YOUNG; i++){
			String sid = Integer.toString(i+1);      
			Location lyoung = model.getAgPos(i);  
			if(i == 0){       
				addPercept("young" + sid, yinf);
			}
						   
			// add agent location to its percepts
			if (lyoung.equals(model.lBar)) {
				addPercept("young" + sid, yab);
				System.out.println("Added lbar percept!");                          
			}
			else if (lyoung.equals(model.lJob)) {
				addPercept("young" + sid, yaj);
				System.out.println("Added ljob percept!");
			}                                                    
			else if (lyoung.equals(model.lHospital)) {
				addPercept("young" + sid, yahos);
				System.out.println("Added lhosp percept!");
			}                  
			else if (lyoung.equals(model.lHome)) {
				addPercept("young" + sid, yahom);
				System.out.println("Added lhome percept!");
			} 
					   
		}  
			                
		// get the Adult location
		for(int i = 0; i < model.NUMBER_OF_ADULT; i++){
			String sid = Integer.toString(i+1);      
			Location ladult = model.getAgPos(i+model.NUMBER_OF_YOUNG);  

			// add agent location to its percepts
			if (ladult.equals(model.lBar)) {
				addPercept("adult" + sid, aab);              
			}
			else if (ladult.equals(model.lJob)) {
				addPercept("adult" + sid, aaj);
			}                                                
			else if (ladult.equals(model.lHospital)) {
				addPercept("adult" + sid, aahos);
			}                  
			else if (ladult.equals(model.lHome)) {
				addPercept("adult" + sid, aahom);
			}                                                        
		}                                                            
	}                                                                               
           

	@Override
    public boolean executeAction(String ag, Structure action) {

    	//DEBUG -- Prior to action, percepts
    	Collection<Literal> allpercb = consultPercepts(ag);
		System.out.println("PERCEPS FROM BEFORE " + ag + " " + allpercb);
		      
		boolean result = false;
        System.out.println("["+ag+"] doing: "+action); 
		
		// Variables for getting agent id
		//String sid =  ag.substring(ag.length() - 1);    
		String sid;                                                                                                                     
		int iid;                                       
		if(ag.startsWith("young")){   //Si es young, si ag es young1 el iid es 0
			sid = ag.substring(5, ag.length());             
			iid =  Integer.parseInt(sid)-1;         
		}else{                     //Si es adult, si ag es adult2 el iid es NUMBER_OF_YOUNG + 1
		    sid = ag.substring(5, ag.length()); 
			iid =  Integer.parseInt(sid)-1 + model.NUMBER_OF_YOUNG;       
		}                                            
		
        if (action.getFunctor().equals("move_towards")) {                                                                                 
            String l = action.getTerm(0).toString();
            Location dest = null;
            if (l.equals("bar")) {
                dest = model.lBar;
            }
		else if (l.equals("job")) {
               dest = model.lJob;
           }  
		   else if (l.equals("hospital")) {
               dest = model.lHospital;
           }         
		   else if (l.equals("home")) {
               dest = model.lHome;
           }                                        
            try {
                result = model.moveTowards(dest,iid);
	
            } catch (Exception e) {
                e.printStackTrace();
            }
                                                                                                                                     
        } 
	else {
		//What to do if action is not defined

	}

        if (result) {
            updatePercepts(ag, sid, iid);       
            try {                                                                                                                 
                Thread.sleep(100);
            } catch (Exception e) {}     
        }

        //DEBUG -- After action, percepts
    	allpercb = consultPercepts(ag);
		System.out.println("PERCEPS AFTER FROM " + ag + " " + allpercb);

        return result;
    }


    /** 	Auxiliar function to translate numbers into weeday literals 	**/

    private Literal get_weeklit(int wday){

    	//Translate number to literal
    	switch(wday){
    		case L:	return lu;
    		case M: return ma;
    		case X: return mi;
    		case J: return ju;
    		case V: return vi;
    		case S: return sa;
    		case D: return dom;
    	}

    	//For other number error
    	System.out.println("[ERROR] Invalid weeday internal record.");
    	System.exit(-1);

    	return Literal.parseLiteral("");
    }
	
	
}
