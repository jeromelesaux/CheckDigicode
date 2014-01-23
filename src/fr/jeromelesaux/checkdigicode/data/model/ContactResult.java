package fr.jeromelesaux.checkdigicode.data.model;

import fr.jeromelesaux.checkdigicode.location.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 07/05/13
 * Time: 10:24
 * To change this template use File | Settings | File Templates.
 */
public class ContactResult {
    List<String> digicodes;
    String Address;
    String displayName;
    String contactId;
    Coordinates coordinates;

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public ContactResult() {
        digicodes= new ArrayList<String>();

    }

    public ContactResult(String displayName) {
        this.displayName = displayName;
        digicodes = new ArrayList<String>();
    }

    public void addDigicode(String digicode) {
        digicodes.add(digicode);
    }

    public List<String> getDigicodes() {
        return digicodes;
    }

    public void setDigicodes(List<String> digicodes) {
        this.digicodes = digicodes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String digicodes() {
        StringBuilder digicodes = new StringBuilder();
        for (String digicode : this.digicodes) {
            digicodes.append(" " + digicode);
        }
        return digicodes.toString();
    }

}
