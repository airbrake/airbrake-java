package airbrake.stacktrace;

public interface BacktraceLine {
	//Returns self for easy calling of the toXml/toString methods
	public BacktraceLine acceptLine(String line);
	public String toString();
	public String toXml();
}
