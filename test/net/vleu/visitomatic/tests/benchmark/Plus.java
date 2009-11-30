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

import net.vleu.visitomatic.ToVisit;
import net.vleu.visitomatic.Visitable;

/**
 * Dummy class that one can visit. It contains two {@link PlusOrJustInteger}. Used only for benchmarks.
 */
final class Plus extends PlusOrJustInteger implements Visitable {

    @ToVisit(position = 0)
    public final PlusOrJustInteger a;
    @ToVisit(position = 1)
    public final PlusOrJustInteger b;

    Plus(PlusOrJustInteger a, PlusOrJustInteger b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public Integer accept(HandWrittenVisitor hw) {
        return hw.visit(a, b);
    }
}
