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


/**
 * Dummy class that visit {@link PlusOrJustInteger} and sum its content. Used only for benchmarks.
 */
final class HandWrittenVisitor {

    Integer visit(Integer v) {
        return v;
    }

    Integer visit(PlusOrJustInteger a, PlusOrJustInteger b) {
        return a.accept(this) + b.accept(this);
    }

    Integer visit(PlusOrJustInteger plusOrInteger) {
        return plusOrInteger.accept(this);
    }
}
