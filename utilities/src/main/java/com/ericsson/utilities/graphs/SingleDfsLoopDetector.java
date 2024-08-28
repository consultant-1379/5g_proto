/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 31, 2021
 *     Author: epaxale
 */

package com.ericsson.utilities.graphs;

import java.util.*;

public class SingleDfsLoopDetector
{
    private int numOfVertices;
    private int time;
    private LinkedList<Integer> adjacencyList[];
    private List<List<Integer>> results = new ArrayList<>();
    private List<Integer> caseEdges = new ArrayList<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SingleDfsLoopDetector(int v)
    {
        numOfVertices = v;
        adjacencyList = new LinkedList[v];

        for (int i = 0; i < v; ++i)
            adjacencyList[i] = new LinkedList();

        time = 0;
    }

    public void addEdge(int v,
                        int w)
    {
        adjacencyList[v].add(w);
    }

    private void dfsTraversalUtil(int nextVertex,
                                  int lowLink[],
                                  int discoveryTimes[],
                                  boolean stackMember[],
                                  Stack<Integer> stack)
    {

        discoveryTimes[nextVertex] = time;
        lowLink[nextVertex] = time;
        time += 1;
        stackMember[nextVertex] = true;
        stack.push(nextVertex);

        int n;

        Iterator<Integer> i = adjacencyList[nextVertex].iterator();

        while (i.hasNext())
        {
            n = i.next();

            if (discoveryTimes[n] == -1)
            {
                dfsTraversalUtil(n, lowLink, discoveryTimes, stackMember, stack);

                lowLink[nextVertex] = Math.min(lowLink[nextVertex], lowLink[n]);
            }
            else if (stackMember[n] == true)
            {
                lowLink[nextVertex] = Math.min(lowLink[nextVertex], discoveryTimes[n]);
            }
        }

        int w = -1;
        if (lowLink[nextVertex] == discoveryTimes[nextVertex])
        {
            while (w != nextVertex)
            {
                w = (int) stack.pop();
                caseEdges.add(w);
                stackMember[w] = false;
            }

            results.add(caseEdges);
            caseEdges = new ArrayList<>();
        }
    }

    public List<List<Integer>> dfsTraversal()
    {
        int discoveryTimes[] = new int[numOfVertices];
        int lowLink[] = new int[numOfVertices];
        for (int i = 0; i < numOfVertices; i++)
        {
            discoveryTimes[i] = -1;
            lowLink[i] = -1;
        }

        boolean stackMember[] = new boolean[numOfVertices];
        Stack<Integer> st = new Stack<Integer>();

        for (int i = 0; i < numOfVertices; i++)
        {
            if (discoveryTimes[i] == -1)
                dfsTraversalUtil(i, lowLink, discoveryTimes, stackMember, st);
        }

        return results;
    }
}