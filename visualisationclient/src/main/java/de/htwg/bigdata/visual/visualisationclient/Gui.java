package de.htwg.bigdata.visual.visualisationclient;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import de.htwg.bigdata.visual.visualisationclient.heatmap.Gradient;
import de.htwg.bigdata.visual.visualisationclient.heatmap.HeatMap;

public class Gui extends JFrame {
	
	private HeatMap heatMapPanel;	
	private Controller controller;
	private final boolean useGraphicsYAxis = true;
	private final int defaultFieldSize = 500;
	SimulationLoader simulationLoader;
	SimulationPlayer simulationPlayer;
	private final JTextField state;
	
	public Gui(final Controller controller) throws Exception {

		super("Ant-Simulation");	    	   
	 
		Color[] gradientWhiteToBlack = Gradient.createGradient(Color.WHITE, Color.BLACK, 100);
		
		heatMapPanel = new HeatMap(new double[defaultFieldSize][defaultFieldSize], useGraphicsYAxis, gradientWhiteToBlack);
	    heatMapPanel.setSize(defaultFieldSize, defaultFieldSize);
	    heatMapPanel.setColorForeground(Color.WHITE);
	    heatMapPanel.setColorBackground(Color.WHITE);
	    this.controller = controller;
	    	  	    
	    Container contentPane = this.getContentPane();	    	    
	    JPanel mainPanel = new JPanel();	
	    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	    JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    JPanel statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    
	    statePanel.add(new JLabel("State"));
	    state = new JTextField(40);
	    state.setText("Ready");
	    statePanel.add(state);
	    
	    paramPanel.add(new JLabel("Simulation name"));
	    final JTextField simulationName = new JTextField(20);
	    paramPanel.add(simulationName);
	   
	    final JCheckBox cbLoadFromFile = new JCheckBox("Load JSON from file", false);
	    paramPanel.add(cbLoadFromFile);
	    
	    paramPanel.add(new JLabel("Fieldsize"));
	    final JTextField fieldSize = new JTextField(10);
	    fieldSize.setText("50");
	    paramPanel.add(fieldSize);
	    
	    paramPanel.add(new JLabel("Stepsize"));
	    final JTextField stepSize = new JTextField(10);
	    stepSize.setText("100");
	    paramPanel.add(stepSize);
	    
	    paramPanel.add(new JLabel("Timefactor"));
	    final JTextField timefactor = new JTextField(10);
	    timefactor.setText("1");
	    paramPanel.add(timefactor);

	    //Go-Button
	    JButton go = new JButton("Go");
	    go.addActionListener(new ActionListener() {    	
			public void actionPerformed(ActionEvent e) {								
				simulationLoader = new SimulationLoader(
						simulationName.getText(), 
						Integer.valueOf(fieldSize.getText()), 
						Integer.valueOf(stepSize.getText()),
						Integer.valueOf(timefactor.getText()),
						cbLoadFromFile.isSelected());
				simulationLoader.execute();
													
			}    	
	    });
	    paramPanel.add(go);	
	    
	    //Stop-Button
	    JButton stop = new JButton("Stop");
	    stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (simulationPlayer != null) { simulationPlayer.cancel(true); }
				if (simulationLoader != null) { simulationLoader.cancel(true); }
				state.setText("Ready");
			}
	    	
	    });
	    paramPanel.add(stop);
	    
	    //Play-Button
	    JButton play = new JButton("Play");
	    play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				simulationPlayer = new SimulationPlayer(Math.round(controller.getStepSize() / Integer.valueOf(timefactor.getText())));
				simulationPlayer.execute();
			}	    	
	    });
	    paramPanel.add(play);
	    
	    //Prev-Button
	    JButton prev = new JButton("Previous Step");
	    prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!simulationPlayer.isCancelled()) { return; }
				heatMapPanel.updateData(controller.getPrevStep().getFields(), useGraphicsYAxis);
			}	    	
	    });
	    paramPanel.add(prev);
	    
	    //Next-Button
	    JButton next = new JButton("Next Step");
	    next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!simulationPlayer.isCancelled()) { return; }
				heatMapPanel.updateData(controller.getNextStep().getFields(), useGraphicsYAxis);
			}	    	
	    });
	    paramPanel.add(next);
	    
	    //Play from begin-Button
	    JButton playFromBegin = new JButton("Play from begin");
	    playFromBegin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.resetSimulation();
				simulationPlayer = new SimulationPlayer(Math.round(controller.getStepSize() / Integer.valueOf(timefactor.getText())));
				simulationPlayer.execute();
			}	    	
	    });
	    paramPanel.add(playFromBegin);	
	    
	    paramPanel.add(new JLabel("RGB start from"));
	    final JTextField rgbStartFrom = new JTextField(5);
	    rgbStartFrom.setText("200");
	    paramPanel.add(rgbStartFrom);
	    
	    paramPanel.add(new JLabel("RGB stepsize"));
	    final JTextField rgbStepSize = new JTextField(5);
	    rgbStepSize.setText("30");
	    paramPanel.add(rgbStepSize);
	    
	    //Apply-RGB-Button
	    JButton applyRGB = new JButton("Apply RGB");
	    applyRGB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				heatMapPanel.updateGradient(Gradient.createSpecialGradient(
						Integer.valueOf(rgbStartFrom.getText()), 
						Integer.valueOf(rgbStepSize.getText())
				));
			}	    	
	    });
	    paramPanel.add(applyRGB);	    


	    
	    
	    mainPanel.add(paramPanel);	 
	    mainPanel.add(statePanel);
	    mainPanel.add(heatMapPanel);	   
	    contentPane.add(mainPanel);
	   
	}
	

	
	// this function will be run from the EDT
	    public static void createAndShowGUI(Controller controller) throws Exception {
	        Gui hmf = new Gui(controller);
	        hmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        hmf.setSize(1920,1080);
	        hmf.setVisible(true);
	    }
	    
	    
	    class SimulationLoader extends SwingWorker<Void,Void> {
	    	private String simulationName;
	    	private int fieldSize;
	    	private int stepSize;
	    	private int timeFactor;
	    	private boolean loadFromFile;
	    	
	    	public SimulationLoader(String simulationName, int fieldSize, int stepSize, int timeFactor, boolean loadFromFile) {
	    		this.simulationName = simulationName;
	    		this.fieldSize = fieldSize;
	    		this.stepSize = stepSize;
	    		this.timeFactor = timeFactor;
	    		this.loadFromFile = loadFromFile;
	    	}
	    	
			@Override
			protected Void doInBackground() throws Exception {				
				state.setText("Load simulation data...");
				controller.loadSimulationData(simulationName, fieldSize, stepSize, loadFromFile);	
				
				if (fieldSize < defaultFieldSize) {
					heatMapPanel.setSize(defaultFieldSize, defaultFieldSize);
				} else {
					heatMapPanel.setSize(fieldSize, fieldSize);
				}
				
				simulationPlayer = new SimulationPlayer(Math.round(stepSize / timeFactor));
				simulationPlayer.execute();
				
				return null;
			}
	    }
	    
		class SimulationPlayer extends SwingWorker<Void, Void> {
			private int stepSize;
			
			public SimulationPlayer(int stepSize) {
				this.stepSize = stepSize;				
			}
			
			
			@Override
			protected Void doInBackground() throws Exception {
				state.setText("Start simulation replay...");
				SimulationStep step = controller.getNextStep();
				while (step != null) {
					heatMapPanel.updateData(step.getFields(), useGraphicsYAxis);
					Thread.sleep(stepSize);
					step = controller.getNextStep();
				}							
				return null;
			}
			
		}

	}

