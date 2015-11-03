package com.zhuoyou.plugin.running;


public class FoodItem {

	private String food;
	private int calories;
	
	public void setName(String food) {
		this.food = food;
	}

	public String getName() {
		return food;
	}

	public void setCal(int cal) {
		this.calories = cal;
	}

	public int getCal() {
		return calories;
	}

}
