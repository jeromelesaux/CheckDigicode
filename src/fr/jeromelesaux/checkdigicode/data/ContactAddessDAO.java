package fr.jeromelesaux.checkdigicode.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import fr.jeromelesaux.checkdigicode.data.model.Contact;
import fr.jeromelesaux.checkdigicode.data.model.ContactAddress;
import fr.jeromelesaux.checkdigicode.data.model.ContactResult;
import fr.jeromelesaux.checkdigicode.location.Constants;
import fr.jeromelesaux.checkdigicode.location.Coordinates;

import java.util.ArrayList;
import java.util.List;

import static fr.jeromelesaux.checkdigicode.location.Constants.cleanString;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 28/04/13
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public class ContactAddessDAO {
    private static final String DB_NAME = "CONTACT_ADRESS_DB";
    private static final int CURRENT_VERSION = 1;
    private static final String DIGICODES_NEAR_LOCATION_QUERY = "SELECT DISTINCT c." + ContactAddressDB.COL_DISPLAYNAME + ", d." +
            ContactAddressDB.COL_DIGICODE + ", c." + ContactAddressDB.COL_ID +
            " FROM " + ContactAddressDB.TABLE_CONTACT + " c LEFT OUTER JOIN " + ContactAddressDB.TABLE_DIGICODE + " d ON c." +
            ContactAddressDB.COL_ID + " = d." + ContactAddressDB.COL_ID;
    private static final String FIND_COORDINATES_FOR_CONTACT ="SELECT DISTINCT c." + ContactAddressDB.COL_DISPLAYNAME + ", c." +
            ContactAddressDB.COL_LATITUDE + ", c." + ContactAddressDB.COL_LONGITUDE +
            ", c." + ContactAddressDB.COL_ADDRESS + ", d." + ContactAddressDB.COL_DIGICODE +
            " FROM " + ContactAddressDB.TABLE_CONTACT +
            " c LEFT OUTER JOIN " + ContactAddressDB.TABLE_DIGICODE + " d ON c." +
            ContactAddressDB.COL_ID + " = d." + ContactAddressDB.COL_ID +
            " WHERE c." + ContactAddressDB.COL_DISPLAYNAME + " LIKE ";
    private SQLiteDatabase db;
    private ContactAddressDB caDB;

    /**
     * @param context
     */
    public ContactAddessDAO(Context context) {
        caDB = new ContactAddressDB(context, DB_NAME, null, CURRENT_VERSION);
    }

    /**
     * ouvre la base en ecriture
     */
    public void open() throws SQLiteException {
        db = caDB.getWritableDatabase();
    }

    public Boolean isOpened() {
        if (db == null) {
            this.open();
        }
        return db.isOpen();
    }

    /**
     * ferme la base
     */
    public void close() {
        db.close();
    }

    /**
     * @return retourne l'instance de la base SQLite
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * insert l'objet ContactAddess dans la base Sqlite
     *
     * @param contactAddress
     * @return success ou echec (-1 en cas d'echec)
     */
    public long insertContactAddress(ContactAddress contactAddress, Contact contact) {
        long ret = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactAddressDB.COL_ID, contactAddress.getId());
        contentValues.put(ContactAddressDB.COL_DISPLAYNAME, contact.getDisplayName());
        contentValues.put(ContactAddressDB.COL_ADDRESS, contactAddress.getAddress());
        contentValues.put(ContactAddressDB.COL_LATITUDE, contactAddress.getCoordinates().getLatitude());
        contentValues.put(ContactAddressDB.COL_LONGITUDE, contactAddress.getCoordinates().getLongitude());

        ret = db.insert(ContactAddressDB.TABLE_CONTACT, null, contentValues);

        Log.w(ContactAddessDAO.class.getName(), "Insert values " + ContactAddressDB.TABLE_CONTACT + " " + contentValues.toString() + " return " + ret);

        ret = insertDigicodeContact(contactAddress);

        return ret;
    }

//    public long insertContact(Contact contact) {
//        long ret = 0;
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ContactAddressDB.COL_ID, contact.getId());
//        contentValues.put(ContactAddressDB.COL_DISPLAYNAME, contact.getDisplayName());
//        ret = db.insert(ContactAddressDB.TABLE_CONTACT,null,contentValues);
//
//        Log.w(ContactAddessDAO.class.getName(),"Insert values " + ContactAddressDB.TABLE_CONTACT + " " + contentValues.toString() + " return " + ret);
//
//        return ret;
//    }

    /**
     * insert l'objet les digicodes de l'objet ContactAddress dans la base
     *
     * @param contactAddress
     * @return
     */
    public long insertDigicodeContact(ContactAddress contactAddress) {
        long valReturned = 0;
        for (String digicode : contactAddress.getDigicodes()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactAddressDB.COL_ID, contactAddress.getId());
            contentValues.put(ContactAddressDB.COL_DIGICODE, digicode);
            valReturned = db.insert(ContactAddressDB.TABLE_DIGICODE, null, contentValues);
            Log.w(ContactAddessDAO.class.getName(), "Insert values " + ContactAddressDB.TABLE_DIGICODE + " " + contentValues.toString() + " return " + valReturned);
        }
        return valReturned;
    }

    public long resetData() {
        long ret = 0;
        if (exists(ContactAddressDB.TABLE_DIGICODE)) {
            ret += db.delete(ContactAddressDB.TABLE_DIGICODE, null, null);
        }
        if (exists(ContactAddressDB.TABLE_CONTACT)) {
            ret += db.delete(ContactAddressDB.TABLE_CONTACT, null, null);
        }
        Log.w(ContactAddessDAO.class.getName(), "Delete return " + ret);
        return ret;
    }

    public boolean exists(String table) {
        try {
            db.execSQL("SELECT * FROM " + table);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    > // 6367 is "standard" radius of earth in km
    > // 1.852 is km per Nautical Mile; this converts R to radius in naut miles
    > private final static double R = 6367.0 / 1.852;
    > private final static double RADIANSPERDEGREE = 0.017453293;
    >
    > private double computeDistance(double lat1, double lon1, double lat2,
    >double lon2)
    > {
    > // all values are of type "double"; lat1,lon1,lat2 and lon2 are the
    > // coordinates we are using in the calculation, in RADIANS.
    > // You will have to convert from degrees to radians using the
    > // conversion factor, RADIANSPERDEGREE
    > lat2 = lat2 * RADIANSPERDEGREE;
    > lon2 = lon2 * RADIANSPERDEGREE;
    >
    > double dlon = lon2 - lon1; // difference in longitude
    > double dlat = lat2 - lat1; // difference in latitude
    >
    > double a = (Math.sin(dlat/2.0)* Math.sin(dlat/2.0)) +
    > (Math.cos(lat1) * Math.cos(lat2) *
    > Math.sin(dlon/2.0) * Math.sin(dlon/2.0));
    >
    > double c = 2.0 * Math.asin(Math.sqrt(a));
    >
    > return (R * c); // Final distance is in nautical miles.
    > }
    >
    Clause where ==  6367 * 2.0 * ASIN(SQRT( SIN( ABS( c.LATITUDE - coordinates.getLatitude() ) /2.0 ) );
    select  6367 * 2 * asin( (sin((lat - 4.9)/2) * sin((lat - 4.9)/2)) + (cos(lat)*cos(4.9)*sin((lon-47.9)/2)*sin((lon-47.9)/2)) ) from coordinates;
     */

    /*    x = (lonRef - lon) * cos ( latRef )
        y = latRef - lat
        distance = EarthRadius * sqrt( x*x + y*y )*/
                /*
                10001.965729km = 90 degrees
                1km = 90/10001.965729 degrees = 0.0089982311916 degrees
                10km = 0.089982311915998 degrees
                 */
    public Coordinates adjustCoordinatesMax(Coordinates coordinates) {
        Coordinates newCoordinates = new Coordinates(coordinates.getLongitude() + Constants.WGS84_100M_APPROX, coordinates.getLatitude() + Constants.WGS84_100M_APPROX);
        return newCoordinates;
    }

    public Coordinates adjustCoordinatesMin(Coordinates coordinates) {
        Coordinates newCoordinates = new Coordinates(coordinates.getLongitude() - Constants.WGS84_100M_APPROX, coordinates.getLatitude() - Constants.WGS84_100M_APPROX);
        return newCoordinates;
    }


    public List<ContactResult> getDigicodesNearLocation(Coordinates coordinates) {

        Coordinates maxCoordinates = adjustCoordinatesMax(coordinates);
        //Coordinates newCoordinates = new Coordinates(coordinates.getLongitude() + (coordinates.getLongitude() * Constants.WGS84_100M_APPROX), coordinates.getLatitude() + (coordinates.getLatitude() *
        //        Constants.WGS84_100M_APPROX));
        Coordinates minCoordinates = adjustCoordinatesMin(coordinates);
        StringBuilder query = new StringBuilder(DIGICODES_NEAR_LOCATION_QUERY);
        query.append(" WHERE ( c.LATITUDE BETWEEN " + String.valueOf(minCoordinates.getLatitude()) + " AND " + String.valueOf(maxCoordinates.getLatitude()) +
                " ) AND ( c.LONGITUDE BETWEEN " + String.valueOf(minCoordinates.getLongitude()) + " AND " + String.valueOf(maxCoordinates.getLongitude()) + " );");
        Cursor cursor = db.rawQuery(query.toString(), null);


        Log.w(ContactAddessDAO.class.getName(), query.toString() + " returns " + cursor.getCount());

        return cursorToDigicodes(cursor);
    }

    public List<ContactResult> cursorToDigicodes(Cursor c) {
        List<ContactResult> results = new ArrayList<ContactResult>();


        if (c.getCount() < 1) {
            return results;
        }


        int digicodeIndex = c.getColumnIndex(ContactAddressDB.COL_DIGICODE);
        int displayNameIndex = c.getColumnIndex(ContactAddressDB.COL_DISPLAYNAME);
        int contactIdIndex = c.getColumnIndex(ContactAddressDB.COL_ID);

        while (c.moveToNext()) {
            ContactResult result = new ContactResult();


            result.setDisplayName(cleanString(c.getString(displayNameIndex)));
            result.setContactId(cleanString(c.getString(contactIdIndex)));
            String digicode = cleanString(c.getString(digicodeIndex));
            result.addDigicode(digicode);
            results.add(result);

        }

        c.close();

        return results;
    }

    public ContactResult getCoordinatesForContact(String contact) {
        Coordinates coordinates = null;
        StringBuilder query = new StringBuilder(FIND_COORDINATES_FOR_CONTACT);
        query.append("'%" + cleanString(contact) +"%'");
        Cursor cursor = db.rawQuery(query.toString(), null);
        Log.w(ContactAddessDAO.class.getName(), query.toString() + " returns " + cursor.getCount());
        if ( cursor.getCount() < 1) {
            return null;
        }


        int displayNameIndex = cursor.getColumnIndex(ContactAddressDB.COL_DISPLAYNAME);
        int latitudeIndex = cursor.getColumnIndex(ContactAddressDB.COL_LATITUDE);
        int longitudeIndex = cursor.getColumnIndex(ContactAddressDB.COL_LONGITUDE);
        int addressIndex = cursor.getColumnIndex(ContactAddressDB.COL_ADDRESS);
        int digicodeIndex = cursor.getColumnIndex(ContactAddressDB.COL_DIGICODE);

        cursor.moveToNext();

        double latitude = cursor.getDouble(latitudeIndex);
        double longitude = cursor.getDouble(longitudeIndex);
        coordinates = new Coordinates(longitude,latitude);

        ContactResult result = new ContactResult();
        result.setCoordinates(coordinates);
        result.setDisplayName(cleanString(cursor.getString(displayNameIndex)));
        result.setAddress(cleanString(cursor.getString(addressIndex)));

        if (cleanString(cursor.getString(digicodeIndex)) != "" ) {
            result.addDigicode(cleanString(cursor.getString(digicodeIndex)));
        }

        while (cursor.moveToNext()) {
            if (cleanString(cursor.getString(digicodeIndex)) != "" ) {
                result.addDigicode(cleanString(cursor.getString(digicodeIndex)));
            }
        }

        cursor.close();

        return result;
    }


    private void showTables() {
        Cursor cursor = db.rawQuery("SELECT tbl_name FROM sqlite_master WHERE TYPE ='TABLE'", null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Log.w(ContactAddessDAO.class.getName(), "table presents in db : " + cleanString(cursor.getString(0)));
            }
        }
    }
}
