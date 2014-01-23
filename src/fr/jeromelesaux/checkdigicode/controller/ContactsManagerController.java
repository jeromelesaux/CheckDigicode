package fr.jeromelesaux.checkdigicode.controller;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.jeromelesaux.checkdigicode.data.ContactAddessDAO;
import fr.jeromelesaux.checkdigicode.data.model.Contact;
import fr.jeromelesaux.checkdigicode.data.model.ContactAddress;
import fr.jeromelesaux.checkdigicode.data.model.ContactResult;
import fr.jeromelesaux.checkdigicode.location.Constants;
import fr.jeromelesaux.checkdigicode.location.Coordinates;
import fr.jeromelesaux.checkdigicode.location.LocationRetriever;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.jeromelesaux.checkdigicode.location.Constants.cleanString;


/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class ContactsManagerController extends AsyncTask<Void,Integer,Void> {
    Context context;
    public ProgressBar progressBar;
    public TextView textView;
    private String textToDisplay;


    private Boolean isAvailable = true;
    private ContactAddessDAO dao;
    private Integer nbContacts;
    private Integer nbContactsTraited;
    private ContactsManagerControllerEnded delegate;
    Pattern expression;

    public ContactsManagerController(Context context,ContactsManagerControllerEnded delegate) {
        this.delegate= delegate;
        this.context = context;
        dao = this.getDao();
        nbContacts = 0;
        nbContactsTraited = 0;
    }

    public ContactAddessDAO getDao(){
        if (dao == null) {
            dao = new ContactAddessDAO(this.context);

        }
        if (!dao.isOpened()) {
            dao.open();
        }
        nbContacts = 0;
        nbContactsTraited = 0;
        return dao;
    }


    public void doAnalyse()
        throws IOException {
        isAvailable = false;
        expression = Pattern.compile(Constants.DIGICODE_REGEX);
        HashMap<String,Contact> contacts;
        contacts = retreiveContacts();
        contacts = findDigicodes(contacts);
        contacts = retreiveLocations(contacts);
        saveData(contacts);
        isAvailable = true;

    }

    public ContactResult getCoordinatesForContactString(String contactToFind){
        dao = this.getDao();
        return dao.getCoordinatesForContact(contactToFind);
    }

    public String getDefaultPhoneNumberForContactId(String id) {

        String defaultPhoneNumber = null;
        ContentResolver cr = context.getContentResolver();
        Cursor phoneCursor = cr.query(
 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 		    null,
 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
 		    new String[]{id}, null);
        if (phoneCursor.getCount() < 1 ) {
            return "";
        }

        int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
        if  ( phoneCursor.moveToNext() ){
             defaultPhoneNumber = cleanString(phoneCursor.getString(phoneIndex));
        }

        if (defaultPhoneNumber == null) {
            defaultPhoneNumber = "";
        }

        phoneCursor.close();
        return defaultPhoneNumber;
    }

    public  HashMap<String,Contact> retreiveContacts() {
        HashMap<String,Contact> contacts;
        contacts = new HashMap<String, Contact>();
        ContentResolver cr = context.getContentResolver();
        Cursor contactCursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (contactCursor.getCount() > 0) {
            nbContacts = contactCursor.getCount();
            textToDisplay = "Retreiving contacts...";
            resetProgressBar();

            while (contactCursor.moveToNext()) {
                String id = cleanString(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID)));
                String name = cleanString(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                //String defaultPhoneNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.))
                Log.w(context.getPackageName(),"Name : " + name + " with id : " + id);
                Contact contact = new Contact(id,name);
                if (!contacts.containsKey(id)) {
                    contacts.put(id,contact);

                }
                Cursor postalsCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = "
                        + id, null, null);

                int poboxIndex =  postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX);
                int streetIndex = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
                int cityIndex = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
                int regionIndex = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION);
                int postCodeIndex = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
                int countryIndex = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
                //int typePosition = postalsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);


                // recuperation des adresses pour le contact identifé par "id"
                while (postalsCursor.moveToNext()) {
                    StringBuilder locationAddress =  new StringBuilder();
                    locationAddress.append(cleanString(postalsCursor.getString(poboxIndex)));
                    locationAddress.append(Constants.separator);
                    locationAddress.append(cleanString(postalsCursor.getString(streetIndex)));
                    locationAddress.append(Constants.separator);
                    locationAddress.append(cleanString(postalsCursor.getString(cityIndex)));
                    locationAddress.append(Constants.separator);
                    locationAddress.append(cleanString(postalsCursor.getString(regionIndex)));
                    locationAddress.append(Constants.separator);
                    locationAddress.append(cleanString(postalsCursor.getString(postCodeIndex)));
                    locationAddress.append(Constants.separator);
                    locationAddress.append(cleanString(postalsCursor.getString(countryIndex)));
                    ContactAddress contactAddress = new ContactAddress();
                    contactAddress.setAddress(locationAddress.toString());
                    contactAddress.setId(id);
                    contacts.get(id).getContactAddresses().add(contactAddress);
                }

                postalsCursor.close();

                String whereClause =  ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] whereParams = new String[] {id,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                Cursor noteCursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    whereClause,
                    whereParams,
                    null
                );
                int notePosition = noteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE);
                while ( noteCursor.moveToNext()) {
                    ContactAddress contactAddress = new ContactAddress();
                    contactAddress.setAddress(cleanString(noteCursor.getString(notePosition)));
                    contactAddress.setId(id);
                    contacts.get(id).getContactAddresses().add(contactAddress);
                    Log.w(ContactsManagerController.class.getName(),"Find note for " + contact.getDisplayName() + " with text value : " + contactAddress.getAddress());

                }
                noteCursor.close();
            }
            nbContactsTraited += 1;
            publishProgress(nbContactsTraited);
        }
        contactCursor.close();
        return contacts;
    }


    private HashMap<String,Contact> retreiveLocations(HashMap<String,Contact> contacts)
        throws IOException {
        nbContacts = contacts.size();
        textToDisplay = "Retreiving locations from contacts addresses...";
        resetProgressBar();
        for ( String contactId : contacts.keySet() ) {
            for (ContactAddress contactAddress : contacts.get(contactId).getContactAddresses()) {
                LocationRetriever locationRetreiver = new LocationRetriever(context);
                Coordinates coordinates = locationRetreiver.retreiveCoordinates(contactAddress.getAddress());
                contactAddress.setCoordinates(coordinates);
            }
            nbContactsTraited += 1;
            publishProgress(nbContactsTraited);
        }
        return contacts;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean avaible) {
        isAvailable = avaible;
    }

    private HashMap<String,Contact> findDigicodes(HashMap<String,Contact> contacts) {
        nbContacts = contacts.size();
        textToDisplay = "Finding digicodes in contacts...";
        resetProgressBar();

        for ( String contactId : contacts.keySet() ) {
            for (ContactAddress contactAddress : contacts.get(contactId).getContactAddresses()) {

                String address = contactAddress.getAddress();
                if ( address != null && address.length() > 0) {
                    Matcher matcher = expression.matcher(address);
                    while (matcher.find()) {
                        String digicode = matcher.group();
                        contactAddress.getDigicodes().add(digicode);
                    }
                }

            }
            nbContactsTraited++;
            publishProgress(nbContactsTraited);
        }
        return contacts;
    }

    private void saveData(HashMap<String,Contact> contacts) {
        dao = this.getDao();
        resetDB();
        textToDisplay = "Saving contacts data...";
        nbContacts = contacts.size();
        resetProgressBar();
        for ( String contactId : contacts.keySet() ) {
          //  dao.insertContact(contacts.get(contactId));
            for (ContactAddress contactAddress : contacts.get(contactId).getContactAddresses()) {
                dao.insertContactAddress(contactAddress, contacts.get(contactId));
            }
            nbContactsTraited+=1;
            publishProgress(nbContactsTraited);
        }
    }

    private void resetDB() {
        dao = this.getDao();
        dao.resetData();
    }


    public List<ContactResult> getDigicodes(Coordinates coordinates) {
        if (isAvailable) {

            textToDisplay = coordinates.toString();
            publishProgress(0);
            dao = this.getDao();
            return dao.getDigicodesNearLocation(coordinates);
        }
        else return null;

    }

    /***
     *
     * @return
     */
    public ProgressBar getProgressBar() {
         return progressBar;
     }

     public void setProgressBar(ProgressBar progressBar) {
         this.progressBar = progressBar;
     }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    private void resetProgressBar() {
        nbContactsTraited = 0;
        progressBar.setProgress(0);
        progressBar.setMax(nbContacts);
    }

    public void updateUI(){
        publishProgress(progressBar.getProgress());
    }


    /***
     * methodes pour le refresh de l'UI du thread principal
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        super.onProgressUpdate(values);
        // Mise à jour de la ProgressBar
        textView.setText(textToDisplay);
        progressBar.setProgress(values[0]);

    }
    @Override
    protected void onPostExecute(Void result) {
          super.onPostExecute(result);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText("Done...");
        delegate.contactsManagerEnded();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (isAvailable) {
                this.doAnalyse();
            }
        } catch (IOException e) {
            Log.d(context.getPackageName(),"Error in traitement doAnalyse " + e.getMessage());
        }
        return null;
    }
}
