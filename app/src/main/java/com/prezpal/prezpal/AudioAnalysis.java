package com.prezpal.prezpal;

import android.net.Uri;

import com.google.cloud.speech.v1.SpeechRecognitionAlternative;

import java.util.ArrayList;
import java.util.List;

/**
 * Static class to perform video Analysis
 * Created by Brian on 2/17/2018.
 */

public class AudioAnalysis {
    // TODO: Adjust arguments of analysis as necessary
    public static List<AnalysisItem> audioAnalysis(Uri videoUri){

        List<AnalysisItem> items = new ArrayList<AnalysisItem>();
        return items;
    }

    /**
     * Based on a list of recorded amplitudes, determine the average pause length and the number of
     * pauses the user had
     * @param maxAmplitudes The list of recorded amplitudes
     * @return The resulting AnalysisItem
     */
    public static AnalysisItem analyzeAmplitudes(List<Integer> maxAmplitudes) {
        int max = 0; // The max amplitude value recorded (used as a baseline for speaking volume)
        int min = Integer.MAX_VALUE; // The minimum amplitude value recorded (used as a baseline for silence)
        boolean talking = false; // Whether the user is currently talking
        int consecutiveCount = 0; // How many iterations the user has been talking/not talking consecutively.
        // Representing a list of pauses, where the integer represents the length of the pause
        List<Long> pauses = new ArrayList<Long>();
        for(Integer amplitude : maxAmplitudes){
           if(amplitude > max) {
               // Update the max amplitude
               max = amplitude;
           }
           if(amplitude < min) {
               // Update the minimum amplitude
               min = amplitude;
           }
           if(talking){
               if(amplitude < (0.25 * (max - min) + min)){
                   // The user is silent now
                   consecutiveCount = 0;
                   talking = false;
               } else {
                   // The user is probably still talking
                   consecutiveCount++;

               }
           } else {
               if(amplitude > (0.75 * (max - min) + min)){
                   // The user is talking now
                   if(consecutiveCount > 10){
                       // The silence lasted more than half a second, so record it
                       pauses.add(Long.valueOf(consecutiveCount) / 20);
                   }
                   consecutiveCount = 0;
                   talking = true;
               } else {
                   // The user is probably still silent
                   consecutiveCount++;
               }
           }
        }

        int totalPauseLength = 0;
        for(Long pauseLength : pauses){
            totalPauseLength += pauseLength;
        }

        long averagePauseLength;
        if(pauses.size() != 0) {
            averagePauseLength = totalPauseLength / pauses.size();
        } else {
            averagePauseLength = 0;
        }

        AnalysisSeverity severity;
        if(averagePauseLength < 1.5){
            // Less than 1.5 seconds
            severity = AnalysisSeverity.OKAY;
        } else if(averagePauseLength < 3){
            // Less than 3 seconds
            severity = AnalysisSeverity.MEDIUM;
        } else {
            // Greater than 3 seconds
            severity = AnalysisSeverity.SEVERE;
        }
        // Potential other Item: number of pauses?
        return new AnalysisItem(severity, "Average Pause Length", "Your average pause length was " + averagePauseLength + " seconds");
    }

    public static AnalysisItem analyzeDuration(Integer expectedDuration, long actualDuration){

        if(actualDuration < expectedDuration){
            int difference = Math.round(expectedDuration - actualDuration);
            return new AnalysisItem(AnalysisSeverity.OKAY, "Duration", "You finished with " + difference + " seconds to spare");
        } else {
            int difference = Math.round(actualDuration - expectedDuration);
            if(difference < 60){
                return new AnalysisItem(AnalysisSeverity.MEDIUM, "Duration", "You went " + difference + " too long");
            } else {
                return new AnalysisItem(AnalysisSeverity.SEVERE, "Duration", "You went " + difference + " too long!");
            }
        }
    }

    public static AnalysisItem analyzeRecognition(SpeechRecognitionAlternative alternative){
        AnalysisSeverity severity;
        if(alternative.getConfidence() > 0.9){
            severity = AnalysisSeverity.OKAY;
        } else if(alternative.getConfidence() > 0.6){
            severity = AnalysisSeverity.MEDIUM;
        } else {
            severity = AnalysisSeverity.SEVERE;
        }
        return new AnalysisItem(severity, "Clarity", "Recognition had a confidence of " + alternative.getConfidence());
    }
}
