package fr.jeromelesaux.checkdigicode.location;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final double MinDouble = 0.;
    public static final String separator = System.getProperty( "line.separator" );
    public static final String DIGICODE_REGEX = "((\\d{3,4}[a-fA-F])|(\\d{2,3}[a-fA-F]\\d)|(\\d{1,2}[a-fA-F]\\d{1,2})|(\\d[a-fA-F]\\d{2,3})|([a-fA-F]\\d{3,4})(\\d{4,5}))(\\s|$)";
    public static final double WGS84_100M_APPROX = 0.00089982311916;

    public static String cleanString(String input) {
        if (input == null) {
            return "";
        }
        else {
            return  input.replace("null","").replace("\n"," ");
        }
    }

}
