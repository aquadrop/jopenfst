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

import com.github.steveash.jopenfst.Arc;
import com.github.steveash.jopenfst.Fst;
import com.github.steveash.jopenfst.MutableFst;
import com.github.steveash.jopenfst.State;
import com.github.steveash.jopenfst.semiring.Semiring;

/**
 * Reverse operation.
 *
 * @author John Salatas <jsalatas@users.sourceforge.net>
 */
public class Reverse {

  /**
   * Default Constructor
   */
  private Reverse() {
  }

  /**
   * Reverses an fst
   *
   * @param infst the fst to reverse
   * @return the reversed fst
   */
  public static MutableFst reverse(Fst infst) {
    infst.throwIfInvalid();

    MutableFst fst = ExtendFinal.apply(infst);

    Semiring semiring = fst.getSemiring();

    MutableFst res = new MutableFst(fst.getStateCount(), semiring);
    res.setInputSymbolsFromThatOutput(fst);
    res.setOutputSymbolsFromThatInput(fst);

    State[] stateMap = new State[fst.getStateCount()];
    int numStates = fst.getStateCount();
    for (int i = 0; i < numStates; i++) {
      State is = fst.getState(i);
      State s = new State(semiring.zero());
      res.addState(s);
      stateMap[is.getId()] = s;
      if (semiring.isNotZero(is.getFinalWeight())) {
        res.setStart(s);
      }
    }

    stateMap[fst.getStartState().getId()].setFinalWeight(semiring.one());

    for (int i = 0; i < numStates; i++) {
      State olds = fst.getState(i);
      State news = stateMap[olds.getId()];
      int numArcs = olds.getNumArcs();
      for (int j = 0; j < numArcs; j++) {
        Arc olda = olds.getArc(j);
        State next = stateMap[olda.getNextState().getId()];
        Arc newa = new Arc(olda.getIlabel(), olda.getOlabel(),
                           semiring.reverse(olda.getWeight()), news);
        next.addArc(newa);
      }
    }

    return res;
  }
}
