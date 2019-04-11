import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


class Server {
	public static void main(String[]args){
		new MainServer();
	}
}

class VirtualServer extends Thread{
	private ServerSocket ss;
	ArrayList <GameClientThread> clients;
	int port;
	int id;
	String[] map;
	public VirtualServer (int id) {
		this.id = id;
		map = readMap();
		clients = new ArrayList<GameClientThread>();
		//количество игроков на сервере
		try {
			port = 6900 + id + 1;
			ss = new ServerSocket(port);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public String[] readMap () {
		try {
		//File file = new File("D:/github/shooter/maps/map1.txt");
		File file = new File("/home/10a/polyakov_om/github/shooter/maps/map1.txt");

		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		String[] map = str.split(":");
		return map;
		}
	  catch (Exception e) {
	  	System.out.println(e);
			return null;
	  }
	}
	public void run () {
		System.out.println(port);
		try {
		while (clients.size() < 2)
		 clients.add(new GameClientThread (ss.accept(), map));
		 new Game(clients).beforeGame();
	 }
	 catch (Exception e) {
	 	System.out.println(e);
	 }
	}
}

class GameClientThread extends Thread {
	DataInputStream dis;
	DataOutputStream dos;
	String clientNickname;
	String[] map;
	public GameClientThread (Socket clientSocket, String[] m) {
		try {
			dis = new DataInputStream(clientSocket.getInputStream());
			dos = new DataOutputStream(clientSocket.getOutputStream());
			this.map = m;
		}
		catch (Exception e) {
			System.out.println(e);
		}
		start();
	}
	public void run () {
		System.out.println("GameClientThread started");
		clientNickname = readClientNickname();
		sendMap();
	}
	public String readClientNickname () {
		try {
			String name = dis.readUTF();
			System.out.println("Nickname " + name);
			dos.writeUTF("true");
			return name;
		}
		catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	public void sendMap () {
		try {
			System.out.println("Sending the Map");
			//
			dos.writeUTF(Integer.toString(map.length));
			for (int i = 0;i<map.length; i++) {
				dos.writeUTF(map[i]);
			}
			//
			System.out.println("Sending of the Map is done");
		}
		catch (Exception e) {
			System.out.println("Sending of the Map failed");
			System.out.println(e);
		}
	}
	public void update () {
		System.out.println("Client update");
	}
}

class WaitServer extends Thread{
	private VirtualServer [] servers;
	public WaitServer (VirtualServer [] servers) {
		this.servers = servers;
		System.out.println("WaitServer created");
	}
	public void run () {
		try {
			ServerSocket ss = new ServerSocket(6900);
			System.out.println("WaitServer started");
			while (true)
			 new ConnectThread (ss.accept(), servers).start();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}

class ConnectThread extends Thread{
	Socket clientSocket;
	VirtualServer[] servers;
	String username;
	public ConnectThread (Socket s, VirtualServer[] servers) {
		this.clientSocket = s;
		this.servers = servers;
		System.out.println("new ConnectThread");
	}
	public void run () {
		try {
		DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
		username = dis.readUTF();
		System.out.println(username);
		String answer = "";
		answer += servers.length;
		for (int i = 0; i < servers.length; i++) answer+=":"+servers[i].port;
    dos.writeUTF(answer);
		System.out.println(answer);
		}
		catch (Exception e) {
			System.out.println(e);
		}

	}
}

class MainServer {
	private VirtualServer [] servers;
	public MainServer () {
		super();
		startServer();
		servers = createVirtualServers(1);
		runServers();
		new WaitServer(servers).start();
	}
	public void startServer () {

	}
	public void runServers () {
		for (int i =0; i<servers.length; i++) servers[i].start();
	}
	public VirtualServer[] createVirtualServers (int count) {
		VirtualServer [] servers = new VirtualServer [count];
		for (int i =0; i<servers.length; i++) servers[i] = new VirtualServer(i);
		return servers;
	}
}

class Game extends Thread{
	ArrayList <GameClientThread> clients;
	public Game (ArrayList <GameClientThread> clients) {
		this.clients = clients;
	}
	public void beforeGame () {
		System.out.println("Game is running");
		start();
	}
	public void run () {
		try {
			while (true) {
				System.out.println("Update server");
				for (int i = 0; i<clients.size();i++) clients.get(i).update();
				sleep(100);
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
class Position {
	private int x;
	private int y;
	public Position (int x, int y) {
		this.x = x;
		this.y = y;
	}
	public void updatePosition (int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Position getPosition () {
		return this;
	}
	public int getX () {
		return  x;
	}
	public int getY () {
		return y;
	}
}

class Gamer {
	Position pos;
	GameClientThread clientThread;
	String team;
	public Gamer (GameClientThread gct, String team) {
		this.clientThread = gct;
		this.team = team;
	}
}
