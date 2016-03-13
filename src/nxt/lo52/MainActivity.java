package nxt.lo52;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

/* @brief
 * La main activity lance la connexion/deconnexion et l'affichage de la map (differente activité)
 */
public class MainActivity extends Activity
{
	//Menu
	private Button mButtonConnexion;
	private Button mButtonDeco;
	private Button mButtonMap;
	private TextView mInfoConnectText;
	private Handler mHandler;
	BluetoothDevice mNXT_Device = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Le handler actualise le texte affiché */
		Log.e(Consts.TAG, "creation Handler");
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				Log.e(Consts.TAG, "HANDLER: Consts.infoConnectText="+Consts.infoConnectText);
				switch (msg.what) {
					default: mInfoConnectText.setText(Consts.infoConnectText); break;
				}
				super.handleMessage(msg);
			}
		};
		
		Consts.positionRobot = new Point(0,0);
		Consts.positionDepart = new Point(0,0);
		setMenu();
		
		if(Consts.mBluetoothAdapter == null) {
			// Demande d'activation bluetooth
			Consts.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			final int REQUEST_ENABLE_BT = 1;
			if (Consts.mBluetoothAdapter == null) {
				Log.e(Consts.TAG, "Device does not support Bluetooth");
				Consts.infoConnectText = "Does not support Bluetooth";
				Message msg = Message.obtain();
				mHandler.sendMessage(msg);
			} else {
				if (!Consts.mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
			}
			Consts.mBluetoothAdapter.cancelDiscovery();
		}
	}
	
	protected void setMenu()
	{
		setContentView(R.layout.menu);
		
		mInfoConnectText = (TextView)findViewById(R.id.textViewInfoBluetoothConnect);
		
		if(Consts.mBluetoothSocket == null) {
			Consts.infoConnectText = "Non connecté";
			Message msg = Message.obtain();
			mHandler.sendMessage(msg);
		} else {
			Consts.infoConnectText = "Connexion établie";
			Message msg = Message.obtain();
			mHandler.sendMessage(msg);
		}
		
		mButtonConnexion = (Button)findViewById(R.id.buttonBluetooth);
		mButtonConnexion.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) 
			{
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					Thread t = new Thread() {
						@Override
						public void run() 
						{
							Set<BluetoothDevice> pairedDevices = Consts.mBluetoothAdapter.getBondedDevices();

							if(Consts.mBluetoothSocket == null) {
								if (pairedDevices.size() > 0)
								{
									for (BluetoothDevice device : pairedDevices) {
										if(device != null && device.getName().equalsIgnoreCase("nxt")) {
											Log.e(Consts.TAG, "Adresse du NXT:" + device.getAddress());
											Consts.infoConnectText = "Adresse du NXT:" + device.getAddress();
											Message msg = Message.obtain();
											mHandler.sendMessage(msg);
											mNXT_Device = device;
										}
									}
								}

								if(mNXT_Device == null) {
									Log.e(Consts.TAG, "device NXT non trouvé!!");
									Consts.infoConnectText = "NXT non trouvé";
									Message msg = Message.obtain();
									mHandler.sendMessage(msg);
								} 
								else
								{
									try {
										Method m = mNXT_Device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
										Consts.mBluetoothSocket = (BluetoothSocket)m.invoke(mNXT_Device, Integer.valueOf(1));
										Consts.infoConnectText = "Connexion ...";
										Message msg = Message.obtain();
										mHandler.sendMessage(msg);

										Consts.mBluetoothSocket.connect();
										Consts.infoConnectText = "Connexion etablie";
										msg = Message.obtain();
										mHandler.sendMessage(msg);
									} catch (Exception e) {
										e.printStackTrace();
										Consts.infoConnectText = "Erreur "+e.getMessage();
										Message msg = Message.obtain();
										mHandler.sendMessage(msg);
										Consts.mBluetoothSocket = null;
									}
								}
							}
						}
					};
					t.start();

					return true;

				default: return false;
				}
			}
		});
		
		mButtonDeco = (Button)findViewById(R.id.buttonDeco);
		mButtonDeco.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event)
			{
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					if(Consts.mBluetoothSocket != null) {
						Thread t = new Thread() {
							@Override
							public void run() 
							{
								Consts.infoConnectText = "Déconnexion ...";
								Message msg = Message.obtain();
								mHandler.sendMessage(msg);
								try {
									Consts.mBluetoothSocket.close();
									Consts.mBluetoothSocket = null;
									Consts.infoConnectText = "Déconnecté";
									msg = Message.obtain();
									mHandler.sendMessage(msg);
									Consts.mBluetoothSocket = null;
								} catch (IOException e) {e.printStackTrace();}
							}
						};
						t.start();
						return true;
					}
					
				default: return false;
				}
			}
		});
		
		mButtonMap = (Button)findViewById(R.id.buttonMap);
		mButtonMap.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) 
			{
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					Intent myIntentPref = new Intent(MainActivity.this, FirstChoiceActivity.class);
				    MainActivity.this.startActivity(myIntentPref);
					return true;
				
				default: return false;
				}
			}
		});
	}
}
