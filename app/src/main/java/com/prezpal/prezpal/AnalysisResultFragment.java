package com.prezpal.prezpal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AnalysisResultFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AnalysisResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalysisResultFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SEVERITY = "SEVERITY";
    private static final String ARG_NAME = "NAME";
    private static final String ARG_DETAILS = "DETAILS";

    // TODO: Rename and change types of parameters
    private AnalysisSeverity severity;
    private String name;
    private String details;

    private OnFragmentInteractionListener mListener;

    public AnalysisResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnalysisResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalysisResultFragment newInstance(AnalysisSeverity severity, String name, String details) {
        AnalysisResultFragment fragment = new AnalysisResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_DETAILS, details);
        args.putString(ARG_SEVERITY, severity.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_analysis_result, container, false);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            details = getArguments().getString(ARG_DETAILS);
            severity = AnalysisSeverity.valueOf(getArguments().getString(ARG_SEVERITY));
        }
        TextView nameView = (TextView) rootView.findViewById(R.id.itemName);
        TextView severityView = (TextView) rootView.findViewById(R.id.itemSeverity);
        Button detailsButton = (Button) rootView.findViewById(R.id.analysisDetails);
        nameView.setText(name);
        severityView.setText(severity.toString());
        // When the user clicks on the Details button, show a pop-up displaying the details
        detailsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                final EditText et = new EditText(getContext());
                et.setText(details);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(et);

                // set dialog message
                alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
