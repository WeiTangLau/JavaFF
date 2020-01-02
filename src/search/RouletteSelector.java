//
//  RouletteSelector.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.planning.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

public class RouletteSelector implements SuccessorSelector
{

	private static RouletteSelector rs = null;

	public static RouletteSelector getInstance()
	{
		if (rs == null)
			rs = new RouletteSelector(); // Singleton, as in NullFilter
		return rs;
	}

	public State choose(Set toChooseFrom) {
		if (toChooseFrom.isEmpty()) {
			return null;
		}

		HashMap<Integer, State> succMap = new HashMap<>();
		ArrayList<Double> fitnessValues = new ArrayList<>();
		BigDecimal bestHeuristic; // best heuristic seen

		Iterator itr = toChooseFrom.iterator();
		State curr = (State) itr.next();
		if (curr.goalReached()) {
			return curr;
		}
		succMap.put(fitnessValues.size(), curr);
		fitnessValues.add(1 / curr.getHValue().doubleValue());
		bestHeuristic = curr.getHValue();
		double totalFitness = 1 / curr.getHValue().doubleValue();

		while (itr.hasNext()) {
			curr = (State) itr.next();
			if (curr.goalReached()) {
				return curr;
			}
			if (curr.getHValue().compareTo(bestHeuristic) < 0) {
				succMap.clear();
				fitnessValues.clear();
				succMap.put(fitnessValues.size(), curr);
				fitnessValues.add(1 / curr.getHValue().doubleValue());
				bestHeuristic = curr.getHValue();
				totalFitness = 1 / curr.getHValue().doubleValue();
			} else if (curr.getHValue().compareTo(bestHeuristic) == 0) {
				succMap.put(fitnessValues.size(), curr);
				fitnessValues.add(1 / curr.getHValue().doubleValue());
				totalFitness += 1 / curr.getHValue().doubleValue();
			}
		}

		double r = javaff.JavaFF.generator.nextDouble() * totalFitness;
		//javaff.JavaFF.infoOutput.println("Total Fitness: " + totalFitness + ", roulette value: " + r);

		double currTotal = 0;
		for (int i = 0; i < fitnessValues.size(); i++) {
			currTotal += fitnessValues.get(i);
			//javaff.JavaFF.infoOutput.println("current total: " + currTotal);
			if (r < currTotal) {
				return succMap.get(i);
			}
		}
		return null;
		/*

		while (itr.hasNext())
		{
			curr = (State) itr.next();
			if (curr.getHValue().compareTo(bestHeuristic) < 0)
			{ // if it has a better heuristic value
				jointBest = new HashSet();
				jointBest.add(curr); // it is the joint best, with only
										// itself
				bestHeuristic = curr.getHValue();
			} else if (curr.getHValue().compareTo(bestHeuristic) == 0)
			{ // if it has an equally good h
				jointBest.add(curr); // then it is joint best with the others
			}
		}
		int nextChosen = javaff.JavaFF.generator.nextInt(jointBest.size()); 

		Iterator skipThrough = jointBest.iterator();
		while (nextChosen > 0)
		{ // skip over the appropriate number of items
			skipThrough.next();
			--nextChosen;
		}

		return ((State) (skipThrough.next())); // return tmstate from set
		*/
	};

};