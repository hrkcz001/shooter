import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;


class Server {
	public static void main(String[]args){
		new ServerInfo("D:/github/shooterFF/serverinfo.txt");
		//ServerInfo.printAllData();
		new MainServer();
	}
}

class VirtualServer extends Thread{
	private ServerSocket ss;
	ArrayList <GameClientThread> clients;
	int port;
	int id;
	Game g;
	String status = "waiting for players";
	String name = "";
	public VirtualServer (int id) {
		this.id = id;
		this.name = "Server " + (id + 1);
		clients = new ArrayList<GameClientThread>();
		//количество игроков на сервере
		try {
			port = 6900 + id + 1;
			ss = new ServerSocket(port);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void run () {
		System.out.println(port);
		try {
		while (clients.size() < Integer.parseInt(ServerInfo.getData(2)))
		{
		 clients.add(new GameClientThread (ss.accept()));
		 status = "waiting for players, " + clients.size() + " of " + ServerInfo.getData(2);
	 }
		 g = new Game(clients);
		 g.beforeGame();
		 status = "in game, " + clients.size() + " player(s)";
	 }
	 catch (Exception e) {
	 	e.printStackTrace();
	 }
	}
}

class GameClientThread extends Thread {
	DataInputStream dis;
	DataOutputStream dos;
	OutputStream os;
	String clientNickname;
	Socket clientSocket;
	public GameClientThread (Socket clientSocket) {
		try {
			dis = new DataInputStream(clientSocket.getInputStream());
			dos = new DataOutputStream(clientSocket.getOutputStream());
			os = clientSocket.getOutputStream();
			this.clientSocket = clientSocket;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		start();
	}
	public void run () {
		System.out.println("GameClientThread started");
		clientNickname = readClientNickname();
	}
	public String readClientNickname () {
		try {
			String name = dis.readUTF();
			System.out.println("Nickname " + name);
			dos.writeUTF("true");
			return name;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public synchronized void sendString (String s) {
		try {
			dos.writeUTF(s);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	public synchronized String readString () {
		try {
			return(dis.readUTF());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
			e.printStackTrace();
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
		for (int i = 0; i < servers.length; i++) answer+=":" + servers[i].name +"/"+servers[i].port+"/" +servers[i].status;
    dos.writeUTF(answer);
		System.out.println(answer);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class MainServer {
	private VirtualServer [] servers;
	public MainServer () {
		super();
		startServer();
		servers = createVirtualServers(Integer.parseInt(ServerInfo.getData(1)));
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

class Game extends Thread {
	ArrayList <GameClientThread> clients;
	ArrayList <Gamer> gamers;
	ArrayList <Barrier> barriers;
	ArrayList <Camera> cameras;
	ArrayList <Weapon> weapons;
	BulletThread bt;
	String[]map;
	public Game (ArrayList <GameClientThread> clients) {
		this.clients = clients;
	}
	public void setBarriers() {
		barriers = new ArrayList<Barrier>();
	}
	public void sendMap (GameClientThread gct) {
		try {
			System.out.println("Sending the Map");
			//
			gct.sendString(Integer.toString(map.length));
			for (int i = 0;i<map.length; i++) {
				gct.sendString(map[i]);
			}
			//
			System.out.println("Sending of the Map is done");
		}
		catch (Exception e) {
			System.out.println("Sending of the Map failed");
			e.printStackTrace();
		}
	}
	public void sendMapToAll() {
		try {
		} catch(Exception e) {
			e.printStackTrace();
		}
		for (int i = 0;i<gamers.size();i++) {
			sendMap(gamers.get(i).clientThread);
		}
	}
	public String[] readMap () {
		try {
	  //System.out.println(ServerInfo.getData(3));

		String s = "D:/github/shooterFF/maps/map2.txt";
		/*if (ServerInfo.getData(3).equals(s)) System.out.println("SOSAMBA");*/
		File file = new File(s);

		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		String[] map = str.split(System.lineSeparator());
		return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public void beforeGame () {
		map = readMap();
		System.out.println("Game is running");
		setBarriers();
		createPlayers();
		createWeapons();
		generatePlayersPositions();
		sendMapToAll();
		sendTestString();
		bt = new BulletThread(gamers, barriers);

		barriers = new ArrayList <Barrier>();
		cameras = new ArrayList<Camera>();
		cameras.add(new Camera(0,0,1600,900,bt,gamers));
		for (int i = 0;i<gamers.size();i++)
		 cameras.get(0).addPlayer(i);
		//cameras.get(0).addPlayer(1);
		//gamers.get(0).pos = new DoublePosition(10,10);
		//cameras.get(0).addPlayer(1);
		bt.addDefaultBullet(1.2131231212,2.1323242433454535434663,new DoublePosition(120,101),"red");
		bt.addDefaultBullet(1,3,new DoublePosition(10,15),"red");
		//in the end
		start();
	}
	public void createWeapons () {
		weapons = new ArrayList<Weapon>();
		weapons.add(new Weapon("gun",1,100,20));
	}
	public void sendTestString(){
		String s;
		long t1;
		long t2;
		long dt;
		for (int i = 0;i<gamers.size();i++) {
			t1 = new Date().getTime();
			gamers.get(i).clientThread.sendString("test");
			s = gamers.get(i).clientThread.readString();
			t2 = new Date().getTime();
			dt = t2 - t1;
			System.out.println (gamers.get(i).nickname + " ping " + dt);
		}
	}
	public void createPlayers () {
		gamers = new ArrayList <Gamer>();
		String[] teams = new String[2];
		teams[0] = "1";
		teams[1] = "-1";
		for (int i = 0;i<clients.size(); i++) {
			gamers.add(new Gamer(clients.get(i), teams[i%2]));
			gamers.get(i).nickname = clients.get(i).clientNickname;
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
	public Position randomPosition (int minX,int maxX,int minY,int maxY,int k) {
		int x = 0;
		int y = 0;
		Random r = new Random();
		x = minX + r.nextInt(maxX - minX);
		y = minY + r.nextInt(maxY - minY);
		Position p = new Position(x,y);
		Position d = new Position(x,y);
		p.x = (p.x - p.x % k) / k;
		p.y = (p.y - p.y % k) / k;
		if (allowablePosition(p)) return d;
		else return randomPosition(minX,maxX,minY,maxY,k);
	}
	public void generatePlayersPositions() {
		for (int i = 0;i<gamers.size(); i++) {
			Position p = randomPosition(0,1600,0,900,10);
			gamers.get(i).setDefaultPosition(p);
		}
	}
	public void cameraUpdate () {
		 //System.out.println(cameras.get(0).forGamer);
		 String s = "";
		 //проход по камерам, отправка пользователям координат
		 for (int i = 0;i<cameras.size();i++)
		 	for (int j = 0;j<cameras.get(i).gamersId.size();j++) {
				s = cameras.get(i).forGamer + "&" + Integer.toString(j) + "&" + cameras.get(i).cameraInfString;
				gamers.get(cameras.get(i).gamersId.get(j)).sendString(s);
				//System.out.println(Integer.parseInt(s.split("&")[2]) + " " + i);

			}
			//System.out.println(s);
	}
	public void run () {
		new Updater(gamers, bt, weapons).start();
		try {
			while (true) {
				cameraUpdate();
				gamersDtUpdate();
				sleep(6);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void gamersDtUpdate() {
		for (int i = 0;i<gamers.size();i++)
		  if (gamers.get(i).dt > 0) gamers.get(i).dt -= 10;
	}
}

class Position {
	int x;
	int y;
	public Position (int x, int y) {
		this.x = x;
		this.y = y;
	}
	public DoublePosition toDoublePosition() {
		return new DoublePosition (x,y);
	}
}

class Gamer {
	DoublePosition pos;
	GameClientThread clientThread;
	String team;
	String nickname;
	int health;
	double v;
	double rotation;
	int dt;
	int weaponId;
	boolean status = true;
	public Gamer (GameClientThread gct, String team) {
		this.clientThread = gct;
		this.team = team;
		this.nickname = clientThread.clientNickname;
		health = 100;
		v = 1;
		rotation = 0;
		dt = 0;
		weaponId = 0;
	}
	public void setDefaultPosition (Position p) {
		pos = p.toDoublePosition();
		System.out.println("PlayerPosition " + p.x + " " + p.y);
	}
	public void damage (int d) {
		health -= d;
		System.out.println(d);
		if (health <= 0) status = false;
	}
	public synchronized void sendString (String s) {
		clientThread.sendString(s);
	}
	public void updatePosition (String a, String b) {
		double vx = Integer.parseInt(a)*v;
		double vy = Integer.parseInt(b)*v;
		if (allowablePosition(vx,vy)) pos = new DoublePosition(pos.x + vx, pos.y + vy);
	}
	public boolean allowablePosition (double vx, double vy) {
		if (pos.x + vx > 0) return true;
		if (pos.x + vx < 1600) return true;
		if (pos.y + vy > 0) return true;
		if (pos.y + vy < 1600) return  true;

		return false;
	}
}

class Bullet {
	double vx;
	double vy;
	int radius;
	DoublePosition p;
	String team;
	int damage;
	ArrayList <Gamer> gamers;
	ArrayList <Barrier> barriers;
	public Bullet (double vx,double vy,int radius,int damage,DoublePosition p,ArrayList<Gamer> gamers, String team, ArrayList<Barrier>barriers) {
		this.vx = vx;
		this.vy = vy;
		this.p = p;
		this.gamers = gamers;
		this.team = team;
		this.damage = damage;
		this.barriers = barriers;
		radius = 1;
	}
	public boolean update () {
		moove();
		return makeDamage(checkCollision());
	}
	public boolean makeDamage (Gamer g) {
		if (g==null) return false;
		//нанесение дамага
		g.damage(damage);
		stop();
		return true;
	}
	public void moove () {
		p.x += vx;
		p.y += vy;
	}
	public void stop () {
		vx = 0;
		vy = 0;
	}
	public Gamer checkCollision () {
		for (int i = 0;i<gamers.size();i++) {
			double s = Math.pow((gamers.get(i).pos.x - p.x),2) + Math.pow((gamers.get(i).pos.y - p.y),2);
			if ((gamers.get(i).team == team)&&(Math.sqrt(s)<=radius))
			 return gamers.get(i);
		}
		return null;
	}
	public boolean outOfZone () {
		if (p.x < 0) return true;
		if (p.y < 0) return true;
		if (p.x > 1600) return true;
		if (p.y > 1600) return true;
		return false;
	}
}

class BulletThread extends Thread{
	ArrayList <Gamer> gamers;
	ArrayList <Barrier> barriers;
	ArrayList <Bullet> bullets;
	int defRadius = 26;
	int defDamage = 10;
	public BulletThread (ArrayList <Gamer> gamers, ArrayList <Barrier> barriers) {
		this.gamers = gamers;
		this.barriers = barriers;
		bullets = new ArrayList<Bullet>();
		//addDefaultBullet(10,10,new Position(10,10),"red");
		start();
	}
	public void run () {
		try {
			while (true) {
				update();
				sleep(10);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void update () {
		for (int i = 0;i<bullets.size();i++) {
			if ((bullets.get(i).update())||(bullets.get(i).outOfZone())) bullets.remove(i);
			else if (checkBarrierCollision(bullets.get(i))) bullets.remove(i);
		}
	}
	public boolean checkBarrierCollision (Bullet g){
		for (int i=0;i<barriers.size();i++) {
			if (barriers.get(i).checkCollision(g.p.x,g.p.y)) return true;
		}
			return false;
	}
	public void addDefaultBullet (double vx, double vy,DoublePosition p,String team) {
		addBullet(vx,vy,defRadius,defDamage,p,gamers,team,barriers);
	}
	public void addBullet(double vx,double vy,int radius,int damage,DoublePosition p,ArrayList<Gamer> gamers, String team, ArrayList<Barrier>barriers){
		bullets.add(new Bullet(vx,vy,radius,damage,p,gamers,team,barriers));
	}
}

class Barrier {
	int xFrom;
	int yFrom;
	int width;
	int height;
	public Barrier (int x, int y, int w, int h) {
		xFrom = x;
		yFrom = y;
		width = w;
		height = h;
	}
	public boolean checkCollision(double x, double y) {
		if (xFrom > x) return false;
		if ((xFrom + width) < x) return false;
		if (yFrom > y) return false;
		if ((yFrom + height) < y) return false;
		return  true;
	}
}

class Camera extends Thread{
	ArrayList<Integer> gamersId;
	ArrayList<Gamer> gamers;
	ArrayList<Bullet> bullets;
	int xFrom;
	int yFrom;
	int width;
	int height;
	String forGamer;
	DecimalFormat decimalFormat;
	String cameraInfString;
	public Camera (int x,int y, int sizeX, int sizeY, BulletThread bt, ArrayList <Gamer> gamers) {
		this.xFrom = x;
		this.yFrom = y;
		this.width = sizeX;
		this.height = sizeY;
		this.bullets = bt.bullets;
		this.gamers = gamers;
		gamersId = new ArrayList<Integer>();
		decimalFormat = new DecimalFormat("#.00");
		forGamer = "";
		System.out.println("Camera created");
		cameraInfString = x + "/" + y + "/" + (x + sizeX) + "/" + (y + sizeY);
		start();
	}
	public void addPlayer(int i) {
		gamersId.add(i);

	}
	public void deletePlayer (int i) {
		gamersId.remove(i);
	}
	public String bulletsToString () {
		String s = "";
		DoublePosition p;
		for (int i = 0;i<bullets.size();i++) {
			p = bullets.get(i).p;
			if (inCameraArea(p)) s+= decimalFormat.format(p.x - xFrom) + "/" + decimalFormat.format(p.y - yFrom) + ":";
		}
		return s;
	}
	public boolean inCameraArea (DoublePosition p) {
		if (xFrom > p.x) return false;
		if ((xFrom + width) < p.x) return false;
		if (yFrom > p.y) return false;
		if ((yFrom + height) < p.y) return false;
		return  true;
	}
	public String gamersToString () {
		String s = "";
		for (int i = 0;i<gamersId.size();i++) {
			s += decimalFormat.format(gamers.get(gamersId.get(i)).pos.x - xFrom) + "/"+ decimalFormat.format(gamers.get(gamersId.get(i)).pos.y - yFrom) + "/" + decimalFormat.format(gamers.get(gamersId.get(i)).rotation) + "/"+ Integer.parseInt(gamers.get(i).team);
			s += "/" + (gamers.get(i).status ? 1 : 0);
			s += "/" + gamers.get(i).health;
			s+=":";
		}
		return s;
	}
	public String getForGamer() {
		return (bulletsToString() + "&" + gamersToString());
	}
	public void run () {
		try {
		while (true) {
			forGamer = getForGamer();
			sleep(10);
		}
		}
	catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class DoublePosition {
	double x;
	double y;
	public DoublePosition (double x, double y){
		this.x = x;
		this.y = y;
	}
}

class ServerInfo {
	public static String[] data;
	public ServerInfo (String s) {
		readData(readDataFile(s));
	}
	public void readData (String s) {
		data = new String[5];
		data = s.split("&");
	}
	public String readDataFile (String way) {
		try {
		File file = new File(way);

		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		return str;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	public static String getData (int i) {
		return  data[i];
	}
	public static void printAllData () {
		for (int i = 0;i<data.length;i++) System.out.print(getData(i) + " ");
	}
}

class Updater extends Thread{
	ArrayList <Gamer> gamers;
	ArrayList <Weapon> weapons;
	BulletThread bt;
	int defBulletV = 11;
	public Updater (ArrayList <Gamer> gamers, BulletThread bt, ArrayList<Weapon> weapons) {
		this.gamers = gamers;
		this.bt = bt;
		this.weapons = weapons;
	}
	public void run () {
		try {
			while (true) {
				update();
				sleep(10);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void update () {
		try {
			Gamer g;
			double angle = 0;
		for (int i = 0;i<gamers.size();i++){
			g = gamers.get(i);
			String[] splitedData = g.clientThread.readString().split("/");
			g.updatePosition(splitedData[0], splitedData[1]);
			g.rotation = DecimalFormat.getNumberInstance().parse(splitedData[2]).doubleValue();
			//System.out.println(Integer.toString(i) + " " + splitedData[1] + " " + splitedData[2]);
			if ((Integer.parseInt(splitedData[3]) == 1)&&(g.dt <= 0)&&(g.status)) {
				//System.out.println("+Bullet");
				g.dt = weapons.get(g.weaponId).dt;
				angle = Math.PI/2 - g.rotation;
				bt.addDefaultBullet(defBulletV*Math.cos(angle),defBulletV*Math.sin(angle),new DoublePosition(g.pos.x + 25*Math.cos(angle), g.pos.y + 25*Math.sin(angle)),g.team);
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	}
}

class Weapon {
	int radius;
	String name;
	int dt;
	int damage;
	public Weapon (String name,int radius, int dt, int damage) {
		this.radius = radius;
		this.dt = dt;
		this.damage = damage;
		this.name = name;
	}
}

class GamerUpdater extends Thread {
	Gamer g;
	public GamerUpdater (Gamer g) {
		this.g = g;
	}
}
