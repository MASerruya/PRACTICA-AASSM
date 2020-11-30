import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

public class SpreadEnv extends Environment {
    
	// Model of the grid
	SpreadModel model;
	SpreadView view;
	 
	public static final Literal yab = Literal.parseLiteral("at(young,bar)"); 
	public static final Literal yaj = Literal.parseLiteral("at(young,job)"); 
	public static final Literal yahos = Literal.parseLiteral("at(young,hospital)");        
	public static final Literal yahom = Literal.parseLiteral("at(young,home)");        
	
	public static final Literal aab = Literal.parseLiteral("at(adult,bar)"); 
	public static final Literal aaj = Literal.parseLiteral("at(adult,job)"); 
	public static final Literal aahos = Literal.parseLiteral("at(adult,hospital)");     
	public static final Literal aahom = Literal.parseLiteral("at(adult,home)");         
	
	
	@Override
    public void init(String[] args) {
        model = new SpreadModel();

        if (args.length == 1 && args[0].equals("gui")) {
            view  = new SpreadView(model);
            model.setView(view);
        }

		updatePercepts();
    }
	
	
	public void updatePercepts() {
		// clear the percepts of the agents    
		clearPercepts("young");
		clearPercepts("adult");

		// get the Young location
		for(int i = 0; i < model.NUMBER_OF_YOUNG; i++){
			String sid = Integer.toString(i+1);      
			Location lyoung = model.getAgPos(i);
						   
			// add agent location to its percepts
			if (lyoung.equals(model.lBar)) {
				addPercept("young" + sid, yab);                          
			}
			else if (lyoung.equals(model.lJob)) {
				addPercept("young" + sid, yaj);
			}                                                    
			else if (lyoung.equals(model.lHospital)) {
				addPercept("young" + sid, yahos);
			}                  
			else if (lyoung.equals(model.lHome)) {
				addPercept("young" + sid, yahom);
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

        } else if (action.getFunctor().equals("is_day")) {                                                                                 
            //logger.info("Failed to execute action "+action);
            String l = action.getTerm(0).toString();
	    int day = 0;
		if (l.equals("L")) {day = 0;}
		else if (l.equals("M")){day = 1;}
		else if (l.equals("X")){day = 2;}
		else if (l.equals("J")){day = 3;}
		else if (l.equals("V")){day = 4;}
		else if (l.equals("S")){day = 5;}
		else if (l.equals("D")){day = 6;}
		else if (l.equals("WEEK")){day = 7;}
		else if (l.equals("WEEKEND")){day = 8;} 
            try {
                result = model.isDay(day);
	
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	else {

	}

        if (result) {
            updatePercepts();
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }
        return result;
    }
	
	
}
