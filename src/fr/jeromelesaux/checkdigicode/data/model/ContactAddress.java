package fr.jeromelesaux.checkdigicode.data.model;

import fr.jeromelesaux.checkdigicode.location.Coordinates;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class ContactAddress implements Serializable {
    private String id;
    private String address;
    private Coordinates coordinates;
    private Set<String> digicodes;

    public ContactAddress() {
        digicodes = new HashSet<String>();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getDigicodes() {
        return digicodes;
    }

    public void setDigicodes(Set<String> digicodes) {
        this.digicodes = digicodes;
    }
}
