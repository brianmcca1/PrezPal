package com.prezpal.prezpal;

/**
 * Created by Brian on 2/15/2018.
 * This class represents a specific detail of Analysis from recorded audio or video
 */

public class AnalysisItem {
    // The Severity of the Analysis Item
    private AnalysisSeverity severity;
    // The name of the item
    private String name;
    // The details of the item
    private String details;

    // Public constructor
    public AnalysisItem(AnalysisSeverity severity, String name, String details){
        this.severity = severity;
        this.name = name;
        this.details = details;
    }

    public AnalysisSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AnalysisSeverity severity) {
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


}
