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

import java.util.ArrayList;
import net.vleu.visitomatic.VisitingMethod;
import net.vleu.visitomatic.Visitor;
import net.vleu.visitomatic.VisitorRunner;
import net.vleu.visitomatic.VisitorRunner.VisitorRunnerException;

/**
 * Allows to measure the size of a {@code VisitableList} and to convert it to an
 * array.
 * @param <T> The parametric type of the visited Lists
 */
public class VisitableListVisitor<T> implements Visitor {
    private final static VisitorRunner LENGTH_VISITOR =
            VisitorRunner.getInstance(VisitableListVisitor.class, "length");
    private final static VisitorRunner TO_ARRAY_VISITOR =
            VisitorRunner.getInstance(VisitableListVisitor.class, "toArray");

    @VisitingMethod(visitName="length")
    private int length(EmptyList<T> _) {
        return 0;
    }
    
    @VisitingMethod(visitName="length")
    private int length(ConsList<T> _, T __, VisitableList<T> tail) throws VisitorRunnerException {
        return 1 + length(tail);
    }

    public int length(VisitableList<T> l) throws VisitorRunnerException {
        return (Integer) LENGTH_VISITOR.visit(this, l);
    }

    @VisitingMethod(visitName="toArray")
    private ArrayList<T> toArray(EmptyList<T> _) {
        return new ArrayList<T>();
    }

    @VisitingMethod(visitName="toArray")
    private ArrayList<T> toArray(ConsList<T> _, T head, VisitableList<T> tail) throws VisitorRunnerException {
        ArrayList<T> rec = toArray(tail);
        rec.add(head);
        return rec;
    }
    
    public ArrayList<T> toArray(VisitableList<T> l) throws VisitorRunnerException {
        return TO_ARRAY_VISITOR.visit(this, l);
    }
}
