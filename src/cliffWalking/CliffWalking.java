package cliffWalking;

//CS394R Assignment 3
//Example 6.6, Sutton & Barto

import java.util.*;
import java.io.*;


public class CliffWalking{	
	int w;
	int l;

	public CliffWalking(int width, int length){
		w = width;
		l = length;
	}
	public String etaGreedy(HashMap<String, Double> qa, double eta){
		Random rand = new Random();
		Double maxValue = Double.NEGATIVE_INFINITY;
		int maxCount = 0;
		int totalCount = 0;
		LinkedList<String> actions = new LinkedList<String>();
		LinkedList<Double> probs = new LinkedList<Double>();
		for(String action : qa.keySet()){
			totalCount++;
			Double currValue = qa.get(action);
			// System.out.printf("%f vs. %f\n", currValue, maxValue);
			int compare = currValue.compareTo(maxValue);
			if(compare > 0){
				maxValue = currValue;
				maxCount = 1;
			}else if(compare == 0){
				// System.out.println("duplicate max");
				maxCount++;
			}
		}
		Double exploreProb = eta/totalCount;
		Double greedyProb = (1.0 - eta)/maxCount + exploreProb;
		Double oldProb = 0.0;
		// System.out.printf("eV: %f\n gV: %f\nmaxCount: %d\n max: %f\n", exploreValue, greedyValue, maxCount, max);
		for(String action : qa.keySet()){
			Double currValue = qa.get(action);
			if(currValue.compareTo(maxValue)==0){
				oldProb += greedyProb;
			}else{
				oldProb += exploreProb;
			}
			// System.out.printf("%f\n", oldValue);
			probs.add(oldProb);
			actions.add(action);
		}
		double r = rand.nextDouble();
		for(int i = 0; i < totalCount; i++){
			if(r < probs.get(i)){
				return actions.get(i);
			}
		}
		return actions.get(totalCount-1);
	}


	public String e_Greedy(HashMap<String,Double> qa,double e){
		Random rand = new Random();
		Double maxValue = Double.NEGATIVE_INFINITY;
		LinkedList<String> actionList = new LinkedList<String>();
		if(rand.nextDouble()<e) {
			//explore
			int size = qa.size();
			int actionNumber = rand.nextInt(size);
			//put all actions into list
			for(String action:qa.keySet()) {
				actionList.add(action);
			}
			return actionList.get(actionNumber);
			
			
		}
		else {
			//greedy
			LinkedList<String> MaxActions = new LinkedList<String>();
			for(String action:qa.keySet()) {
				if(qa.get(action).compareTo(maxValue)>0) {
					//update maxValue
					maxValue = qa.get(action);
					MaxActions.clear();//find new maxValue, reset Arraylist
					MaxActions.add(action);
				}
				else if(qa.get(action).compareTo(maxValue)==0) {
					MaxActions.add(action);
				}
			}
			if(MaxActions.size()==1) {
//				System.out.println("Greedy :one number:");
				return MaxActions.get(0);
			}
			else {
				int index = rand.nextInt(MaxActions.size());
//				System.out.println("Greedy :same e situation:"+index);
				return MaxActions.get(index);
			}
		}
		
	}


	public Double getMaxQAV(HashMap<String, Double> qa){
		Double maxValue = Double.NEGATIVE_INFINITY;
		String maxAction = null;
		for(String action : qa.keySet()){
			Double currValue = qa.get(action);
			int compare = currValue.compareTo(maxValue);
			if(compare > 0){
				maxValue = currValue;
				maxAction = action;
			}
		}
		return maxValue;
	}

	public HashMap<String,HashMap<String,Double>> Q_Learning(double eta,double alpha,double gamma,int episode){
		HashMap<String,HashMap<String,Double>> qTable = new HashMap<>();
		CliffEnviroment ce = new CliffEnviroment(w, l);
		int StepCount = 0;
		//Initialize Q
		for(int i=0;i<w;i++) {
			for(int j=0;j<l;j++) {
				HashMap<String,Double> qData = new HashMap<>();
				qData.put("up", 0.0);
				qData.put("left", 0.0);
				qData.put("down", 0.0);
				qData.put("right", 0.0);
				qTable.put(String.format("(%d, %d)", i,j), qData);
			}
		}
		
		//Loop for each episode
		for(int i=0;i<episode;i++) {
			//Initialize S
			ce.reset();
			StepCount = 0;
			while(!ce.terminate()) {
				StepCount++;
				//Choose A from S using policy derived from Q
				String S = ce.getState();
				String A = e_Greedy(qTable.get(S),eta);
				//Take Action A ,observe R,S`
				ce.action(A);
				int R = ce.getReward();
				String S_ = ce.getState();
				Double value ;
				value = qTable.get(S).get(A)+alpha*(R+gamma*getMaxQAV(qTable.get(S_))-qTable.get(S).get(A));
				HashMap<String,Double> qData = qTable.get(S);
				qData.put(A, value);
				qTable.put(S, qData);
				//S<--S`
				S = S_;
			}
			System.out.println("Episode: "+i+"\t"+StepCount);
		}
		
		
		return qTable;
	}
	
	public HashMap<String,HashMap<String,Double>> DoubleQLearning(double eta,double alpha ,double gamma,int episode){
		HashMap<String,HashMap<String,Double>> SumQTable = new HashMap<>();
		HashMap<String,HashMap<String,Double>> QTable1 = new HashMap<>();
		HashMap<String,HashMap<String,Double>> QTable2 = new HashMap<>();
		Random random = new Random();
		CliffEnviroment ce = new CliffEnviroment(w,l);
		//Initialize Q1 and Q2 ,
		for(int i=0;i<w;i++) {
			for(int j=0;j<l;j++) {
				HashMap<String,Double> qData = new HashMap<>();
				qData.put("up", -0.25);
				qData.put("left",-0.25);
				qData.put("down", -0.25);
				qData.put("right", -0.25);
				QTable1.put(String.format("(%d, %d)", i,j), qData);
			}
		}
		for(int i=0;i<w;i++) {
			for(int j=0;j<l;j++) {
				HashMap<String,Double> qData = new HashMap<>();
				qData.put("up", 0.25);
				qData.put("left",0.25);
				qData.put("down", 0.25);
				qData.put("right", 0.25);
				QTable2.put(String.format("(%d, %d)", i,j), qData);
			}
		}
		//Loop for each episode
		
		for(int i=0;i<episode;i++) {
			//Initialize S
			ce.reset();
			int steps = 0;
			//Loop for each step of episode
			while(!ce.terminate()) {
				steps++;
				//Choose A from S using the policy e-greedy in Q1+Q2
				String S = ce.getState();
//				SumQTable = SumQ(QTable1,QTable2);
				for(String State:QTable1.keySet()){
					//get current state value in Q1
					HashMap<String,Double> Data1 = QTable1.get(State); 
					HashMap<String,Double> Data2 = QTable2.get(State);
					HashMap<String,Double> SumData = new HashMap<String,Double>();
					
					for(String action:Data1.keySet()) {
						SumData.put(action, Data1.get(action)+Data2.get(action));
						
					}
					
					SumQTable.put(State,SumData);
				}

				String A = e_Greedy(SumQTable.get(S),eta);
				//Take action A,observe R,S`
				ce.action(A);
				int R = ce.getReward();
				String S_ = ce.getState();
				//Update QTable1 and QTable2
				Double value;
				if(random.nextDouble()<0.5) {
					value = QTable1.get(S).get(A)+alpha*(R+gamma*getMaxQAV(QTable2.get(S_))-QTable1.get(S).get(A));
					HashMap<String,Double> QData = QTable1.get(S);
					QData.put(A, value);
					QTable1.put(S, QData);
				}
				else {
					value = QTable2.get(S).get(A)+alpha*(R+gamma*getMaxQAV(QTable1.get(S_))-QTable2.get(S).get(A));
					HashMap<String,Double> QData = QTable2.get(S);
					QData.put(A, value);
					QTable2.put(S, QData);
				}
			}
			System.out.println("Episode "+i+"\t"+steps);
			
		}
		
		
		
		return SumQTable;
	}
	public HashMap<String,HashMap<String,Double>> SumQ(HashMap<String,HashMap<String,Double>> Q1,HashMap<String,HashMap<String,Double>> Q2){
		HashMap<String,HashMap<String,Double>> res = new HashMap<>();
		
		for(String State:Q1.keySet()){
			//get current state value in Q1
			HashMap<String,Double> Data1 = Q1.get(State); 
			HashMap<String,Double> Data2 = Q2.get(State);
			HashMap<String,Double> SumData = new HashMap<String,Double>();
			
			for(String action:Data1.keySet()) {
				SumData.put(action, Data1.get(action)+Data2.get(action));
				
			}
			
			res.put(State,SumData);
		}
		return res;
	}
	public HashMap<String,HashMap<String,Double>> Sarsa(double eta,double alpha,double gamma,int episode){
		HashMap<String,HashMap<String,Double>> qTable = new HashMap<String,HashMap<String,Double>>();
		CliffEnviroment ce = new CliffEnviroment(w,l);
		//Initialize Q
		for(int i=0;i<w;i++) {
			for(int j=0;j<l;j++) {
				HashMap<String,Double> qData = new HashMap<String,Double>();
				qData.put("up", 0.0);
				qData.put("left", 0.0);
				qData.put("down", 0.0);
				qData.put("right", 0.0);
				qTable.put(String.format("(%d, %d)", i,j),qData);
			}
		}
		
		for(int i=0;i<episode;i++) {
			//Reset Agent State
			int StepCount = 0;
			ce.reset();
			//Initialize S
			String S = ce.getState();
			//Choose A from S using policy derived from Q (e-greedy)
			String A = e_Greedy(qTable.get(S), eta);
			//Loop for each step of episode
			
			while(!ce.terminate()) {
				
				//Take action A,observe R,S` 
				ce.action(A);
				int R = ce.getReward();
				String S_ = ce.getState();
				//Choose A` from S` using policy derived from Q(e-greedy)
				String A_ = e_Greedy(qTable.get(S_), eta);
				//Update Q
				HashMap<String,Double> qData = qTable.get(S);
				Double value = qTable.get(S).get(A)+alpha*(R+gamma*(qTable.get(S_).get(A_))-qTable.get(S).get(A));
				
				qData.put(A, value);
				qTable.put(S,qData);
				//S<--S`; A<--A`
				S = S_;
				A = A_;
				StepCount++;
			}
			System.out.println("Episode "+i+":\t"+StepCount);

		}
		
		
		return qTable;
		
	}

	public void printPolicy(HashMap<String, HashMap<String, Double>> q){
		HashMap<String, String> trajectory = new HashMap<String, String>();
		CliffEnviroment ce = new CliffEnviroment(w, l);
		ce.reset();
		while(!ce.terminate()){
			String s = ce.getState();
//			String a = etaGreedy(q.get(s), 0.0);
			String a = e_Greedy(q.get(s), 0.0);
			trajectory.put(s, a.substring(0,1));
			ce.action(a);
			// System.out.printf("%s-%s", s, a);
		}
		System.out.printf("\n.");
		for(int i = 0; i < w; i++){
			System.out.printf("_");
		}
		System.out.printf(".\n");
		for(int j = (l-1); j > -1; j--){
			System.out.printf("|");
			for(int i = 0; i < w; i++){
				String a = trajectory.get(String.format("(%d, %d)", i,j));
				if(a != null){
					System.out.printf("%s",a);
				}else{
					System.out.printf(" ");
				}
			}
			System.out.printf("|\n");
		}
		System.out.printf(".");
		for(int i = 0; i < w; i++){
			System.out.printf("_");
		}
		System.out.printf(".\n");
		// System.out.printf("\n");
		System.out.println(trajectory.size());
	}
	public void printQtable(HashMap<String,HashMap<String,Double>> Q_Table) {
		HashMap<String,Double> ActAndProb = new HashMap<String,Double>();
		int PointCount = 0;
		for(String position:Q_Table.keySet()) {
			ActAndProb.clear();
			ActAndProb = Q_Table.get(position);
			System.out.println(position+"\t");
			PointCount++;
			for(String action:ActAndProb.keySet()) {
				System.out.println("\t"+action+":"+ActAndProb.get(action));
			}
		}
		System.out.println(PointCount);
	}

	public static void main(String[] args){		

		CliffWalking cw = new CliffWalking(60,4);
//		HashMap<String, HashMap<String, Double>> qS = cw.Sarsa(0.01, 0.5, 0.5, 3000);
//		HashMap<String, HashMap<String, Double>> qQL = cw.Q_Learning(0.01, 0.5, 0.5, 3000);		

//		cw.printPolicy(qS);
//		cw.printPolicy(qQL);
		
		HashMap<String,HashMap<String,Double>> DoubleQ = cw.DoubleQLearning(0.1, 0.9, 0.5, 2000);
		cw.printPolicy(DoubleQ);
		

	}
}