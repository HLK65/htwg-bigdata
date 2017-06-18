package de.htwg.bigdata.visual.visualisationclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;

public class Controller {
	private Connector connector;
	private InputStreamParser parser;
	private List<SimulationStep> steps;
	private ListIterator<SimulationStep> iterator;
	private int stepSize;
	private int timeFactor;
	
	
	public Controller() {
		connector = new Connector();
	}
	
	public void loadSimulationData(String simulationName, int fieldSize, int stepSize, boolean loadFromFile) {
		this.stepSize = stepSize;
		this.timeFactor = timeFactor;
		parser = new InputStreamParser(fieldSize);
		InputStream dataStream;
		try {
			if (loadFromFile) {
				dataStream = connector.getSimulationDataFromFile(simulationName);
			} else {
				dataStream = connector.getSimulationData(simulationName, fieldSize, stepSize);
			}			
			steps = parser.getData(dataStream);
			iterator = steps.listIterator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getStepSize() {
		return stepSize;
	}
	
	public SimulationStep getNextStep() {
		if (iterator.hasNext()) { return iterator.next(); }
		return null;
	}
	
	public SimulationStep getPrevStep() {
		if (iterator.hasPrevious()) { return iterator.previous(); }
		return null;
	}
	
	public void resetSimulation() {
		iterator = steps.listIterator();
	}


}
