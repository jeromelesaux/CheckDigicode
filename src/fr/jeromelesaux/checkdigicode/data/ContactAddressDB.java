package fr.jeromelesaux.checkdigicode.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 28/04/13
 * Time: 10:40
 * To change this template use File | Settings | File Templates.
 */
public class ContactAddressDB extends SQLiteOpenHelper {


    public static final String TABLE_CONTACT = "CONTACT";
    public static final String TABLE_DIGICODE = "DIGICODE";
    public static final String COL_LONGITUDE = "LONGITUDE";
    public static final String COL_LATITUDE = "LATITUDE";
    public static final String COL_ID = "ID";
    public static final String COL_DIGICODE = "DIGICODE_VALUE";
    public static final String COL_ADDRESS = "ADDRESS";
    public static final String COL_DISPLAYNAME = "DISPLAY_NAME";
    private static final String CREATE_TABLE1 = "CREATE TABLE " + TABLE_CONTACT +
        " ("+COL_ID + " TEXT NOT NULL, " + COL_DISPLAYNAME +  " TEXT,  " + COL_ADDRESS  + " TEXT, " +
        COL_LATITUDE + " DOUBLE, " + COL_LONGITUDE + " DOUBLE " + ");" ;
    private static final String CREATE_TABLE2 = "CREATE TABLE " + TABLE_DIGICODE + " ( " + COL_DIGICODE + " TEXT, " +
        COL_ID + " TEXT NOT NULL, FOREIGN KEY( " + COL_ID  + " ) REFERENCES " +
        TABLE_CONTACT + " (" + COL_ID + ")" +
        ");";

    private static final String DROP_DB1 = "DROP TABLE IF EXISTS " + TABLE_DIGICODE  + ";" ;
    private static final String DROP_DB2 = "DROP TABLE IF EXISTS " + TABLE_CONTACT + ";";





    public ContactAddressDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);


    }

    public ContactAddressDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version,
                            DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(ContactAddressDB.class.getName(), "Create DB with Script : " + CREATE_TABLE1);
        db.execSQL(CREATE_TABLE1);
        Log.w(ContactAddressDB.class.getName(), "Create DB with Script : " + CREATE_TABLE2);
        db.execSQL(CREATE_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ContactAddressDB.class.getName(), "Upgrade DB with oldVersion "+ oldVersion + " newVersion " + newVersion);
        db.execSQL(DROP_DB1);
        db.execSQL(DROP_DB2);
        onCreate(db);
    }
}
