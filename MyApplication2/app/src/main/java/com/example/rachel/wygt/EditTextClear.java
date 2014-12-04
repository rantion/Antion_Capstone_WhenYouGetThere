package com.example.rachel.wygt;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Rachel on 11/1/14.
 */
public class EditTextClear extends EditText {

    private Drawable imgCloseButton = getResources().getDrawable(R.drawable.clear);

    public EditTextClear(Context context) {
        super(context);
        init();
    }

    public EditTextClear(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextClear(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {

        // Set bounds of the Clear button so it will look ok
        this.setCompoundDrawablePadding((int)getResources().getDimension(R.dimen.abc_dropdownitem_text_padding_left));
        int currentHeight = (int)getResources().getDimension(R.dimen.abc_action_bar_default_height);
        int percent = currentHeight/2;
        imgCloseButton.setBounds(0, 0,(int)getResources().getDimension(R.dimen.abc_action_bar_default_height)-percent,(int)getResources().getDimension(R.dimen.abc_action_bar_default_height)-percent);
            // There may be initial text in the field, so we may need to display the  button
        handleClearButton();

        //if the Close image is displayed and the user remove his finger from the button, clear it. Otherwise do nothing
        this.setOnTouchListener(new OnTouchListener() {
            int percent = (int)getResources().getDimension(R.dimen.abc_action_bar_default_height)/2;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                EditTextClear et = EditTextClear.this;

                if (et.getCompoundDrawables()[2] == null)
                    return false;

                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                if (event.getX() > et.getWidth() - et.getPaddingRight() -(int)getResources().getDimension(R.dimen.abc_action_bar_default_height)-percent) {
                    et.setText("");
                    EditTextClear.this.handleClearButton();
                }
                return false;
            }
        });
        //if text changes, take care of the button
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

               EditTextClear.this.handleClearButton();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    //intercept Typeface change and set it with our custom font
//    public void setTypeface(Typeface tf, int style) {
//        if (style == Typeface.BOLD) {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-B 0.602.otf"));
//        } else {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-R 0.602.otf"));
//        }
//    }

    void handleClearButton() {
        if (this.getText().toString().equals(""))
        {
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
        }
        else
        {
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgCloseButton, this.getCompoundDrawables()[3]);
        }
    }

}
