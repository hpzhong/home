package com.zhuoyou.plugin.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.antilost.R;

public class CustomAlertDialog extends Dialog {

	public CustomAlertDialog(Context context,int theme) {
        super(context,theme);
    }
	
	public CustomAlertDialog(Context context) {
        super(context);
    }

	public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private Boolean flag;
  
        private DialogInterface.OnClickListener
                        positiveButtonClickListener,
                        negativeButtonClickListener;
  
        public Builder(Context context) {
            this.context = context;
        }
        
        public Builder setMessage(String message) {
        	this.message = message;
        	return this;
        }
        
        public Builder setMessage(int message) {
        	this.message = (String) context.getText(message);
        	return this;
        }

        public Builder setTitle(int title) {
        	this.title = (String) context.getText(title);
        	return this;
        }
        
        public Builder setTitle(String title) {
        	this.title = title;
        	return this;
        }
        
        public Builder setContentView(View v) {
        	this.contentView = v;
        	return this;
        }
        
        public Builder setCancelable(Boolean flag) {
        	this.flag = flag;
        	return this;
        }
        
        public Builder setPositiveButton(int positiveButtonText,DialogInterface.OnClickListener listener) {
        	this.positiveButtonText = (String) context.getText(positiveButtonText);
        	this.positiveButtonClickListener = listener;
        	return this;
        }
        
        public Builder setPositiveButton(String positiveButtonText,DialogInterface.OnClickListener listener) {
    		this.positiveButtonText = positiveButtonText;
    		this.positiveButtonClickListener = listener;
    		return this;
        }

        public Builder setNegativeButton(int negativeButtonText,DialogInterface.OnClickListener listener) {
        	this.negativeButtonText = (String) context.getText(negativeButtonText);
        	this.negativeButtonClickListener = listener;
        	return this;
        }
        
        public Builder setNegativeButton(String negativeButtonText,DialogInterface.OnClickListener listener) {
        	this.negativeButtonText = negativeButtonText;
        	this.negativeButtonClickListener = listener;
        	return this;
        }
        
        public CustomAlertDialog create() {
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	final CustomAlertDialog dialog = new CustomAlertDialog(context,R.style.CustomAlertDialogBackground);
        	View layout = inflater.inflate(R.layout.custom_alert_dialog, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                   ((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(
                            		dialog, 
                                    DialogInterface.BUTTON_POSITIVE);
                        }
                     });
                }
            } else {
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                        	negativeButtonClickListener.onClick(
                            		dialog, 
                                    DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, 
                                new LayoutParams(
                                        LayoutParams.WRAP_CONTENT, 
                                        LayoutParams.WRAP_CONTENT));
            }
            dialog.setContentView(layout);
            dialog.setCancelable(flag);
        	return dialog;
        }
	}

}
