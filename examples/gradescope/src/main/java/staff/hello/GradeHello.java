package staff.hello;

import com.github.tkutcher.jgrade.CheckstyleGrader;
import com.github.tkutcher.jgrade.Grade;
import com.github.tkutcher.jgrade.Grader;
import com.github.tkutcher.jgrade.gradedtest.GradedTestResult;

public class GradeHello {

    @Grade
    public void debugMode(Grader grader) {
        if (HelloTest.DEBUG) {
            GradedTestResult r = new GradedTestResult(
                    "Debug Mode Warning", "",
                    0.0, GradedTestResult.HIDDEN
            );
            r.addOutput("WARNING: Autograder in DEBUG mode, not " +
                    "checking student submission. If seeing this on " +
                    "Gradescope it means you forgot to switch DEBUG " +
                    "to false");
            grader.addGradedTestResult(r);
        }
    }

    @Grade
    public void runUnitTests(Grader grader) {
        grader.runJUnitGradedTests(HelloTest.class);
    }

    @Grade
    public void runCheckstyle(Grader grader) {
        // FIXME - Better than knowing running from the classes directory...
        CheckstyleGrader checker = new CheckstyleGrader(10.0, 1.0,
                "../lib/checkstyle-10.5.0-all.jar", "../src/main/java/student/hello/");
        checker.setConfig("../res/sun_checks.xml");
        GradedTestResult result = checker.runForGradedTestResult();
        grader.addGradedTestResult(result);
    }
}
