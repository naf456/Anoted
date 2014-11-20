package com.ream.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.widget.EditText;

/**
 * A placeholder fragment containing a simple view.
 */
public class DocumentEditorFragment extends Fragment {

    private static final String ARG_DOCUMENT_REF = "documentRef";
    private static final String ARG_DOCUMENT_TITLE = "documentTitle";
    private static final String ARG_DOCUMENT_CONTENT = "documentContent";

    private Callbacks callback;
    private EditText stage;
    private long documentRef = -1;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DocumentEditorFragment newInstance(long documentRef,String title, String content) {
        DocumentEditorFragment fragment = new DocumentEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCUMENT_REF, documentRef);
        args.putString(ARG_DOCUMENT_TITLE, title);
        args.putString(ARG_DOCUMENT_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    public DocumentEditorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        documentRef = getArguments().getLong(DocumentEditorFragment.ARG_DOCUMENT_REF);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.ream.app.R.layout.fragment_document_editor, container, false);
        stage = (EditText)view.findViewById(com.ream.app.R.id.document_stage);
        stage.setText(getArguments().getString(DocumentEditorFragment.ARG_DOCUMENT_CONTENT));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (DocumentEditorFragment.Callbacks)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity has not implemented DocumentEditorFragment.Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        ActionBar actionbar = getActivity().getActionBar();
        actionbar.setTitle(getArguments().getString(DocumentEditorFragment.ARG_DOCUMENT_TITLE));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDocument();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveDocument();
    }

    private void saveDocument(){
        Log.d("DocumentEditor", "Saving document");
        if(callback != null) { callback.saveDocument(documentRef, stage.getText().toString()); }
    }

    public interface Callbacks {
        public abstract void saveDocument(long docRef, String content);
    }
}
