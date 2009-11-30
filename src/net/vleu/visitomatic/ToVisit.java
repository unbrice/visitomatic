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
package net.vleu.visitomatic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Those fields or methods, part of a {@link Visitable}, are visited by a
 * {@link Visitor}.<br />
 * The order in which elements are visited depend on the
 * {@link ToVisit#value()} attribute.
 * @see ToVisit#value()
 * @see Visitable
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})// This annotation can only be applied to methods and fields.
public @interface ToVisit {

    /**
     * The position into which to visit this field or method. The lowest positions
     * are visited first.
     * Two elements (method or field) cannot share a single position.<br />
     * Positions must be consecutive numbers starting from 0.
     */
    int position();
}
