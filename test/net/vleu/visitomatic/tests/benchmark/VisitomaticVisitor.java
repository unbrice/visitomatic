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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import net.vleu.visitomatic.VisitingMethod;
import net.vleu.visitomatic.Visitor;
import net.vleu.visitomatic.VisitorRunner;
import net.vleu.visitomatic.VisitorRunner.VisitorRunnerException;

final class VisitomaticVisitor implements Visitor {

    private final static VisitorRunner SUM_RUNNER =
            VisitorRunner.getInstance(VisitomaticVisitor.class, "sum");

    @VisitingMethod(visitName="sum")
    private Integer sum(Plus _, PlusOrJustInteger a, PlusOrJustInteger b) throws VisitorRunnerException {
        return visit(a) + visit(b);
    }

    @VisitingMethod(visitName="sum")
    private Integer sum(JustInteger _, Integer value) {
        return value;
    }

    public Integer visit(PlusOrJustInteger it) throws VisitorRunnerException {
        return SUM_RUNNER.visit(this, it);
    }

    public Integer visitInParallel(Plus it) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureA = SUM_RUNNER.futureVisit(this, it.a);
        FutureTask<Integer> futureB = SUM_RUNNER.futureVisit(this, it.b);
        new Thread(futureA).start();
        futureB.run();
        return futureA.get() + futureB.get();
    }
}
