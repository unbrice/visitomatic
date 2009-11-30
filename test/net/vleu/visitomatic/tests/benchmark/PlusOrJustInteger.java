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

import net.vleu.visitomatic.Visitable;

/**
 * Either a Plus or a JustInteger. Used only for benchmarks.
 */
abstract class PlusOrJustInteger implements Visitable {

    abstract Integer accept(HandWrittenVisitor hw);

    /** Generates a PlusOrJustInteger tree of the specified height */
    public static PlusOrJustInteger buildTree(int height) {
        if (height == 0)
            return new JustInteger();
        PlusOrJustInteger recursive = buildTree(height - 1);
        Plus plus = new Plus(recursive, recursive);
        return plus;
    }
}
