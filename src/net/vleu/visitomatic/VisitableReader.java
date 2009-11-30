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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * A VisitableReader gives informations about a {@link Visitable}.<br />
 * More specifically, they analyze fields marked with {@link ToVisit} and
 * give informations about them.<br />
 * A VisitableReader built for a class can be used many times for all
 * instances of this class. Doing so is more efficient.
 * @see Visitable
 * @see ToVisit
 */
public class VisitableReader {

    /** The Visitable class associated to this VisitableReader */
    private final Class<? extends Visitable> visitableClass;
    /** The fields we care about, this doesn't change for a given VisitableReader. */
    private final Field[] fieldsToVisit;
    /** The methods we care about, this doesn't change for a given VisitableReader. */
    private final Method[] methodsToVisit;
    /** The maximum of size of fieldsToVisit and size of methodsToVisit */
    private final int numberOfAccessiblesToVisit;
    /** The return value for {@link #readTypes()}.<br />
     * Caching it led to a 20% improvement on performance in the 'sum' benchmark */
    private final Class[] valuesTypes;

    /**
     * Builds a new VisitableReader.<br />
     * A VisitableReader built for a class can be used many times for all
     * instances of this class. Doing so is more efficient.
     * @param visitableClass The class for which to build a VisitableReader
     */
    public VisitableReader(Class<? extends Visitable> visitableClass) {
        this.visitableClass = visitableClass;
        /* Read fields */
        Field[] declaredFields = visitableClass.getDeclaredFields();
        fieldsToVisit = selectAccessibleObjectsToVisit(declaredFields,
                                                       Field.class);
        /* Read methods */
        Method[] declaredMethods = visitableClass.getDeclaredMethods();
        methodsToVisit = selectAccessibleObjectsToVisit(declaredMethods,
                                                        Method.class);
        /* Set the other private variables */
        numberOfAccessiblesToVisit = Math.max(fieldsToVisit.length,
                                              methodsToVisit.length);
        valuesTypes = readTypesFromVisitable();
        /* And finally, do some safety check */
        if (fieldsToVisit.length < methodsToVisit.length)
            validatePositions(fieldsToVisit, methodsToVisit);
        else
            validatePositions(methodsToVisit, fieldsToVisit);
    }

    /**
     * Ensure that, for each index from 0 to biggest.length, both arrays defines
     * one and only one value.
     * @param smallest The smallest of the two arrays to check
     * @param biggest The biggest of the two arrays to check.
     * @throws java.lang.IllegalArgumentException If both arrays define a value or no value for a given index
     */
    private void validatePositions(AccessibleObject[] smallest, AccessibleObject[] biggest) throws IllegalArgumentException {
        final String errorMsg = "Invalid position for fields and methods to visit in ";
        /* Check that each positions as one and only one value, for the first positions */
        for (int i = 0; i < smallest.length; i++)
            if (smallest[i] == null && biggest[i] == null ||
                smallest[i] != null && biggest[i] != null)
                throw new IllegalArgumentException(errorMsg + visitableClass + " at position " + i);
        /* Check that there is no null value in biggest after the end of smallest array */
        for (int i = smallest.length; i < biggest.length; i++)
            if (biggest[i] == null)
                throw new IllegalArgumentException(errorMsg + visitableClass + " at position " + i);
    }

    /**
     * Goes through the array of {@link AccessibleObject} and select those
     * annotated by {@code ToVisit}.
     * @param declared The declared objects.
     * @param resultType The type of elements in the array that we will return.
     * @return An array of {@link AccessibleObject}, the position of the object
     *         in this array corresponds to the position described in the
     *         {@code ToVisit} annotation.
     */
    private <T extends AccessibleObject> T[] selectAccessibleObjectsToVisit(T[] declared, Class<T> resultType) {
        /* Go through all fields, find the annotated one and record them and
         * their positions */
        final ArrayList<T> accessibles =
                new ArrayList<T>();
        final ArrayList<Integer> positions = new ArrayList<Integer>();
        int maxPosition = -1;
        for (final T accessible : declared) {
            final ToVisit annotation = accessible.getAnnotation(ToVisit.class);
            if (null != annotation) {
                accessible.setAccessible(true);
                accessibles.add(accessible);
                positions.add(annotation.position());
                if (annotation.position() > maxPosition)
                    maxPosition = annotation.position();
            }
        }
        /* Put them in the result array at their declared positions */
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(resultType, maxPosition + 1);
        for (int i = 0; i < accessibles.size(); ++i)
            result[positions.get(i)] = accessibles.get(i);
        return result;
    }

    /**
     * Reads the values from fields and methods marked with {@link ToVisit} in
     * the provided {@code Visitable} and returns them as an array of objects.
     * @param visitable The {@code Visitable} to inspect
     * @return An array containing the visitable and the values of its fields
     *         and methods marked with {@code ToVisit}, in the order specified
     *         by {@code ToVisit}
     * @throws IllegalArgumentException The visitable isn't
     * @throws IllegalAccessException Java refuses to grant us the right to inspect this object
     * @throws InvocationTargetException We tried to invoke a method which threw an exception
     * @see ToVisit
     */
    public Object[] readValues(Visitable visitable) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Object[] result = new Object[getNumberOfValues()];
        readValues(visitable, result);
        return result;
    }

    /**
     * Reads the values from fields and methods marked with {@link ToVisit} in
     * the provided {@code Visitable} and put their value in the provided
     * array of objects.<br />
     * Overloading this method allows to change the way values are allocated.
     * @param visitable The {@code Visitable} to inspect
     * @param result An array that will be modified to containing the visitable and the
     *        values of its fields and methods marked with {@code ToVisit}, in
     *        the order specified by {@code ToVisit}.<br />
     *        <b>The size of this array must be the return value of
     *        {@link #getNumberOfValues}.</b>
     * @throws IllegalArgumentException The visitable isn't
     * @throws IllegalAccessException Java refuses to grant us the right to inspect this object
     * @throws InvocationTargetException We tried to invoke a method which threw an exception
     * @see ToVisit
     */
    protected void readValues(Visitable visitable, Object[] result) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (result.length != getNumberOfValues())
            throw new IllegalAccessException("The supplied 'result' array is of size " +
                                             result.length +
                                             " whereas it should be of size " +
                                             getNumberOfValues());
        result[0] = visitable;
        for (int n = 0; n < fieldsToVisit.length; ++n)
            if (fieldsToVisit[n] != null)
                result[1 + n] = fieldsToVisit[n].get(visitable);
        for (int n = 0; n < methodsToVisit.length; ++n)
            if (methodsToVisit[n] != null)
                result[1 + n] = methodsToVisit[n].invoke(visitable,
                                                         (Object[]) null);
    }

    /**
     * @return the size of the return value of {@link #readValues(Visitable)},
     * which is also the size of the second argument of
     * {@link #readValues(Visitable, Object[])}<br />
     * <b>This value do not change during the lifetime of the object.</b>
     */
    public int getNumberOfValues() {
        return numberOfAccessiblesToVisit+1;
    }

    /**
     * Reads the values from fields and methods marked with {@link ToVisit} in
     * the {@code Visitable} and returns their types as an array of Classes.
     * @return An array containing the Class of visitable and the Class of
     *         its values (methods and fields) marked with {@code ToVisit},
     *         in the order specified by {@code ToVisit}
     * @see ToVisit
     */
    Class[] readTypes() {
        return valuesTypes;
    }

   /**
     * Does the same as {@link #readTypes} but without relying on
    * {@link #valuesTypes}.<br />
    * It is used to initialize {@link #valuesTypes}.
     * @return An array containing the Class of visitable and the Class of
     *         its values (methods and fields) marked with {@code ToVisit},
     *         in the order specified by {@code ToVisit}
     * @throws java.lang.IllegalArgumentException The visitable isn't
     * @throws java.lang.IllegalAccessException Java refuses to grant us the right to inspect this object
     * @see ToVisit
     */
    private Class[] readTypesFromVisitable() {
        final Class[] result = new Class[numberOfAccessiblesToVisit + 1];
        result[0] = visitableClass;
        for (int n = 0; n < fieldsToVisit.length; ++n)
            if (fieldsToVisit[n] != null)
                result[1 + n] = fieldsToVisit[n].getType();
        for (int n = 0; n < methodsToVisit.length; ++n)
            if (methodsToVisit[n] != null)
                result[1 + n] = methodsToVisit[n].getReturnType();
        return result;
    }
}
