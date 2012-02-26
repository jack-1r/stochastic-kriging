package se.sics.stochastic.executors;

import java.net.Socket;
import java.util.Set;

public class NetExecutorInstance {
//	private InetAddress ip;
//	private int port;
	private Socket clientSocket;
	private int nExp;
	private int nCores;
	
	public int getnCores() {
		return nCores;
	}

	Set<Integer> expSet;

	public NetExecutorInstance(Socket clientSocket, int nCores) {
		this.nCores = nCores;
		this.clientSocket = clientSocket;
	}

//	public InetAddress getAddress() {
//		return ip;
//	}
//
//	public int getPort() {
//		return port;
//	}

	public Socket getClientSK() {
		return clientSocket;
	}
	
	public int getExpCount() {
		return nExp;
	}
	
	public void decreaseCount() {
		--nExp;
	}
	
	public void increaseCount() {
		++nExp;
	}
	
	public Set<Integer> getExpSet() {
		return expSet;
	}
	
}
