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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

/**
 * This {@link VisitorRunner} is implemented by reflection. It doesn't have
 * dependences on third-party libraries.
 */
final class ReflectionBasedVisitorRunner extends VisitorRunner {

    /** The class of the visitors we will run */
    private final Class visitorClass;
    /** The name of the visit, we will ignore methods that are not annotated with it */
    private final String visitName;
    /** Associate to arrays of types methods that accept them as arguments */
    private final Map<ParametersList, Method> parametersToMethods;
    /** Associate to classes of {@code Visitable} the {@link VisitableReader}
     * that can parse them */
    private final Map<Class<? extends Visitable>, VisitableReader> visitableToReaders;

    /**
     * Inspect the {@link #visitorClass} and register all its VisitingMethods
     * in the {@link #parametersToMethods} map.
     */
    private void registerMethods() throws VisitorRunnerInitialisationException {
        try {
            for (final Method m : visitorClass.getDeclaredMethods()) {
                /* Look for the right annotation */
                final VisitingMethod annotation =
                        m.getAnnotation(VisitingMethod.class);
                if (annotation != null && visitName.equals(
                        annotation.visitName())) {
                    /* Found it ! Let's register this method */
                    ParametersList parameters = new ParametersList(m.
                            getParameterTypes());
                    m.setAccessible(true);
                    parametersToMethods.put(parameters, m);
                }
            }
        }
        catch (Exception ex) {
            throw new VisitorRunnerInitialisationException(ex);
        }
    }

    /** Retrieves a method annotated by {@code @VisitingMethod} that takes the provided types as arguments*/
    private Method getMethod(Class[] parameterTypes) throws VisitorRunnerException {
        final Method result = parametersToMethods.get(new ParametersList(
                parameterTypes));
        if (result == null)
            throw new VisitorRunnerException(
                    "Failed to find a method for visit '" +
                    visitName + "' taking as arguments :" +
                    Arrays.toString(parameterTypes));
        else
            return result;
    }

    /**
     * Analyzes a {@code Visitor} Class and prepares itself
     * to run on one or more {@code Visitor} of this class on one ore more
     * {@code Visitable}s.</p>
     * @param visitorClass The class of the Visitor we will run
     * @param visitName The name of the visit, as given to {@link VisitingMethod}s in the {@code Visitor}
     * @see Visitor
     * @see VisitingMethod
     */
    protected ReflectionBasedVisitorRunner(Class<? extends Visitor> visitorClass, String visitName) throws VisitorRunnerInitialisationException {
        this.visitName = visitName;
        this.visitorClass = visitorClass;
        parametersToMethods = new HashMap<ParametersList, Method>();
        visitableToReaders =
                new ConcurrentHashMap<Class<? extends Visitable>, VisitableReader>();
        registerMethods();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "<VisitorRunner for " + visitorClass.getName() + '#' + visitName +
               '>';
    }

    /** {@inheritDoc} */
    @Override
    public <R> R visit(Visitor visitor, Visitable visitable) throws VisitorRunnerException {
        final Class<? extends Visitable> visitableClass = visitable.getClass();
        /* Get the VisitableReader from the cache, or put it in the cache */
        VisitableReader reader = visitableToReaders.get(visitable.getClass());
        if (reader == null) {
            reader = new TLSVisitableReader(visitableClass);
            visitableToReaders.put(visitableClass, reader);
        }
        /* Get the method from the Visitor */
        final Method method = getMethod(reader.readTypes());
        /* Call it with the visitName from the Visitable */
        try {
            @SuppressWarnings("unchecked")
            R result = (R) method.invoke(visitor, reader.readValues(visitable));
            return result;
        }
        catch (Exception ex) {
            throw new VisitorRunnerException(ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <R> FutureTask<R> futureVisit(final Visitor visitor, final Visitable visitable) {
        final Callable<R> callable = new Callable<R>() {

            /** {@inheritDoc} */
            @Override
            public R call() throws VisitorRunnerException {
                // Type infence don't work well, we have to indicate manually the type
                return ReflectionBasedVisitorRunner.this.<R>visit(visitor, visitable);
            }
        };
        return new FutureTask<R>(callable);
    }

    /**
     * <p>This version of {@link VisitableReader} use Thread Local Storage to
     * minimize the number of allocations. <b>It is not reentrant.</b><br/>
     * It  reduced drastically the number of allocations and reduced the time on
     * the Tree benchmark by 25%</p>
     * @see VisitableReader
     */
    private static final class TLSVisitableReader extends VisitableReader {

        /** Provides arrays suitable for being the second argument of
         * {@link #readValues(Visitable, Object[])} */
        ThreadLocal<Object[]> arrayProvider;

        public TLSVisitableReader(Class<? extends Visitable> visitableClass) {
            super(visitableClass);
            arrayProvider = new ThreadLocal<Object[]>() {
                /* {@inheritDoc}
                 * <p>This version returns an empty array of a size suitable for use with
                 * {@link VisitableReader#readValues(Visitable, Object[])}</p>
                 */

                @Override
                public Object[] initialValue() {
                    return new Object[getNumberOfValues()];
                }
            };
        }

        /**
         * {@inheritDoc}
         * <p>This version use Thread Local Storage to minimize the number of allocations. <b>It is not reentrant.</b></p>
         */
        @Override
        public Object[] readValues(Visitable visitable) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            Object[] result = arrayProvider.get();
            readValues(visitable, result);
            return result;
        }
    }

    /**
     * A List of parameters.<br />
     * This class exists because profiling as shown that the use of Java's
     * generic containers for Parameters classes were of high cost (35% of the
     * total runtime on a benchmark I ran). The hash and equals function where
     * the most expensive.
     */
    private static final class ParametersList {

        private final Class[] classArray;
        private final int hash;

        /**
         * Build a new ParametersList that contains the provided array. This
         * array must not be modified.
         * @param classArray
         */
        public ParametersList(final Class[] classArray) {
            this.classArray = classArray;
            hash = Arrays.hashCode(classArray);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof ParametersList) {
                final ParametersList otherClassList = (ParametersList) other;
                /* First test the hashs, this is really fast
                 * we could also use length but on my tests it wasn't helping
                 */
                if (otherClassList.hash != hash)
                    return false;
                /* Only then, go through the array */
                for (int i = 0; i < classArray.length; i++)
                    if (otherClassList.classArray[i] != classArray[i])
                        return false;
                /* Didn't found any diference */
                return true;
            }
            else
                return false;
        }
    }
}
