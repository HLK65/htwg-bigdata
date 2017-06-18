package de.htwg.bigdata.visual.visualisationclient;

public class SimulationStep implements Comparable<SimulationStep>{
	private int step;
	private int time;
	private double[][] fields;
	
	public SimulationStep(int step, int time, int fieldSize) {
		this.step = step;
		this.time = time;
		fields = new double[fieldSize][fieldSize];
	}
	
	public void setField(int x, int y, int concentration) {
		
		if (x > fields.length - 1 ) { 
			System.out.println("x to big");
			throw new RuntimeException("x to big"); 
			}
		if (y > fields[x].length - 1) { 
			System.out.println("y to big");
			throw new RuntimeException("y to big"); 
			}
		if (concentration < 0) { throw new RuntimeException("concentration should not be negative"); }	  				  		

		try {
			fields[x][y] = (double) concentration;
		} catch (Exception e) {
			System.out.println("x: " + x);
			System.out.println("y: " + y);
			System.out.println("concentration: " + concentration);		  				  		
			System.out.println(e);			
		}
		
	}
	
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public double[][] getFields() {
		return fields;
	}
	public void setFields(double[][] fields) {
		this.fields = fields;
	}

	public int compareTo(SimulationStep that) {
		return this.step - that.step;		
	}
}
