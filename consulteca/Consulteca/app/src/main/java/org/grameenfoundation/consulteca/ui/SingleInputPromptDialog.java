package org.grameenfoundation.consulteca.ui;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

/**
 *
 */
public abstract class SingleInputPromptDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener {

    private final CustomAutoCompleteTextView input;
    private Context context;
    private String farmerId;

    public SingleInputPromptDialog(Context context, int title, int message) {
        super(context);
        this.context = context;
        setMessage(message);

        input = new CustomAutoCompleteTextView(context);
        input.setSingleLine(true);
        input.setThreshold(3);
        input.setAdapter(new SearchFarmerAdapter(context));
        input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                farmerId = view.getTag().toString();
            }
        });
        setView(input);
        setPositiveButton(R.string.ok, this);
        setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (farmerId == null || farmerId.length() <= 0) {
                Toast.makeText(this.context,
                        org.grameenfoundation.consulteca.R.string.missing_client_id, Toast.LENGTH_LONG).show();
                return;
            }

            if (onOkClicked(farmerId)) {
                dialog.dismiss();
            }
        } else {
            onCancelClicked(dialog);
        }
    }

    /**
     * called when the "ok" button is pressed
     *
     * @param input
     * @return true or false, if the dialog should be closed. false if not.
     */
    protected abstract boolean onOkClicked(String input);

    /**
     * called when the "cancel" button is pressed and closes the dialog.
     *
     * @param dialog
     */
    protected void onCancelClicked(DialogInterface dialog) {
        dialog.dismiss();
    }

    class CustomAutoCompleteTextView extends AutoCompleteTextView {

        public CustomAutoCompleteTextView(Context context){
            super(context);
        }

        @Override
        public boolean onKeyPreIme (int keyCode, KeyEvent event){
            if(keyCode == KeyEvent.KEYCODE_BACK){
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            return true;
        }

    }
}
