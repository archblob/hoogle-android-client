package com.hoogleclient.results;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PrettyType {

    public static enum TypeStyles {
        KeywordBold
    }

    private static final Set<String> keywordS;
    private static final Set<String> contextS;

    static {
        keywordS = new HashSet<String>();
        keywordS.addAll(Arrays.asList(
                "class"
              , "data"
              , "keyword"
              , "module"
              , "newtype"
              , "package"
              , "type"
        ));

        contextS = new HashSet<String>();
        contextS.add("::");
    }

    private final Map<TypeStyles, Set<IntTuple>> operations;

    public PrettyType() {
        operations = new EnumMap<TypeStyles, Set<IntTuple>>(TypeStyles.class);
        operations.put(TypeStyles.KeywordBold,new HashSet<IntTuple>());
    }

    public SpannableString applyAll(String typeS) {
        /* just a shortcut, all we have for now is this one style */
        boldKeywords(typeS);

        return prettyify(typeS);
    }

    private void boldKeywords(String typeS) {

        if (typeS != null) {
            String[] typeA;

            typeA = typeS.trim().split(" ", 3);

            final Integer typeALength = typeA.length;


            if (typeALength > 0) {
                final String keyword = typeA[0];

                if (keywordS.contains(keyword)) {
                    final Set<IntTuple> currSet;
                    currSet = operations.get(TypeStyles.KeywordBold);

                    if (typeALength >= 2) {
                        final String context = typeA[1];

                        if (!contextS.contains(context)) {
                            currSet.add(new IntTuple(0, keyword.length()));
                        }
                    } else {
                            currSet.add(new IntTuple(0, keyword.length()));
                    }
                }

            }

        }

    }

    private SpannableString prettyify(String typeS) {
        SpannableString typeSpn = new SpannableString(typeS);

        for (EnumMap.Entry<TypeStyles,Set<IntTuple>> entry : operations.entrySet()) {

            final StyleSpan appStyle;

            switch (entry.getKey()) {
                case KeywordBold: appStyle = new StyleSpan(Typeface.BOLD);
                                  break;
                default: appStyle = null; /* is there an identity style I should use ? */
            }

            for (IntTuple tp: entry.getValue()) {
                if (appStyle != null) {
                    typeSpn.setSpan(appStyle,tp.fst,tp.snd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

        }

        return typeSpn;
    }

    private class IntTuple {
        public final Integer fst;
        public final Integer snd;

        public IntTuple(Integer fst, Integer snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }
}
