package zephyr.android.BioHarnessBT;

public class PacketTypeRequest {
	
	public boolean GP_ENABLE;
	public void EnableGP(boolean input){GP_ENABLE= input;}
	
	public boolean ECG_ENABLE;
	public void EnableECG(boolean input){ECG_ENABLE= input;}
	
	public boolean BREATHING_ENABLE;
	public void EnableBreathing(boolean input){BREATHING_ENABLE= input;}
	
	public boolean RtoR_ENABLE;
	public void EnableTtoR(boolean input){RtoR_ENABLE= input;}
	
	public boolean ACCELEROMETER_ENABLE;
	public void EnableAccelerometry(boolean input){ACCELEROMETER_ENABLE= input;}
	
	public boolean SUMMARY_ENABLE;
	public void EnableSummary(boolean input){SUMMARY_ENABLE= input;}
	
	public boolean EVENT_ENABLE;
	public void EnableEvent(boolean input){EVENT_ENABLE= input;}
	
	public boolean LOGGING_ENABLE;
	public void EnableLogging(boolean input){LOGGING_ENABLE= input;}
	
	public PacketTypeRequest()
	{
		GP_ENABLE= false;
		ECG_ENABLE= false;
		BREATHING_ENABLE= false;
		RtoR_ENABLE= false;
		ACCELEROMETER_ENABLE= false;
		SUMMARY_ENABLE= false;
		EVENT_ENABLE= false;
		LOGGING_ENABLE = false;
	}

}
