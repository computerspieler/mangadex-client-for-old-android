package fr.speilkoun.mangareader.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.format.Time;
import android.util.Log;

public class ISO8601DateParser {
    static String TAG = "ISO8601DateParser";

    static final Pattern REGEX;
    static {
        //AAAA-MM-JJTHH:MM:SS,ss-/+FF:ff
        REGEX = Pattern.compile(
            // Date
            "(\\d{4})-(\\d{2})-(\\d{2})" +
            "("+
                // Time
                "T(\\d{2}):(\\d{2}):(\\d{2})([.,](\\d+))?"+
                // Timezone
                "(Z|([+\\-])(\\d{2})[:]?(\\d{2}))" +
            ")?"
        );
    }

    public static Time parse(String str) {
        Time output = new Time();
        
        Matcher m = REGEX.matcher(str);
        if(!m.find()) {
            Log.e(TAG, "Could not parse the following date: " + str);
            return null;
        }

        // TODO: msecs support
        int second = 0, minute = 0, hour = 0;
        int tz_min = 0, tz_hour = 0;

        if(m.group(4).length() > 0) {
            hour = Integer.parseInt(m.group(5));
            minute = Integer.parseInt(m.group(6));
            second = Integer.parseInt(m.group(7));

            if(!m.group(10).equals("Z")) {
                tz_hour = Integer.parseInt(m.group(12));
                tz_min = Integer.parseInt(m.group(13));

                if(m.group(11) == "-") {
                    tz_hour *= -1;
                    tz_min *= -1;
                }
            }
        }

        output.set(
            second,
            minute,
            hour,
            Integer.parseInt(m.group(3)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(1))
        );
        output.gmtoff = (tz_min + tz_hour * 60) * 60;

        return output;
    }
}
