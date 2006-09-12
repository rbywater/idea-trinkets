package org.intellij.trinkets.logFilter.impl;

final class GroupMonitoringConfiguration {
    private int group;
    private String matched;
    private boolean monitor;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getMatched() {
        return matched;
    }

    public void setMatched(String matched) {
        this.matched = matched;
    }


    public boolean isMonitor() {
        return monitor;
    }

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GroupMonitoringConfiguration that = (GroupMonitoringConfiguration) o;

        return !(group != that.group || monitor != that.monitor || (matched != null ? !matched.equals(that.matched) : that.matched != null));

    }

    public int hashCode() {
        int result;
        result = group;
        result = 31 * result + (matched != null ? matched.hashCode() : 0);
        result = 31 * result + (monitor ? 1 : 0);
        return result;
    }
}
