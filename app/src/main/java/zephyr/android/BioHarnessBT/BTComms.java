package zephyr.android.BioHarnessBT;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import android.bluetooth.BluetoothSocket;

// A class creating a thread and provides a handle to this in BTClient
public class BTComms extends Thread {
	private BluetoothSocket _btStream;
	
	private OutputStream _ostream;
	private InputStream _istream;
	
	public boolean canWrite() {return _ostream != null;};
	public boolean canRead() {return _istream != null;};
	//eventSubscribers is an array of objects that implement ReceivedListner interface
	private Vector<ReceivedListener> eventSubscribers = new Vector<ReceivedListener>();
	
	private void OnReceived(byte[] bytes)
	{
		//Creates a copy of eventsubscribers which implement the Received Listener Interface, i.e. implement the Received method
		Vector<ReceivedListener> handler = (Vector<ReceivedListener>) eventSubscribers.clone();
		Iterator<ReceivedListener> iter = handler.iterator();
		while(iter.hasNext())
		{//Looks through the list of objects that implement Received interface and processes the received data in "bytes"
			iter.next().Received(new ReceivedEvent(this, bytes));
		}
	}
	
	public void addReceivedEventListener(ReceivedListener listener)
	{
		eventSubscribers.add(listener);
	}
	
	public void removeReceivedEventListener(ReceivedListener listener)
	{
		eventSubscribers.remove(listener);
	}
	
	
	private LinkedBlockingQueue<byte[]> _queue = new LinkedBlockingQueue<byte[]>();
	//This function processes the incoming data  and runs forever
	private void CallingReceivers()
	{
		try 
		{	
			while (true)
			{
				OnReceived(_queue.take());
			}
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Constructor for BT comms thread. This creates a new thread. This thread inititally sleeps for sometime and then calls 
	//the CallingReceivers() function inside of which is a loop that runs forever
	public BTComms(BluetoothSocket btStream)
	{
		_btStream = btStream;
		try
		{
			_ostream = _btStream.getOutputStream();
			_istream = _btStream.getInputStream();
			//I think this creates a new thread to process the received data  
			(new Thread() {public void run() {try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			CallingReceivers();}}).start();
		}
		catch (IOException e)
		{
			System.out.println("Can't create input/output streams.");
		}
	}
	
	@Override
	//This is the comms thread whose job is to keep reading forever from the BTRFCOMM socket and push it into the data queue
	public void run()
	{
		byte[] buffer = new byte[512];
		
		if (_istream != null)
			
		{
			//Runs for ever. This thread tries to read BioHarness data and place it in the buffer.
			//This thread runs forever
			while (true)
			{
				try 
				{
					int nbRead = 0;
					//Reads bytes from this stream into buffer. nbReads holds 
					//number of bytes read
					if (_istream != null)
						nbRead = _istream.read(buffer);
					else
						break;
					if (nbRead > 0)
					{
						try
						{	//Stores the nbRead bytes of data read into "data" 
							byte[] data = new byte[nbRead];
							System.arraycopy(buffer, 0, data, 0, nbRead);
							_queue.put(data);
						}
						catch (InterruptedException ex)
						{}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
					break;
				}
			}
		}
	}
	
	public void write(byte[] bytes)
	{
		if (_ostream != null)
		{
			try {
				_ostream.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void Close()
	{
		if (_istream != null)
		{
			try {
				_istream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_istream = null;
		}
		if (_ostream != null)
		{
			try {
				_ostream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_ostream = null;
		}
	}
}
