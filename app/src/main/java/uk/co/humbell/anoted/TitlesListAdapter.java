package uk.co.humbell.anoted;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

/**
 * Created by nathan on 07/11/14.
 */
public class TitlesListAdapter extends BaseAdapter {

    Context mContext;
    List<String> mDocumentTitles;
    boolean mEditMode = false;
    Callbacks mCallback;

    public TitlesListAdapter(Context context, List<String> documentTitles, Callbacks callback) {
        this.mContext = context;
        this.mDocumentTitles = documentTitles;
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return mDocumentTitles.size();
    }

    @Override
    public Object getItem(int position) {
        return mDocumentTitles.get(position);
    }

    /**
     * Adapter does not provide ID's, so always returns -1.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater lf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = lf.inflate(R.layout.document_list_item, parent, false);
        }

        TextView titleEditText = (TextView)convertView.findViewById(R.id.document_list_item_title_text);
        ImageButton removeButton = (ImageButton)convertView.findViewById(R.id.document_list_item_remove_btn);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback == null){ return; }
                mCallback.onDocumentRemove(position);
            }
        });

        if(mEditMode) {
            removeButton.setVisibility(View.VISIBLE);

            //Due to way I formatted the layout, we have to set up the TextView layout programmatically.
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.document_list_item_remove_btn);
            params.addRule(RelativeLayout.END_OF, R.id.document_list_item_remove_btn);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            titleEditText.setLayoutParams(params);
        } else {
            removeButton.setVisibility(View.GONE);
            //This has to be called programmatically, as it resets when visibility changes.
            removeButton.setFocusable(false);

            //Due to way I formatted the layout, we have to set up the TextView layout programmatically.
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            titleEditText.setLayoutParams(params);
        }
        titleEditText.setText(mDocumentTitles.get(position));
        return convertView;
    }

    /**
     * Update the documents titles to display.
     */
    public void updateTitles(List<String> titles) {
        this.mDocumentTitles = titles;
        this.notifyDataSetChanged();
    }

    /**
     * Set the title adapter into edit mode - it display the remove button next to the title item.
     */
    public void setEditMode(boolean mode){
        this.mEditMode = mode;
        Log.d("DRAWER_DOCUMENT", "In Edit Mode: " + mEditMode);
        this.notifyDataSetChanged();
    }

    /**
     * Set the title adapter into edit mode - it display the remove button next to the title item.
     */
    public boolean getEditMode(){
        return mEditMode;
    }

    /**
     * Callback interface implementing observer methods for this documentAdapter.
    */
    public interface Callbacks {
        /**
         * Called when the document remove button is clicked of list item.
         */
        abstract void onDocumentRemove(int position);
    }
}
