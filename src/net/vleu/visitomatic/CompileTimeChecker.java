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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * This annotation processor does compile-time safety checks of the usage
 * of VisitOMatic annotations : {@link ToVisit} and  {@link VisitingMethod}.
 */
@SupportedAnnotationTypes(value = {"net.vleu.visitomatic.VisitingMethod",
    "net.vleu.visitomatic.ToVisit"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class CompileTimeChecker extends AbstractProcessor {

    /** {@inheritDoc} */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        /* Process each annotation */
        processToVisit(roundEnv);
        /* Claim them */
        return true;
    }

    /**
     * Prints a message of the specified kind at the location of the element.
     * @see Messager#printMessage(Diagnostic.Kind, CharSequence, Element)
     * @param kind the kind of message
     * @param msg the message, or an empty string if none
     * @param e the element to use as a position hint
     */
    private void printMessage(Diagnostic.Kind kind,
            CharSequence msg, Element e) {
        processingEnv.getMessager().printMessage(kind, msg, e);
    }

    /**
     * Checks that the supplied method has no arguments.
     * @param method the method to check
     */
    private void processMethodsToVisit(ExecutableElement method) {
        // Check that it takes no arguments
        if (method.getParameters().size() != 0) {
            printMessage(Diagnostic.Kind.ERROR,
                    "Methods ToVisit must take no arguments",
                    method);
        }
    }

    /**
     * For each list of positions, checks that it defines only positions
     * which are consecutive numbers starting from 0.
     * @param collectionOfPositions The collection to check
     */
    private void checkPositionsToVisit(Collection<ArrayList<PositionAndElement>> collectionOfPositions) {
        /* For each list of positions… */
        for (ArrayList<PositionAndElement> positions : collectionOfPositions) {
            // Sort it
            Collections.sort(positions);
            // Check that the sorted values are from 0 to positions.size()-1
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i).position != i) {
                    printMessage(Diagnostic.Kind.ERROR,
                            "Positions must be consecutive numbers starting from 0",
                            positions.get(i).element);
                }
            }
        }
    }

    /**
     * Do safety checks on the usage of {@code ToVisit}.
     * @param roundEnv The environment to check
     */
    private void processToVisit(RoundEnvironment roundEnv) {
        Map<Element, ArrayList<PositionAndElement>> elementsToPositions =
                new HashMap<Element, ArrayList<PositionAndElement>>();
        /* For each annotated element… */
        for (Element element : roundEnv.getElementsAnnotatedWith(ToVisit.class)) {
            Element enclosing = element.getEnclosingElement();
            /* Create the array of positions for the enclosing element, if necessary */
            if (!elementsToPositions.containsKey(enclosing)) {
                elementsToPositions.put(enclosing,
                        new ArrayList<PositionAndElement>());
            }
            /* Register its position */
            ToVisit annotation = element.getAnnotation(ToVisit.class);
            elementsToPositions.get(enclosing).add(new PositionAndElement(
                    annotation.position(), element));
            /* If it is a method, check its arguments */
            if (element.getKind() == ElementKind.METHOD) {
                processMethodsToVisit((ExecutableElement) element);
            }
        }
        /* Check positions */
        checkPositionsToVisit(elementsToPositions.values());
    }

    /**
     * This is a {@code ToVisit} position, along with the corresponding element
     */
    private final static class PositionAndElement implements Comparable<PositionAndElement> {

        /** The position of the element in the visit */
        public final int position;
        /** The element to visit */
        public final Element element;

        /** Build a new pair */
        public PositionAndElement(int position, Element element) {
            this.position = position;
            this.element = element;
        }

        /**
         * {@inheritDoc}
         * The objects are compared using their positions.
         */
        @Override
        public int compareTo(PositionAndElement other) {
            return position - other.position;
        }

        /**
         * {@inheritDoc}
         * Two objects are equal if their positions are.
         */
        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            } else if (!(other instanceof PositionAndElement)) {
                return false;
            } else {
                return position == ((PositionAndElement) other).position;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 41 * 7 + this.position;
        }
    }
}
