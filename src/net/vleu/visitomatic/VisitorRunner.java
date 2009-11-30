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

import java.util.concurrent.FutureTask;

/**
 * Allows to run a {@link Visitor} on a {@link Visitable}.<br/>
 *
 * <p>More specifically, when its
 * {@link ReflectionBasedVisitorRunner#visit(Visitor, Visitable)} method
 * is called, it will analyze the provided {@code Visitable}, extract the
 * fields annotated by {@code ToVisit} and use them as argument to call a
 * method annotated by {@code VisitingMethod} from the {@code Visitor}</p>
 *
 * <p>The expected usage is that there will be a private static final instance
 * of ReflectionBasedVisitorRunner per {@code Visitor} class, ready to be called
 * on instances when needed.</p>
 *
 * @see Visitor
 * @see Visitable
 */
public abstract class VisitorRunner {

    /**
     * Call {@link #getInstance} to get an instance.
     */
    protected VisitorRunner() {
    }

    /**
     * Analyzes a {@code Visitor} Class and and returns a {@code VisitorRunner}
     * ready to run on one or more {@code Visitor} of this class on one ore more
     * {@code Visitable}s.</p>
     * @param visitorClass The class of the {@code Visitor}s the {@code VisitorRunner} we will run
     * @param visitName The name of the visit, as given to {@link VisitingMethod}s in the {@code Visitor}
     * @return A {@code VisitorRunner} for {@code Visitor}s and {@code Visitable}s of those classes
     * @throws VisitorRunnerInitialisationException If we failed to analyze the visitor
     * @see Visitor
     * @see VisitingMethod
     */
    public final static VisitorRunner
            getInstance(Class<? extends Visitor> visitorClass, String visitName)
            throws VisitorRunnerInitialisationException {
        return new ReflectionBasedVisitorRunner(visitorClass, visitName);
    }

    /**
     * Analyzes the provided {@code Visitable}, extracts the
     * fields annotated by {@code ToVisit} and uses them as argument to call a
     * method annotated by {@code VisitingMethod} from the {@linkplain Visitor}
     * @param <R> The return type
     * @param visitor The visitor to run
     * @param visitable The visitable to visit
     * @return The return value from the suitable method of the visitor
     * @throws VisitorRunnerException  An error occurs during the running of the visitor
     * @see ToVisit
     * @see VisitingMethod
     */
    public abstract <R> R visit(Visitor visitor, Visitable visitable) throws VisitorRunnerException;

   /**
     * Return a {@link FutureTask} that analyzes the provided {@code Visitable},
     * extracts the fields annotated by {@code ToVisit} and uses them as argument
     * to call a method annotated by {@code VisitingMethod} from the
     * {@linkplain Visitor}
     * @param <R> The return type
     * @param visitor The visitor to run
     * @param visitable The visitable to visit
     * @return The return value from the suitable method of the visitor
     * @see ToVisit
     * @see VisitingMethod
     */
    public abstract <R> FutureTask<R> futureVisit(Visitor visitor, Visitable visitable);

    /**
     * Represents an error that occurred during the operation of a
     * <code>VisitorRunner</code>.
     */
    public final class VisitorRunnerException extends Exception {

        private static final long serialVersionUID = 1L;

        protected VisitorRunnerException(Throwable cause) {
            super(VisitorRunner.this + " failed with an exception", cause);
        }

        protected VisitorRunnerException(String message) {
            super(message);
        }
    }

    /**
     * Represents an error that occurred during the static analysis of a
     * <code>Visitor</code>.
     */
    public final class VisitorRunnerInitialisationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        protected VisitorRunnerInitialisationException(Throwable cause) {
            super(VisitorRunner.this + " failed with an exception", cause);
        }

        protected VisitorRunnerInitialisationException(String message) {
            super(message);
        }
    }
}
