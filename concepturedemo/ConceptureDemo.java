package concepturedemo;

import concepture.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;

public class ConceptureDemo extends Frame
implements MouseListener, MouseMotionListener, WindowListener
{		
	static final long serialVersionUID = 12345;

	Concepture concept;		
	Answer prediction;
	
	boolean playMode = false;
	boolean testMode = false;
	boolean newGestureMode = false;
	boolean correctionMode = false;
	boolean queryMode = false;
	boolean corrected = false;
	
	int numEntered = 0;
	int fileCounter = 0;
	int mistakeCounter = 0;
	int testCounter = 0;
	
	long totalTime = 0;
	
	Vector<String> testSet = new Vector<String>();
	Vector<String> predictions = new Vector<String>();

	String sessionName = null; 
	String imagePrefix = null;
	String label = null;
	
	String[] commands = {"Recognize", "Train", "Report"};
	
	Vector<Stats> statistics;
	Stats st;
	
	FlatButton yellowPoint;
	FlatButton pinkPoint;
	FlatButton orangePoint;
	
	MenuBar mbar;
	
	private String getFileName()
	{
		final JFileChooser fc = new JFileChooser(new File("./")); 
		int returnVal = fc.showOpenDialog(null); 
		
		if(returnVal == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile().getAbsolutePath();
		
		return null;
	}
	
	ActionListener correct = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { switchCorrectMode(); }};
	
	ActionListener editGestures = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { deleteGesture(); }}; 
	
	ActionListener newSketch = new ActionListener()
	{ public void actionPerformed(ActionEvent e) 
	{ 
		playMode = true; 
		concept.reInitialize();
	}}; 
	
	ActionListener newTest = new ActionListener()
	{ public void actionPerformed(ActionEvent e) 
	{ 
		String filename = getFileName();
		if(filename == null)
			return;
		
		try
		{
		    BufferedReader in = new BufferedReader(new FileReader(filename));
		    String str;
		    do
		    {
		    	str = in.readLine();
		    	testSet.add(str);
		    }while(str != null);
		} 
		catch(IOException ioe) {}
		
		System.out.println(testSet.elementAt(testCounter));
		
		totalTime = 0;
	
		testMode = true; 
		concept.reInitialize();
	}}; 
	
	ActionListener quitTest = new ActionListener()
	{ public void actionPerformed(ActionEvent e) 
	{ 
		String filename = getFileName();
		if(filename == null)
			return;
		
		try
		{
		    BufferedWriter out = new BufferedWriter(new FileWriter(filename));		    
		    for(int i=0; i<predictions.size(); i++)
		    {
		    	out.write(testSet.elementAt(i) +" "+ predictions.elementAt(i));
		    	out.newLine();
		    }		 
		    out.close();
		} 
		catch(IOException ioe) {}
		
		System.out.println("Total time: " + totalTime);
		System.out.println("Elapsed time: " + concept.getElapsedTime());
		totalTime = 0;
		testMode = false; 
		concept.reInitialize();
	}}; 
	
	ActionListener quitSketch = new ActionListener()
	{ public void actionPerformed(ActionEvent e) 
	{ 
		playMode = false; 
		concept.reInitialize();		
	}}; 
	
	ActionListener loadGestures = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { readGestures(); }}; 
	
	ActionListener loadImage = new ActionListener()
	{ public void actionPerformed(ActionEvent e) 
	{ 
		concept.loadBackgroundImage(getFileName());
	}}; 
	
	ActionListener saveGestures = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { writeGestures(false); }}; 
	
	ActionListener saveGesturesAs = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { writeGestures(true); }}; 
	
	ActionListener saveSnapshot = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { takeSnapshot(); }}; 
	
	ActionListener saveStatistics = new ActionListener()
	{ public void actionPerformed(ActionEvent e) { writeStats(); }}; 
	
	private void writeStats()
	{
		String filename = getFileName();
		if(filename == null)
			return;
		
		try
		{
		    BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		    out.write("Gesture #examples #queries #corrections #undos");
		    out.newLine();
		    
		    Enumeration<Stats> e = statistics.elements();
		    while(e.hasMoreElements())
		    {
		    	Stats s = e.nextElement();
		    	out.write(s.getName() +" "+ 
		    			s.getNumExamples() +" "+ 
		    			s.getNumQueries() +" "+
		    			s.getNumCorrections() +" "+
		    			s.getNumUndos());
		    	out.newLine();
		    }
		    
		    out.close();
		} 
		catch(IOException e) {}
	}
	
	private void takeSnapshot()
	{
		BufferedImage offScreen = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics temp = offScreen.getGraphics();
		temp.setColor(getBackground());
		temp.fillRect(0, 0, getWidth(), getHeight());
		temp.setColor(getForeground());				
		paint(temp);				
		
		if(imagePrefix == null)
		{
			imagePrefix = JOptionPane.showInputDialog("Please enter a prefix for images:");
			if(imagePrefix == null)
				return;		// the user has cancelled						
		}
			
		File outputfile = new File("./snapshots/" + imagePrefix + fileCounter + ".png");
		try
		{
			ImageIO.write(offScreen, "png", outputfile);
			fileCounter += 1;
		}
		catch(IOException ioe)
		{
			JOptionPane.showMessageDialog(null, "Could not take a snapshot!");			          
		}
	}
	
	private void learnGesture(boolean finished)
	{
		st.updateNumExamples(numEntered);	
	
		if(!finished)
			concept.registerGesture();
				
		newGestureMode = false;
		queryMode = false;
		System.out.println(new Date());
		changeCommands("Recognize", "Train", "Report");
		statistics.add(st);
		st = null;
	}
	
	public boolean askQuery(boolean answer)
	{
		try
		{
			if(concept.learnGesture(answer))							
				return true;	// the language is learned				
			st.updateNumQueries(1);						
		}
		catch(RuntimeException re)
		{
			System.out.println(re);
			JOptionPane.showMessageDialog(null, 
					"The gesture could not be learned due to incomplete examples or inconsistent answers!");
		}
		return false;
	}
	
	public Gesture.Animation chooseAnimation()
	{
		String[] loaded = {"FETCH", "DELETE", "BACK", "FORTH", 
				"ROTATE", "TWINKLE", "ANNOTATE"};
		String response = (String) JOptionPane.showInputDialog(
                null                       // Center in window.
              , "Select an action for the gesture"        // Message
              , "Choose action"               // Title in titlebar
              , JOptionPane.QUESTION_MESSAGE  // Option type
              , null                       // Icon (none)
              , loaded                    // Button text as above.
              , loaded[0]    
            );
		if(response == null)
			return Gesture.Animation.FETCH;
		
		return Gesture.Animation.valueOf(response);
	}
	
	private void deleteGesture()
	{		
		String[] loaded = concept.getGestures();
		if(loaded.length == 0)
		{
			JOptionPane.showMessageDialog(null, 
					"There are no gestures to delete!");
		}
		else
		{
			String response = (String) JOptionPane.showInputDialog(
                    null                       // Center in window.
                  , "Select the gesture to be deleted"        // Message
                  , "Delete Gesture"               // Title in titlebar
                  , JOptionPane.QUESTION_MESSAGE  // Option type
                  , null                       // Icon (none)
                  , loaded                    // Button text as above.
                  , loaded[0]    
                );
			
			if(response != null)
			{
				for(int i=0; i<loaded.length; i++)
				{
					if(loaded[i].equals(response))
						concept.deleteGesture(i);
				}						
			}
		}
	}
	
	private void startNewGesture()
	{
		String getstr = JOptionPane.showInputDialog("Please enter a name for the concepture:");
		if(getstr != null)
		{
			concept.setNewGesture(getstr);
			System.out.println("Name: " + getstr);
			newGestureMode = true;						
			numEntered = 0;		
			st = new Stats(getstr);
			
			commands[0] = "Enter";
			commands[1] = "Commit";
			commands[2] = "Undo";
			
			concept.setGestureAction( chooseAnimation() );
			
			Color newColor = JColorChooser.showDialog(null, 
					"Enter a color for the gesture " + concept.getName(),		             		              
	                 new Color(0,0,0));	
			
			if(newColor == null)
				concept.setGestureColor(new Color(0,0,0));
			else
				concept.setGestureColor(newColor);			
		}		
	}
	
	private void eraseLast()
	{
		concept.eraseLastExample();
		if(numEntered > 0)
		{
			numEntered -= 1;
			st.updateNumUndos(1);
		}	
	}
	
	private void switchCorrectMode()
	{
		if(correctionMode)				
			correctionMode = false;									
		else
		{
			if(newGestureMode)
				eraseLast();
			correctionMode = true;
			concept.setCorrecting(correctionMode);
		}
	}
	
	private void readGestures()
	{
		sessionName = getFileName();
		if(sessionName != null && !concept.loadSession(sessionName))
			JOptionPane.showMessageDialog(null, "Could not read the file: " + sessionName + " !");										
	}
	
	private void writeGestures(boolean dontCheck)
	{						
		if(dontCheck || sessionName == null)
			sessionName = getFileName();				
		if(sessionName != null && !concept.saveSession(sessionName))
			JOptionPane.showMessageDialog(null, "Could not write to the file: " + sessionName + " !");			
	}
	
	private void initializeMenu()
	{		
		mbar = new MenuBar();						
		
		Menu m = new Menu("Edit");
		//////////////////////////////////////////////////////////////////////////////////////////
		MenuItem mi = new MenuItem("Correct");
		mi.addActionListener(correct);
		m.add(mi);			
		
		mi = new MenuItem("Delete...");
		mi.addActionListener(editGestures);
		m.add(mi);				
		mbar.add(m);
		
		m = new Menu("Load");
		//////////////////////////////////////////////////////////////////////////////////////////
		mi = new MenuItem("Load gestures");
		mi.addActionListener(loadGestures);
		m.add(mi);
		mbar.add(m);
		
		mi = new MenuItem("Load image");
		mi.addActionListener(loadImage);
		m.add(mi);
		mbar.add(m);
		
		m = new Menu("Save");
		////////////////////////////////////////////////////////////////////////////////////////			
		mi = new MenuItem("Save gestures");
		mi.addActionListener(saveGestures);
		m.add(mi);
		
		mi = new MenuItem("Save gestures as");
		mi.addActionListener(saveGesturesAs);
		m.add(mi);
		
		m.addSeparator();
		
		mi = new MenuItem("Save snapshot", new MenuShortcut(KeyEvent.VK_A));
		mi.addActionListener(saveSnapshot);
		m.add(mi);		
		
		mi = new MenuItem("Save statistics");
		mi.addActionListener(saveStatistics);
		m.add(mi);		
		mbar.add(m);
		
		m = new Menu("Sketch");
		//////////////////////////////////////////////////////////////////////////////////////////
		mi = new MenuItem("New sketch");			
		mi.addActionListener(newSketch);
		m.add(mi);
		
		mi = new MenuItem("Quit sketch");			
		mi.addActionListener(quitSketch);
		m.add(mi);
		mbar.add(m);
		
		m = new Menu("Test");
		//////////////////////////////////////////////////////////////////////////////////////////
		mi = new MenuItem("New Test");			
		mi.addActionListener(newTest);
		m.add(mi);
		
		mi = new MenuItem("Quit Test");			
		mi.addActionListener(quitTest);
		m.add(mi);
		mbar.add(m);
 
		setMenuBar(mbar);
	}
	
	public ConceptureDemo(String title)
	{					
		super(title);		// calls the super class' constructor              
		setLayout(new FlowLayout());			
		
		concept = new Concepture();	
		statistics = new Vector<Stats>();
		st = null;
		
		initializeMenu();
		
		yellowPoint = new FlatButton(40, this.getHeight()/2-100, 60, 60, Color.YELLOW);
		pinkPoint = new FlatButton(40, this.getHeight()/2-20, 60, 60, Color.PINK);
		orangePoint = new FlatButton(40, this.getHeight()/2+60, 60, 60, Color.ORANGE);
	
		addMouseMotionListener(this);
		addMouseListener(this);
		addWindowListener(this);				
    }
	
	public void mouseEntered(MouseEvent e) //mouse entered canvas
	{	}

	public void mouseExited(MouseEvent e) //mouse left canvas
	{	}

	public void mouseClicked(MouseEvent e) //mouse pressed-depressed (no motion in between), if there's motion -> mouseDragged
	{			
		if(playMode)
		{
			concept.recognize();
			concept.setEntering(false);
		}
		else if(correctionMode)
		{
			concept.pointerClicked(e.getX(), e.getY());
			corrected = true;
			if(st != null)
				st.updateNumCorrections(1);
		}
		update(e);
	}
	
	private void changeCommands(String s0, String s1, String s2)
	{
		commands[0] = s0;
		commands[1] = s1;
		commands[2] = s2;
	}
	
	private void doYellow()
	{
		if(playMode)
			return;
		
		if(queryMode)
		{
			if(askQuery(true))			
				learnGesture(true);			
		}
		else if(newGestureMode)
		{
			label = concept.addExample(corrected);			
			System.out.println("Entered: " + label);
			numEntered += 1;
			prediction = null;
		}	
		else if(testMode)
		{
			long start = System.currentTimeMillis();
			prediction = concept.recognizeGesture(corrected);
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("total time: " + elapsed);
			label = null;
			if(prediction != null)
				predictions.add(prediction.getName());
			else
				predictions.add("none");
			testCounter++;
			System.out.println(testCounter +" "+ testSet.elementAt(testCounter));
		}
		else
		{
			long start = System.currentTimeMillis();
			prediction = concept.recognizeGesture(corrected);
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("total time: " + elapsed);
			label = null;
		}
		concept.setCorrecting(false);	
		concept.setEntering(false);
		corrected = false;					
	}
	
	private void doPink()
	{		
		if(playMode || testMode)		
			return;
		
		prediction = null;
		if(queryMode)
		{
			if(askQuery(false))
				learnGesture(true);
		}
		else if(newGestureMode)		
		{			
			queryMode = true;
			changeCommands("Yes", "No", "Done");
			concept.initLearning();
			if(askQuery(true))
				learnGesture(true);
		}		
		else	
		{
			System.out.println(new Date());
			changeCommands("Enter", "Commit", "Undo");
			newGestureMode = true;
			startNewGesture();			
		}
	}
	
	private void doOrange()
	{
		if(playMode || testMode)		
			return;
		
		prediction = null;
		if(queryMode)				
			learnGesture(false);					
		else if(newGestureMode)		
			eraseLast();			
		else
		{			
			mistakeCounter++;			
			System.out.println("Mistakes: " + mistakeCounter);
		}		
	}
	
	public void mousePressed(MouseEvent e)
	{			
		if(correctionMode)
			return;
		
		yellowPoint.setIfNear(e.getX(), e.getY());
		pinkPoint.setIfNear(e.getX(), e.getY());
		orangePoint.setIfNear(e.getX(), e.getY());
		concept.pointerPressed(e.getX(), e.getY());		
		update(e);
	}

	public void mouseReleased(MouseEvent e)
	{	
		if(correctionMode)
		{
			mouseClicked(e);		
			return;
		}
	
		concept.clicked = true;
		if(yellowPoint.isPressed())
			doYellow();
		else if(pinkPoint.isPressed())
			doPink();
		else if(orangePoint.isPressed())
			doOrange();
		else
			concept.clicked = false;
		
		concept.pointerReleased(e.getX(), e.getY());	
		
		yellowPoint.setPressed(false);
		pinkPoint.setPressed(false);
		orangePoint.setPressed(false);
		
		update(e);
	}

	public void mouseMoved(MouseEvent e)
	{	
		update(e);
	}

	public void mouseDragged(MouseEvent e)
	{		
		if(correctionMode || queryMode)
			return;

		concept.pointerDragged(e.getX(), e.getY());
		update(e);
	}
	
	public void update(MouseEvent e)
	{
		repaint();
		e.consume();
	}
	
	public void update(Graphics g)
	{
		Image offScreen = createImage(getSize().width, getSize().height);
		Graphics temp = offScreen.getGraphics();
		temp.setColor(getBackground());
		temp.fillRect(0, 0, getWidth(), getHeight());
		temp.setColor(getForeground());

		paint(temp);
		temp.dispose();
		g.drawImage(offScreen, 0, 0, null);
	}
	
	public void drawCommands(Graphics g)
	{	
		if(!playMode)
		{
			g.setColor(Color.black);
			yellowPoint.drawButton(g, commands[0], this.getHeight()/2-110);		
			pinkPoint.drawButton(g, commands[1], this.getHeight()/2-20);
			orangePoint.drawButton(g, commands[2], this.getHeight()/2+70);			
		}
	}

	public void paint(Graphics g)
	{
		if(playMode)
		{
			concept.render2(g);
		}
		else if(queryMode)
		{
			concept.render3(g, this.getWidth()/3, 120);
			g.setFont(new Font("Courier",Font.PLAIN,16));
			g.drawString("Is this an example of the concepture?", this.getWidth()/4, 80);	
		}
		else
		{	
			g.setFont(new Font("Courier", Font.PLAIN, 16));
			if(prediction != null)
			{
				g.drawString("Concepture: " + prediction.getName(), this.getWidth()/2-80, this.getHeight()-30);			
			}
			g.setColor(Color.black);
			concept.render(g);
		}
		drawCommands(g);
	}
	
    public void windowClosing(WindowEvent event) 
    { 
    	System.exit(0); 
    }         
    
    public void windowClosed(WindowEvent event){}
	public void windowDeiconified(WindowEvent event){}
	public void windowIconified(WindowEvent event){}
	public void windowActivated(WindowEvent event){}
	public void windowDeactivated(WindowEvent event){}
	public void windowOpened(WindowEvent event){}
	
	public static void main(String[] args)
	{
		ConceptureDemo cd = new ConceptureDemo("Conceptures");	
		cd.setSize(960, 540);
		cd.setVisible(true);		
	}
	
}


