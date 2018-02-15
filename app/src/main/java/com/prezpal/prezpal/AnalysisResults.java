package com.prezpal.prezpal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian on 2/15/2018.
 * This class maintains all of the results of Video and Audio analyses, in the form of a list of
 * AnalysisItems objects
 */


public class AnalysisResults {

    private List<AnalysisItem> items;

    public AnalysisResults(){
        this.items = new ArrayList<AnalysisItem>();
    }

    public void addItem(AnalysisItem item){
        this.items.add(item);
    }

    public List<AnalysisItem> getItems(){
        return this.items;
    }
}
