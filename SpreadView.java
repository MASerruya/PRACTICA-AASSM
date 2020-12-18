import jason.environment.grid.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** Class that implements the View of Covid City application */
public class SpreadView extends GridWorldView {

	SpreadModel sModel;

	/**
	 * Class constructor
	 */
	public SpreadView(SpreadModel model) {
		super(model, "Covid City", 1000);
		sModel = model;
		defaultFont = new Font("Arial", Font.BOLD, 14);
		setVisible(true);
		repaint();
	}

	/**
	 * Draw applcation objects
	 * 
	 * @param g
	 * @param x      coord of the object
	 * @param y      coord of the object
	 * @param object id object to be drawn
	 */
	@Override
	public void draw(Graphics g, int x, int y, int object) {

		super.drawAgent(g, x, y, Color.lightGray, -1);
		switch (object) {
		case SpreadModel.JOB:
			// if (lRobot.equals(hmodel.lFridge)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.blue, -1);
			g.setColor(Color.blue);
			drawString(g, x, y, defaultFont, "JOB");
			break;
		case SpreadModel.BAR:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.orange, -1);
			g.setColor(Color.orange);
			drawString(g, x, y, defaultFont, "BAR");
			break;
		case SpreadModel.HOSPITAL:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.red, -1);
			g.setColor(Color.red);
			drawString(g, x, y, defaultFont, "HOSPITAL");
			break;
		case SpreadModel.HOME1:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.green);
			drawString(g, x, y, defaultFont, "HOME_1");
			break;
		case SpreadModel.HOME2:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.green);
			drawString(g, x, y, defaultFont, "HOME_2");
			break;
		case SpreadModel.HOME3:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.green);
			drawString(g, x, y, defaultFont, "HOME_3");
			break;
		case SpreadModel.HOME4:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.green);
			drawString(g, x, y, defaultFont, "HOME_4");
			break;
		case SpreadModel.HOME5:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.green);
			drawString(g, x, y, defaultFont, "HOME_5");
			break;
		case SpreadModel.SPORTS:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.orange);
			drawString(g, x, y, defaultFont, "SPORTS");
			break;
		case SpreadModel.SCHOOL:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.orange);
			drawString(g, x, y, defaultFont, "SCHOOL");
			break;
		case SpreadModel.PARK:
			// if (lRobot.equals(hmodel.lOwner)) {
			// super.drawAgent(g, x, y, Color.yellow, -1);
			// }
			// super.drawAgent(g, x, y, Color.green, -1);
			g.setColor(Color.orange);
			drawString(g, x, y, defaultFont, "PARK");
			break;
		}
		repaint();
	}

	/**
	 * Draw agent
	 * 
	 * @param g
	 * @param x      coord of the agent
	 * @param y      coord of the agent
	 * @param object id agent to be drawn
	 */
	@Override
	public void drawAgent(Graphics g, int x, int y, Color c, int id) {

		Location lAgent = sModel.getAgPos(id);
		// Location lAdult = sModel.getAgPos(1);

		if (id < sModel.NUMBER_OF_YOUNG) { // young case

			if (lAgent.equals(sModel.lJob)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lJob)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lBar)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHospital)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome1)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome2)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome3)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome4)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome5)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lSports)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lSchool)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lPark)) {
				c = Color.yellow;
			} else {
				c = Color.magenta;
			}

			super.drawAgent(g, x, y, c, -1);
			g.setColor(Color.black);
			super.drawString(g, x, y, defaultFont, "Young");

		} else { // adult case
			if (lAgent.equals(sModel.lJob)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lJob)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lBar)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHospital)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome1)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome2)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome3)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome4)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lHome5)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lSports)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lSchool)) {
				c = Color.yellow;
			} else if (lAgent.equals(sModel.lPark)) {
				c = Color.yellow;
			} else {
				c = Color.pink;
			}
			super.drawAgent(g, x, y, c, -1);
			g.setColor(Color.black);
			super.drawString(g, x, y, defaultFont, "Adult");
		}
	}

}
