package com.example.se415017.maynoothskyradar.helpers;

import android.text.InputFilter;
import android.text.Spanned;

import io.realm.annotations.PrimaryKey;

/**
 * Created by se415017 on 17/02/2016.
 *
 * This limits the amount of decimal points that the user can enter.
 */
public class DecimalDigitsInputFilter implements InputFilter {
    private final int decimalDigitLimit;

    public DecimalDigitsInputFilter(int decimalDigits) {
        this.decimalDigitLimit = decimalDigits; // limit of decimal digits allowed after the dot
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int dotPos = -1;
        int len = dest.length();
        for (int i = 0; i < len; i++) {
            char c = dest.charAt(i);
            if (c == '.' || c == ',') {
                dotPos = i;
                break;
            }
        }
        if (dotPos >= 0) {
            // dealing with too many dots
            if (source.equals(".") || source.equals(","))
                return "";

            // if the text is entered before the dot
            if (dend <= dotPos)
                return null;

            if (len - dotPos > decimalDigitLimit) {
                return "";
            }

        }
        return null;
    }
}
