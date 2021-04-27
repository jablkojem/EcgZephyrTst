package zephyr.android.BioHarnessBT;

public class ZephyrPacketArgs {
		
	private byte[] _bytes;
	public byte[] getBytes() { return _bytes;}

	private int _msgID;
	public int getMsgID() {return _msgID;}
	
	private byte _status;
	public byte getStatus() {return _status;}
	
	private byte _NumRcvdBytes;
	public byte getNumRvcdBytes(){return _NumRcvdBytes;}
	
	private byte _CrcStatus;
	public byte getCRCStatus(){return _CrcStatus;}
	//public ZephyrPacketArgs(int msgID, byte[] data, byte status)
	public ZephyrPacketArgs(int msgID, byte[] data, byte status, byte NumRcvdBytes, byte CrcFailStatus)
	{
		_msgID = msgID;
		_bytes = data;
		_status = status;	
		_NumRcvdBytes= NumRcvdBytes;
		_CrcStatus = CrcFailStatus;
		
	}
}
