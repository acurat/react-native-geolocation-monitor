package com.acurat.geofence;

import java.io.Serializable;
import java.util.List;

public class RNGeofenceDataObject implements Serializable {

    private String transitionType;
    private List<String> requestIds;

    public String getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(String transitionType) {
        this.transitionType = transitionType;
    }

    public List<String> getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(List<String> requestIds) {
        this.requestIds = requestIds;
    }

    @Override
    public String toString() {
        return "GeofenceDataObject{" +
                "transitionType='" + transitionType + '\'' +
                ", requestIds=" + requestIds +
                '}';
    }
}
