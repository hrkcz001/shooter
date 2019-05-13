import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.text.DecimalFormat;

class Client {

	public static void main (String [] args) throws Exception{

    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    Wind frame = new Wind(size);

  }

}

class WaitServerConnecter{

	DataInputStream dis;
  DataOutputStream dos;
	Socket s;

	public Boolean setSocket(String ip, int port){

		try{

			s = new Socket (ip,port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());

			return(true);

		}
		catch(Exception e){

			e.printStackTrace();
			return(false);

		}

	}

	public String getServerList(){

		String res;

	  try{

			res = dis.readUTF();
			s.close();
			return(res);

		}
		catch(Exception e) {

			e.printStackTrace();
			return("false");

		}

	}

}

class GameServerConnection {

	DataInputStream dis;
    DataOutputStream dos;
    String[] map;
	String username = "PLAYER";

	public GameServerConnection(String ip, int port) throws Exception{

		Scanner sc = new Scanner(System.in);
		Socket s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

	}

	public void enterServer(String username) throws Exception {

		this.username = username;
		dos.writeUTF(username);
		String answ = dis.readUTF();

	}

	public void downloadMap() throws Exception {

			int length = Integer.parseInt(dis.readUTF());
			map = new String[length];
			for (int i = 0; i < length; i++) {

				map[i] = dis.readUTF();

			}

			dos.writeUTF(dis.readUTF());

 }

}

class Screen extends JComponent{

	char[][] map;
	Boolean mapOn = false;
	Dimension size;
	DataInputStream dis;
	double kWidth;
	double kHeight;
	Point myPos = new Point(0, 0);
	Boolean myPosBoolean = false;
	int myNum;

  DecimalFormat df = new DecimalFormat("#.00");

	String[] oldBulletString = {"-100,00/-100,00:"};

	public Screen(GameServerConnection gsc, Dimension size, ServerConnectionThread sct){

		dis = gsc.dis;
		String[] stringMap = gsc.map;
		map = new char[stringMap.length][];
		for(int i = 0; i < stringMap.length; i++){
			map[i] = stringMap[i].toCharArray();
		}
		this.size = size;
		kWidth = size.width / 1600.0;
		kHeight = size.height / 900.0;
		myPosBoolean = false;
		sct.startedScreen = true;

		javax.swing.Timer timer = new javax.swing.Timer(2, new ActionListener(){

			public void actionPerformed(ActionEvent ae){

				repaint();

			}

		});

		timer.start();

	}

  public void paintComponent(Graphics gr){

		super.paintComponents(gr);
		Graphics2D g =(Graphics2D)(gr);

		if(!mapOn){

		try{

			g.setPaint(Color.BLACK);
			g.fillRect(0, 0, size.width, size.height);

			String dataForPaint = dis.readUTF();
			//System.out.println(dataForPaint);

			if(!dataForPaint.isEmpty()){


				String[] splitedData = dataForPaint.split("&");

				if(!splitedData[0].isEmpty()){

					g.setPaint(Color.RED);

					for(int i = 0; i < oldBulletString.length; i++){

						g.fillRect((int)(df.parse(oldBulletString[i].split("/")[0]).doubleValue() * kWidth), (int)(df.parse(oldBulletString[i].split("/")[1]).doubleValue() * kHeight), (int)(4 * kWidth), (int)(4 * kHeight));

					}

					String[] bulletString = splitedData[0].split(":");

					oldBulletString = bulletString;

					for(int i = 0; i < bulletString.length; i++){

						g.fillRect((int)(df.parse(bulletString[i].split("/")[0]).doubleValue() * kWidth), (int)(df.parse(bulletString[i].split("/")[1]).doubleValue() * kHeight), (int)(3 * kWidth), (int)(3 * kHeight));

					}

				}

				if(!splitedData[1].isEmpty()){

					if(splitedData.length >= 3){
						myNum = Integer.parseInt(splitedData[2]);
					}

					String[] playersString = splitedData[1].split(":");

					g.setPaint(Color.BLUE);

					for(int i = 0; i < playersString.length; i++){

						int playerX = (int)(df.parse(playersString[i].split("/")[0]).doubleValue() * kWidth);
						int playerY = (int)(df.parse(playersString[i].split("/")[1]).doubleValue() * kHeight);
						double angle = Math.PI / 2 - df.parse(playersString[i].split("/")[2]).doubleValue();

						if((splitedData.length >= 3) && (i == myNum)){

							myPos = new Point(playerX, playerY);
							myPosBoolean = true;

						}

						int rotationX = (int)(playerX + 25 * kWidth * Math.cos(angle));
						int rotationY = (int)(playerY + 25 * kHeight * Math.sin(angle));

						g.fillOval((int)(playerX - 25 * kWidth), (int)(playerY - 25 * kHeight), (int)(50 * kWidth), (int)(50 * kHeight));
						g.setColor(Color.WHITE);
						g.drawLine(playerX, playerY, rotationX, rotationY);

					}


				}

			}

		}catch(Exception e){e.printStackTrace();}
	}
	else{

		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[i].length; j++){
				switch(map[i][j]){
					case 'f': g.setPaint(Color.GREEN);
					          break;
					case 'b': g.setPaint(Color.BLACK);
										break;
				}
				g.fillRect(j*5 + size.width / 2 - map[0].length * 5, i*5 + size.height / 2 - map.length * 5, 5, 5);
			}
		}

  }

}

class Wind extends JFrame{

	final Dimension size;
	Screen screen;

  public Wind(Dimension size) throws Exception{

    super("Shooter");
		this.size = size;
    setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setUndecorated(true);
    setResizable(false);

    MainPane main = new MainPane(this, size);

		addKeyListener(new KeyAdapter(){

			public void keyPressed(KeyEvent ke){

				if(main.started){

					switch(ke.getKeyCode()){

						case 87: main.sct.w = true; break;
						case 65: main.sct.a = true; break;
						case 83: main.sct.s = true; break;
						case 68: main.sct.d = true; break;
						case 77: if(screen.mapOn){screen.mapOn = false;}
										 else{screen.mapOn = true;} break;

					}

				}

			}

			public void keyReleased(KeyEvent ke){

				if(main.started){

					switch(ke.getKeyCode()){

						case 87: main.sct.w = false; break;
						case 65: main.sct.a = false; break;
						case 83: main.sct.s = false; break;
						case 68: main.sct.d = false; break;

					}

				}

			}

		});

    add(main);
    setVisible(true);
    main.setVisible(true);

  }

}

class MainPane extends JLayeredPane{

	Wind wind;
	Dimension size;
	int currentX;
	int currentY;
	JPanel startGamePanel;
	ServerConnectionThread sct;
	Boolean started = false;

  public MainPane(Wind wind, Dimension size) throws Exception{

    super();
    setSize(size);
    setLocation(0, 0);
		setOpaque(true);
    setBackground(new Color(100, 200, 100));
		setLayout(null);
		this.wind = wind;
		this.size = size;
		this.wind = wind;

		createServerTable();

		JLabel startButton = new JLabel("Find Server");
		startButton.setLocation(size.width / 80, size.height / 2);
		startButton.setFont(new Font("Arial", Font.BOLD, size.height / 36));
		startButton.setForeground(new Color(200, 50, 200));
		startButton.setSize(size.height / 5, size.height / 36);
		startButton.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent ae){

				startGamePanel.setLocation(size.width / 6, size.height / 10);
				startGamePanel.setVisible(true);

			}

		});
		add(startButton, 2);



  }

	public void createServerTable(){

		startGamePanel = new JPanel();

		startGamePanel.setLocation(size.width / 6, size.height / 10);
		startGamePanel.setSize(size.width * 2 / 3, size.height * 3 / 4 + size.height / 20);
		startGamePanel.setBackground(new Color(255, 100, 255));
		startGamePanel.setLayout(null);

		startGamePanel.addMouseListener(new MouseAdapter(){

			public void mousePressed(MouseEvent e) {
					currentX = e.getX();
					currentY = e.getY();
			}

			public void mouseDragged(MouseEvent e) {
					startGamePanel.setLocation(startGamePanel.getLocation().x + e.getX() - currentX, startGamePanel.getLocation().y + e.getY() - currentY);
			}

		});
		startGamePanel.addMouseMotionListener(new MouseMotionAdapter() {
							public void mouseDragged(MouseEvent e) {

									startGamePanel.setLocation(startGamePanel.getLocation().x + e.getX() - currentX,
													startGamePanel.getLocation().y + e.getY() - currentY);
							}
		});

		JLabel close = new JLabel("close");
		close.setForeground(new Color(150, 255, 150));
		close.setLocation(size.width * 2 / 3 - size.width / 40, size.height / 640);
		close.setSize(size.height / 10, size.height / 40);
		close.setFont(new Font("Arial", Font.BOLD, size.height / 60));
		close.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent me){

				startGamePanel.setVisible(false);

			}

		});
		startGamePanel.add(close);

		JLabel reload = new JLabel("reload");
		reload.setForeground(new Color(150, 255, 150));
		reload.setLocation(size.width * 2 / 3 - size.width / 15, size.height / 640);
		reload.setSize(size.height / 10, size.height / 40);
		reload.setFont(new Font("Arial", Font.BOLD, size.height / 60));
		reload.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent me){

				Point curPoint = startGamePanel.getLocation();
				startGamePanel.setVisible(false);
				createServerTable();
				startGamePanel.setLocation(curPoint);
				startGamePanel.setVisible(true);

			}

		});
		startGamePanel.add(reload);

		WaitServerConnecter wsc = new WaitServerConnecter();
		Boolean connectionRes = wsc.setSocket("localhost",6900);

		if(connectionRes){

				JTextArea jta = new JTextArea("Player");
				jta.setSize(size.width * 2 / 3, size.height / 40);
				jta.setLocation(0, size.height / 40);
				jta.setBackground(new Color(150, 255, 150));
				jta.setForeground(new Color(255, 100, 255));
				startGamePanel.add(jta);

			  String answ = wsc.getServerList();
				String[] server = answ.split(":");
				String[][] tableData = new String[server.length - 1][3];

				for(int i = 1; i < server.length; i++){

					tableData[i - 1] = server[i].split("/");

				}

				TableModel tm = new DefaultTableModel(){
  				public boolean isCellEditable(int rowIndex, int columnIndex){
      			return(false);
  				}
					public Class<?> getColumnClass(int columnIndex) {
    				return(String.class);
					}
					public int getColumnCount() {
    				return(3);
					}
					public String getColumnName(int columnIndex) {
    				switch (columnIndex) {
        			case 0:
            			return "Name";
        			case 1:
            			return "Port";
        			case 2:
            			return "Players";
        		}
    				return "";
					}
					public int getRowCount() {
    				return(tableData.length);
					}
					public Object getValueAt(int rowIndex, int columnIndex){
						return(tableData[rowIndex][columnIndex]);
					}
				};

				JTable table = new JTable(tm);
				table.getColumnModel().setColumnSelectionAllowed(false);

				int height = size.height / 45;

				JPanel serverPanel = new JPanel();
				JScrollPane js = new JScrollPane(serverPanel);

				for(int i = 0; i < tableData.length; i++){

					JButton jb = new JButton("Connect");
					jb.setLocation(size.width * 2 / 3 - size.width / 9, height * i);
					jb.setSize(size.width / 9 - size.width / 150, height);
					final int tmp = i;
					jb.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent ae){

							try{
								setVisible(false);
								sct = new ServerConnectionThread(Integer.parseInt(tableData[tmp][1]), jta.getText(), wind);
								started = true;
							}
							catch(Exception e){
								e.printStackTrace();
							}

						}
					});

					serverPanel.add(jb);

				}

				table.setRowHeight(height);
				table.setLocation(0, 0);
				table.setSize(size.width * 2 / 3 - size.width / 9, height * tableData.length);
				serverPanel.add(table);
				serverPanel.setLayout(null);
				serverPanel.setPreferredSize(new Dimension(size.width * 2 / 3, height * tableData.length));
				js.setLocation(0, size.height / 20);
				js.setSize(size.width * 2 / 3, size.height * 3 / 4);
				js.getVerticalScrollBar().setPreferredSize(new Dimension(size.width / 150, 0));

				js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				table.getColumnModel().getColumn(0).setPreferredWidth(size.width / 3 - size.width / 27);
				table.getColumnModel().getColumn(1).setPreferredWidth(size.width / 6 - size.width / 27);
				table.getColumnModel().getColumn(2).setPreferredWidth(size.width / 6 - size.width / 27);

				startGamePanel.add(js);

				startGamePanel.setVisible(false);

				add(startGamePanel, 1);

    }
    else{

     JLabel refused = new JLabel("Не установлено соединение с сервером");
     refused.setFont(new Font("Arial", Font.BOLD, size.height / 50));
     refused.setForeground(Color.RED);
     refused.setLocation(size.width / 5, size.height * 3 / 8 + size.height / 40);
		 refused.setSize(size.width, size.height / 50);
     startGamePanel.add(refused);

		 startGamePanel.setVisible(false);
		 add(startGamePanel, 1);

    }

	}

}

class ServerConnectionThread extends Thread{

	int port;
	String username;
	Wind wind;
	Boolean started = true, startedScreen = false;
	DataOutputStream dos;
	Boolean w = false, a = false, s = false, d = false;


	public ServerConnectionThread(int port, String username, Wind wind){

		this.port = port;
		this.username = username;
		this.wind = wind;
		start();

	}

	public void run(){

		try{

			GameServerConnection gsc = new GameServerConnection("localhost", port);
			gsc.enterServer(username);
			gsc.downloadMap();

			this.dos = gsc.dos;

			new ResponseThread(this).start();

			wind.screen = new Screen(gsc , wind.size, this);

			wind.add(wind.screen);
			wind.setVisible(true);


		}
		catch(Exception e){

			e.printStackTrace();

		}

	}

}

class ResponseThread extends Thread{

	ServerConnectionThread sct;
	String x = "0";
	String y = "0";

	public ResponseThread(ServerConnectionThread sct){

		this.sct = sct;

	}

	public void run(){

		try{

			while(sct.started){

				if(sct.w && sct.s){
					y = "0";
				}
				else{
					if(sct.w){
						y = "-1";
					}
					else{
						if(sct.s){
							y = "1";
						}
						else{
							y = "0";
						}
					}
				}

				if(sct.a && sct.d){
					x = "0";
				}
				else{
					if(sct.a){
						x = "-1";
					}
					else{
						if(sct.d){
							x = "1";
						}
						else{
							x = "0";
						}
					}
				}

				double rotation = 0.0;

				if((sct.startedScreen) && (sct.wind.screen.myPosBoolean)){

					Point mouse = MouseInfo.getPointerInfo().getLocation();

					rotation = Math.atan2(mouse.x - sct.wind.screen.myPos.x, mouse.y - sct.wind.screen.myPos.y);

				}

				if(sct.startedScreen){sct.dos.writeUTF(x + "/" + y + "/" + sct.wind.screen.df.format(rotation) + "/1");}

				Thread.sleep(10);

			}

		}catch(Exception e){e.printStackTrace();}

	}

}
