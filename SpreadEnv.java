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
import java.awt.Color;
import java.io.*;
//DEBUG
import java.util.*;

public class SpreadEnv extends Environment {

	/****************** CONSTANTS ********************/

	// Model of the grid
	SpreadModel model;
	SpreadView view;

	// Project
	private static MAS2JProject project;

	// Week days
	public static final int L = 0;
	public static final int M = 1;
	public static final int X = 2;
	public static final int J = 3;
	public static final int V = 4;
	public static final int S = 5;
	public static final int D = 6;
	// Day counter.
	public static int day_counter = 0;
	// Cases to check if it is someday between L & V or between S & D.
	public static final int WEEK = 7;
	public static final int WEEKEND = 8;

	// A day in the system is turned into the following number of seconds.
	public static final int DAY = 75;

	// Agents management arrays.
	public String[] allAgents;
	public boolean[] allAgentsInfectionStatus;
	public int[] daysInfected;
	public int[] daysCanInfect;

	// List of agents of each home.
	ArrayList<ArrayList<String>> lhomes;

	// Control string for further comparisons.
	public static final String home = new String("home");

	// Internal variable to keep track of the current day
	private int curr_day;

	/***********************************************************************/
	/******************************* LITERALS ******************************/
	/***********************************************************************/

	// Location
	public static final Literal agab = Literal.parseLiteral("at(bar)");
	public static final Literal agaj = Literal.parseLiteral("at(job)");
	public static final Literal agahos = Literal.parseLiteral("at(hospital)");
	public static final Literal agasp = Literal.parseLiteral("at(sports)");
	public static final Literal agasch = Literal.parseLiteral("at(school)");
	public static final Literal agapk = Literal.parseLiteral("at(park)");
	public static Literal[] agahom;

	// Weekdays and time
	public static final Literal newday = Literal.parseLiteral("new_day");
	public static final Literal dweek = Literal.parseLiteral("is_week");
	public static final Literal dweekend = Literal.parseLiteral("is_weekend");

	public static final Literal lu = Literal.parseLiteral("is_monday");
	public static final Literal ma = Literal.parseLiteral("is_tuesday");
	public static final Literal mi = Literal.parseLiteral("is_wednesday");
	public static final Literal ju = Literal.parseLiteral("is_thursday");
	public static final Literal vi = Literal.parseLiteral("is_friday");
	public static final Literal sa = Literal.parseLiteral("is_saturday");
	public static final Literal dom = Literal.parseLiteral("is_sunday");

	// Infection
	public static final Literal aginf = Literal.parseLiteral("is_infected");
	public static final Literal pat0 = Literal.parseLiteral("is_patient0");
	public static final Literal caninfect = Literal.parseLiteral("can_infect");

	// Recovered
	public static final Literal rec = Literal.parseLiteral("recovered");

	// Quarentine
	public static final Literal quar = Literal.parseLiteral("quarentine");

	// Responsability
	public static final Literal resp1 = Literal.parseLiteral("is_low_responsible");
	public static final Literal resp2 = Literal.parseLiteral("is_medium_responsible");
	public static final Literal resp3 = Literal.parseLiteral("is_high_responsible");
	public static final Literal[] respArray = { resp1, resp2, resp3 };

	/********************************************************/
	/****************** SET UP METHODS **********************/
	/********************************************************/

	/**
	 * Initiate the program
	 * 
	 * @param args
	 */
	@Override
	public void init(String[] args) {

		// Instance of SpreadModel class
		model = new SpreadModel();

		// Create the athome literals taking into account the number of homes involved.
		agahom = new Literal[model.NHOMES];
		for (int i = 0; i < model.NHOMES; i++) {
			agahom[i] = Literal.parseLiteral("at(home" + (i + 1) + ")");
		}

		if (args.length == 2 && args[0].equals("gui")) {
			view = new SpreadView(model);
			model.setView(view);

			// Store initial parsed projects (to get a list of all initial agents)
			try {
				jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileInputStream(args[1]));
				project = parser.mas();

			} catch (Exception e) {
				// Catch exceptions in project initialization
				e.printStackTrace();
			}
		}
		System.out.println("Starting the life in the Covid City...\n");

		// Set initial day to Friday.
		curr_day = V;

		// Fill the arrays
		List<AgentParameters> agp = project.getAgents();
		int numberOfAgents = agp.size();

		// Initialize arrays
		allAgents = new String[numberOfAgents];			//Array containing the names of all involved agents.
		allAgentsInfectionStatus = new boolean[numberOfAgents];	//Infection status of all agents.
		daysInfected = new int[numberOfAgents]; 		//Number of days that each agent has lived with the virus.
		daysCanInfect = new int[numberOfAgents];		//Number of days that each agent will be able to infect having symptoms or not having them.

		for (int i = 0; i < numberOfAgents; i++)
		{
			daysInfected[i] = 0;
			daysCanInfect[i] = 0;
		}

		// Getting the names to save into allAgents array
		for (AgentParameters ap : agp)

			allAgents[agp.indexOf(ap)] = ap.name;

		// Set the responsability degree to agents
		setResponsability(allAgents);

		// Infect the patients at begining and communicate it to the spreadView
		String[] infectedAtBegining = { allAgents[0] };
		infectAtBegining(infectedAtBegining);

		for (int i = 0; i < numberOfAgents; ++i)

			allAgentsInfectionStatus[i] = false;

		// First infected.
		allAgentsInfectionStatus[0] = true;

		view.setAllAgents(allAgents);
		view.setInformationOfInfections(allAgentsInfectionStatus);

		// Infect the patient 0 - It will not be recovered nor going to hospital
		addPercept(allAgents[0], pat0);

		// Update the percepts to all the agents
		updatePercepts();

		// Create the homes
		lhomes = new ArrayList<>();
		for (int i = 0; i < model.NHOMES; ++i)
			lhomes.add(new ArrayList<String>());

		// Fill the homes
		for (AgentParameters agent : project.getAgents()) {
			String initbels = agent.getOption("beliefs");
			// For every home, if the agent contains the home literal, add it and stop the loop
			for (int i = 0; i < model.NHOMES; ++i)
				if (initbels.contains("is_home" + String.valueOf(i + 1))) {
					lhomes.get(i).add(agent.name);
					break;
				}
		}

		// Creating a executor sevice to schedule a task that adds the newday belief each 'DAY' seconds elapsed
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		// Schedule, dayelapsed->task to run, delay DAY to first execution, schedule each DAY seconds
		executorService.scheduleWithFixedDelay(dayelapsed, DAY, DAY, TimeUnit.SECONDS);

	}

	/**
	 * Method to define which agents are infected at the begining
	 * 
	 * @param ag agents to be infected
	 */
	private void infectAtBegining(String[] ags) {

		// Verify not empty array
		if (ags.length > 0) {

			for (int k = 0; k < ags.length; k++) {
				addPercept(ags[k], aginf);
				addPercept(ags[k], caninfect);
				daysCanInfect[k] = 3;
				System.out.println("[" + ags[k] + "] " + "Agent infected! (Patient 0)");
			}
		}
	}

	/**
	 * Method that defines randomly how responsible an agent is
	 * 
	 * @param ag agent to be set
	 */
	private void setResponsability(String[] ags) {

		// Verify not empty array
		if (ags.length > 0) {

			Random rand = new Random();
			int value = 0;
			int respons = 0;

			for (int k = 0; k < ags.length; k++) {
 				// Young case: 50%Low, 40%Medium, 10%High
				if (ags[k].startsWith("young"))
				{
					value = rand.nextInt(9) + 1;

					if (value < 6) { // Low
						respons = 0;
					} else if (value >= 6 && value < 10) { // Medium
						respons = 1;
					} else { // High
						respons = 2;
					}

					// Add the percept that a yoing agent has some resposibility degree.
					addPercept(ags[k], respArray[respons]);

				}
 				// Adult case: 20%Low, 30%Medium, 50%High
				else if (ags[k].startsWith("adult"))
				{
					value = rand.nextInt(9) + 1;

					if (value < 3) { // Low
						respons = 0;
					} else if (value >= 3 && value < 6) { // Medium
						respons = 1;
					} else { // High
						respons = 2;
					}

					// Add the percept that a adult agent has some resposibility degree.
					addPercept(ags[k], respArray[respons]);
				}
			}
		}
	}

	/********************************************************/
	/****************** ACTION METHODS **********************/
	/********************************************************/

	/**
	 * Excecution of an action
	 * 
	 * @param ag
	 * @param action
	 */
	@Override
	public boolean executeAction(String ag, Structure action) {

		// Value returned by the current method.
		boolean result = false;

		// Variable modified in those actions that do not want to wait
		boolean nowait = false;

		// Variables for getting agent id
		String 	sid;
		int 	iid;

		// Getting the id of the agent
		sid = ag.substring(5, ag.length());
		if (ag.startsWith("young")) {
			iid = Integer.parseInt(sid) - 1;

		} else {
			iid = Integer.parseInt(sid) - 1 + model.NUMBER_OF_YOUNG;
		}

		// Update the agents color in spreadView considering if it is infected or if it is not.
		if (containsPercept(ag, aginf)) {
			allAgentsInfectionStatus[iid] = true;
		} else {
			allAgentsInfectionStatus[iid] = false;
		}
		view.setInformationOfInfections(allAgentsInfectionStatus);

		// Every young or adult has to delete its own newday when 1st action is done
		removePercept(ag, newday);

		// Perform the do_things action considering different scenarios.
		if (action.getFunctor().equals("do_things")) {

			try {Thread.sleep(300);} 
			catch (Exception e) {}

			// Getting do_things argument (location)
			String l = action.getTerm(0).toString();

			// Setting a probability to get infected (even if there is no already infected
			// agent in the location -> simulate more agents than they are)
			getNumInfected(ag, l);
			boolean infected = false;		 //Considers if the agent will end up infected and will show symptoms.
			boolean asymptomatic = false;		 //Considers if the agent will end up infected and will not show symptoms.
			double randomNum = Math.random();	 //Infection probability.
			int numInfected = getNumInfected(ag, l);
			randomNum = randomNum - numInfected*0.1; //Increse the infection probability considering the number of infected agents in the same location.

			//If an agent is in the bar and it does not have the virus, consider infecting it.
			if (l.equals("bar") && !(containsPercept(ag, caninfect) || containsPercept(ag, aginf)))
			{
				//Symptomatic probability.
				if (randomNum < 0.2) {
					infected = true;
				}
				//Asymptomatic probability.
				else if (randomNum < 0.3 && ag.startsWith("young")){
					asymptomatic = true;
				}
			}
			//If an agent is at its job and it does not have the virus, consider infecting it.
			else if (l.equals("job") && !(containsPercept(ag, caninfect) || containsPercept(ag, aginf)))
			{
				//Symptomatic probability.
				if (randomNum < 0.05) {
					infected = true;
				}
				//Asymptomatic probability.
				else if (randomNum < 0.3 && ag.startsWith("young")){
					asymptomatic = true;
				}
			}
			else if (l.equals("sports") && !(containsPercept(ag, caninfect) || containsPercept(ag, aginf)))
			{
				if (randomNum < 0.1) {
					infected = true;
				}
				//Asymptomatic probability.
				else if (randomNum < 0.3 && ag.startsWith("young")){
					asymptomatic = true;
				}
			} 
			else if (l.equals("school") && !(containsPercept(ag, caninfect) || containsPercept(ag, aginf)))
			{
				if (randomNum < 0.1) {
					infected = true;
				} else if (randomNum < 0.3 && ag.startsWith("young")){
					asymptomatic = true;
				}
			}
			else if (l.equals("park") && !(containsPercept(ag, caninfect) || containsPercept(ag, aginf)))
			{
				if (randomNum < 0.1) {
					infected = true;
				} else if (randomNum < 0.2 && ag.startsWith("young")){
					asymptomatic = true;
				}
			}
			else if (l.equals("home"))
			{
				if (containsPercept(ag, rec)) {
					removePercept(ag, rec);
				}

			}
			else if (l.equals("hospital"))
			{
				int i = 0;
				for (i = 0; i < allAgents.length; i++) {
					if (allAgents[i].equals(ag)) {
						break;
					}
				}

				// Updating infected days counter for agent
				daysInfected[i] = daysInfected[i] + 1;

				// Act according responsability of the user: High, Medium, Low
				if (containsPercept(ag, resp1) && daysInfected[i] >= 2) {
					if (containsPercept(ag, aginf)) {
						removePercept(ag, aginf);
						remove_quarentine(ag);
					}

					addPercept(ag, caninfect);
					addPercept(ag, rec);
					daysInfected[i] = 0;
					daysCanInfect[i] = 3;
					System.out.println("[" + ag + "] " + "Agent cured! (Still infects!)");
				}

				if (containsPercept(ag, resp2) && daysInfected[i] >= 4) {
					if (containsPercept(ag, aginf)) {
						removePercept(ag, aginf);
						remove_quarentine(ag);
					}
					addPercept(ag, caninfect);
					addPercept(ag, rec);
					daysInfected[i] = 0;
					daysCanInfect[i] = 2;
					System.out.println("[" + ag + "] " + "Agent cured! (Still infects!)");
				}

				if (containsPercept(ag, resp3) && daysInfected[i] >= 5) {
					if (containsPercept(ag, aginf)) {
						removePercept(ag, aginf);
						System.out.println("[" + ag + "] " + "Agent cured!");
						remove_quarentine(ag);
					}
					addPercept(ag, rec);
					daysInfected[i] = 0;
				}

			}

			// After being in the location, check if has been infected
			if (infected) {
				//In case infected remoe quarentine
				removePercept(ag, quar);
				//And infect
				addPercept(ag, aginf);
				System.out.println("[" + ag + "] " + "Agent infected at " + l + "!");
			}
			if (asymptomatic && ag.startsWith("young")) {
				int i = 0;
				for (i = 0; i < allAgents.length; i++) {
					if (allAgents[i].equals(ag)) {
						break;
					}
				}
					addPercept(ag, caninfect);
					System.out.println("[" + ag + "] " + "Agent infected! (No synthoms)");
					daysCanInfect[i] = 3;
				}
			

			result = true;
                                   
			// Move_towards
		} else if (action.getFunctor().equals("move_towards")) {
			String l = action.getTerm(0).toString();

			Location dest = null;

			if (l.equals("bar")) {
				dest = model.lBar;
			} else if (l.equals("job")) {
				dest = model.lJob;
			} else if (l.equals("hospital")) {
				dest = model.lHospital;
			} else if (l.equals("sports")) {
				dest = model.lSports;
			} else if (l.equals("school")) {
				dest = model.lSchool;
			} else if (l.equals("park")) {
				dest = model.lPark;
			} else if (home.equals(l.substring(0, 4))) {

				for (int i = 0; i < model.NHOMES; i++) {
					if (l.equals("home" + (i + 1))) {
						dest = model.lHomes[i];
						break;
					}
				}
			}
			try {
				result = model.moveTowards(dest, iid);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// TODO: optimizar

			// Remove the literal determining the starting position.
			if (containsPercept(ag, agab)) {
				removePercept(ag, agab);
			} else if (containsPercept(ag, agaj)) {
				removePercept(ag, agaj);
			} else if (containsPercept(ag, agahos)) {
				removePercept(ag, agahos);
			} else if (containsPercept(ag, agasp)) {
				removePercept(ag, agasp);
			} else if (containsPercept(ag, agasch)) {
				removePercept(ag, agasch);
			} else if (containsPercept(ag, agapk)) {
				removePercept(ag, agapk);
			} else {

				for (int i = 0; i < model.NHOMES; i++) {
					if (containsPercept(ag, agahom[i])) {
						removePercept(ag, agahom[i]);
						break;
					}
				}
			}
		}

		// add_quarentine
		else if (action.getFunctor().equals("add_quarentine")) {

			// Action to add the quarentine belief to an agent
			nowait = true;
			result = true;
			if(!containsPercept(ag, aginf)){
				addPercept(ag, quar);
				System.out.println("[" + ag + "] " + "Agent quarentine!");
			} 

		} else {
			// What to do if action is not defined
		}

		try {
			// In case the action has to set the nowait flag, sleep the thread.
			if (!nowait)
				Thread.sleep(100);
		} catch (Exception e) {
		}

		// Add the literal determining the new position.
		updatePosition(ag, sid, iid);

		return result;
	}

	/**
	 * Updates the position of an agent
	 * 
	 * @param ag  whose position is going to be updated
	 * @param sid
	 * @param iid
	 */
	public void updatePosition(String ag, String sid, int iid) {

		Location lagent = model.getAgPos(iid);

		String agent;
		if (ag.startsWith("young")) {
			agent = "young" + sid;

		} else {
			agent = "adult" + sid;
		}

		// Setting percepts according location
		if (lagent.equals(model.lBar)) {
			addPercept(agent, agab);
			System.out.println("[" + agent + "] " + "at bar");

		} else if (lagent.equals(model.lJob)) {
			addPercept(agent, agaj);
			System.out.println("[" + agent + "] " + "at job");

		} else if (lagent.equals(model.lHospital)) {
			addPercept(agent, agahos);
			System.out.println("[" + agent + "] " + "at hospital");

		} else if (lagent.equals(model.lSports)) {
			addPercept(agent, agasp);
			System.out.println("[" + agent + "] " + "at sports");

		} else if (lagent.equals(model.lSchool)) {
			addPercept(agent, agasch);
			System.out.println("[" + agent + "] " + "at school");

		} else if (lagent.equals(model.lPark)) {
			addPercept(agent, agapk);
			System.out.println("[" + agent + "] " + "at park");

		} else {

			for (int i = 0; i < model.NHOMES; i++) {
				if (lagent.equals(model.lHomes[i])) {
					addPercept(agent, agahom[i]);
					System.out.println("[" + agent + "] " + "at home");
					break;
				}
			}
		}
	}

	/********************************************************/
	/****************** ROUTINE METHODS *********************/
	/********************************************************/

	/**
	 * Task to execute each day elapsed
	 */
	private Runnable dayelapsed = () -> {

		// Updating curr_day variable
		curr_day = (curr_day + 1) % 7;

		// Remove previous day-perceptions
		clearDay();

		// Update percepts
		updatePercepts();


		//Manage House infections
		//For each home
		for (ArrayList<String> home : lhomes)
		{	//For each agent in a gome
			for (String ag : home)
			{	//In case he/she/they can infect and is not quareentined
				if ((containsPercept(ag, aginf) || containsPercept(ag, caninfect)) && !containsPercept(ag, quar))
				{	//Infect each agent
					for (String ag_ : home)
					{	//In case the agent is not quareentined or infected
						if (!containsPercept(ag_, aginf) && !containsPercept(ag_, caninfect)  && !containsPercept(ag_, quar))
						{
							Random rand = new Random();
							int x = rand.nextInt(100);

							//Young agents: * Infect with no synthoms (0.3/1)
							//				* Infect (0.3/1)

							if (ag_.startsWith("young") && x < 30)
							{
								addPercept(ag_, aginf);
								System.out.println("[" + ag_ + "] " + "Agent infected at home!");
							}
							else if (ag_.startsWith("young") && x < 60)
							{
								addPercept(ag_, caninfect);
								System.out.println("[" + ag_ + "] " + "Agent infected at home -> Asynthomatic!");
							}

							//Adult agents: * Infect (0.6/1)

							else if (ag_.startsWith("adult") && x < 60)
							{
								addPercept(ag_, aginf);
								System.out.println("[" + ag_ + "] " + "Agent infected at home!");
							}
						}
					}
					//Once reached the first infected in a house stop and jump to next
					break;
				}
			}
		}

		for (int i = 0; i < allAgents.length; i++) {
			String ag = allAgents[i];

			// New day for all the agents
			addPercept(ag, newday);

			// Update days the agent can infect (-1)
			if (containsPercept(ag, caninfect)) {
				daysCanInfect[i] = daysCanInfect[i] - 1;
				if (daysCanInfect[i] == 0) {
					removePercept(ag, caninfect);
					removePercept(ag, aginf);
					System.out.println("[" + ag + "] " + "Agent stop infecting!");
				}
			}
		}


		double infected 		= 0;
		double asymptomatic 		= 0;
		double quarentine 		= 0;
		double at_hospital 		= 0;
		double quarentine_inf		= 0;
		double quarentine_not_inf	= 0;

		//Calculating statistics to be printed at the end of the day.
		for (String ag : allAgents)
		{
			//Number of infected agents.
			if (containsPercept(ag, aginf)) infected++;

			//Number of asymptomatic agents.
			if (containsPercept(ag, caninfect) && !containsPercept(ag, aginf)) asymptomatic++;

			//Number of quarantined agents.
			if (containsPercept(ag, quar)) quarentine++;

			//Number of agents quarantined and infected.
			if (containsPercept(ag, quar) && containsPercept(ag, aginf)) quarentine_inf++;

			//Number of agents quarantined but not infected.
			if (containsPercept(ag, quar) && !containsPercept(ag, aginf)) quarentine_not_inf++;

			//Number of agents in hospital.
			if (containsPercept(ag, agahos)) at_hospital++;
		}

		//Number of irresponsible agents: infected but not quarentined.
		double irresponsible = infected - quarentine;

		System.out.println("***********************************************************************************************************************");
		System.out.println("***********************************************************************************************************************");
		System.out.println("\t\t\t STATISTICS DAY " +day_counter);
		System.out.println();
		System.out.println("Total number of agents:\t\t\t"+15);
		System.out.println("Infected agents:\t\t\t"+infected+" (" +((infected*100)/allAgents.length)+ "% of all agents)");
		System.out.println("Asymptomatic agents:\t\t\t"+asymptomatic+ " (" +((asymptomatic*100)/infected)+ "% of all infected agents)");
		System.out.println("Agents in hospital:\t\t\t"+at_hospital);
		System.out.println("Agents in quarantine:\t\t\t"+quarentine);
		System.out.println("Agents in quarantine and infected:\t\t"+quarentine_inf+" ("+((quarentine_inf*100)/infected)+"% of all infected agents)");
		System.out.println("Agents in quarantine but not infected:\t\t"+quarentine_not_inf);
		System.out.println("Unconscious agents (infected and not quarantined):\t"+irresponsible);
		System.out.println("***********************************************************************************************************************");
		System.out.println("***********************************************************************************************************************");

		//Increase by one the day counter.
		day_counter++;

		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		// Debug log code, decide if remove in final version.
		System.out.println("Newday starting -> " + new java.util.Date());

	};

	/**
	 * Update the percepts for all the agents
	 */
	public void updatePercepts() {

		// Add the percept of the current day
		addPercept(get_weeklit(curr_day));

		// Add the percept of week/weekend
		if (curr_day < S) {
			addPercept(dweek);
		} else {
			addPercept(dweekend);
		}

		// TODO: optimizar

		for (int i = 0; i < allAgents.length; i++) {

			Location lagent = model.getAgPos(i);

			// add agent location to its percepts
			if (lagent.equals(model.lBar)) {
				addPercept(allAgents[i], agab);
				//[DEBUG] System.out.println("Added lbar percept!");
			} else if (lagent.equals(model.lJob)) {
				addPercept(allAgents[i], agaj);
				//[DEBUG] System.out.println("Added ljob percept!");
			} else if (lagent.equals(model.lHospital)) {
				addPercept(allAgents[i], agahos);
				//[DEBUG] System.out.println("Added lhosp percept!");
			} else if (lagent.equals(model.lSports)) {
				addPercept(allAgents[i], agasp);
				//[DEBUG] System.out.println("Added lSports percept!");
			} else if (lagent.equals(model.lSchool)) {
				addPercept(allAgents[i], agasch);
				//[DEBUG] System.out.println("Added lSchool percept!");
			} else if (lagent.equals(model.lPark)) {
				addPercept(allAgents[i], agapk);
				//[DEBUG] System.out.println("Added lSchool percept!");
			} else {

				for (int j = 0; j < model.NHOMES; ++j) {
					if (lagent.equals(model.lHomes[j])) {
						addPercept(allAgents[i], agahom[j]);
						//[DEBUG] System.out.println("Added lHome percept!");
						break;
					}
				}
			}
		}
	}

	/**
	 * Method that clear perceptions from the current day
	 */
	public void clearDay() {

		if (containsPercept(lu)) {
			removePercept(lu);
		} else if (containsPercept(ma)) {
			removePercept(ma);
		} else if (containsPercept(mi)) {
			removePercept(mi);
		} else if (containsPercept(ju)) {
			removePercept(ju);
		} else if (containsPercept(vi)) {
			removePercept(vi);
		} else if (containsPercept(sa)) {
			removePercept(sa);
		} else if (containsPercept(dom)) {
			removePercept(dom);
		}

		if (containsPercept(dweek)) {
			removePercept(dweek);
		} else if (containsPercept(dweekend)) {
			removePercept(dweekend);
		}
	}

	/********************************************************/
	/****************** AUXILIAR METHODS ********************/
	/********************************************************/

	/**
	 * Auxiliar function to translate numbers into weeday literals
	 * 
	 * @param ag wday
	 * @return result of the int to literal convertion
	 */
	private Literal get_weeklit(int wday) {

		// Translate number to literal
		switch (wday) {
		case L:
			return lu;
		case M:
			return ma;
		case X:
			return mi;
		case J:
			return ju;
		case V:
			return vi;
		case S:
			return sa;
		case D:
			return dom;
		}

		// For other number error
		System.out.println("[ERROR] Invalid weeday internal record.");
		System.exit(-1);

		return Literal.parseLiteral("");
	}

	/**
	 * Method that returns the amount of infected agents in a certain location
	 * 
	 * @param ag agent who will check the location
	 * @param l  location to be checked
	 * @return number of agents infected in location
	 */
	public int getNumInfected(String ag, String l) {

		int res = 0;

		for (int i = 0; i < allAgents.length; i++) {
			String auxAgent = allAgents[i];

			Collection<Literal> allperc = consultPercepts(auxAgent);

			for (Iterator<Literal> iterator = allperc.iterator(); iterator.hasNext();) {
				String value = iterator.next().toString();

				if (value.contains("at") && value.contains(l)) {
					if (containsPercept(auxAgent, caninfect)) {
						res++;
					}
				}
			}
		}
		return res;
	}

	/**
	 * Removes a quarentine belief from all the agents in a home.
	 *
	 * @param agent The recovered agent
	 */
	public void remove_quarentine(String agent) {

		// Identify home index
		int home_i = 0;
		// For each home, check if the agent on it.
		while (!lhomes.get(home_i).contains(agent))
			++home_i;

		for (String ag : lhomes.get(home_i))
			if (!ag.equals(agent) && containsPercept(ag, aginf))
				return;

		// Remove quarentine for all the agents (but the revovered)
		for (String ag : lhomes.get(home_i))
			if (!ag.equals(agent)){
				System.out.println("[" + ag + "] " + "Agent removed from quarentine!");
				removePercept(ag, quar);
			}

	}

	/********************************************************/
	/****************** NO USADOS // EN PRUEBA **************/
	/********************************************************/

}
