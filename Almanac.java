import java.util.Calendar;
import java.util.TimeZone;

class Luna {
    double age; // age since newmoon
    double distance; //moon distance in earth radii
    double lat_m;    // moon latitude about eccliptic
    double lon_m;    // moon longitude on eccliptic
    String phase;

    public Luna(double age, double distance, double lat_m, double lon_m, String phase) {
        this.age = age;
        this.distance = distance;
        this.lat_m = lat_m;
        this.lon_m = lon_m;
        this.phase = phase;
    }
}

public class Main {

    //=====================Solar===================
    static int d_o_y() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        return dayOfYear;
    }

    static double get_offset() {
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        offset /= 60000; // convert to minutes
        return offset;
    }

    static void min_2_hr(double mins, StringBuilder str) {
        int hr, mn, sc;
        hr = (int) mins / 60;
        mins /= 60;
        mins = mins - hr;
        mins *= 60;
        mn = (int) mins;
        mins = mins - mn;
        mins *= 60;
        sc = (int) mins;
        str.append(String.format("%02d:%02d:%02d", hr, mn, sc));
    }

    //code from libhdate.
    static void get_utc_sun_time_deg(double latitude, double longitude, double Sun_angle_deg, double[] sunrise, double[] sunset) {
        double gama; /* location of sun in yearly cycle in radians */
        double eqtime; /* diffference betwen sun noon and clock noon */
        double decl; /* sun declanation */
        double ha; /* solar hour engle */
        double sunrise_angle = Math.PI * Sun_angle_deg / 180.0; /* sun angle at sunrise/set */

        int day_of_year;

        /* get the day of year */
        day_of_year = d_o_y();

        /* get radians of sun orbit around earth */
        gama = 2.0 * Math.PI * ((double) (day_of_year - 1) / 365.0);

        /* get the diff betwen suns clock and wall clock in minutes */
        eqtime = 229.18 * (0.000075 + 0.001868 * Math.cos(gama)
                - 0.032077 * Math.sin(gama) - 0.014615 * Math.cos(2.0 * gama)
                - 0.040849 * Math.sin(2.0 * gama));

        /* calculate suns declanation at the equator in radians */
        decl = 0.006918 - 0.399912 * Math.cos(gama) + 0.070257 * Math.sin(gama)
                - 0.006758 * Math.cos(2.0 * gama) + 0.000907 * Math.sin(2.0 * gama)
                - 0.002697 * Math.cos(3.0 * gama) + 0.00148 * Math.sin(3.0 * gama);

        /* we use radians, ratio is 2pi/360 */
        latitude = Math.PI * latitude / 180.0;

        /* the sun real time diff from noon at sunset/rise in radians */
        ha = Math.acos(Math.cos(sunrise_angle.
            - Math.sin(latitude) * Math.sin(decl))
            / (Math.cos(latitude) * Math.cos(decl)));

    /* set sunrise/sunset time */
    double sunrise_min = 720 - (4.0 * (longitude + ha) - eqtime);
    double sunset_min = 720 - (4.0 * (longitude - ha) - eqtime);

    /* adjust sunrise/sunset time for UTC offset */
    double offset = get_offset();
    sunrise_min -= offset;
    sunset_min -= offset;

    /* convert minutes to hh:mm:ss format */
    StringBuilder sunrise_str = new StringBuilder();
    StringBuilder sunset_str = new StringBuilder();
    min_2_hr(sunrise_min, sunrise_str);
    min_2_hr(sunset_min, sunset_str);

    /* return the results */
    sunrise[0] = sunrise_min;
    sunset[0] = sunset_min;
}

//=====================Lunar===================
static void get_lunar_data(Calendar date, double latitude, double longitude, Luna luna) {
    int year = date.get(Calendar.YEAR);
    int month = date.get(Calendar.MONTH) + 1; // January is 0
    int day = date.get(Calendar.DAY_OF_MONTH);
    int hour = date.get(Calendar.HOUR_OF_DAY);
    int minute = date.get(Calendar.MINUTE);
    int second = date.get(Calendar.SECOND);

    // Calculation of Julian date (days since 1900-01-01 12:00:00 UTC)
    int a = (14 - month) / 12;
    int y = year + 4800 - a;
    int m = month + 12 * a - 3;
    double julianDate = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045.5;

    // Calculation of T (number of Julian centuries since 1900-01-01 12:00:00 UTC)
    double T = (julianDate - 2451545.0) / 36525;

    // Calculation of L0 (mean longitude of the Moon)
    double L0 = 218.3164477 + 481267.88123421 * T - 0.0015786 * T * T + T * T * T / 538841.0 - T * T * T * T / 65194000.0;

    // Calculation of l (mean anomaly of the Moon)
    double l = 134.962975 + 477198.8673981 * T + 0.0086972 * T * T + T * T * T / 69699.0 - T * T * T * T / 14712000.0;

    // Calculation of lp (mean anomaly of the Sun)
    double lp = 357.5291092 + 35999.0502909 * T - 0.0001536 * T * T + T * T * T / 24490000.0;

    // Calculation of D (difference in longitude between the Moon and the Sun)
    double D = 297.8502042 + 445267.1115168 * T - 0.00163 * T * T + T * T * T / 545868.0 - T * T * T * T / 113065000.0;

    // Calculation of M (mean elongation of the Moon)
    double M = 357.5291092 + 35999.0502909 * T - 0.0001536 * T * T + T * T * T / 24490000.0;

    // Calculation of M' (mean anomaly of the Moon)
    double M_ = 134.9629795 + 477198.8675055 * T + 0.0087027 * T * T + T * T * T / 69699.0 - T * T * T * T / 14712000.0;

    // Calculation of F (argument of latitude of the Moon)
    double F = 93.272095 + 483202.0175233 * T - 0.0036539 * T * T - T * T * T / 3526000.0 + T * T * T * T / 863310000.0;

    // Calculation of E (eccentricity of the Earth's orbit)
    double E = 1 - 0.002516 * T - 0.0000074 * T * T;

    // Calculation of Ae (distance of the Moon from the Earth)
    double Ae = 6378.14 / 3844;

    // Calculation of moon distance (in Earth radii)
    double moonDistance = Ae / Math.sin(Math.toRadians(Pi / 180) * Math.toRadians(D));

    // Calculation of Moon's apparent right ascension (RA)
    double RA = Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(L0)) * Math.cos(Math.toRadians(23.4406))), Math.cos(Math.toRadians(L0)));

    // Calculation of Moon's apparent declination (Dec)
    double Dec = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(23.4406)) * Math.sin(Math.toRadians(L0))));

    // Calculation of GMST (Greenwich Mean Sidereal Time)
    double GMST = 280.46061837 + 360.98564736629 * (julianDate - 2451545) + 0.000387933 * T * T - T * T * T / 38710000 + longitude;

    // Calculation of LMST (Local Mean Sidereal Time)
    double LMST = GMST + hour * 15 + minute * 0.25 + second * 0.004166666666666667;

    // Calculation of hour angle (HA)
    double HA = LMST - RA;

    // Calculation of lunar phase
    double phase = (1 - Math.cos(Math.toRadians(D))) / 2;

    // Set the values in the Luna object
    luna.setLunarPhase(phase);
    luna.setLunarDistance(moonDistance);
    luna.setLunarRA(RA);
    luna.setLunarDec(Dec);
    luna.setLunarHA(HA);
}

// Conversion from minutes to hh:mm:ss format
static void min_2_hr(double min, StringBuilder time_str) {
    double temp;
    int hour, minute, second;
    int inttemp;

    temp = min / 60.0;
    hour = (int) temp;
    inttemp = (int) ((temp - hour) * 60.0);
    minute = inttemp;
    temp = (inttemp - minute) * 60.0;
    second = (int) Math.round(temp);

    time_str.append(String.format("%02d", hour)).append(":")
            .append(String.format("%02d", minute)).append(":")
            .append(String.format("%02d", second));
}
