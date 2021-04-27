package zephyr.android.BioHarnessBT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//import android.R.*;
import android.app.Activity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {
	protected static final ConnectedListener<BTClient> NULL = null;
	private final int GEN_PACKET = 1200;
	private final int ECG_PACKET = 1202;
	private final int BREATH_PACKET = 1204;
	private final int R_to_R_PACKET = 1206;
	private final int ACCELEROMETER_PACKET = 1208;
	private final int SERIAL_NUM_PACKET = 1210;
	private final int SUMMARY_DATA_PACKET =1212;
	private final int EVENT_DATA_PACKET =1214;
	public byte[] DataBytes;
	
	private int TextSize = 14;
	BluetoothAdapter adapter = null;
	BTClient _bt;
	ZephyrProtocol _protocol;
	ConnectedListener<BTClient> _listener;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);
        //Obtaining the handle to modify the text size of text coming from the GP information
        TextView tv = (TextView) findViewById(R.id.genText);
        //Setting the text size for GP data
        tv.setTextSize(TextSize);
      //Obtaining the handle to modify the text size of text coming from the ECG information
        tv = (TextView) findViewById(R.id.ecgText);
      //Setting the text size for ECG data
        tv.setTextSize(TextSize);
        
      //Obtaining the handle to modify the text size of text coming from the Breathing Packet information
      //Setting the text size for Breathing data
        tv = (TextView) findViewById(R.id.breathText);
        tv.setTextSize(TextSize);
        
        //Obtaining the handle to modify the text size of text coming from the Breathing Packet information
        //Setting the text size for Breathing data
          tv = (TextView) findViewById(R.id.Accelerometertext);
          tv.setTextSize(TextSize);
        
        //Obtaining the handle to modify the text size of text coming from the Breathing Packet information
        //Setting the text size for R to R data
          tv = (TextView) findViewById(R.id.RtoRText);
          tv.setTextSize(TextSize);
      //Obtaining the handle to act on the CONNECT button
        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null)
        	btnConnect.setOnClickListener(new OnClickListener() {
				@Override
				//Functionality to act if the button CONNECT is touched
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*Obtain a handle to the default bluetooth adaptor for communication*/
					adapter = BluetoothAdapter.getDefaultAdapter();
					/*Obtaining the Bluetooth MAC information from the screen of the phone*/
					EditText mac = (EditText) findViewById(R.id.EditTextMAC);
					/*Create a new thread called to manage BT communications
					/called BTClient using the information entered in the textbox */
					_bt = new BTClient(adapter, mac.getText().toString());
					/*Instantiates a new object implementing the connected Listener interface and
					/passes it the handler object*/
					_listener = new ConnectListenerImpl(handler,DataBytes);
				
					/*Adds the ConnectListenerImpl to the BTClients eventsubscriber list to respond to OnConnected() method*/
					_bt.addConnectedEventListener(_listener);
					/*Kick off the thread to launch its activity*/
					if(_bt.IsConnected())
					{
						_bt.start();
					}
					else
					{
						 TextView tv = (TextView) findViewById(R.id.genText);
						 String ErrorText  = "Unable to connect to BioHarness !";
						 tv.setText(ErrorText);
					}
				}
        	});
      /*Obtaining the handle to act on the DISCONNECT button*/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null)
        	btnDisconnect.setOnClickListener(new OnClickListener() {
				@Override
				/*Functionality to act if the button DISCONNECT is touched*/
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*Reset the global variables*/
					
					/*This disconnects listener from acting on received messages*/	
					_bt.removeConnectedEventListener(_listener);
					/*Close the communication with the device & throw an exception if failure*/
					_bt.Close();
				
				}
        	});
    }
    private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BOnd state", "BOND_STATED = " + device.getBondState());
		}
    }
    
    private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			try {
				BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
				byte[] pin = (byte[])m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    final Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		TextView tv;
    		switch (msg.what)
    		{
    		case GEN_PACKET:
    		String genText = msg.getData().getString("genText");
    		tv = (TextView)findViewById(R.id.genText);
    		if (tv != null) tv.setText(genText);
    		break;
    		case ECG_PACKET:
    		String ecgText = msg.getData().getString("ecgText");
    		tv = (TextView)findViewById(R.id.ecgText);
    		if (tv != null) tv.setText(ecgText);
    		break;
    		case BREATH_PACKET:
        		String breathText = msg.getData().getString("breathText");
        		tv = (TextView)findViewById(R.id.breathText);
        		if (tv != null) tv.setText(breathText);
        		break;
    		case R_to_R_PACKET:
        		String RtoRText = msg.getData().getString("RtoRText");
        		tv = (TextView)findViewById(R.id.RtoRText);
        		if (tv != null) tv.setText(RtoRText);
        		break;
    		case ACCELEROMETER_PACKET:
        		String AccelerometerText = msg.getData().getString("Accelerometertext");
        		tv = (TextView)findViewById(R.id.Accelerometertext);
        		if (tv != null) tv.setText(AccelerometerText);
    			break;
    		case SERIAL_NUM_PACKET:
    			String SerialNumtext = msg.getData().getString("SerialNumtxt");
    			tv = (EditText)findViewById(R.id.labelSerialNumber);
    			if (tv != null)tv.setText(SerialNumtext);
    			break;
    		case SUMMARY_DATA_PACKET:
        		String SummaryText = msg.getData().getString("SummaryDataText");
        		tv = (TextView)findViewById(R.id.SummaryDataText);
        		if (tv != null) tv.setText(SummaryText);
    			break;
    		case EVENT_DATA_PACKET:
        		String EventText = msg.getData().getString("EventDataText");
        		tv = (TextView)findViewById(R.id.EventDataText);
        		if (tv != null) tv.setText(EventText);
    			break;
    		}
    	}
 };
}    

