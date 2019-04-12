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
		File file = new File("D:/github/shooter/maps/map1.txt");
		//File file = new File("/home/10a/polyakov_om/github/shooter/maps/map1.txt");

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
		 new Game(clients).beforeGame(map);
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
	public void sendFile (File f) {
		System.out.println("Start of sending file");
		byte [] mybytearray  = new byte [(int)f.length()];
		try {
			dos.write(mybytearray,0,mybytearray.length);
		}
		catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("Done");
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
		String answer = "";
		answer += servers.length;
		//:port/name/col:
		for (int i = 0; i < servers.length; i++) answer+=":"+servers[i].port+"/"+"name"+ "/" +"0";
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
	ArrayList <Gamer> gamers;
	String[]map;
	public Game (ArrayList <GameClientThread> clients) {
		this.clients = clients;
	}
	public void beforeGame (String[]map) {
		this.map = map;
		System.out.println("Game is running");
		createPlayers();
		generatePlayersPositions();
		sendTestFile();
		start();

	}
	public void sendTestFile () {
		//File f = new File("");
		File f = new File("test.txt");
		//вызов отправки файла для каждого из игроков
	}
	public void createPlayers () {
		gamers = new ArrayList <Gamer>();
		String team = "none";
		for (int i = 0;i<clients.size(); i++) {
			gamers.add(new Gamer(clients.get(i), team));
		}

	}
	public boolean allowablePosition(Position p) {
		char c = map[p.x].charAt(p.y);
		if (isValid(c)) return true;
		else return false;
	}
	public boolean isValid (char c) {
		ArrayList<Character> cl = new ArrayList<Character>();
		cl.add('d');
		for (int i=0; i<cl.size();i++) if (cl.get(i)==c) return false;

		return true;
	}
	public Position randomPosition (int minX,int maxX,int minY,int maxY) {
		int x = 0;
		int y = 0;
		Random r = new Random();
		x = minX + r.nextInt(maxX - minX + 1);
		y = minY + r.nextInt(maxY - minY + 1);
		Position p = new Position(x,y);
		if (allowablePosition(p)) return p;
		else return randomPosition(minX,maxX,minY,maxY);
	}
	public void generatePlayersPositions() {
		for (int i = 0;i<gamers.size(); i++) {
			Position p = randomPosition(0,1000,0,1000);
			gamers.get(i).setDefaultPosition(p);
		}
	}
	public void run () {
		try {
			while (true) {
				//System.out.println("Update server");
				for (int i = 0; i<gamers.size();i++) gamers.get(i).update();
				sleep(100);
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}

class Position {
	int x;
	int y;
	public Position (int x, int y) {
		this.x = x;
		this.y = y;
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
	public void setDefaultPosition (Position p) {
		pos = p;
		System.out.println("PlayerPosition " +p.x + " " + p.y);
	}
	public void update () {
		//System.out.println("Gamer update");
	}
	public void sendFile (File f) {
		clientThread.sendFile(f);
	}
}
