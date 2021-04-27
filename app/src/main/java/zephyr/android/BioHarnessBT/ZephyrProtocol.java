package zephyr.android.BioHarnessBT;


import java.util.*;






public class ZephyrProtocol implements ReceivedListener {
	private BTComms _comms;
	private ZephyrPacket _packet;
	private int BREATHING_PACKET_ACK = 0x15;
	private int RtoR_PACKET_ACK = 0x19;
	private int ACCELEROMETER_PACKET_ACK = 0xBC;
	private int SUMMARY_DATA_PACKET_ACK = 0xBD; 
	private int LOGGING_DATA_PACKET_ACK = 0x4B;

	private PacketTypeRequest _ZephyrRequestedPacketTypes = new PacketTypeRequest();
	//This is a array of objects that implement the ZephyrPacketListener interface. 
	//There are 2 classes that implement ZephyrPacketListener.
	//1. SetGeneralPacketListener
	//2. SetECGListener
	
	private Vector<ZephyrPacketListener> eventSubscribers = new Vector<ZephyrPacketListener>();
	
	private void OnZephyrPacket(ZephyrPacketArgs packet)
	{
		Vector<ZephyrPacketListener> handler = (Vector<ZephyrPacketListener>) eventSubscribers.clone();
		Iterator<ZephyrPacketListener> iter = handler.iterator();
		while(iter.hasNext())
		{
			//This loop looks to process all objects that implement the Received Packet interface and 
			iter.next().ReceivedPacket(new ZephyrPacketEvent(this, packet));
		}
	}
	
	public void addZephyrPacketEventListener(ZephyrPacketListener listener)
	{
		eventSubscribers.add(listener);
	}
	
	public void removeZephyrPacketEventListener(ZephyrPacketListener listener)
	{
		eventSubscribers.remove(listener);
	}
//Constructor for ZephyrProtocol. This instantiates the BH to send GP and ECG Packets
	public ZephyrProtocol(BTComms comms, PacketTypeRequest ReqPcktType )
	{
		_packet = new ZephyrPacket();
		 
		_comms = comms;
		//Ties the ZephyrProtocol object's Received Method to respond to the ReceivedPacket event
		_comms.addReceivedEventListener(this);

	
		_ZephyrRequestedPacketTypes = ReqPcktType;
		new Thread() {public void run() { try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} GetSerialNumber(true);
		SetGeneralPacket(_ZephyrRequestedPacketTypes.GP_ENABLE); 
		
		SetECGPacket(_ZephyrRequestedPacketTypes.ECG_ENABLE);
		SetBreathingPacket(_ZephyrRequestedPacketTypes.BREATHING_ENABLE);
		SetRtoRPacket(_ZephyrRequestedPacketTypes.RtoR_ENABLE);
		SetAccelerometerPacket(_ZephyrRequestedPacketTypes.ACCELEROMETER_ENABLE); 
		SetSummaryDataPacket(_ZephyrRequestedPacketTypes.SUMMARY_ENABLE);
		SetEventPacket(_ZephyrRequestedPacketTypes.EVENT_ENABLE);
		SetLoggingDataPacket(_ZephyrRequestedPacketTypes.LOGGING_ENABLE);
		SendLifeSign();}}.start();
	}
	
	public void SendLifeSign()
	{
		if (_comms.canWrite())
		{
			System.out.println("Sending life sign packet.");
			_comms.write(_packet.getLifeSignMessage());
		}
		
		if (_comms.canWrite())
		{
			new Thread() {public void run() {try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} SendLifeSign();}}.start();
		}
	}
	
	public boolean SetGeneralPacket(boolean activate)
	{
		 class SetGeneralPacketListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (eventArgs.getPacket().getMsgID() == 0x14)
						//Set General Packet transmit state
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			}
		//Creates and Adds GeneralPacketListener object to the list of event subscribers to respond to the received General Packet Listener event	
		 SetGeneralPacketListener task = new SetGeneralPacketListener();
		 addZephyrPacketEventListener(task);
		 // Here we enable the BioHarness to receive a General Packet
		if (_comms.canWrite())
			_comms.write(_packet.getSetGeneralPacketMessage(activate));
		
		try {
			synchronized(task){
			task.wait(1600);}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Remove listening for GP acknowlegement receipt
		removeZephyrPacketEventListener(task);
		System.out.println(String.format("Set General Packet status %s", task.ack));
		
		return task.ack;
	}

	public boolean SetECGPacket(boolean activate)
	{
		 class SetECGListener implements ZephyrPacketListener{
			private boolean ack = false;
			
			@Override
			public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
				// TODO Auto-generated method stub
				if (eventArgs.getPacket().getMsgID() == 0x16)
				{
					if(eventArgs.getPacket().getStatus() == 0x06)
						ack = true;
					synchronized(this){
					this.notify();}
				}	
			}
		 }
		
		 SetECGListener task = new SetECGListener();
		 
		addZephyrPacketEventListener(task);
		if (_comms.canWrite())
			_comms.write(_packet.getSetECGPacketMessage(activate));
		
		try {
			synchronized(task){
			task.wait(800);}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Remove Listening for ECG Ack Packet
		removeZephyrPacketEventListener(task);
		System.out.println(String.format("Set ECG Packet status %s", task.ack));
		
		return task.ack;
	}
	public boolean SetBreathingPacket(boolean activate)
	{
		 class SetBreathingPacketListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (BREATHING_PACKET_ACK == eventArgs.getPacket().getMsgID())
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			 }
		 SetBreathingPacketListener task = new SetBreathingPacketListener();
		 addZephyrPacketEventListener(task);
			if (_comms.canWrite())
				_comms.write(_packet.getSetBreathingPacketMessage(activate));
			
			try {
				synchronized(task){
				task.wait(1600);}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Remove Listening for ECG Ack Packet
			removeZephyrPacketEventListener(task);
			System.out.println(String.format("Set Breathing Packet status %s", task.ack));
			
			return task.ack;
		 
	}
	
	public boolean SetRtoRPacket(boolean activate)
	{
		 class SetRtoRListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (RtoR_PACKET_ACK==eventArgs.getPacket().getMsgID())
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			 }
		 SetRtoRListener task = new SetRtoRListener();
		 addZephyrPacketEventListener(task);
			if (_comms.canWrite())
				_comms.write(_packet.getSetRtoRPacketMessage(activate));
			
			try {
				synchronized(task){
				task.wait(1600);}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Remove Listening for ECG Ack Packet
			removeZephyrPacketEventListener(task);
			System.out.println(String.format("Set R to R Packet status %s", task.ack));
			
			return task.ack;
	}
	
	public boolean SetAccelerometerPacket(boolean activate)
	{
		 class SetAccelerometerListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (ACCELEROMETER_PACKET_ACK==eventArgs.getPacket().getMsgID())
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			 }
		 SetAccelerometerListener task = new SetAccelerometerListener();
		 addZephyrPacketEventListener(task);
			if (_comms.canWrite())
				_comms.write(_packet.getSetAccelerometerPacketMessage(activate));
			
			try {
				synchronized(task){
				task.wait(1600);}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Remove Listening for ECG Ack Packet
			removeZephyrPacketEventListener(task);
			System.out.println(String.format("Set Accelerometer Packet status %s", task.ack));
			
			return task.ack;
	}
	
	public  void GetSerialNumber(boolean activate)
	{
		if (_comms.canWrite())
			_comms.write(_packet.getSetSerialNumberMessage(activate));
	}
	public boolean SetSummaryDataPacket(boolean activate)
	{
		 class SetSummaryDataPacketListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (SUMMARY_DATA_PACKET_ACK==eventArgs.getPacket().getMsgID())
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			 }
		 SetSummaryDataPacketListener task = new SetSummaryDataPacketListener();
		 addZephyrPacketEventListener(task);
			if (_comms.canWrite())
				_comms.write(_packet.getSetSummaryPacketMessage(activate));
			
			try {
				synchronized(task){
				task.wait(1600);}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Remove Listening for Summary Ack Packet
			removeZephyrPacketEventListener(task);
			System.out.println(String.format("Set Summary Data Packet status %s", task.ack));
			
			return task.ack; 
	}
	public boolean SetLoggingDataPacket(boolean activate)
	{
		 class SetLoggingPacketListener implements ZephyrPacketListener{
				private boolean ack = false;
				
				@Override
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					// TODO Auto-generated method stub
					if (LOGGING_DATA_PACKET_ACK==eventArgs.getPacket().getMsgID())
					{
						if(eventArgs.getPacket().getStatus() == 0x06)
							ack = true;
						synchronized(this){
						this.notify();}
					}	
				}
			 }
		 SetLoggingPacketListener task = new SetLoggingPacketListener();
		 addZephyrPacketEventListener(task);
			if (_comms.canWrite())
				_comms.write(_packet.getSetLoggingDataMessage(activate));
			
			try {
				synchronized(task){
				task.wait(1600);}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Remove Listening for Summary Ack Packet
			removeZephyrPacketEventListener(task);
			System.out.println(String.format("Set Logging Data Packet status %s", task.ack));
			
			return task.ack; 
	}
	public  void SetEventPacket(boolean activate)
	{
		if (_comms.canWrite())
			_comms.write(_packet.getSetEventDataPacketMessage(activate));
	}
	
	@Override
	public void Received(ReceivedEvent eventArgs) {
			Vector<byte[]> packets =_packet.Serialize(eventArgs.getBytes());
			
			Iterator<byte[]> iter = packets.iterator();
			//This function parses the byte by byte until there are no more bytes to be processed 
			while(iter.hasNext())
			{
				ZephyrPacketArgs msg = null;
				try {
					//This function parses the byte by byte looking for a pattern of
					//a STX, Valid CRC, inconsistency in packet length, and ETX. If all the criteria are met, the 
					//function returns an object of type ZephyrPacketArgs
					msg = _packet.Parse(iter.next());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (msg != null)
				{
					OnZephyrPacket(msg);
				}
			}
	}
}
