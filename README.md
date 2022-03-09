# visitomatic

Visitomatic implements pattern matching in Java, à la Haskell, Caml and
others.

An annotation marks what is to be matched, the relevant fields & methods
of the object. The match is made against overloaded methods of an
object.

This is in fact the [visitor pattern](http://www.oodesign.com/visitor-pattern.html), hence the name.

## Background

Object-oriented languages such as Java tend to structure the code
according to data more than according to functions, attaching the
functions (called "methods") to the objects. Functional languages on the
other side tend to structure the code according to functions, using
pattern matching to dispatch the data.

Visitomatic allows you to structure *some parts* of your program in the
functional way, for when it makes a difference.

# Step by step example

## An example problem

Let's consider binary trees whose nodes are an operator and leaves are
integers.

A `treeSum` function for summing the value of those trees would actually
be split in many classes when written in Java, like this:

``` java
interface OperatorOrJustInteger {
    public int treeSum();
}

class JustInteger implements OperatorOrJustInteger {
  private int value = 1;
  // …
  public int treeSum() {
    return value;
  }
}

class Plus implements OperatorOrJustInteger {
  private OperatorOrJustInteger left;
  private OperatorOrJustInteger right;
  // …
  public int treeSum() {
     return left.treeSum() + right.treeSum();
  }
}
```

## What Visitomatic can bring

In the example above, the "plus" operation is distributed over many
classes. Often this is OK but certain classes of softwares are much
easier to read if the whole operation is at one place.

The classic way of doing this in Object-Oriented programing languages,
is the so-called [visitor
pattern](http://www.oodesign.com/visitor-pattern.html). But in Java,
this requires writing boilerplate code which most of the time is not
type-safe.

Visitomatic allows you to make the design pattern explicit, supressing
the need for this tedious code. And it does that without relying on an
external pre-processor, using Java annotations instead (plus it is fast,
comes with extensive JavaDoc, is thread-safe, type-safe and hand-made
`fa-smile-o`).

I will demonstrate how to use VisitOMatic on the `treeSum` function
presented above. I've chosen this example because of its simplicity, but
keep in mind that VisitOMatic is more usefull when the visited objects
are complex.

## Visitomatic in this context

As we said before, an element of this tree is either an `Integer` or a
`Plus` node with two children. Here is a possible definition for a such
tree, please note that it does not bundle the implementation of
`treeSum`.

``` java
/* An element of the tree */
abstract class OperatorOrJustInteger {
}

/* Tree nodes */
class Plus extends OperatorOrJustInteger {
    private OperatorOrJustInteger left;
    private OperatorOrJustInteger right;
}

/* Tree leaves */
class JustInteger extends OperatorOrJustInteger
{
    public Integer getValue() {
        return 42;
    }
}
```

## Making it a `Visitable`

Now we want to mark what is interesting in this tree : in many objects,
most fields are not interesting.

``` java
/* An element of the tree */
abstract class OperatorOrJustInteger {
}

/* Tree nodes, marked as a Visitable */
class Plus extends OperatorOrJustInteger implements Visitable {
    @ToVisit // This annotation means that this field is usefull to visitors.
    private OperatorOrJustInteger left;
    @ToVisit // Same here.
    private OperatorOrJustInteger right;
}

/* Tree leaves */
class JustInteger extends OperatorOrJustInteger implements Visitable
{
    @ToVisit // Here the return value is the usefull part
    public Integer getValue() {
        return 42;
    }
}
```

At this point, our tree is done. **We won't modify it** when adding the
sum operation, or any other operation.

## Implementing the sum as a `Visitor`

Now that the tree nodes are `Visitable`, we now can write our sum and
many other functions outside of the tree class. We are helped in doing
so by the availability of pattern matching.

Let's start with a `SumVisitor` class. It implements the empty `Visitor`
interface.

``` java
class SumVisitor implements Visitor {
}
```

It doesn't do much right now. Let's add some stuff. First we are going
to define what to do with objects of class `JustInteger`. This method
will be given the object and its fields annotated with `@ToVisit`. An
annotation identifies it as a part of a specific visit.

``` java
/* What to do when we find a single integer */
class SumVisitor implements Visitor {
    @VisitingMethod(visitName="sum")
    private int sum(JustInteger it, int value) {
        return value;
    }
}
```

Let's do the same with the OperatorOrJustInteger.

``` java
/* What to do when we find a node with two children */
    @VisitingMethod(visitName="sum")
    private int sum(Plus it, OperatorOrJustInteger left,
                    OperatorOrJustInteger right) {
        return sum(left)+sum(right);
    }
```

So far so good ? We're quite done. All we need is to add the `sum
(OperatorOrJustInteger tree)` method that will call the others.  
 This require adding an `VisitorRunner` that is responsible for
dispatching the `Visitable` to the right methods.

``` java
/* This private object caches annotations. */
private static final VisitorRunner SUM_RUNNER = VisitorRunner.getInstance(SumVisitor.class, "sum");
```

And to invoke it when we are given a OperatorOrJustInteger :

``` java
/* This will call the appropriate sum() method */
    public Integer sum(OperatorOrJustInteger it) throws VisitorRunnerException {
        return SUM_RUNNER.visit(this, it);
    }
```

And we're done \!

## Writing a `Visitor`, the final result

The final code for the `Visitor` is :

``` java
class SumVisitor implements Visitor {
    /* This private object caches annotations. */
    private static final VisitorRunner SUM_RUNNER =
        VisitorRunner.getInstance(SumVisitor.class, "sum");

/* What to do when we find a single integer */
    @VisitingMethod(visitName="sum")
    private int sum(JustInteger it, int value) {
        return value;
    }

/* What to do when we find a node with two children */
    @VisitingMethod(visitName="sum")
    private int sum(Plus it, OperatorOrJustInteger left,
                    OperatorOrJustInteger right) throws VisitorRunnerException {
        return sum(left)+sum(right);
    }

/* This will call the appropriate sum() method */
    public Integer sum(OperatorOrJustInteger it) throws VisitorRunnerException {
        return SUM_RUNNER.visit(this, it);
    }
}
```

This visitor is a stand-alone implementation of a sum. It can be adapted
to other classes and did not require modifying the
`OperatorOrJustInteger` class.  
 This technique is usefull when you want to expose structured data on
which API-clients will perform operations, without modifying those data.
