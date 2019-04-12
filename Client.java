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
		gsc.waitForTestFile();
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

	public void waitForTestFile () {
		try {
			System.out.println("Waiting for testFile command");
			System.out.println(dis.readUTF());
			getTestFile();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public void getTestFile() {
		try {
			System.out.println("Starting of downloading the File");
			File f = new File("D:/github/shooter/files/f.txt");
			byte [] mybytearray  = new byte [100];
			InputStream is = s.getInputStream();
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int bytesRead = is.read(mybytearray,0,mybytearray.length);
			int current = 0;

			current = bytesRead;

      do {
         bytesRead =
            is.read(mybytearray, current, (mybytearray.length-current));
         if(bytesRead >= 0) current += bytesRead;
      } while(bytesRead > -1);

      bos.write(mybytearray, 0 , current);
      bos.flush();
			System.out.println("Downloading of file is done");

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
