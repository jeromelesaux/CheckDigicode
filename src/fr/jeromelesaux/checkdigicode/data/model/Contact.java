package fr.jeromelesaux.checkdigicode.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 12:08
 * To change this template use File | Settings | File Templates.
 */
public class Contact implements Serializable {

    private List<ContactAddress> contactAddresses;
    private String id;
    private String displayName;


    public Contact() {
        this.contactAddresses = new ArrayList<ContactAddress>();
    }

    public Contact(String id, String displayName)  {
        this.contactAddresses = new ArrayList<ContactAddress>();
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<ContactAddress> getContactAddresses() {
          return contactAddresses;
      }

      public void setContactAddresses(List<ContactAddress> contactAddresses) {
          this.contactAddresses = contactAddresses;
    }

}
