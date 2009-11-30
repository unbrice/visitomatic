/*
 * Copyright 2009 Brice Arnould
 *
 * This file is part of VisitOMatic.
 *
 * VisitOMatic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VisitOMatic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VisitOMatic.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.vleu.visitomatic.tests.benchmark;

import java.util.concurrent.ExecutionException;
import net.vleu.visitomatic.VisitorRunner.VisitorRunnerException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the performance of a simple arithmetic tree implementation.
 */
public final class PlusOrJustIntegerTest {

    private final static int HEIGHT = 24; // Usually 24
    private static final Plus TREE =
            (Plus) PlusOrJustInteger.buildTree(HEIGHT);
    private static long HANDWRITTEN_VISITOR_TIME;

    @BeforeClass
    public static void calibrate() {
        final HandWrittenVisitor handWrittenVisitor = new HandWrittenVisitor();
        System.gc(); // Garbage collect before the test so it won't alter the results
        final long before = System.currentTimeMillis();
        handWrittenVisitor.visit(TREE);
        HANDWRITTEN_VISITOR_TIME = System.currentTimeMillis() - before;
        System.out.println("HandWritten visitor took " +
                           HANDWRITTEN_VISITOR_TIME + "ms");
    }

    /** Check that the time taken by the automatic visitor is of the same
     * order of magnitude than the handwritten one, and that they take the same.
     */
    @Test
    public void testPerformances() throws VisitorRunnerException {
        final VisitomaticVisitor visitomaticVisitor = new VisitomaticVisitor();
        System.gc(); // Garbage collect before the test so it won't alter the results
        final long before = System.currentTimeMillis();
        final int result = visitomaticVisitor.visit(TREE);
        final long duration = System.currentTimeMillis() - before;
        System.out.println("Generated visitor took " + duration + "ms");
        assertTrue("The generated visitor returned an invalid result",
                   result == 1 << HEIGHT);
        assertTrue("The generated visitor took too much time",
                   duration < HANDWRITTEN_VISITOR_TIME * 10);
    }

   /** Check that the automatic visitor is thread-safe.
    */
    @Test
    public void testParallelism() throws InterruptedException, ExecutionException {
        final VisitomaticVisitor visitomaticVisitor = new VisitomaticVisitor();
        System.gc(); // Garbage collect before the test so it won't alter the results
        final long before = System.currentTimeMillis();
        final int result = visitomaticVisitor.visitInParallel(TREE);
        final long duration = System.currentTimeMillis() - before;
        System.out.println("Generated visitor, ran in parallel, took " + duration + "ms");
        assertTrue("The generated visitor returned an invalid result",
                   result == 1 << HEIGHT);
    }
}