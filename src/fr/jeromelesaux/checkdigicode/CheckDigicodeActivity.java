package fr.jeromelesaux.checkdigicode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.CheckDigicode.R;
import fr.jeromelesaux.checkdigicode.controller.ContactsManagerController;
import fr.jeromelesaux.checkdigicode.controller.ContactsManagerControllerEnded;
import fr.jeromelesaux.checkdigicode.data.model.ContactResult;
import fr.jeromelesaux.checkdigicode.location.Coordinates;
import fr.jeromelesaux.checkdigicode.location.GPSTracker;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class CheckDigicodeActivity extends Activity implements ContactsManagerControllerEnded {

    private ContactsManagerController controller;
    private GPSTracker tracker;
    private Timer timer = null;
    private TextView textView;
    private EditText contactSearchEditText;
    private Button findContactButton;
    private TextView searchDisplayTextView;
    private Boolean activityAlreadyComputed = false;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //this.createFakeNotification();

        // link objects with UI
        textView = (TextView) findViewById(R.id.checkDigicodeMainText);
        contactSearchEditText = (EditText) findViewById(R.id.contactSearchEditText);
        findContactButton = (Button) findViewById(R.id.findContactButton);
        searchDisplayTextView = (TextView) findViewById(R.id.searchTextView);
        contactSearchEditText.setEnabled(false);
        findContactButton.setEnabled(false);
        // link ended

        textView.setText("Waiting for GPS.");
        tracker = new GPSTracker(this.getApplicationContext());


    }

    @Override
    public void onResume() {
        super.onResume();

        if (!activityAlreadyComputed) {
            // test du GPS
            if (!tracker.canGetLocation()) {
                final Context context = this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enable GPS ?");
                builder.setMessage("Your GPS is disabled to use CheckDigicode, you may need to switch on.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = builder.create();

                // show it
                alertDialog.show();
            }

            controller = new ContactsManagerController(this, this);

            controller.setProgressBar((ProgressBar) findViewById(R.id.mainProgressBar));
            controller.setTextView((TextView) findViewById(R.id.checkDigicodeMainText));

            controller.execute();
            activityAlreadyComputed = true;
        }

    }


    public void checkContactsLocation() {
        if (!controller.isAvailable()) {
            return;
        }
        Coordinates coordinates = new Coordinates(tracker.getLongitude(), tracker.getLatitude());
        List<ContactResult> results = controller.getDigicodes(coordinates);

        if (results != null && results.size() != 0) {
            // create notification
            this.createNotification(results);

//            StringBuilder stringBuilder = new StringBuilder("The digicode(s) found is (are) : \n");
//            for (String digicode : result.getDigicodes()) {
//                stringBuilder.append(digicode + "\n");
//            }
//
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Your near " + result.getDisplayName());
//            builder.setMessage(stringBuilder.toString());
//            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    dialog.cancel();
//                }
//            });
//            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    // if this button is clicked, just close
//                    // the dialog box and do nothing
//                    dialog.cancel();
//                }
//            });
//
//            // create alert dialog
//            AlertDialog alertDialog = builder.create();
//
//
//            // show it
//            alertDialog.show();
//

        }
    }

    public void createFakeNotification() {

        Uri phoneData = Uri.parse("tel:+33145344273");
        Intent notificationIntent = new Intent(Intent.ACTION_DIAL);
        notificationIntent.setData(phoneData);
        PendingIntent dialerIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);

        builder.setStyle(new Notification.InboxStyle()
                .addLine("Contact found jls with blabla")
                .addLine("digicodes : ......")
                .addLine("..............")
                .setSummaryText("summary text"));

        Notification notification = builder.setContentTitle("Contact found.")
                .setSmallIcon(R.drawable.digicode_image)
                .setContentIntent(dialerIntent)
                .addAction(android.R.drawable.ic_dialog_dialer, "Call", dialerIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        notificationManager.notify(01, notification);


    }


    public void createNotification(List<ContactResult> results) {
        for (ContactResult result : results) {
       /*
        Resources resources = this.getApplicationContext().getResources();
        Bitmap bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.digicode_image);
        bitmapIcon = Bitmap.createScaledBitmap(bitmapIcon, 60, 60, false);
         */
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("with possible digicode(s) : ");

            if (result != null) {

                for (String digicode : result.getDigicodes()) {
                    stringBuilder.append(digicode + " ");
                }
            }


            String phoneNumber = "tel:" + controller.getDefaultPhoneNumberForContactId(result.getContactId());
            Uri phoneData = Uri.parse(phoneNumber);
            Intent notificationIntent = new Intent(Intent.ACTION_DIAL);
            notificationIntent.setData(phoneData);
            PendingIntent dialerIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            // Build notification

//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setSmallIcon(R.drawable.digicode_image);
//        builder.setTicker("Contact's address found.");
//        builder.setContentTitle("Contact's address found.");
//        builder.setLargeIcon(bitmapIcon);
//        builder.setContentIntent(dialerIntent);
//
//        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle(builder);
//        bigPictureStyle.setBigContentTitle("Contact's address found.");
//        bigPictureStyle.setSummaryText(stringBuilder.toString());
//        bigPictureStyle.bigLargeIcon(bitmapIcon);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBigContentTitle("Contact Found " + result.getDisplayName())
                    .addLine(stringBuilder.toString());


            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Contact Found " + result.getDisplayName())
                    .setContentText(stringBuilder.toString())
                    .setSmallIcon(R.drawable.digicode_image)
                    .setContentIntent(dialerIntent)
                    .addAction(android.R.drawable.ic_dialog_dialer, "Call", dialerIntent)
                    .setStyle(inboxStyle).build();


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Hide the notification after its selected
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            notificationManager.notify(Integer.parseInt(result.getContactId()), notification);
        }

    }

    @Override
    public void contactsManagerEnded() {

        contactSearchEditText.setEnabled(true);
        contactSearchEditText.setFocusable(true);
        contactSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactSearchEditText.setText("");
            }
        });
        findContactButton.setEnabled(true);
        findContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContactAndGetCoordinates();
            }
        });


        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    checkContactsLocation();
                }
            }, 0, 30000);
        }
    }


    public void searchContactAndGetCoordinates() {
        String contactToFind = contactSearchEditText.getText().toString();
        ContactResult contactResult = controller.getCoordinatesForContactString(contactToFind);
        if (contactResult == null) {
            searchDisplayTextView.setText("No contact found with : " + contactToFind);
        } else {
            searchDisplayTextView.setText("Find Contact : " + contactResult.getDisplayName() + " with coordinates : " +
                    contactResult.getCoordinates().toString() + "  \nwith address : " + contactResult.getAddress() + "\nand digicode(s) " + contactResult.digicodes());
        }

    }

}
