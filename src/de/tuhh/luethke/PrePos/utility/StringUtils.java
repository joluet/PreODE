package de.tuhh.luethke.PrePos.utility;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final SimpleDateFormat ISO_8601_BASE = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss");

    private static final Pattern ISO_8601_EXTRAS = Pattern
	    .compile("^(\\.\\d+)?(?:Z|([+-])(\\d{2}):(\\d{2}))?$");

    /**
     * Gets the time, in milliseconds, from an XML date time string as defined
     * at http://www.w3.org/TR/xmlschema-2/#dateTime
     * 
     * @param xmlDateTime
     *            the XML date time string
     */
    public static long getTime(String xmlDateTime) {
	// Parse the date time base
	ParsePosition position = new ParsePosition(0);
	Date date = ISO_8601_BASE.parse(xmlDateTime, position);
	if (date == null) {
	    throw new IllegalArgumentException("Invalid XML dateTime value: "
		    + xmlDateTime + " (at position " + position.getErrorIndex()
		    + ")");
	}

	// Parse the date time extras
	Matcher matcher = ISO_8601_EXTRAS.matcher(xmlDateTime
		.substring(position.getIndex()));
	if (!matcher.matches()) {
	    // This will match even an empty string as all groups are optional.
	    // Thus a
	    // non-match means invalid content.
	    throw new IllegalArgumentException("Invalid XML dateTime value: "
		    + xmlDateTime);
	}

	long time = date.getTime();

	// Account for fractional seconds
	String fractional = matcher.group(1);
	if (fractional != null) {
	    // Regex ensures fractional part is in (0,1)
	    float fractionalSeconds = Float.parseFloat(fractional);
	    long fractionalMillis = (long) (fractionalSeconds * 1000.0f);
	    time += fractionalMillis;
	}

	// Account for timezones
	String sign = matcher.group(2);
	String offsetHoursStr = matcher.group(3);
	String offsetMinsStr = matcher.group(4);
	if (sign != null && offsetHoursStr != null && offsetMinsStr != null) {
	    // Regex ensures sign is + or -
	    boolean plusSign = sign.equals("+");
	    int offsetHours = Integer.parseInt(offsetHoursStr);
	    int offsetMins = Integer.parseInt(offsetMinsStr);

	    // Regex ensures values are >= 0
	    if (offsetHours > 14 || offsetMins > 59) {
		throw new IllegalArgumentException("Bad timezone: "
			+ xmlDateTime);
	    }

	    long totalOffsetMillis = (offsetMins + offsetHours * 60L) * 60000L;

	    // Convert to UTC
	    if (plusSign) {
		time -= totalOffsetMillis;
	    } else {
		time += totalOffsetMillis;
	    }
	}
	return time;
    }

}
