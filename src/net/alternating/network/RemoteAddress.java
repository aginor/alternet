package net.alternating.network;

public class RemoteAddress implements Comparable{

	private String ip;
	private int port;
	
	public RemoteAddress(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public boolean equals(Object o){
		RemoteAddress other = (RemoteAddress) o;
		
		return ip.equals(other.getIp()) && port == other.getPort();
	}
	
	public int hashcode() {
		return ip.hashCode()+port;
	}
	
	
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public int compareTo(Object arg0) {
		RemoteAddress other = (RemoteAddress) arg0;
		
		if(ip.equals(other.getIp())){
			return port - other.getPort();
		} else {
			return ip.compareTo(other.getIp());
		}
		
	}
	
	
	
	
	
}
