package com.prezpal.prezpal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Brian on 2/15/2018.
 * This class represents a specific detail of Analysis from recorded audio or video
 */

public class AnalysisItem implements Parcelable{
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(severity.toString());
        out.writeString(name);
        out.writeString(details);
    }

    public static final Parcelable.Creator<AnalysisItem> CREATOR = new Parcelable.Creator<AnalysisItem>() {
        public AnalysisItem createFromParcel(Parcel in) {
            return new AnalysisItem(in);
        }

        public AnalysisItem[] newArray(int size) {
            return new AnalysisItem[size];
        }
    };

    public AnalysisItem(Parcel in){
        this.severity = AnalysisSeverity.valueOf(in.readString());
        this.name = in.readString();
        this.details = in.readString();
    }


}
