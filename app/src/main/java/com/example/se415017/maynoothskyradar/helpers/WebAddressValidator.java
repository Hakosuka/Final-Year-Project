package com.example.se415017.maynoothskyradar.helpers;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

/**
 * Created by se415017 on 16/02/2016.
 */
public abstract class WebAddressValidator implements TextWatcher {
    private String addressString;
    private TextView tv;
    final String TAG = getClass().getSimpleName();

    public WebAddressValidator() {  }
    public WebAddressValidator(TextView tv) {
        this.tv = tv;
    }

    public abstract void validate(TextView tv, String text);

    @Override
    final public void afterTextChanged(Editable source) {
        addressString = source.toString();
        Log.d(TAG, addressString);
    }

    //@Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }

    //@Override
    public void onTextChganged(CharSequence s, int start, int count, int after) {   }

    //@Override
    public boolean isValid(CharSequence addressString) {
        return (Patterns.WEB_URL.matcher(addressString).matches()
            || Patterns.IP_ADDRESS.matcher(addressString).matches());
    }

    public String getValue() { return addressString; }

}
