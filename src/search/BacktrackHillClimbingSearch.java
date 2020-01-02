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
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

public class BacktrackHillClimbingSearch extends Search
{
	protected Hashtable closed;
	protected Queue open;
	protected Filter filter = null;
	protected ArrayList<State> path;
	protected SuccessorSelector successorSelector = null;
	
	public BacktrackHillClimbingSearch(State s)
	{
		this(s, new HValueComparator());
	}

	public BacktrackHillClimbingSearch(State s, Comparator c)
	{
		super(s);
		setComparator(c);

		path = new ArrayList<>();
		closed = new Hashtable();
		open = new LinkedList();
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
		path.add(start);

		javaff.JavaFF.infoOutput.println("initial heuristic: " + start.getHValue());

		while (!open.isEmpty()) {
			State s = removeNext(); // get the next one
			if (s.goalReached()) {
				return s;
			}

			Set successors = s.getNextStates(filter.getActions(s)); // and find its neighbourhood
			successors.addAll(path);
			State chosenSuccessor = successorSelector.choose(successors);

			if (needToVisit(chosenSuccessor)) {
				if (chosenSuccessor != null) {
					open.add(chosenSuccessor);
					if (chosenSuccessor.getHValue().compareTo(bestHeuristic) < 0) {
						bestHeuristic = chosenSuccessor.getHValue();

						int size = path.size();
						for (int i = size - 1; i >= 0; i--) {
							if (!path.get(i).getNextStates(filter.getActions(s)).contains(chosenSuccessor)) {
								path.remove(i);
							}
						}
						path.add(chosenSuccessor);
					}

					javaff.JavaFF.infoOutput.println(chosenSuccessor.getHValue());
				}
			}
			if (chosenSuccessor == null || !needToVisit(chosenSuccessor)) {
				open.add(successorSelector.choose(successors));
			}
		}
		return null;
	}
}
