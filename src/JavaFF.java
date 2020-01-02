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

package javaff;

import javaff.data.UngroundProblem;
import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.parser.PDDL21parser;
import javaff.planning.State;
import javaff.planning.TemporalMetricState;
import javaff.planning.HelpfulFilter;
import javaff.planning.NullFilter;
import javaff.planning.RandomThreeFilter;
import javaff.search.BacktrackHillClimbingSearch;
import javaff.search.BestFirstSearch;
import javaff.search.BestSuccessorSelector;
import javaff.search.DepthFirstSearch;
import javaff.search.HybridSearch;
import javaff.search.MostActionSelector;
import javaff.search.RouletteSelector;
import javaff.search.EnforcedHillClimbingSearch;
import javaff.search.HillClimbingSearch;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

public class JavaFF {
	public static BigDecimal EPSILON = new BigDecimal(0.01);
	public static BigDecimal MAX_DURATION = new BigDecimal("100000"); // maximum duration in a duration constraint
	public static boolean VALIDATE = false;

	public static Random generator = null;

	public static PrintStream planOutput = System.out;
	public static PrintStream parsingOutput = System.out;
	public static PrintStream infoOutput = System.out;
	public static PrintStream errorOutput = System.err;

	public static void main(String args[]) {
		EPSILON = EPSILON.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		MAX_DURATION = MAX_DURATION.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		generator = new Random();

		if (args.length < 2) {
			System.out.println("Parameters needed: domainFile.pddl problemFile.pddl [random seed] [outputfile.sol");

		} else {
			File domainFile = new File(args[0]);
			File problemFile = new File(args[1]);
			File solutionFile = null;
			if (args.length > 2) {
				generator = new Random(Integer.parseInt(args[2]));
			}

			if (args.length > 3) {
				solutionFile = new File(args[3]);
			}

			Plan plan = plan(domainFile, problemFile);

			if (solutionFile != null && plan != null)
				writePlanToFile(plan, solutionFile);

		}
	}

	public static Plan plan(File dFile, File pFile) {
		// ********************************
		// Parse and Ground the Problem
		// ********************************
		long startTime = System.currentTimeMillis();

		UngroundProblem unground = PDDL21parser.parseFiles(dFile, pFile);

		if (unground == null) {
			System.out.println("Parsing error - see console for details");
			return null;
		}

		// PDDLPrinter.printDomainFile(unground, System.out);
		// PDDLPrinter.printProblemFile(unground, System.out);

		GroundProblem ground = unground.ground();

		long afterGrounding = System.currentTimeMillis();

		// ********************************
		// Search for a plan
		// ********************************

		// Get the initial state
		TemporalMetricState initialState = ground.getTemporalMetricInitialState();

		State goalState = goalState = performFFSearch(initialState);

		long afterPlanning = System.currentTimeMillis();

		TotalOrderPlan top = null;
		if (goalState != null)
			top = (TotalOrderPlan) goalState.getSolution();
		if (top != null)
			top.print(planOutput);

		/*
		 * javaff.planning.PlanningGraph pg = initialState.getRPG(); Plan plan =
		 * pg.getPlan(initialState); plan.print(planOutput); return null;
		 */

		// ********************************
		// Schedule a plan
		// ********************************

		// TimeStampedPlan tsp = null;

		// if (goalState != null)
		// {

		// infoOutput.println("Scheduling");

		// Scheduler scheduler = new JavaFFScheduler(ground);
		// tsp = scheduler.schedule(top);
		// }

		// long afterScheduling = System.currentTimeMillis();

		// if (tsp != null) tsp.print(planOutput);

		double groundingTime = (afterGrounding - startTime) / 1000.00;
		double planningTime = (afterPlanning - afterGrounding) / 1000.00;

		// double schedulingTime = (afterScheduling - afterPlanning)/1000.00;

		double totalTime = groundingTime + planningTime;
		infoOutput.println("Instantiation Time =\t\t" + groundingTime + "sec");
		infoOutput.println("Planning Time =\t" + planningTime + "sec");

		// infoOutput.println("Scheduling Time =\t"+schedulingTime+"sec"); totalTime =
		// totalTime + schedulingTime;

		infoOutput.println("Total execution time:");
		infoOutput.println(groundingTime + planningTime);

		// #cost-problem comment the two lines below
		infoOutput.println("Plan Cost:");
		if (top != null)
			infoOutput.println(top.getCost());

		return top;
	}

	private static void writePlanToFile(Plan plan, File fileOut) {
		try {
			FileOutputStream outputStream = new FileOutputStream(fileOut);
			PrintWriter printWriter = new PrintWriter(outputStream);
			plan.print(printWriter);
			printWriter.close();
		} catch (FileNotFoundException e) {
			errorOutput.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			errorOutput.println(e);
			e.printStackTrace();
		}

	}

	public static State performFFSearch(TemporalMetricState initialState) {

		// Implementation of standard FF-style search

		infoOutput.println("Performing Hybrid Search, starts with EHC then follow by Best-First Search");

		// Now, initialise a searcher
		// This is for the Satisfycing path

		HybridSearch HCS = new HybridSearch(initialState);
		HCS.setSuccessorSelector(RouletteSelector.getInstance());
		HCS.setFilter(NullFilter.getInstance());
		State bestGoalState = HCS.search();


		// This is for the Optimising Path
		/*
		HybridSearch HCS;
		int bestPlanLength = Integer.MAX_VALUE;
		State bestGoalState = null;

		// Run the algorithm repeatedly
		for (int i = 0; i < 100; i++) {
			HCS = new HybridSearch(initialState);
			HCS.setSuccessorSelector(RouletteSelector.getInstance());
			HCS.setFilter(NullFilter.getInstance());
			State goalState = HCS.search();
			TotalOrderPlan thePlan = (TotalOrderPlan) goalState.getSolution();
			int planLength = thePlan.getPlanLength();
			if (planLength < bestPlanLength) {
				bestGoalState = goalState;
				bestPlanLength = planLength;
				infoOutput.println("Best Length: " + bestPlanLength);
			}
		}
		infoOutput.println("Best Length: " + bestPlanLength);
		*/


		// Keeping all the legacy code to demonstrate the various methods that I have tried
		/*
		EnforcedHillClimbingSearch searchAlgo = new EnforcedHillClimbingSearch(initialState);
		searchAlgo.setFilter(HelpfulFilter.getInstance()); // and use the helpful actions neighbourhood
		State goalState = searchAlgo.search();

		HillClimbingSearch searchAlgo = new HillClimbingSearch(initialState);;
		searchAlgo.setFilter(HelpfulFilter.getInstance()); // and use the helpful actions neighbourhood
		searchAlgo.setSuccessorSelector(MostActionSelector.getInstance());
		searchAlgo.setMaxDepth(20);
		State goalState = searchAlgo.search();
		// Try and find a plan using EHC

		/*
		for (int depthBound = 5; depthBound < 20; depthBound++) {
			searchAlgo = new HillClimbingSearch(initialState);
			searchAlgo.setFilter(HelpfulFilter.getInstance()); // and use the helpful actions neighbourhood
			searchAlgo.setSuccessorSelector(BestSuccessorSelector.getInstance());
			infoOutput.println("Current depth bound:" + depthBound);
			searchAlgo.setMaxDepth(depthBound);
			goalState = searchAlgo.search();
			if (goalState != null) {
				break;
			}
		}


		if (goalState == null) {
			infoOutput.println("EHC with helpful actions failed, using EHC with all actions");
			searchAlgo = new HillClimbingSearch(initialState);
			searchAlgo.setFilter(NullFilter.getInstance());
			searchAlgo.setSuccessorSelector(BestSuccessorSelector.getInstance());
			goalState = searchAlgo.search();
		}

		if (goalState == null) // if we can't find one
		{
			infoOutput.println("EHC failed, using best-first search, with all actions");

			// create a Best-First Searcher
			BestFirstSearch BFS = new BestFirstSearch(initialState);

			// ... change to using the 'all actions' neighbourhood (a null filter, as it
			// removes nothing)

			BFS.setFilter(NullFilter.getInstance());

			// and use that
			goalState = BFS.search();
		}
		*/

		return bestGoalState;
	}
}
