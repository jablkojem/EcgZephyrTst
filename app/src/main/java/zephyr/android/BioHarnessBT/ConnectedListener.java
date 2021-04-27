package zephyr.android.BioHarnessBT;

public interface ConnectedListener<T> {
	public void Connected(ConnectedEvent<T> eventArgs);
}
