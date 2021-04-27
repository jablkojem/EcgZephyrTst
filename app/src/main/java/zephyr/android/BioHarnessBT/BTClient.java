package zephyr.android.BioHarnessBT;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import android.bluetooth.*;


public class BTClient extends Thread {
	UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothAdapter _adapter;
	
	private BluetoothSocket _btStream;
	private String _connectionString;

	private BluetoothDevice _device;
	public BluetoothDevice getDevice() {return _device;}
	
	private BTComms _comms;
	public BTComms getComms() {return _comms;}
	
	
	private boolean _isConnected = false;	
	public boolean IsConnected() {return _isConnected;}
	
	public boolean _isValidBlueToothAddress = false;
	public void IsValidBlueToothAddress() {_isValidBlueToothAddress = _adapter.checkBluetoothAddress(_connectionString);}
	private Vector<ConnectedListener<BTClient>> eventSubscribers = new Vector<ConnectedListener<BTClient>>();
	/*This is an array of objects that implement the ConnectedListener interface. Right now the only class that
	implements it is ConnectListenerImpl class and it implements the Connected method*/
	private void OnConnected()
	{
		@SuppressWarnings("unchecked")
		//Create a new  Vector with exactly same elements as eventSubscribers
		Vector<ConnectedListener<BTClient>> handler = (Vector<ConnectedListener<BTClient>>) eventSubscribers.clone();
		Iterator<ConnectedListener<BTClient>> iter = handler.iterator();
		while(iter.hasNext())
		{
			iter.next().Connected(new ConnectedEvent<BTClient>(this));
		}
	}
	
	public void addConnectedEventListener(ConnectedListener<BTClient> listener)
	{
		eventSubscribers.add(listener);
	}
	
	public void removeConnectedEventListener(ConnectedListener<BTClient> listener)
	{
		eventSubscribers.remove(listener);
	}
	
	public BTClient(BluetoothAdapter adapter, String connectionString)
	{
		//Initialize the class variables
		_adapter = adapter; //Assign the adaptor
		// Initializing all other variables to null
		_isConnected = false;
		_comms = null;
		_btStream = null;
		_device = null;
		_connectionString = connectionString;
		IsValidBlueToothAddress();
		//Get a device for the given BT MAC address
		_device = _adapter.getRemoteDevice(_connectionString);
		try {
			//Create a Secure RFComm Bluetooth socket for secure outgoing connection with device
			//using a given "UUID--using well known UUID SPP suggested by Android??"
			_btStream = _device.createRfcommSocketToServiceRecord(MY_UUID);
			if (_btStream != null)
			{	//Attempt to connect to a remote device...Needs an exception check here to 
				//see if it fails
				_btStream.connect();
				_isConnected = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{		

			//Needs to call this to be sure enough to cancel a discovery service
			//since it is heavy weight service
			_adapter.cancelDiscovery();
			
		
		if (_isConnected == true)
			StartCommunication();
		else
			System.out.println("Can't connect to the BioHarness.");
	}
	
	private void StartCommunication()
	{
		//Create a new commmunication thread and run it
		_comms = new BTComms(_btStream);
		_comms.start();
		
		//Calls Connected method which calls ReceivedPacketMethod that prints information on the phone's text box based on the message type
		OnConnected();
	}
	
	public void Close()
	{
		if (_btStream != null)
		{
			try {
				_comms.Close();
				_btStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		_isConnected = false;
		//_btStream = null;
	}
}
