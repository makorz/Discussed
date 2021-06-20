package pl.makorz.discussed.Controllers.Functions;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class DescriptionFilter implements InputFilter {

    private static final String TAG = "ProfileActivity";
    Context context;

    public DescriptionFilter(Context context2) {
        this.context = context2;
    }

    @Override
    public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
        // if adde text is valid return source
        // if invalid return empty string

        if (charSequence == null || charSequence.toString().isEmpty()) {
            return "";
        }
        String inputText = charSequence.toString();
        String loginfo = String.format("Added text " + inputText + " has length %d", inputText.length());
        Log.i(TAG, loginfo);
        List<Integer> validCharCharactersInt = new ArrayList<Integer>();
        validCharCharactersInt.add((int) Character.DECIMAL_DIGIT_NUMBER);
        validCharCharactersInt.add((int) Character.UPPERCASE_LETTER);
        for (char inputChar : inputText.toCharArray()) {
            int type = Character.getType(inputChar);
            if (!validCharCharactersInt.contains(type)) {
                Toast.makeText(context, "Wrong character!!!", Toast.LENGTH_SHORT).show();
                return "";
            }

        }
        return charSequence;
    }


}
