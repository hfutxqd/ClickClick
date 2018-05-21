package xyz.imxqd.mediacontroller.ui.fragments;

import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseFragment extends Fragment {
    public CharSequence getBigNumberText(String text) {
        Spannable spannable = new SpannableString(text);
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            spannable.setSpan(new RelativeSizeSpan(2f), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
}
