package main;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ECplot extends JPanel
{	
	// --------------------------------------------------------------------------------------------
	// Window size
	// --------------------------------------------------------------------------------------------
	static int dimX = 800, dimY = 600;
	public static double[] points = {dimX/2, 1, dimX/2, 1};
	
	// --------------------------------------------------------------------------------------------
	// coefficients of the EC
	// --------------------------------------------------------------------------------------------
	static public int a = 1;
	static public int b = -12;
	static public int c = 20;
	static public int count = 0;
	
	// --------------------------------------------------------------------------------------------
	// this is supposed to help find appropriate ranges for the first plot
	// --------------------------------------------------------------------------------------------
	static private double B = (double) b / a;
	static private double C = (double) c / a;	
	
	static double valueX = 5; //2 * (B > 0? Math.cbrt(C) : Math.sqrt(-B));
	static double valueY = 10;//2 * (B > 0? Math.sqrt(c) : C + (3/8)*B*Math.sqrt(-B));
	
	// --------------------------------------------------------------------------------------------
	// not sure if these have to be global, but it works
	// --------------------------------------------------------------------------------------------
	
	final static JSplitPane splitPaneLR = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	final static JSplitPane splitPaneUD = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	final static JLabel EC = new JLabel();
	final static JTextArea area = new JTextArea();
	final static JScrollPane scroll = new JScrollPane (area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	// --------------------------------------------------------------------------------------------
	// range for the x and y axes, along with the step we're using when plotting the graph
	// --------------------------------------------------------------------------------------------

	static double maxValueX, maxValueY, dist;
	static double X1, X2, X3, Y1, Y2, Y3;
	
	// --------------------------------------------------------------------------------------------
	// helpers for the plotting
	// --------------------------------------------------------------------------------------------
	static private List<Integer> curves = new ArrayList<Integer>();	   // saves coefficients
	static private List<Integer> finitePts = new ArrayList<Integer>(); // saves finite order points
	
	static boolean check = false; 							// helps with plotting around the roots
	static int ticks = 4;									// # of ticks with labeling
	static int factor = 2;									// # of ticks in between w/o labeling
	static int numTicks = ticks*factor;						// total # of ticks
	static boolean triggerL = false							// both triggerL and triggerR need
				 , triggerR = false							// to be true in order to draw lines
				 , doNL = true;							 	// do Nagell-Lutz theorem?
	
	/**
	 * Constructor
	 * @param vX range for x
	 * @param vY range for y
	 * @param dX x-dimension of the window
	 * @param dY y-dimension of the window
	 */
	public ECplot(double vX, double vY, int dX, int dY)
	{
		dimX = dX;
		dimY = dY;
		maxValueX = vX;
		maxValueY = Math.abs(vY);
		dist = vX / 300;
		setPreferredSize(new Dimension(dimX, dimY));
	}
	
	/**
	 * Creates the frame and all its contents
	 * @param args
	 */
	public static void main(String[] args)
	{
		// ----------------------------------------------------------------------------------------
		// Display the equation of the EC
		// ----------------------------------------------------------------------------------------
		EC.setText(EC());
		EC.setPreferredSize(new Dimension(150,20));
		EC.setHorizontalTextPosition(JLabel.CENTER);
		
		scroll.setPreferredSize(new Dimension(100, 30));

		// ----------------------------------------------------------------------------------------
		// Set up the Window (title, size)
		// ----------------------------------------------------------------------------------------
		JFrame frame = new JFrame("Elliptic Curves Demo");
		frame.setSize(dimX, dimY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// ----------------------------------------------------------------------------------------
		// Create the plot
		// ----------------------------------------------------------------------------------------
		refreshPlot();
		
		// ----------------------------------------------------------------------------------------
		// Create panels for the left side
		// panel2 contains buttons to manipulate the view and spinners to manipulate the EC 
		// ----------------------------------------------------------------------------------------
		Panel panel2 = new Panel();
		Panel panel3 = new Panel();

		panel2.setLayout(new GridLayout(15,2));
		panel3.setLayout(new GridLayout(4,1));
		
		// ----------------------------------------------------------------------------------------
		// Bunch of buttons, each one needs an ActionListener
		// ----------------------------------------------------------------------------------------

		JButton b1 = new JButton("zoom in");   JButton b2 = new JButton("zoom out");
		JButton b3 = new JButton("stretch x"); JButton b4 = new JButton("shrink x");
		JButton b5 = new JButton("stretch y"); JButton b6 = new JButton("shrink y");
		
		JButton b7 = new JButton("save last curve"); JButton b8 = new JButton("delete last curve");
		
		b1.addActionListener(new ActionListener() {		 	// zoom in
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
                valueX /= 1.1; valueY /= 1.1;                
                refreshPlot();
            }
        });
		b2.addActionListener(new ActionListener() {			// zoom out
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
            	valueX *= 1.1; valueY *= 1.1;
            	refreshPlot();
            }
        });
		b3.addActionListener(new ActionListener() {			// stretch in x-direction
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
                valueX /= 1.1;
                refreshPlot();
            }
        });
		b4.addActionListener(new ActionListener() {			// shrink in x-direction
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
            	valueX *= 1.1;
            	refreshPlot();
            }
        });
		b5.addActionListener(new ActionListener() {		 	// strech in y-direction
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
                valueY /= 1.1;                
                refreshPlot();
            }
        });
		b6.addActionListener(new ActionListener() {			// shrink in y-direction
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
            	valueY *= 1.1;
            	refreshPlot();
            }
        });
		
		b7.addActionListener(new ActionListener() {			// save last curve
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
            	curves.add(0, c); curves.add(0, b); curves.add(0, a);
            	refreshPlot();
            }
        });
		b8.addActionListener(new ActionListener() {			// delete last curve
            public void actionPerformed(ActionEvent e) {
            	doNL = false;
            	if(!curves.isEmpty())
            	{
            		for(int i = 0; i < 3; i++)
            			curves.remove(0);
            	}  
            	refreshPlot();
            }
        });
		
		
		// ----------------------------------------------------------------------------------------
		// Similar to the buttons, just with spinners
		// ----------------------------------------------------------------------------------------
		
		SpinnerModel spinnerModelA = new SpinnerNumberModel(a, -1000, 1000, 1);
		SpinnerModel spinnerModelB = new SpinnerNumberModel(b, -1000, 1000, 1);
		SpinnerModel spinnerModelC = new SpinnerNumberModel(c, -1000, 1000, 1);
		
		final JSpinner spinnerA = new JSpinner(spinnerModelA);
		final JSpinner spinnerB = new JSpinner(spinnerModelB);
		final JSpinner spinnerC = new JSpinner(spinnerModelC);
		
		spinnerA.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
            	doNL = true;
				int newA = (int)spinnerA.getValue();
				a = (newA == 0? -a : newA);				
				spinnerA.setValue(new Integer(a));
				if(a != 1) area.setText("");
				refreshPlot();
			}
		});		
		spinnerB.addChangeListener(new ChangeListener()	{
			public void stateChanged(ChangeEvent e) {
            	doNL = true;
				b = (int) spinnerB.getValue();
				refreshPlot();
			}
		});		
		spinnerC.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
            	doNL = true;
				c = (int) spinnerC.getValue();
				refreshPlot();
			}
		});
		
		// ----------------------------------------------------------------------------------------
		// Give the spinners some labels
		// ----------------------------------------------------------------------------------------
		
		JLabel A = new JLabel("           A:");
		JLabel B = new JLabel("           B:");
		JLabel C = new JLabel("           C:");

		// ----------------------------------------------------------------------------------------
		// Filler labels to make the buttons smaller (friggin hate dem big buttons)
		// Then add all the buttons, spinners, and dummies to the upper panel
		// and everything else to the lower panel
		// ----------------------------------------------------------------------------------------
		JLabel[] dummies = new JLabel[16];
		for(int i = 0; i < 16; i++)
			dummies[i] = new JLabel(" ");
		
		panel2.add(dummies[0]); panel2.add(dummies[1]);
		panel2.add(b1);
		panel2.add(b2);
		panel2.add(dummies[2]); panel2.add(dummies[3]);
		panel2.add(b3);
		panel2.add(b4);
		panel2.add(dummies[4]); panel2.add(dummies[5]);
		panel2.add(b5);
		panel2.add(b6);
		panel2.add(dummies[6]); panel2.add(dummies[7]);
		panel2.add(A);
		panel2.add(spinnerA);
		panel2.add(dummies[8]); panel2.add(dummies[9]);
		panel2.add(B);
		panel2.add(spinnerB);
		panel2.add(dummies[10]); panel2.add(dummies[11]);
		panel2.add(C);
		panel2.add(spinnerC);
		panel2.add(dummies[12]); panel2.add(dummies[13]);
		
		panel3.add(EC);
		panel3.add(scroll);
		panel3.add(b7); panel3.add(b8);
		
		// ----------------------------------------------------------------------------------------
		// Split the Frame into 3 parts, left and right - and for the left side, up and down
		// ----------------------------------------------------------------------------------------		
		splitPaneUD.setTopComponent(panel2);
		splitPaneUD.setBottomComponent(panel3);
		
		splitPaneLR.setLeftComponent(splitPaneUD);
		
		// ----------------------------------------------------------------------------------------
		// Finally, pack everything into the frame
		// ----------------------------------------------------------------------------------------
		frame.getContentPane().add(splitPaneLR);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Converts the coefficients to a String representing the EC
	 * @return EC in form of a String
	 */
	static String EC()
	{
		String s = "";
		int x = 17;
		for(int i = a; i != 0; i/= 10)
			x--;		
		for(int i = b; i != 0; i/= 10)
			x--;		
		for(int i = c; i != 0; i/= 10)
			x--;		
		for(int i = 0; i < x; i++)
			s += " ";
		
		s += "y² = ";
		if(a != 0)
			s += (a == -1?"-":"") + (a*a == 1?"":Integer.toString(a)) + "x³ ";
		if(b != 0)
			s += (b < 0? "- ": "+ ") + (b*b == 1?"":Integer.toString(Math.abs(b))) + "x ";
		if(c != 0)
			s += (c < 0? "- ": "+ ") + Integer.toString(Math.abs(c));
		return s;
	}
	
	/**
	 * draws axes, labels them, and plots the graph
	 */
	public void paintComponent(Graphics g)
	{
		// ----------------------------------------------------------------------------------------
		// draw the x and y axes
		// ----------------------------------------------------------------------------------------
		g.drawLine(dimX/2, 0, dimX/2, dimY); // y-axis
		g.drawLine(0, dimY/2, dimX, dimY/2); // x-axis
		
		// ----------------------------------------------------------------------------------------
		// draw the ticks on both axes and label them
		// ----------------------------------------------------------------------------------------
		double[] tickX = getTicks(maxValueX);				// tick values for x-axis
		double[] tickY = getTicks(maxValueY);				// tick values for y-axis
		
		int x1 = dimX/(2*(numTicks+1)), x2 = dimY/2;		// coord for first tick on the x-axis
		int y1 = dimY/(2*(numTicks+1)), y2 = dimX/2;		// coord for first tick on the y-axis
		g.setFont(new Font("Sansserif", Font.PLAIN, dimX/60));
		
		for(int i = 0; i < 2*numTicks + 1; i++)
		{
			if(i != numTicks)								// don't make ticks at the origin
			{
				g.drawLine(x1, x2+4, x1, x2-4);
				g.drawLine(y2+4, y1, y2-4, y1);
				
				// --------------------------------------------------------------------------------
				// label the ticks, leave (factor-1) spaces in between
				// --------------------------------------------------------------------------------
				if(i % factor == 0)
				{
					if(i > numTicks)
					{
						g.drawString( tickX[i] + "", x1-dimX/60, x2+dimX/40);
						g.drawString(-tickY[i] + "", y2-dimX/22, y1+dimY/100);
					}
					else
					{
						g.drawString( tickX[i] + "", x1-dimX/60, x2+dimX/40);
						g.drawString(-tickY[i] + "", y2-dimX/25, y1+dimY/100);
					}
				}
			}

			x1 += dimX/(2*(numTicks+1));
			y1 += dimY/(2*(numTicks+1));
		}
		
		// ----------------------------------------------------------------------------------------
		// plot the graph
		// if leading coefficient is > 0, start from the right - otherwise, start from the left
		// ----------------------------------------------------------------------------------------
		g.setColor(Color.BLUE);
		
		int sign = (a > 0? 1 : -1);
		
		// plot the curve
		double xCoord = -sign * maxValueX, yCoord = 0;
		
		check = false;
		do
		{
			double xOld = xCoord, yOld = yCoord;
			
			xCoord += sign * dist;
			double temp = a * (xCoord * xCoord * xCoord) + b * xCoord + c;
			
			// make temp positive
			if(temp < 0)
			{
				if(check)
				{
					xCoord -= sign * dist;
					yCoord = Math.sqrt(a * (xCoord * xCoord * xCoord) + b * xCoord + c);
					
					x1 = x2 = valueToCoord(xCoord, true);
					y1 = y2 = valueToCoord(yCoord, false);
					
					g.drawLine(x1, y1, x2, dimY-y2);
				}
				while(temp < 0)
				{
					xCoord += sign * dist;
					temp = a * (xCoord * xCoord * xCoord) + b * xCoord + c;
				}
				xCoord += sign * dist/100;
				yCoord = Math.sqrt(a * (xCoord * xCoord * xCoord) + b * xCoord + c);
				
				x1 = x2 = valueToCoord(xCoord, true);
				y1 = y2 = valueToCoord(yCoord, false);	
				
				g.drawLine(x1, y1, x2, dimY-y2);	
				
				check = true;
			}
			// draw partial lines of the graph
			// since the graph is symmetric, do the same thing on the bottom as on the top
			else
			{
				yCoord = Math.sqrt(temp);
				
				x1 = valueToCoord(xOld,   true);
				x2 = valueToCoord(xCoord, true);
				y1 = valueToCoord(yOld,   false);
				y2 = valueToCoord(yCoord, false); 
				
				g.drawLine(x1, y1, x2, y2);					// upper part
				g.drawLine(x1, dimY-y1, x2, dimY-y2);		// lower part
			}
			
		}while(sign * xCoord < maxValueX);
		
		// ----------------------------------------------------------------------------------------
		// plot circles and lines
		// ----------------------------------------------------------------------------------------
		double x, y, d; int yPos1 = 0, yPos2 = 0, xPos1 = 0, xPos2 = 0; boolean check = true;
		
		// draw the point generated by the LMB
		x = points[0];										// x-value for point
		d = a*x*x*x+b*x+c;									// determinant
		if(d < 0)											// can't plot complex numbers
			check = false;
		else
		{
			xPos1 = valueToCoord(x, true);					// convert x-value into x-coord
			y = Math.sqrt(d);
			yPos1 = valueToCoord(y, false);					// convert y-value into y-coord
			if(points[1] < 0) yPos1 = dimY - yPos1;			// possibly negate the y-coord
			g.fillOval(xPos1 - 5, yPos1 - 5, 10, 10);		// draw a circle around the LMB point
		}
		
		// draw the point generated by the RMB
		x = points[2];										// x-value for point
		d = a*x*x*x+b*x+c;									// determinant
		if(d < 0)											// can't plot complex numbers
			check = false;
		else
		{
			xPos2 = valueToCoord(x, true); 					// convert x-value into x-coord
			y = Math.sqrt(d);
			yPos2 = valueToCoord(y, false);					// convert y-value into y-coord
			if(points[3] < 0) yPos2 = dimY - yPos2;			// possibly negate the y-coord
			g.fillOval(xPos2 - 5, yPos2 - 5, 10, 10);		// draw a circle around the RMB point
		}
		
		if(triggerL && triggerR && check)					// both points need to be on the curve
		{			
			// compute the new point algebraically
			X1 = points[0];	Y1 = Math.sqrt(a*X1*X1*X1+b*X1+c); if(points[1] < 0) Y1 = -Y1;
			X2 = points[2];	Y2 = Math.sqrt(a*X2*X2*X2+b*X2+c); if(points[3] < 0) Y2 = -Y2;
			
			double[] p = add(X1, Y1, X2, Y2);
			X3 = p[0]; Y3 = -p[1];
			
			// when you zoom out far enough, there will be problems displaying the line thru
			// P1=(X1,X2) and P2=(X2,Y2), so instead of using X2=xPos2 and Y2 = dimY - yPos2,
			// rather draw a line thru P1 and P3=(X3,Y3).
			X1 = xPos1; Y1 = dimY - yPos1;
			X2 = xPos2; Y2 = dimY - yPos2;
			if(Math.abs(X3-X2) > Math.abs(X3-X1))
			{
				X2 = valueToCoord(X3, true); 
				Y2 = dimY - valueToCoord(Y3, false);
			}
			else
			{
				X1 = valueToCoord(X3, true); 
				Y1 = dimY - valueToCoord(Y3, false);
			}
			
			double y0 = Y1 - X1 * (Y2 - Y1)/(X2 - X1);		// interceptions with y-boundaries
			double yF = Y1 + (dimX - X1) * (Y2 - Y1)/(X2 - X1);
			
			if(y0 > 0 && y0 < dimY)							// the interception with the left
			{												// y-boundary is within the bounds,
				x1 = 0;										// i.e. 0 < y0 < dimY
				y1 = (int) (dimY - y0);
			}
			else
			{
				if(y0 <= 0)									// interception is too low
				{											// solve for x
					x1 = (int) ((-Y1 + X1*(Y2 - Y1)/(X2 - X1))*(X2 - X1)/(Y2 - Y1));
					y1 = dimY;
				}
				else										// interception is too high
				{											// solve for x
					x1 = (int)((dimY-Y1 + X1*(Y2 - Y1)/(X2 - X1))*(X2 - X1)/(Y2 - Y1));
					y1 = 0;
				}
			}
			
			if(yF > 0 && yF < dimY)							// the interception with the right
			{												// y-boundary is within the bounds,
				x2 = dimX;									// i.e. 0 < yF < dimY
				y2 = (int) (dimY - yF);
			}
			else
			{
				if(yF <= 0)									// interception is too low	
				{											// solve for x
					x2 = (int) ((-Y1 + X1*(Y2 - Y1)/(X2 - X1))*(X2 - X1)/(Y2 - Y1));
					y2 = dimY;
				}
				else										// interception is too high
				{											// solve for x
					x2 = (int) ((dimY-Y1 + X1*(Y2 - Y1)/(X2 - X1))*(X2 - X1)/(Y2 - Y1));
					y2 = 0;
				}
			}
			
			// draw an infinite line thru P1=(X1, Y1) and P2=(X2, Y2)
			g.setColor(Color.GRAY);
			g.drawLine(x1, y1, x2, y2);
			
			// now draw a vertical line at the intersection point
			xPos1 = valueToCoord(X3, true);					// x-coordinate of P1+P2
			yPos1 = valueToCoord(Y3, false);				// y-coordinate of P1+P2
				
			xPos2 = xPos1;									// x-coordinate of intersection
			yPos2 = dimY - yPos1;							// y-coordinate of intersection
			
			x1 = x2 = xPos1;								// coordinates to draw the line
			y1 = 0; y2 = dimY;								// thru those two points
			
			g.setColor(Color.GRAY);	g.drawLine(x1, y1, x2, y2);	
			g.setColor(Color.CYAN);	g.fillOval(xPos1 - 5, yPos1 - 5, 10, 10);
			g.setColor(Color.BLUE); g.fillOval(xPos2 - 5, yPos2-5, 10, 10);
		}
		// ----------------------------------------------------------------------------------------
		// plot saved functions
		// ----------------------------------------------------------------------------------------
		for(int i = 0; i < curves.size() / 3; i++)
		{
			int a = curves.get(i*3 + 0);
			int b = curves.get(i*3 + 1);
			int c = curves.get(i*3 + 2);
			
			g.setColor(Color.BLUE);
			
			sign = (a > 0? 1 : -1);
			
			// plot the curve
			xCoord = -sign * maxValueX; yCoord = 0;
			
			check = false;
			do
			{
				double xOld = xCoord, yOld = yCoord;
				
				xCoord += sign * dist;
				double temp = a * (xCoord * xCoord * xCoord) + b * xCoord + c;
				
				// make temp positive
				if(temp < 0)
				{
					if(check)
					{
						xCoord -= sign * dist;
						yCoord = Math.sqrt(a * (xCoord * xCoord * xCoord) + b * xCoord + c);
						
						x1 = x2 = valueToCoord(xCoord, true);
						y1 = y2 = valueToCoord(yCoord, false);	
						
						g.drawLine(x1, y1, x2, dimY-y2);
					}
					while(temp < 0)
					{
						xCoord += sign * dist;
						temp = a * (xCoord * xCoord * xCoord) + b * xCoord + c;
					}
					xCoord += sign * dist/100;
					yCoord = Math.sqrt(a * (xCoord * xCoord * xCoord) + b * xCoord + c);
					
					x1 = x2 = valueToCoord(xCoord, true);
					y1 = y2 = valueToCoord(yCoord, false);	
					
					g.drawLine(x1, y1, x2, dimY-y2);	
					
					check = true;
				}
				// draw partial lines of the graph
				// since the graph is symmetric, do the same thing on the bottom as on the top
				else
				{
					yCoord = Math.sqrt(temp);
					
					x1 = valueToCoord(xOld,   true);
					x2 = valueToCoord(xCoord, true);
					y1 = valueToCoord(yOld,   false); 
					y2 = valueToCoord(yCoord, false); 
					
					g.drawLine(x1, y1, x2, y2);					// upper part
					g.drawLine(x1, dimY-y1, x2, dimY-y2);		// lower part
				}
				
			}while(sign * xCoord < maxValueX);
		}
		
		// ----------------------------------------------------------------------------------------
		// plot points of finite order
		// ----------------------------------------------------------------------------------------
		for(int i = 0; i*2 < finitePts.size(); i++)
		{
			double X = finitePts.get(2 * i + 0);
			double Y = finitePts.get(2 * i + 1);
			
			x1 = valueToCoord(X, true); x2 = x1;
			y1 = valueToCoord(Y, false); y2 = dimY - y1;
			
			g.setColor(Color.BLACK);
			g.fillOval(x1 - 4, y1 - 4, 8, 8);
			g.fillOval(x2 - 4, y2 - 4, 8, 8);
		}
	}
	
	/**
	 * finds the values of the ticks on the axis 
	 * @param value (half of the) length of the axis
	 * @return Returns an array with the values for all ticks to be displayed
	 */
	private static double[] getTicks(double value)
	{
		double increment = value / (numTicks + 1), currentTick = -1*(value);
		double[] tick = new double[2*numTicks + 1];
		for(int i = 0; i < 2*numTicks + 1; i++)
		{
			currentTick += increment;
			tick[i] = Math.round(currentTick*100.0)/100.0;
		}
		return tick;
	}
	
	/**
	 * Renews the plot. This is usually called when a coefficient is changed or
	 * a point is added on the curve for point addition. This also calls the
	 * NagellLutz-function to display points of finite order. 
	 * @return
	 */
	private static ECplot refreshPlot()
	{
		if(doNL) NagellLutz();
		ECplot plot = new ECplot(valueX, valueY, dimX, dimY);
		plot.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent arg0) { }
			public void mouseClicked(MouseEvent arg0)  { }
			public void mouseEntered(MouseEvent arg0)  { }
			public void mouseExited(MouseEvent arg0)   { }
			public void mousePressed(MouseEvent e) 
			{
				doNL = false;
				double x = coordToValue(e.getX(), true);
				double y = coordToValue(e.getY(), false);
				
				if(e.getButton() == MouseEvent.BUTTON1)		// adds point #1 when LMB is pressed
				{
					triggerL = true;						// LMB needs to be pressed at least
					points[0] = x;							// once before a line can be drawn.
					points[1] = (y > 0? 1 : -1);			// save x-value and whether to draw
				}											// above or below the y-axis.
				
				if(e.getButton() == MouseEvent.BUTTON3)		// adds point #2 when RMB is pressed
				{
					triggerR = true;						// RMB needs to be pressed at least
					points[2] = x;							// once before a line can be drawn.
					points[3] = (y > 0? 1 : -1);			// save x-value and whether to draw
				}											// above or below the y-axis.
				refreshPlot();				
			}
		});
		splitPaneLR.setRightComponent(plot);
		EC.setText(EC());
		return plot;
	}
	
	/**
	 * calculates all points of finite order using the Nagell-Lutz Theorem
	 */
	private static void NagellLutz()
	{
		finitePts.clear();
		if(c == 0)
		{
			// y² = ax³ + bx = x(ax² + b), so x = 0 is a root
			finitePts.add(0); finitePts.add(0);
			
			// the other root is when x² = -b/a
			double x = -b/a;
			if(isSquare(x))
			{
				finitePts.add(+(int)Math.sqrt(x)); finitePts.add(0);
				finitePts.add(-(int)Math.sqrt(x)); finitePts.add(0);
			}
		}
		if(a == 1)
		{
			String s = "";
			int D = - 4*b*b*b - 27*c*c; 						// discriminant
			List<Integer> divisors = new ArrayList<Integer>();	// find all divisors of D
			
			for(int i = 1; i <= Math.abs(D) / 2; i++)
			{
				if(D % i == 0)
				{
					divisors.add(new Integer(i));
				}
			}
			divisors.add(new Integer(D));			
			divisors.add(new Integer(0));
			
			for(Integer i : divisors)
			{
				// find integer solutions for
				//    i² = x³ + ax² + bx + c
				// <=> 0 = x³ + ax² + bx + (c - i²)
				int constantTerm = c - i*i;
				
				// get the divisors of the constant term
				List<Integer> list = new ArrayList<Integer>();
				for(int j = 1; j <= Math.abs(constantTerm); j++)
				{
					if(constantTerm % j == 0)
					{
						list.add(new Integer(j));
						list.add(new Integer(-j));
					}
				}
				
				// plug all divisors j in for x:
				// 0 = j³ + aj² + bj + c - i²
				for(Integer j : list)
				{
					int check = j*j*j + b*j + constantTerm;
					
					if(check == 0)
					{
						if(i != 0)
							s += ("(" + j + ", " + i + "), " + "(" + j + ", " + (-i) + ")\n");
						else
							s += ("(" + j + ", 0)\n");
						finitePts.add(j); finitePts.add(i);
					}
				}
			}
			if(c == 0)
			{
				// y² = ax³ + bx = x(ax² + b), so x = 0 is a root
				finitePts.add(0); finitePts.add(0);
				
				// the other root is when x² = -b/a
				double x = -b/a;
				if(isSquare(x))
				{
					int j = (int)Math.sqrt(x);
					s += "(0,0)\n" + "(" + j + ",0)\n" + "(" + (-j) + ",0)\n";
				}
			}
			area.setText(s);
		}
	}
	
	/**
	 * Adds two points on the elliptic curve
	 * @param x1 x coordinate of the first point
	 * @param y1 y coordinate of the first point
	 * @param x2 x coordinate of the second point
	 * @param y2 y coordinate of the second point
	 * @return Returns the sum of the two given points
	 */
	static double[] add(double x1, double y1, double x2, double y2)
	{
		double[] result = new double[2];
		
		if(x1 != x2)
		{
			double L = (y2 - y1)/(x2 - x1);
			double V = y1 - L*x1;
			result[0]  = L*L - x1 - x2;
			result[1]  = L*result[0] + V;
		}
		else
		{
			if(y1 == y2)
			{
				double x = x1, y = y1;
				double L = (3*x*x + b)/(2*y);
				result[0] = L*L - 2*x;
				result[1] = y + L*(result[0] - x);
			}
			else
			{
				result[0] = 0; result [1] = 0;
			}			
		}
		result[1] *= -1;
		return result;
	}
	
	/**
	 * I don't know why this doesn't work well
	 * @param x the x-coordinate of the point
	 * @return Returns the order of the given point
	 */
	static int order(double x)
	{
		int result = 1;
		
		double y = (int) Math.sqrt(x*x*x + b*x + c);
		if(y == 0) return 2;
		
		double[] points = {x, y};
		do 
		{
			points = add(points[0], points[1], x, y);
			result++;
		} while(!(points[0] == x && points[1] == y) && result < 10);
		
		return result;
	}
	
	/**
	 * small helper function to convert a value to its corresponding x- or y-coordinate
	 * @param d value to be converted
	 * @param x true if this is an x-value, false if this a y-value
	 * @return returns the corresponding x- or y-coordinate
	 */
	static int valueToCoord(double d, boolean x)
	{
		if(x)	return (int) (dimX/2 + d*dimX / (2*maxValueX));
		else	return (int) (dimY/2 + d*dimY / (2*maxValueY));
	}
	
	/**
	 * small helper function to convert a coordinate to its corresponding x- or y-value
	 * @param i coordinate to be converted
	 * @param x true if this is an x-coordinate, false if this a y-coordinate
	 * @return returns the corresponding x- or y-value
	 */
	static double coordToValue(int i, boolean x)
	{
		if(x) 	return (i - dimX/2) * 2 * maxValueX / dimX;
		else	return (i - dimY/2) * 2 * maxValueY / dimY;
	}
	
	/**
	 * determines whether a given number is square
	 * @param d possible square number
	 * @return true if d is square, false otherwise
	 */
	static boolean isSquare(double d)
	{
		if(d > (int) d) // only check for whole integers
			return false;

		for(int i = 0; i <= d; i++)
		{
			if(i*i == d)	return true;
			if(i*i > d)		return false;
		}
		return false;
	}
}