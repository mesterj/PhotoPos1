package hu.kite.szamtech.proba.photopos1;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

	private LocationManager lm;
	private final String PICTURE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();

	private final int PICTURE_REQUEST = 109;
	TextView tv;
	LocationListener listener;
	Location locCli;
	String gpsContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		listener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				tv.setText("Próbálom meghatározni a pontos helyzetet");

			}

			@Override
			public void onProviderDisabled(String provider) {
				tv.setText("A GPS le van tíltva, engedélyezd !");
			}

			@Override
			public void onLocationChanged(Location location) {
				Long ido = location.getTime();
				String dateString = DateFormat.format("yyyy-MM-dd hh:mm:ss",
						new Date(ido)).toString();
				String koordinates = "";

				if (location.getAccuracy() < 5) {

					koordinates = " Szélesség: (Lat) " + location.getLatitude()
							+ " Hosszúság : (Long) " + location.getLongitude()
							+ " Pontosság :  " + location.getAccuracy();

				}

				if (!koordinates.equals(""))
					tv.setText(dateString + koordinates);

			}
		};
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		tv = (TextView) findViewById(R.id.textView1);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.button1) {
			File pictureFile = new File(PICTURE_PATH + "/tmp.jpg");

			Uri uri = Uri.fromFile(pictureFile);

			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(takePictureIntent, PICTURE_REQUEST);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == PICTURE_REQUEST) {
			/*
			 * ImageView ivPhoto = (ImageView) findViewById(R.id.imageView1);
			 * try { File f = new File(PICTURE_PATH + "/tmp.jpg");
			 * FileInputStream fis = new FileInputStream(f); Bitmap img =
			 * BitmapFactory.decodeStream(fis); ivPhoto.setImageBitmap(img); }
			 * catch (Exception ex) { Toast.makeText(this, "Error van ! " +
			 * ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			 * 
			 * }
			 */
			// kepkirak();
			String idoString = "Jelenleg nincs el�rhet� GPS id�!";

			if (hasGoodCoordinates()) {
				locCli = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Long idoKesz = locCli.getTime();
				idoString = DateFormat.format("yyyy-MM-dd HH:mm:ss",
						new Date(idoKesz)).toString();

				// tv.setText("A k�p koordin�t�i: sz�lesss�g: " +
				// locCli.getLatitude() + " hossz�s�g: " + locCli.getLatitude()+
				// " k�sz�t�s ideje: " + idoString );
				tv.setText("GPS idö: " + idoString);
				gpsContent = "GPS idö : " + idoString + " Szélesség: "
						+ locCli.getLatitude() + " hosszúság: "
						+ locCli.getLongitude()+ " idö : "
                        + idoString    ;
				EditText etEmail = (EditText) findViewById(R.id.eTEmail);
				String mailCim = etEmail.getText().toString();
				String[] adatok = { etEmail.getText().toString() };
				try {
					File pdfFile = PdfMaker(gpsContent, PICTURE_PATH + "/tmp.jpg",
							adatok);
					String varos = getLocationName(locCli.getLatitude(), locCli.getLongitude());
					sendEmail(mailCim, pdfFile, idoString
							+ " szélesség : " + locCli.getLatitude()
							+ " hosszúság : " + locCli.getLongitude()
							+ " városban  : " + varos);
				} catch (Exception ex) {
					ex.getLocalizedMessage();
				}
			}
			else {
				gpsContent="Sajnos nem sikerült meghatározni a pontos helyet.";
				Toast.makeText(this, "Sajnos nem sikerült meghatározni a helyet", Toast.LENGTH_LONG).show();
			

			EditText etEmail = (EditText) findViewById(R.id.eTEmail);
			String mailCim = etEmail.getText().toString();
			String[] adatok = { etEmail.getText().toString() };
			try {
				File pdfFile = PdfMaker(gpsContent, PICTURE_PATH + "/tmp.jpg",
						adatok);
				sendEmail(mailCim, pdfFile,gpsContent);
			} catch (Exception ex) {
				ex.getLocalizedMessage();
			}
			
			}
		}
	}

	/*
	 * public void kepkirak() { ImageView ivPhoto = (ImageView)
	 * findViewById(R.id.imageView1); try { File f = new File(PICTURE_PATH +
	 * "/tmp.jpg"); FileInputStream fis = new FileInputStream(f); Bitmap img =
	 * BitmapFactory.decodeStream(fis); ivPhoto.setImageBitmap(img); } catch
	 * (Exception ex) { Toast.makeText(this, "Error van ! " +
	 * ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
	 * 
	 * } }
	 */

	public boolean hasGoodCoordinates() {
		try {
		locCli = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            /*Akkor van pontos helyszin ha a pontosság 15 méternél kisebb és a gps idő és az
            eszköz ideje nem tér el jelentősen 5 percen belül van.
             */
            Calendar gpsTime = Calendar.getInstance();
            gpsTime.setTimeInMillis(locCli.getTime());
            Calendar deviceTime = Calendar.getInstance();
            deviceTime.setTimeInMillis(System.currentTimeMillis());


		if (locCli.getAccuracy() < 15 && idohatar(gpsTime,deviceTime) )
			return true;
		else
			return false;
		}
		catch (Exception ex) {
		Log.e("PHOTOPOS", "Nincs GPS jel");
		return false;
	}
	}

    private boolean idohatar(Calendar gpsTime, Calendar deviceTime) {
        if (gpsTime.getTimeInMillis()-120000 <= deviceTime.getTimeInMillis() &&
                deviceTime.getTimeInMillis() <= gpsTime.getTimeInMillis()+120000) {
            return true;
        }
        else
            return false;

    }

    public void sendEmail(String to, File f, String content) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");// text/plain
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
		emailIntent.putExtra(Intent.EXTRA_TEXT, content);
		Uri uri = Uri.parse("file://" + f);
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		try {
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Nincs email kliens telepítve.",
					Toast.LENGTH_SHORT).show();
		}

	}

	public File PdfMaker(String gpsContent, String picfilePath,
			String[] formData) throws DocumentException, IOException {

		Document document = new Document();
		File filename = new File(PICTURE_PATH + "/pdf.pdf");
		PdfWriter.getInstance(document, new FileOutputStream(filename));

		document.open();

		Paragraph formBekezdes = new Paragraph();
		formBekezdes.add("\n");

		formBekezdes.add(new Paragraph("A form adatai: "));
		for (int i = 0; i < formData.length; i++) {
			formBekezdes.add(new Paragraph(formData[i]));
		}
		document.add(formBekezdes);

		document.add(new Paragraph(" GPS koordináták: " + gpsContent));
		Image img = Image.getInstance(picfilePath);
		img.scaleToFit(240f, 320f);
		document.add(img);

		document.close();
		return filename;
	}
	
	public String getLocationName(double lattitude, double longitude) {

	    String cityName = "Nincs találat";
	    if (!Geocoder.isPresent()) {
	    	Toast.makeText(this, "Nincs GeoCoder ", Toast.LENGTH_LONG).show();
	    return "Nincs GeoCoder ezért nem tudom a helyet megállapítani"; }
	    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());	    
	    try {

	        List<Address> addresses = gcd.getFromLocation(lattitude, longitude,
	                10);

	        for (Address adrs : addresses) {
	            if (adrs != null) {

	                String city = adrs.getLocality();
	                if (city != null && !city.equals("")) {
	                    cityName = city;
	                    System.out.println("város ::  " + cityName);
	                } else {

	                }
	                // // you should also try with addresses.get(0).toSring();

	            }

	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return cityName;

	}

    @Override
    protected void onStop() {
        super.onStop();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeUpdates(listener);

    }
}
