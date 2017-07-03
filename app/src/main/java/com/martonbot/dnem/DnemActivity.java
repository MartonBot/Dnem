package com.martonbot.dnem;

import java.util.Collections;
import java.util.List;

public class DnemActivity {

    private long id;
    private String label;
    private String details;

    // most recent timestamp first
    public List<DnemTrackingLog> trackingLogs;

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDetails() {
        return details;
    }

    public DnemActivity(long id, String label, String details) {
        this.id = id;
        this.label = label;
        this.details = details;
    }

    public void setTrackingLogs(List<DnemTrackingLog> trackingLogs) {
        this.trackingLogs = trackingLogs;
    }

    private void sortTrackingLogs() {
        Collections.sort(trackingLogs);
    }

    public void addNewTrackingLog(DnemTrackingLog newTrackingLog) {
        trackingLogs.add(0, newTrackingLog);
    }
}
