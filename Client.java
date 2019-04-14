import java.util.*;
import java.io.*;
import java.net.*;



class Client {
	public static void main (String [] args) throws Exception {
		String answ = new WaitServerConnecter("localhost",6900).getServerList("Valera");
		System.out.println(answ);
		String[] servers = answ.split(":");
		for (int i = 0; i<servers.length;i++) {
			servers[i] = servers[i].split("/")[0];
		}
		System.out.println(servers[1]);
		GameServerConnection gsc = new GameServerConnection("localhost",Integer.parseInt(servers[1]));
		gsc.enterServer("Valera");
		gsc.downloadMap();
		gsc.getTestString();
	}
}

class WaitServerConnecter {
	DataInputStream dis;
	DataOutputStream dos;
  Scanner sc;
	Socket s;
	public WaitServerConnecter(String ip, int port) {
		try {
		sc = new Scanner(System.in);
		s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
	  }
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public String getServerList (String name) {
		String req = name;
		String res = "";
	  try {
			dos.writeUTF(req);
			res = dis.readUTF();
			closeSocket();
			return res;
		}
		catch (Exception e) {
			System.out.println(e);
			return "false";
		}
	}
	public void closeSocket () {
		try {
		sc.close();
		s.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}

class GameServerConnection {
	DataInputStream dis;
	DataOutputStream dos;
	Socket s;
  String map;

	public synchronized void sendString (String s) {
		try {
			dos.writeUTF(s);
		}
		catch (Exception e){
			System.out.println(e);
		}
	}
	public synchronized String readString () {
		try {
			return(dis.readUTF());
		}
		catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public void getTestString() {
		try {
			String s = readString();
			sendString(s);
			System.out.println("Test String + '" + s + "'");
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public GameServerConnection (String ip, int port) {
		try {
		Scanner sc = new Scanner(System.in);
		Socket s = new Socket (ip,port);
		this.s = s;
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public void enterServer (String username) {
		try {
		dos.writeUTF(username);
		String answ = dis.readUTF();
		System.out.println(answ);
		//дальнейшие действия
		}
	  catch (Exception e) {
	  	System.out.println(e);
	  }
	}

	public void downloadMap () {
		try {
			System.out.println("Downloading the Map");
			//
			int length = Integer.parseInt(dis.readUTF());
			System.out.println(length);
			String[] map = new String[length];
			for (int i = 0;i<length; i++) {
				map[i] = dis.readUTF();
				//System.out.println(map[i]);
			}
			//
			System.out.println("Downloading of the Map is done");
		}
		catch (Exception e) {
			System.out.println("Downloading of the Map failed");
			System.out.println(e);
		}

	}
}
