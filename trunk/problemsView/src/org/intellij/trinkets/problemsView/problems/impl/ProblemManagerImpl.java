package org.intellij.trinkets.problemsView.problems.impl;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Alarm;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.problems.ProblemManager;
import org.intellij.trinkets.problemsView.problems.ProblemManagerListener;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class ProblemManagerImpl implements ProblemManager, ApplicationComponent {
    private static final Logger LOGGER = Logger.getInstance("#ProblemManager");

    private final List<ProblemInspection> inspections = new ArrayList<ProblemInspection>(0);
    private final List<Problem> problems = new ArrayList<Problem>(0);
    private final Set<ProblemManagerListener> listeners = new HashSet<ProblemManagerListener>(0);

    private final Alarm alarm = new Alarm();
    private final Runnable reinspectTask = new Runnable() {
        public void run() {
            idle = false;
            try {
                refresh();
                scheduleAlarm();
            } finally {
                idle = true;
            }
        }
    };
    private boolean idle = true;
    private boolean paused = false;

    public void addProblem(@NotNull Problem problem) {
        problems.add(problem);
        fireProblemAdded(problem);
    }

    private void fireProblemAdded(Problem problem) {
        for (ProblemManagerListener listener : listeners) {
            listener.problemAdded(problem);
        }
    }

    public void removeProblem(@NotNull Problem problem) {
        problems.remove(problem);
        fireProblemRemoved(problem);
    }

    private void fireProblemRemoved(Problem problem) {
        for (ProblemManagerListener listener : listeners) {
            listener.problemRemoved(problem);
        }
    }

    public void addProblemInspection(@NotNull ProblemInspection problemInspection) {
        if (!inspections.contains(problemInspection)) {
            inspections.add(problemInspection);
        }
    }

    public void removeProblemInspection(@NotNull ProblemInspection problemInspection) {
        inspections.remove(problemInspection);
    }

    public void addProblemManagerListener(@NotNull ProblemManagerListener listener) {
        listeners.add(listener);
    }

    public void removeProblemManagerListener(@NotNull ProblemManagerListener listener) {
        listeners.remove(listener);
    }

    public boolean isIdle() {
        return idle;
    }


    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void refresh() {
        try {
            if (!paused && canReinspect()) {
                reinspect();
            }
        } catch (Throwable t) {
            LOGGER.error(t);
            fireInspectionFailed();
        }
    }

    public void reinspect() {
        problems.clear();
        fireInspectionStarted();
        for (ProblemInspection inspection : inspections) {
            Problem[] problems = inspection.inspect();
            for (Problem problem : problems) {
                addProblem(problem);
            }
        }
        fireInspectionCompleted();
    }

    private boolean canReinspect() {
        for (ProblemManagerListener listener : listeners) {
            if (!listener.canRunInspection()) {
                return false;
            }
        }
        return true;
    }

    private void fireInspectionStarted() {
        for (ProblemManagerListener listener : listeners) {
            listener.inspectionStarted();
        }
    }

    private void fireInspectionCompleted() {
        for (ProblemManagerListener listener : listeners) {
            listener.inspectionCompleted();
        }
    }

    private void fireInspectionFailed() {
        for (ProblemManagerListener listener : listeners) {
            listener.inspectionFailed();
        }
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "Trinkets.ProblemManager";
    }

    public void initComponent() {
        scheduleAlarm();
    }

    private void scheduleAlarm() {
        alarm.addRequest(reinspectTask, 10000);
    }

    public void disposeComponent() {
        alarm.cancelAllRequests();
    }

    public Iterator<Problem> iterator() {
        return problems.iterator();
    }
}
