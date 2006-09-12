package org.intellij.trinkets.problemsView.problems;

/**
 * Listener for add and remove problems from manager.
 *
 * @author Alexey Efimov
 */
public interface ProblemManagerListener {
    void inspectionStarted();

    void inspectionCompleted();

    void inspectionFailed();

    void problemAdded(Problem problem);

    void problemRemoved(Problem problem);

    /**
     * If some one listener return false, then manager will no runned inspections.
     *
     * @return <code>true</code> if manager can run inspections now
     */
    boolean canRunInspection();
}
