package com;

public class StateAndAction {
	public double alpha;//alpha
	public double gamma;
	public double epsilon;
	public int  stateLength = 6;
	public int  actionLength = 6;
	public double [][] reward={
			{-1,-1,-1,-1,0,-1},
			{-1,-1,-1,0,-1,100},
			{-1,-1,-1,0,-1,-1},
			{-1,0,0,-1,0,-1},
			{0,-1,-1,0,0,100},
			{-1,0,-1,-1,0,100},
	}; 
	public double[][] qValues={
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
	}; 
	public StateAndAction(){
		this.alpha = 0.8;
		this.epsilon = 0.2;
		this.gamma = 0.5;
	}
	public double getReward(int state,int action){
		return this.reward[state][action];
	}
	public double getqValues(int state,int action){
		return this.qValues[state][action];
	}
	public void setqValues(int state,int action,double reward){
		this.qValues[state][action] = reward;
	}
	public double getMaxQValues(int state){
		double maxQ = -50000;
		for(int i = 0; i < this.qValues[state].length;i++){
			if(this.reward[state][i] != -1){
				if(this.qValues[state][i] > maxQ){
					maxQ = this.qValues[state][i];
				}
			}
		}
		return maxQ;
	}
}
