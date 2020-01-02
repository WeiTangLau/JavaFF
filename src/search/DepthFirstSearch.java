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

import javaff.planning.State;
import javaff.planning.Filter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Hashtable;


public class DepthFirstSearch extends Search
{
	private static final int MAXIMUM_DEPTH = 10000;
	protected int maxDepth;
	protected Hashtable closed;
	protected Stack<State> open;
	protected Filter filter = null;
	
	public DepthFirstSearch(State s)
    {
		this(s, new HValueComparator());
	}

	public DepthFirstSearch(State s, Comparator c)
    {
		super(s);
		setComparator(c);

		maxDepth = MAXIMUM_DEPTH;
		closed = new Hashtable();
		open = new Stack<>();
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void setFilter(Filter f)
	{
		filter = f;
	}

	public void updateOpen(State S)
    {
		open.addAll(S.getNextStates(filter.getActions(S)));
	}

	public State removeNext()
    {
		State S = open.pop();
                /*
                System.out.println("================================");
		S.getSolution().print(System.out);
		System.out.println("----Helpful Actions-------------");
		javaff.planning.TemporalMetricState ms = (javaff.planning.TemporalMetricState) S;
		System.out.println(ms.helpfulActions);
		System.out.println("----Relaxed Plan----------------");
		ms.RelaxedPlan.print(System.out);
                */
		return S;
	}

	public boolean needToVisit(State s) {
		Integer Shash = new Integer(s.hashCode());
		State D = (State) closed.get(Shash);
		
		if (closed.containsKey(Shash) && D.equals(s)) return false;
		
		closed.put(Shash, s);
		return true;
	}

	public State performDFS(State curr, Hashtable closed) {
		if (curr.goalReached()) {
			return curr;
		}
		Set succ = curr.getNextStates(filter.getActions(curr));
		Iterator<State> itr = succ.iterator();
		while (itr.hasNext()) {
			State next = itr.next();
			if (next.goalReached()) {
				return next;
			}
			if (needToVisit(next)) {
				State result = performDFS(next, closed);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public State search() {
		return performDFS(start, closed);
		/*
		open.add(start);

		int currDepth = 0;

		while (!open.isEmpty()) {
			State s = removeNext();
			if (needToVisit(s)) {
				++nodeCount;
				if (s.goalReached()) {
					return s;
				} else {
					updateOpen(s);
				}
			}
		}
		return null;
		 */
	}



}