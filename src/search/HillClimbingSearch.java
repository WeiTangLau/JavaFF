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
import javaff.search.SuccessorSelector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;
import java.util.Iterator;

public class HillClimbingSearch extends Search
{
	private static final int MAXIMUM_DEPTH = 10000;
	protected int maxDepth;
	protected Hashtable closed;
	protected Queue open;
	protected Filter filter = null;
	protected SuccessorSelector successorSelector = null;
	
	public HillClimbingSearch(State s)
	{
		this(s, new HValueComparator());
	}

	public HillClimbingSearch(State s, Comparator c)
	{
		super(s);
		setComparator(c);

		maxDepth = MAXIMUM_DEPTH;
		closed = new Hashtable();
		open = new LinkedList();
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void setFilter(Filter f)
	{
		filter = f;
	}

	public void setSuccessorSelector(SuccessorSelector ss) {
		successorSelector = ss;
	}

	public State removeNext() {
		return (State) open.remove();
	}
	
	public boolean needToVisit(State s) {
		Integer Shash = new Integer(s.hashCode()); // compute hash for state
		State D = (State) closed.get(Shash); // see if its on the closed list
		
		if (closed.containsKey(Shash) && D.equals(s)) return false;  // if it is return false
		
		closed.put(Shash, s); // otherwise put it on
		return true; // and return true
	}
	
	public State search() {
		if (start.goalReached()) { // wishful thinking
			return start;
		}

		BigDecimal bestHeuristic = start.getHValue(); // set to initial state's H value instead of infinity as per Algorithm 1
		needToVisit(start); // dummy call (adds start to the list of 'closed' states so we don't visit it again
		
		open.add(start); // add it to the open list

		javaff.JavaFF.infoOutput.println("initial heuristic: " + start.getHValue());

		int depth = 0;

		while (!open.isEmpty()) {
			if (depth == maxDepth) {
				return null;
			}

			State s = removeNext(); // get the next one
			if (s.goalReached()) {
				return s;
			}

			Set successors = s.getNextStates(filter.getActions(s)); // and find its neighbourhood
			State chosenSuccessor = successorSelector.choose(successors);

			if (needToVisit(chosenSuccessor)) {
				if (chosenSuccessor != null) {
					open.add(chosenSuccessor);
					javaff.JavaFF.infoOutput.println(chosenSuccessor.getHValue());
				}
			}

			/*
			Set<State> bestSuccessors = new HashSet<>();
			BigDecimal bestHeuristic = s.getHValue(); // set to initial state's H value instead of infinity as per Algorithm 1

			while (succItr.hasNext()) {
				State succ = (State) succItr.next(); // next successor
				if (needToVisit(succ)) {
					if (succ.goalReached()) {
						return succ;
					}
					if (succ.getHValue().compareTo(bestHeuristic) < 0) {
						bestHeuristic = succ.getHValue();
						bestSuccessors.add(succ);
					} else if (succ.getHValue().equals(bestHeuristic)) {
						bestSuccessors.add(succ);
					}
				}
			}
			if (bestSuccessors.isEmpty()) {
				open.clear();
			} else {
				State chosenSuccessor = successorSelector.choose(bestSuccessors);
				open.clear();
				open.add(chosenSuccessor);
				javaff.JavaFF.infoOutput.println(chosenSuccessor.getHValue());
			}
			*/
			depth++;
		}
		return null;
	}
}
