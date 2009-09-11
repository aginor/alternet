/*
 * This is an alternative server/client library for Processing. 
 * Copyright (C)2009 Andreas Löf 
 * Email: andreas@alternating.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package net.alternating.alternet;

/**
 * This is a simple class that is used to contain a hostname/ip-address and a
 * port. It is used to distinguish between clients and servers in the Server and
 * Client classes.
 * 
 * @author Andreas L&ouml;f
 * @see Server
 * @see Client
 */
public class RemoteAddress implements Comparable {
    
    private String ip;
    private int port;
    
    /**
     * Constructs a new remote address.
     * 
     * @param ip
     *            the IP address or the hostname
     * @param port
     *            the port
     */
    public RemoteAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
   
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteAddress other = (RemoteAddress) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}



	/**
     * Returns the address.
     * 
     * @return the address
     */
    public String getIp() {
        return ip;
    }
    
    /**
     * Returns the port.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(Object arg0) {
        RemoteAddress other = (RemoteAddress) arg0;
        
        if (ip.equals(other.getIp())) {
            return port - other.getPort();
        } else {
            return ip.compareTo(other.getIp());
        }
        
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return ip + ":" + port;
    }
    
}
