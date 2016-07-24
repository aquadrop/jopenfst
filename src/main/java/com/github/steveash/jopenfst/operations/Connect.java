/*
 * Copyright 2014 Steve Ash
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.steveash.jopenfst.operations;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.github.steveash.jopenfst.Arc;
import com.github.steveash.jopenfst.Fst;
import com.github.steveash.jopenfst.MutableFst;
import com.github.steveash.jopenfst.State;
import com.github.steveash.jopenfst.semiring.Semiring;

import java.util.ArrayList;

/**
 * Connect operation.
 *
 * @author John Salatas <jsalatas@users.sourceforge.net>
 */
public class Connect {

  /**
   * Calculates the coaccessible states of an fst
   */
  private static void calcCoAccessible(Fst fst, State state,
                                       ArrayList<ArrayList<State>> paths,
                                       ArrayList<State> coaccessible) {
    // hold the coaccessible added in this loop
    ArrayList<State> newCoAccessibles = new ArrayList<>();
    for (ArrayList<State> path : paths) {
      int index = path.lastIndexOf(state);
      if (index != -1) {
        if (state.getFinalWeight() != fst.getSemiring().zero()
            || coaccessible.contains(state)) {
          for (int j = index; j > -1; j--) {
            if (!coaccessible.contains(path.get(j))) {
              newCoAccessibles.add(path.get(j));
              coaccessible.add(path.get(j));
            }
          }
        }
      }
    }

    // run again for the new coaccessibles
    for (State s : newCoAccessibles) {
      calcCoAccessible(fst, s, paths, coaccessible);
    }
  }

  /**
   * Copies a path
   */
  private static void duplicatePath(int lastPathIndex, State fromState,
                                    State toState, ArrayList<ArrayList<State>> paths) {
    ArrayList<State> lastPath = paths.get(lastPathIndex);
    // copy the last path to a new one, from start to current state
    int fromIndex = lastPath.indexOf(fromState);
    int toIndex = lastPath.indexOf(toState);
    if (toIndex == -1) {
      toIndex = lastPath.size() - 1;
    }
    ArrayList<State> newPath = Lists.newArrayList(lastPath.subList(fromIndex, toIndex));
    paths.add(newPath);
  }

  /**
   * The depth first search recursion
   */
  private static State dfs(Fst fst, State start,
                           ArrayList<ArrayList<State>> paths, ArrayList<Arc>[] exploredArcs,
                           ArrayList<State> accessible) {
    int lastPathIndex = paths.size() - 1;

    ArrayList<Arc> currentExploredArcs = exploredArcs[start.getId()];
    paths.get(lastPathIndex).add(start);
    if (start.getNumArcs() != 0) {
      int arcCount = 0;
      int numArcs = start.getNumArcs();
      for (int j = 0; j < numArcs; j++) {
        Arc arc = start.getArc(j);
        if ((currentExploredArcs == null)
            || !currentExploredArcs.contains(arc)) {
          lastPathIndex = paths.size() - 1;
          if (arcCount++ > 0) {
            duplicatePath(lastPathIndex, fst.getStartState(), start,
                          paths);
            lastPathIndex = paths.size() - 1;
            paths.get(lastPathIndex).add(start);
          }
          State next = arc.getNextState();
          addExploredArc(start.getId(), arc, exploredArcs);
          // detect self loops
          if (next.getId() != start.getId()) {
            dfs(fst, next, paths, exploredArcs, accessible);
          }
        }
      }
    }
    lastPathIndex = paths.size() - 1;
    accessible.add(start);

    return start;
  }

  /**
   * Adds an arc top the explored arcs list
   */
  private static void addExploredArc(int stateId, Arc arc,
                                     ArrayList<Arc>[] exploredArcs) {
    if (exploredArcs[stateId] == null) {
      exploredArcs[stateId] = Lists.newArrayList();
    }
    exploredArcs[stateId].add(arc);

  }

  /**
   * Initialization of a depth first search recursion
   */
  private static void depthFirstSearch(Fst fst, ArrayList<State> accessible,
                                       ArrayList<ArrayList<State>> paths,
                                       ArrayList<Arc>[] exploredArcs,
                                       ArrayList<State> coaccessible) {
    State currentState = fst.getStartState();
    State nextState = currentState;
    do {
      if (!accessible.contains(currentState)) {
        nextState = dfs(fst, currentState, paths, exploredArcs,
                        accessible);
      }
    } while (currentState.getId() != nextState.getId());
    int numStates = fst.getStateCount();
    for (int i = 0; i < numStates; i++) {
      State s = fst.getState(i);
      if (s.getFinalWeight() != fst.getSemiring().zero()) {
        calcCoAccessible(fst, s, paths, coaccessible);
      }
    }
  }

  /**
   * Trims an Fst, removing states and arcs that are not on successful paths.
   *
   * @param fst the fst to trim
   */
  public static void apply(MutableFst fst) {
    Semiring semiring = fst.getSemiring();
    Preconditions.checkNotNull(semiring);

    ArrayList<State> accessible = new ArrayList<>();
    ArrayList<State> coaccessible = new ArrayList<>();
    @SuppressWarnings("unchecked")
    ArrayList<Arc>[] exploredArcs = new ArrayList[fst.getStateCount()];
    for (int i = 0; i < fst.getStateCount(); i++) {
      exploredArcs[i] = null;
    }
    ArrayList<ArrayList<State>> paths = new ArrayList<>();
    paths.add(new ArrayList<State>());

    depthFirstSearch(fst, accessible, paths, exploredArcs, coaccessible);

    ArrayList<State> toDelete = new ArrayList<>();

    int numStates = fst.getStateCount();
    for (int i = 0; i < numStates; i++) {
      State s = fst.getState(i);
      if (!(accessible.contains(s) || coaccessible.contains(s))) {
        toDelete.add(s);
      }
    }

    fst.deleteStates(toDelete);
  }
}
