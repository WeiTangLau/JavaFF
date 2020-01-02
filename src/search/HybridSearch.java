/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 *
 * (Questions/bug reports now to be sent to Andrew Coles)
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.search;

import javaff.JavaFF;
import javaff.planning.State;
import javaff.planning.Filter;
import javaff.planning.HelpfulFilter;
import javaff.search.EnforcedHillClimbingSearch;
import javaff.search.BestFirstSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;
import java.util.Iterator;

public class HybridSearch extends Search {
	protected BigDecimal bestHValue;

	protected Hashtable closed;
	protected LinkedList open;
	protected Filter filter = null;
	protected javaff.search.SuccessorSelector successorSelector = null;

	public HybridSearch(State s) {
		this(s, new HValueComparator());
	}

	public HybridSearch(State s, Comparator c) {
		super(s);
		setComparator(c);

		closed = new Hashtable();
		open = new LinkedList();
	}

	public void setSuccessorSelector(javaff.search.SuccessorSelector ss) {
		successorSelector = ss;
	}

	public void setFilter(Filter f) {
		filter = f;
	}

	public State removeNext() {
		/*
		int r = javaff.JavaFF.generator.nextInt(open.size());
		return (State) ((LinkedList) open).remove(r);
		 */

		return (State) ((LinkedList) open).removeFirst();
	}

	public boolean needToVisit(State s) {
		Integer Shash = new Integer(s.hashCode()); // compute hash for state
		State D = (State) closed.get(Shash); // see if its on the closed list

		if (closed.containsKey(Shash) && D.equals(s)) return false;  // if it is return false

		closed.put(Shash, s); // otherwise put it on
		return true; // and return true
	}

	/**
	 * This method runs the EHC starting from the initial state. If no solution
	 * is found, BFS is being called. If BFS found a state with a lower heuristic
	 * than bestSoFar, run EHC from this state S. Continue until a solution is found.
	 */
	public State search() {
		State bestSoFar = start;
		while (true) {
			bestSoFar = enforcedHillClimbingSearch(bestSoFar);
			if (bestSoFar.goalReached()) {
				return bestSoFar;
			}

			bestSoFar = bestFirstSearch(bestSoFar);
			if (bestSoFar.goalReached()) {
				return bestSoFar;
			}
		}
	}

	public State enforcedHillClimbingSearch(State curr) {

		if (curr.goalReached()) { // wishful thinking
			return curr;
		}

		needToVisit(curr); // dummy call (adds start to the list of 'closed' states so we don't visit it again

		open.add(curr); // add it to the open list
		bestHValue = curr.getHValue(); // and take its heuristic value as the best so far
		State bestSoFar = curr;

		javaff.JavaFF.infoOutput.println("initial heuristic: " + bestHValue);

		while (!open.isEmpty()) // whilst still states to consider
		{
			State s = removeNext(); // get the next one

			// Set successors = s.getNextStates(filter.getActions(s)); // and find its neighbourhood
			List<State> arr = new ArrayList<>(s.getNextStates(filter.getActions(s)));

			while (!arr.isEmpty()) {
				int r = javaff.JavaFF.generator.nextInt(arr.size());
				State succ = arr.remove(r);
				if (needToVisit(succ)) {
					if (succ.goalReached()) { // if we've found a goal state - return it as the solution
						return succ;
					} else if (succ.getHValue().compareTo(bestHValue) < 0) {
						// if we've found a state with a better heuristic value than the best seen so far

						bestHValue = succ.getHValue(); // note the new best avlue
						bestSoFar = succ;
						javaff.JavaFF.infoOutput.println(bestHValue);
						open = new LinkedList(); // clear the open list
						open.add(succ); // put this on it
						break; // and skip looking at the other successors
					} else {
						open.add(succ); // otherwise, add to the open list
					}
				}
			}
			/*
			Iterator succItr = successors.iterator();

			while (succItr.hasNext()) {
				State succ = (State) succItr.next(); // next successor

				if (needToVisit(succ)) {
					if (succ.goalReached()) { // if we've found a goal state - return it as the solution
						return succ;
					} else if (succ.getHValue().compareTo(bestHValue) < 0) {
						// if we've found a state with a better heuristic value than the best seen so far

						bestHValue = succ.getHValue(); // note the new best avlue
						bestSoFar = succ;
						javaff.JavaFF.infoOutput.println(bestHValue);
						open = new LinkedList(); // clear the open list
						open.add(succ); // put this on it
						break; // and skip looking at the other successors
					} else {
						open.add(succ); // otherwise, add to the open list
					}
				}
			}

			 */
		}
		return bestSoFar;
	}

	public State bestFirstSearch(State curr) {
		open.clear();
		open.add(curr);
		BigDecimal bestSoFar = curr.getHValue();

		while (!open.isEmpty())
		{
			State s = removeNext();
			if (needToVisit(s)) {
				++nodeCount;
				if (s.goalReached() || s.getHValue().compareTo(bestSoFar) < 0) {
					return s;
				} else {
					updateOpen(s);
				}
			}

		}
		return null;
	}

	public void updateOpen(State S)
	{
		open.addAll(S.getNextStates(filter.getActions(S)));
	}

}
