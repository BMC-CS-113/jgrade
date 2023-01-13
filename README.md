# JGrade

_A library for grading Java assignments_

This is a fork of [espertusnu/jgrade](https://github.com/espertusnu/jgrade)
created by Ellen Spertus[https://ellenspertus.com/], which is a fork of
[tkutcher/JGrade](https://github.com/tkutcher/jgrade),
created by Tim Kutcher. 

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Details](#details)
- [Features](#features)
- [Development](#development)
    - [Requirements](#requirements)
    - [Ideas / Wishlist](#wishlist)

---

## Overview

JGrade is a helper tool with various classes designed to assist in course
instructors "autograding" an assignment,
inspired by
the [Gradescope Autograder](https://gradescope-autograders.readthedocs.io/en/latest/)
. There are classes
that the client can integrate with directly, or use the jar's main method (and
provide a class with annotations) that
wraps a lot of common functionality (
see [examples](https://github.com/tkutcher/jgrade/tree/development/examples)).
It was designed to produce the output needed for Gradescope while being
extensible enough to produce different
outputs and configure the specific JSON output Gradescope is looking for.

## Quick Start/Tutorial

To make use of this, you first need to build the jgrader jar file 
using maven. The jar includes many classes you can make use of, 
as well as a main method for running and producing grading output.

### Working on BMC CS lab servers

First clone this repository on a CS lab machine. 
The lab machines do not have maven installed. 
Download maven from https://maven.apache.org/download.cgi
and then install your
own copy of maven by following these instructions: https://maven.apache.org/install.html.

#### Installing maven
From your home directory run the following commands:

```
wget https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz
tar xzvf apache-maven-3.8.7-bin.tar.gz
```

Next add maven to your PATH. In your ~/.bash_profile add the following line:

```
export PATH="/home/<USER>/apache-maven-3.8.7/bin/:$PATH"
```

where `<USER>` is your user name. Then run `source .bash_profile` to update your bash.

To confirm mvn is installed, you can
now run `mvn -version`.

### Creating the jgrader jar file.
Run `mvn package` from the root of the `jgrade/` directory. This should create
a new directory called `target` and a jar file in that directory
called `jgrade-2.1.1-all.jar` (there are other files in that directory
but you can ignore them for now).

Next, copy that jar file to `examples/gradescope/lib`.

### Running an existing autograder

We are now ready to look at an example autograder. In order to make 
sure that jar file is created, move to the `examples/gradescope` directory.
From that directory, run `./run_autograder --local`. You should then see
some json printed to the terminal, these are three autograder tests. If you 
scroll up in your terminal, you should see the following as the begining of the output 
from the previous command.

```
making clean classes/ directory
compiling java files...
---
DONE
```

This means that we successfully compiled the student's and autograder'
Java classes. To confirm this, poke through the newly created `classes/` directory.
You will see two subdirectories, `student/` and `staff/`. Both of these will have
class files based on the Java files in `src/main/java/`.

### Checkstyle
Read the error messages that resulted from the `./run_autograder --local` command.
What do you think is causing the error? Thinking about this for a second before
continue reading.

<details><summary>Answer (click here to continue reading)</summary>
<p>

You should notice that the autograder is checking for codestyle and is 
enforcing checkstyle compliance. 

</p>
</details>

Next, download the checkstyle jar from 
https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.5.0/checkstyle-10.5.0-all.jar
and store it in `lib/`.
Now if you run, `./run_autograder --local` you should no longer see the error. 

### Assignment and autograder example

Now you are ready to look at an example assignment and autograder. Jump to
https://github.com/BMC-CS-113/jgrade/tree/dev/examples/gradescope
and read through the README there which describes the different files in the example.
Most of the files in that directory are for creating and running an autograder.
Running `make_autograder.sh` will create a zip file in the `zips/` directory 
that you will upload to gradescope. Make sure to read that README to understand each subdirectory
and where the JUnit tests are that the autograder uses. 

If this is your first time using
gradescope as part of a course staff, watch Ellen Spertus's
(10 minute Youttube video)[https://youtu.be/o1FHbHZwyUY]
on How to use JGrade for autograding Java assignments on Gradescope.
If you are familiar with gradescope as an instructor/TA, you can skip the video.

Next, ping @azpoliak. He will add you to the course
Gradescope as an instructor. From there, your task will be to create a
new assignment on gradescope for this example assignment.
Follow the steps in the Youtube video for creating an assignment.
You can test the autograder on gradescope by adding any of the mock 
student submissions from the test submissions directory: https://github.com/BMC-CS-113/jgrade/tree/dev/examples/gradescope/test_submissions

## Details

With this, you could have the following setup:

A class that runs some unit tests we want to treat their success as a grade (
these would import student code):

```java
import com.github.tkutcher.jgrade.gradedtest.GradedTest;
import org.junit.Test;

import static com.github.tkutcher.jgrade.gradedtest.GradedTestResult.HIDDEN;
import static com.github.tkutcher.jgrade.gradedtest.GradedTestResult.VISIBLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExampleGradedTests {
    @Test
    @GradedTest(name = "True is true", points = 2.0, visibility = VISIBLE)
    public void trueIsTrue() {
        assertTrue(true);
    }

    @Test
    @GradedTest(name = "False is false", number = "2", points = 3.0, visibility = HIDDEN)
    public void falseIsFalse() {
        assertFalse(false);
    }

    @Test
    @GradedTest(name = "Captures output")
    public void capturesOutput() {
        System.out.println("hello");
    }

    @Test
    @GradedTest(name = "This test should fail")
    public void badTest() {
        fail();
    }
}
```

and a main method with some other grading-related non-unit-testing
logic `MyGrader.java`:

```java
import com.github.tkutcher.jgrade.BeforeGrading;
import com.github.tkutcher.jgrade.AfterGrading;
import com.github.tkutcher.jgrade.Grade;
import com.github.tkutcher.jgrade.Grader;
import com.github.tkutcher.jgrade.gradedtest.GradedTestResult;

import static com.github.tkutcher.jgrade.gradedtest.GradedTestResult.HIDDEN;


public class BasicGraderExample {

    /* All @Grade/@BeforeGrading/@AfterGrading methods must take exactly one parameter
     * of type Grader. This parameter is the same grader throughout.
     *
     * @BeforeGrading methods are run before others.
     */
    @BeforeGrading
    public void initGrader(Grader grader) {
        grader.startTimer();
    }

    /* You can run unit tests that are annotated with @GradedTest to add
     * GradedTestResults to the Grader in this way.
     */
    @Grade
    public void runGradedUnitTests(Grader grader) {
        grader.runJUnitGradedTests(ExampleGradedTests.class);
    }

    /* You can also manually add GradedTestResults you create to the grader. */
    @Grade
    public void singleTestResult(Grader grader) {
        grader.addGradedTestResult(
                new GradedTestResult("manual test", "1", 1.0, HIDDEN)
        );
    }

    /* Grader.startTimer() and Grader.stopTimer() can be used to time the grader */
    @Grade
    public void loopForTime(Grader grader) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 1000) ;
    }

    /* @AfterGrading methods are run after all other methods. */
    @AfterGrading
    public void endGrader(Grader grader) {
        grader.stopTimer();
    }
}
```

Then, you could run

```shell script
java -jar ../lib/jgrade-1.1-all.jar -c MyGrader -o results.json
```

and get GradeScope-formatted json. See the [examples](/examples) for more
complete examples and how to set up a script
to work with GradeScope, and expand the usage below to see the arguments you can
provide this main program.

<details><summary>Usage</summary>
<p>

```
-c,--classname arg            the class containing annotated methods to grade
-f,--format output-format     specify output, one of 'json' (default) or 'txt'
-h,--help<br>
   --no-output                don't produce any output (if user overriding)
-o destination                save output to another file (if not specified,
                              prints to standard out)
   --pretty-print             pretty-print output (when format is json)
-v,--version

```

</p>
</details>

## Features

The way I used this library is to have a base class for the course (for example,
a `_226Grader`) that contains
annotated methods for functionality/grading parts that are consistent across all
assignments. For example, the
`@BeforeGrading` method starts a timer and the `@AfterGrading` method stops it.
There is a `@Grade` method that
does the "grading" of style with checkstyle. Subclasses, for
example `Assignment1Grader` (or `Assignment0Grader`
I suppose :wink:), extend this and add  `@Grade` methods to add
assignment-specific grading.
See the gradescope folder in the examples for a rough example setup.

See the [API Docs](https://tkutcher.gitlab.io/jgrade/api) for more complete
documentation.

### `CheckstyleGrader`

With the `CheckstyleGrader` you can specify grading deductions for checkstyle
errors. This method below, for example,
would check the students files and deduct a point for each checkstyle error
type (missing javadoc, require this, etc.).

```java
@Grade
public void runCheckstyle(Grader grader){
        CheckstyleGrader checker=new CheckstyleGrader(5.0,1.0,MY_CHECKSTYLE_JAR,STUDENTFILES);
        checker.setConfig(MY_CHECKSTYLE_CONFIG);
        GradedTestResult result=checker.runForGradedTestResult();
        result.setScore(Math.max(0,5-checker.getErrorTypeCount()));
        grader.addGradedTestResult(result);
        }
```

### `DeductiveGraderStrategy`

You can use this strategy to make failed tests deduct points from a total. So
say in the current assignment there are two
parts, A and B, each worth 25 points. If someone fails 30 tests for part B each
worth one point, you don't want that to cut
in to the assignment A portion:

```java
public class GradeAssignment7 extends Grade226Assignment {

    private static final int AVL_POINTS = 30;
    private static final int TREAP_POINTS = 20;

    @Grade
    public void gradeAvlTree(Grader grader) {
        grader.setGraderStrategy(new DeductiveGraderStrategy(AVL_POINTS, "AvlTreeMap"));
        grader.runJUnitGradedTests(GradeAvlTreeMap.class);
    }

    @Grade
    public void gradeBinaryHeapPQ(Grader grader) {
        grader.setGraderStrategy(new DeductiveGraderStrategy(TREAP_POINTS, "TreapMap"));
        grader.runJUnitGradedTests(GradeTreapMap.class);
    }
}
```

### `CLITester`

A class to help wrap testing command line programs. You subclass `CLITester`,
then implement
the `getInvocation()` method for how the command line program is invoked, then
you can use
`runCommand(String)` to get the output in an object that you can test for
expected output.


---

## Development

- `mvn install` to compile
- `mvn test` to run unit tests
- `mvn checkstyle:checkstyle` to run checkstyle
- `mvn javadoc:jar` to generate API docs.

Check out [contributing](/CONTRIBUTING.md) for more.

### Requirements

JGrade is written
in [Java 8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html)
.
Since the library has classes designed to run alongside
JUnit, [JUnit 4](https://junit.org/junit4/) is a dependency
for the entire project (as opposed to just for running the projects own unit
tests).
The [org.json](https://mvnrepository.com/artifact/org.json/json) package is used
in producing correctly formatted
JSON output, and
the [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) library
is used for
reading the command line in the main program.

For simplicity, the main jar (appended with "-all") includes all of these
dependencies.

### Wishlist

- Feedback for required files
    - In our autograder, we built in something that took a list of required
      files and created a visible test case worth 0 points of what files were
      missing - this helped students debug.
    - Could try and move some of this there.
- Actual Observer pattern
    - Allow for people to specify custom handlers whenever things like new
      graded test results are added
    - Old "observer" terminology not really an observer
