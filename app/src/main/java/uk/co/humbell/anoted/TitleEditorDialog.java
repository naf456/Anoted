package uk.co.humbell.anoted;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import uk.co.humbell.anoted.store.SimpleDocument;

/**
 * Created by nathan on 08/11/14.
 */
public class TitleEditorDialog extends DialogFragment {

    Callbacks mCallback;

    private static final String ARGS_DOCUMENT_REF = "document_reference";
    private static final String ARGS_DOCUMENT_TITLE = "document_title";

    public static TitleEditorDialog getInstance(Long documentRef, String documentTitle) {

        if(documentRef == null) { throw new NullPointerException("Title editor requires a document ID"); }
        if(documentTitle == null) { documentTitle = ""; }

        TitleEditorDialog fragment = new TitleEditorDialog();
        Bundle args = new Bundle();

        args.putLong(TitleEditorDialog.ARGS_DOCUMENT_REF, documentRef);
        args.putString(TitleEditorDialog.ARGS_DOCUMENT_TITLE, documentTitle);
        fragment.setArguments(args);

        return fragment;
    }

    public static TitleEditorDialog getInstance(SimpleDocument document) {
        return getInstance(document.getID(), document.getName());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callbacks)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NameDocumentDialog.Callbacks.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = (LayoutInflater)this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_view_change_document_title, null);

        final EditText title = (EditText)view.findViewById(R.id.change_document_title_title);
        title.setText(getArguments().getString(TitleEditorDialog.ARGS_DOCUMENT_TITLE));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.dialog_title_change_document_title)
            .setView(view)
            .setPositiveButton(R.string.abc_action_mode_done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mCallback != null) {
                        mCallback.onChangeDocumentTitle(getArguments().getLong(TitleEditorDialog.ARGS_DOCUMENT_REF),
                                title.getText().toString());
                    }
                }
            });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Provides the callback interface for an activity to implement.
     */
    public interface Callbacks {
        /**
         * Called when the user has submitted a new title to a document.
         */
        public abstract void onChangeDocumentTitle(long documentRef, String title);
    }
}
