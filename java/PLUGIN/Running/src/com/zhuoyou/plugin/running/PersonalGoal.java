package com.zhuoyou.plugin.running;


public class PersonalGoal {
	public int mGoalSteps;
	public int mGoalCalories;
	public long id;
	
	public PersonalGoal(){
		
	}
	
	public PersonalGoal(int step , int cal){
		mGoalSteps = step;
		mGoalCalories = cal;
	}
	
	public int getStep() {
		return mGoalSteps;
	}

	public void setStep(int step) {
		this.mGoalSteps = step;
	}

	public int getCal() {
		return mGoalCalories;
	}

	public void setCal(int cal) {
		this.mGoalCalories = cal;
	}
	
	public String toString(){
		return mGoalSteps + "|" + mGoalCalories + "|";
	}
}
