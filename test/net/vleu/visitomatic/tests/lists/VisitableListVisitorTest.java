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
package net.vleu.visitomatic.tests.lists;

import java.util.Arrays;
import net.vleu.visitomatic.VisitorRunner.VisitorRunnerException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for VisitOMatic using VisitableListVisitor.
 */
public final class VisitableListVisitorTest {

    /** An empty list contained by sharedListOfSize1 */
    private VisitableList<String> emptyList;
    /** This list contains emptyList and is contained by consListOfSize5 */
    private VisitableList<String> sharedListOfSize1;
    /** A list of size 5 containing sharedListOfSize1 */
    private VisitableList<String> consListOfSize5;
    private VisitableListVisitor measurer;

    @Before
    public void setUp() {
        emptyList = new EmptyList<String>();
        sharedListOfSize1 = new ConsList<String>("a", emptyList);
        consListOfSize5 = new ConsList<String>("e", new ConsList<String>("d", new ConsList<String>("c", new ConsList<String>("b", sharedListOfSize1))));
        measurer = new VisitableListVisitor();
    }

    /**
     * Tests the toArray method, of class VisitableListVisitor.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testLength() throws VisitorRunnerException {
        assertEquals(0, measurer.length(emptyList));
        assertEquals(5, measurer.length(consListOfSize5));
        assertEquals(1, measurer.length(sharedListOfSize1));
    }

    /**
     * Tests the toArray method, of class VisitableListVisitor.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testToArray() throws VisitorRunnerException {
        String[] emptyArray = {};
        String[] abcdeArray = {"a","b","c","d","e"};
        String[] aArray = {"a"};
        assertEquals(Arrays.asList(emptyArray), measurer.toArray(emptyList));
        assertEquals(Arrays.asList(abcdeArray), measurer.toArray(consListOfSize5));
        assertEquals(Arrays.asList(aArray), measurer.toArray(sharedListOfSize1));
    }
}