/**
 *
 * Copyright 1999-2012 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

package com.github.steveash.jopenfst.operations;

import com.github.steveash.jopenfst.Fst;
import com.github.steveash.jopenfst.io.Convert;
import com.github.steveash.jopenfst.FstInputOutput;
import com.github.steveash.jopenfst.semiring.TropicalSemiring;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Compose Testing for
 *
 * Examples provided by M. Mohri, "Weighted Automata Algorithms", Handbook of Weighted Automata, Springer-Verlag, 2009,
 * pp. 213–254.
 *
 * @author John Salatas <jsalatas@users.sourceforge.net>
 */
public class ComposeTest {

  @Test
  public void testCompose() {
    System.out.println("Testing Composition...");
    Fst fstA = Convert.importFst("data/tests/algorithms/compose/A",
                                 new TropicalSemiring());
    Fst fstB = Convert.importFst("data/tests/algorithms/compose/B",
                                 new TropicalSemiring());
    Fst composed = FstInputOutput
        .loadModel("data/tests/algorithms/compose/fstcompose.fst.ser");

    Fst fstComposed = Compose.get(fstA, fstB, new TropicalSemiring());

    assertTrue(composed.equals(fstComposed));

    System.out.println("Testing Composition Completed!\n");
  }
}
