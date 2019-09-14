package ct.drowsiness;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    int sts1=0,sts2=0;
    private ProgressDialog pDialog, pDialog1, pDialog2, pDialog3;
    ArrayList<String> volume = new ArrayList<String>();
    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static String url_onoff ="http://www.ctcorphyd.com/Drowsy/onoff.php";
    private static final String url_status= "http://www.ctcorphyd.com/Drowsy/alertsts.php";
    private static String url_volume ="http://www.ctcorphyd.com/Drowsy/volume.php";
    private static String url_all ="http://www.ctcorphyd.com/Drowsy/sendall.php";
    double latitude;
    double longitude;
    int cnt=0;
    SwitchCompat switchCompat;
    String sts,vol,status="0";
    ImageView imageView1;
   TextView tv;
    String phoneNumber="",emailAddress="";
    private final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        switchCompat=(SwitchCompat)findViewById(R.id.switchButton);
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        tv = (TextView) findViewById(R.id.tv);
        isSmsPermissionGranted();
        requestReadAndSendSmsPermission();

        GPSTracker gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            //location=gps.getLocation();
        }else{
            gps.showSettingsAlert();
        }
        new SendAll().execute();
        doTheAutoRefresh();




        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    ImageView iv = (ImageView) findViewById(R.id.imageView);
                    iv.setBackgroundResource(R.drawable.raspberryon);
                    sts= "1";

                    //Snackbar.make(buttonView, " ON", Snackbar.LENGTH_LONG).setAction("ACTION", null).show();

                } else {
                    ImageView iv = (ImageView) findViewById(R.id.imageView);
                    iv.setBackgroundResource(R.drawable.raspberryoff);
                   // Snackbar.make(buttonView, "OFF", Snackbar.LENGTH_LONG).setAction("ACTION", null).show();
                    sts = "0";
                }
                new Sending().execute();

            }
        });

        volume.add("Volume");
        volume.add("30");
        volume.add("60");
        volume.add("100");

        // Create the ArrayAdapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this
                ,android.R.layout.simple_spinner_item,volume);

        // Set the Adapter
        spinner.setAdapter(arrayAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int i, long l) {
                        vol=volume.get(i);
                if(!vol.equals("Volume")){

                    if(vol.equals("30"))
                        vol="1";
                    if(vol.equals("60"))
                        vol="2";

                    if(vol.equals("100"))
                        vol="3";

                    new SendVolume().execute();
                }

            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });



    }


    class Sending extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            //pDialog.setMessage("Loding...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
          //  pDialog.show();
        }

        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sts",sts));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_onoff,
                    "POST", params);

            // check log cat for response
            Log.d("Response for Register", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 2) {

                    sts1=2;
                }

                if (success == 1) {
                    sts1=1;
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(sts1==1) {

               // Toast.makeText(getApplicationContext(), "Inserted", Toast.LENGTH_LONG).show();
            }
             pDialog.dismiss();
        }

    }

    class SendVolume extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog1 = new ProgressDialog(MainActivity.this);
            pDialog1.setIndeterminate(false);
            pDialog1.setCancelable(true);
           }

        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("volume",vol));
            JSONObject json = jsonParser.makeHttpRequest(url_volume,
                    "POST", params);
            Log.d("Response for Register", json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    sts2=1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            if(sts2==1) {
                //Toast.makeText(getApplicationContext(), "Inserted", Toast.LENGTH_LONG).show();
            }
            pDialog1.dismiss();
        }

    }


    class SendAll extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            }

        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jsonParser.makeHttpRequest(url_all,
                    "POST", params);
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    sts2=1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            if(sts2==1) {
               // Toast.makeText(MainActivity.this,"sendall",Toast.LENGTH_SHORT).show();
           new MainActivity.Status().execute();
            }
        }

    }
    class Status extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jsonParser.makeHttpRequest(url_status,
                    "POST", params);
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    sts2=1;
                    status=json.getString("alert");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            if(sts2==1) {
//Toast.makeText(MainActivity.this,"Status=s"+status,Toast.LENGTH_SHORT).show();
                GPSTracker gps = new GPSTracker(MainActivity.this);
                if(gps.canGetLocation()){
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                }

            if(status.equals("1")){
                //Toast.makeText(MainActivity.this,"cnt=s"+cnt,Toast.LENGTH_SHORT).show();
                if(cnt==0) {
                    String adrs = getAddress(MainActivity.this, latitude, longitude);
                       SmsManager sms = SmsManager.getDefault();
                     sms.sendTextMessage(phoneNumber, null,"vasista is drowsy at "+adrs, null, null);
                    Toast.makeText(MainActivity.this, "send sms"+phoneNumber, Toast.LENGTH_SHORT).show();
                   // doTheAutoRefresh(10000);
                    cnt=1;
                }
            }else{
                cnt=0;
            }

            }


        }

    }

    private void doTheAutoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doRefreshingStuff(); // this is where you put your refresh code
                doTheAutoRefresh();
            }
        }, 1000);
    }


    public void  doRefreshingStuff(){
        // Toast.makeText(this,"k",Toast.LENGTH_LONG).show();
        new Status().execute();
    }


    public String getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add =obj.getAddressLine(0);
                    //obj.getSubLocality();// perticular area like ameerpet.
            //add = add + "\n" + obj.getCountryName();
            // add = add + "\n" + obj.getCountryCode();
            //add = add + "\n" + obj.getAdminArea();
            //add = add + "\n" + obj.getPostalCode();
            //add = add + "\n" + obj.getSubAdminArea();
            // add = add + "\n" + obj.getAddressLine(1);
            //add = add + "\n" + obj.getSubThoroughfare();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /* Request runtime SMS permission*/
    private void requestReadAndSendSmsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
    }


    public void contact(View v){

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c =  getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {


                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if ( hasPhone.equalsIgnoreCase("1"))
                    hasPhone = "true";
                else
                    hasPhone = "false" ;

                if (Boolean.parseBoolean(hasPhone))
                {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
                    while (phones.moveToNext())
                    {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }
                tv.setText("Phone : "+phoneNumber);
                Log.d("name", name + " num" + phoneNumber + " ");
            }
            c.close();
        }
    }



}
