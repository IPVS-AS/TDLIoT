package connector;

public abstract class AbstractClient {

	private boolean isAvailable = false;
	
	public boolean isAvailable() {
		return isAvailable;
	};
	
	public abstract void close();
}
