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

import net.vleu.visitomatic.ToVisit;

/**
 * A visitable linked list used only for tests.
 */
public class ConsList<T> implements VisitableList<T> {

    @ToVisit(position = 0)
    private final T head;

    @ToVisit(position = 1)
    public final VisitableList<T> getTail() {
        return tail;
    }
    private final VisitableList<T> tail;

    @Override
    public String toString() {
        return "<" + head + "," + tail + ">";
    }

    public ConsList(T head, VisitableList<T> tail) {
        this.head = head;
        this.tail = tail;
    }
}
