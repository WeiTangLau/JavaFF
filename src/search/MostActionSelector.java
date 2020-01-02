//
//  MostActionSelector.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.planning.State;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

public class MostActionSelector implements SuccessorSelector
{

	private static MostActionSelector mas = null;

	public static MostActionSelector getInstance()
	{
		if (mas == null)
			mas = new MostActionSelector(); // Singleton, as in NullFilter
		return mas;
	}

	public State choose(Set toChooseFrom) {

		if (toChooseFrom.isEmpty())
			return null;

		State bestSuccessor;
		BigDecimal bestHeuristic; // best heuristic seen

		Iterator itr = toChooseFrom.iterator();
		bestSuccessor = (State) itr.next();
		bestHeuristic = bestSuccessor.getHValue(); // and has the best heuristic
		State curr;

		while (itr.hasNext()) {
			curr = (State) itr.next();
			if (curr.getHValue().compareTo(bestHeuristic) < 0) { // if it has a better heuristic value
				bestSuccessor = curr;
				bestHeuristic = curr.getHValue();
			} else if (curr.getHValue().compareTo(bestHeuristic) == 0) { // if it has an equally good h
				if (curr.getActions().size() > bestSuccessor.getActions().size()) {
					bestSuccessor = curr;
				}

			}
		}
		return bestSuccessor;
	};

};