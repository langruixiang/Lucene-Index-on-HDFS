package BalanceServer;

import java.io.IOException;

public class BalanceServerMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BalanceServer balanceServer = new BalanceServer("/home/lang/workspace/BigWeb/config.xml");
		BalanceServerThread balanceThread = new BalanceServerThread(balanceServer);
		balanceThread.start();		
	}

}
