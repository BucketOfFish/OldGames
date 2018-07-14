import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
@SuppressWarnings("serial")
public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener,KeyListener {
	Thread thread=null;
	MediaTracker tracker=new MediaTracker(this);
	Image floor[]=new Image[20],area,target;
	Graphics2D cast;
	ArrayList<magic> grimoire=new ArrayList<magic>(),spellbook=new ArrayList<magic>();
	magic heal,freeze,unlock,kill;
	ArrayList<creature> bestiary=new ArrayList<creature>();
	creature self,person,zombie;
	ArrayList<item> inventory=new ArrayList<item>();
	item savespot;
	ArrayList<layout> map=new ArrayList<layout>();
	int blocked[]={1,3,5},lastx,lasty,currentmap=1,difficulty=0,castingx,castingy,menuchoice=0,menucolor=0
		,menucolor2=0,menucolor3=0,music=0;
	String state="loading";
	ArrayList<String> messages=new ArrayList<String>();
	final int WIDTH=300,HEIGHT=325,CASTX=201,CASTY=226,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,FIELDWIDTH=300,FIELDHEIGHT=225,MAPWIDTH=15,MAPHEIGHT=15,FLOORWIDTH=20,LEEWAY=3,TOLERANCE=3
		,FLOORHEIGHT=15,RIGHT=0,DOWN=1,LEFT=2,UP=3;
	final int DIR[]={LEFT,UP,RIGHT,DOWN};
	public void effect(creature target,int spell) {
		if(spellbook.get(spell)==heal) {
			target.recover(10);
		}
		else if(spellbook.get(spell)==freeze) {
			try{
				if(map.get(currentmap).terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]==1)
					map.get(currentmap).terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]=4;
			}
			catch(ArrayIndexOutOfBoundsException e) {}
		}
		else if(spellbook.get(spell)==unlock) {
			try{
				if(map.get(currentmap).terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]==5)
					map.get(currentmap).terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]=0;
			}
			catch(ArrayIndexOutOfBoundsException e) {}
		}
		else if(spellbook.get(spell)==kill) {
			for(int count=0;count<map.get(currentmap).population.size();count++)
				if(map.get(currentmap).population.get(count).x<=castingx&&map.get(currentmap).population.get(count).x+map.get(currentmap).population.get(count).size>=castingx&&map.get(currentmap).population.get(count).y<=castingy&&map.get(currentmap).population.get(count).y+map.get(currentmap).population.get(count).size>=castingy)
					map.get(currentmap).population.get(count).damage(map.get(currentmap).population.get(count).HP);
		}
	}
	public void magic() {
		heal=new magic("Heal","plus.gif",0);
		freeze=new magic("Freeze","flake.gif",1);
		unlock=new magic("Unlock","key.gif",1);
		kill=new magic("Kill","slash.gif",1);
		spellbook.add(heal);
		spellbook.add(freeze);
		spellbook.add(unlock);
		spellbook.add(kill);
	}
	public void creature() {
		self=new creature("player");
		person=new creature("person");
		zombie=new creature("zombie");
		zombie.speed=2;
	}
	public void item() {
		savespot=new item("savespot",false);
	}
	public void populate() {
		map.get(1).add(savespot,140,105);
		map.get(4).add(zombie,140,105);
		map.get(2).add(savespot,140,105);
		map.get(3).add(savespot,140,105);
		map.get(7).add(zombie,120,85);
		map.get(7).add(zombie,160,125);
		map.get(7).add(zombie,120,125);
		map.get(7).add(zombie,160,85);
		Random wheel=new Random();
		for(int count=0;count<10;count++)
			map.get(8).add(zombie,wheel.nextInt(260)+20,wheel.nextInt(170)+15);
		for(int count=0;count<50;count++)
			map.get(9).add(zombie,wheel.nextInt(260)+20,wheel.nextInt(170)+15);
	}
	public void init() {
		repaint();
		target=getImage("target.gif");
		magic();
		creature();
		item();
		resize(WIDTH,HEIGHT);
		try {
			FileInputStream in=new FileInputStream("maps.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			map.add(new layout());
			while(true) {
				String next=file.readLine();
				int mapcount=convert(next);
				map.add(new layout());
				for(int j=0;j<MAPHEIGHT;j++) {
					next=file.readLine();
					for(int i=0;i<MAPWIDTH;i++)
						map.get(mapcount).terrain[i][j]=convert(next.substring(i*4,i*4+3));
				}
				file.readLine();
			}
		}
		catch(IOException e) {
			System.exit(0);
		}
		catch(Exception e) {}
		for(int count=0;count<getImage("floors.gif").getHeight(this)/FLOORHEIGHT;count++) {
			floor[count]=createImage(new FilteredImageSource(getImage("floors.gif").getSource(),new CropImageFilter(0,count*FLOORHEIGHT,FLOORWIDTH,FLOORHEIGHT)));
		}
		populate();
		area=createImage(CASTWIDTH,CASTHEIGHT);
		cast=(Graphics2D)area.getGraphics();
		cast.drawImage(target,0,0,this);
		cast.setColor(Color.red);
		cast.setStroke(new BasicStroke(3));
		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		state="menu";
	}
	class item implements Cloneable {
		Image icon;
		String name,property;
		boolean pickable;
		int x,y,sizex,sizey,count=0;
		public Object clone() {
			try {
				return super.clone();
			}
			catch(Exception e) {
				throw new InternalError(e.toString());
			}
		}
		item(String name,boolean pickable) {
			this.name=name;
			this.pickable=pickable;
			sizex=x;
			sizey=y;
			icon=target;
			icon=getImage(name+".gif");
		}
	}
	class creature extends item {
		int HP=50,maxHP=50,x=140,y=105,move=-1,speed=4,direction=DOWN,size=20,damage=1;
		Image icon[]=new Image[4];
		creature(String name) {
			super(name,false);
			for(int count=0;count<4;count++) {
				icon[count]=createImage(new FilteredImageSource(super.icon.getSource(),new CropImageFilter(0,count*20,20,20)));
				tracker.addImage(icon[count],0);
				try {
					tracker.waitForID(0);
				}
				catch (InterruptedException e) {}
			}
		}
		public int[] grid() {
			int output[]={(x+size/2)/FLOORWIDTH,(y+size/2)/FLOORHEIGHT};
			return output;
		}
		public void recover(int gain) {
			HP+=gain;
			if(HP>maxHP)
				HP=maxHP;
		}
		public void damage(int lose) {
			HP-=lose;
			if(HP<=0)
				map.get(currentmap).remove(this);
		}
	}
	class magic {
		Image icon;
		String name;
		int target;
		magic(String name,String image,int target) {
			this.name=name;
			icon=getImage(image);
			this.target=target;
			grimoire.add(this);
		}
	}
	class layout {
		ArrayList<creature> population=new ArrayList<creature>();
		ArrayList<item> stock=new ArrayList<item>();
		int terrain[][]=new int[MAPWIDTH][MAPHEIGHT];
		void add(creature me,int x,int y) {
			me.x=x;
			me.y=y;
			population.add((creature)me.clone());
		}
		void add(item it,int x,int y) {
			it.x=x;
			it.y=y;
			stock.add((item)it.clone());
		}
		void remove(creature me) {
			population.remove(me);
		}
		void remove(item it) {
			stock.remove(it);
		}
		layout() {}
	}
	public boolean contain(int number,int[] array) {
		boolean output=false;
		for(int count=0;count<array.length;count++) {
			if(number==array[count]) {
				count=array.length;
				output=true;
			}
		}
		return output;
	}
	public int convert(String input) {
		int number=0;
		for(int count=input.length();count>0;count--) {
			if(input.charAt(count-1)>57||input.charAt(count-1)<48) {
				if(count==1&&input.charAt(count-1)=='-')
					number*=-1;
				else
					number=-1;
				count=0;
			}
			else
				number+=(input.charAt(count-1)-48)*Math.pow(10,input.length()-count);
		}
		return number;
	}
	public void mousePressed(MouseEvent e) {
		lastx=e.getX();
		lasty=e.getY();
	}
	public void mouseDragged(MouseEvent e) {
		int x=e.getX(),y=e.getY();
		cast.drawLine(lastx-CASTX,lasty-CASTY,x-CASTX,y-CASTY);
		lastx=x;
		lasty=y;
	}
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==3&&state.equals("game")) {
			int spell=match();
			castingx=e.getX();
			castingy=e.getY();
			if(spell>=0) {
				messages.add("Cast "+spellbook.get(spell).name);
				effect(self,spell);
			}
			else if(spell==-2)
				messages.add("Casting failed");
			cast.drawImage(target,0,0,this);
			cast.setColor(Color.red);
		}
	}
	public int match() {
		for(int count=0;count<spellbook.size();count++) {
			float similarity=compare(area,spellbook.get(count).icon);
			if(similarity>=.7)
				return count;
			else if(similarity==-1)
				return -1;
		}
		return -2;
	}
	public float compare(Image a,Image b) {
		try {
			int pixelsOne[]=new int[CASTWIDTH*CASTHEIGHT];
			PixelGrabber grabber=new PixelGrabber(a,0,0,CASTWIDTH,CASTHEIGHT,pixelsOne,0,CASTWIDTH);
			grabber.grabPixels();
			int pixelsTwo[]=new int[CASTWIDTH*CASTHEIGHT];
			PixelGrabber grabber2=new PixelGrabber(b,0,0,CASTWIDTH,CASTHEIGHT,pixelsTwo,0,CASTWIDTH);
			grabber2.grabPixels();
			pixelsTwo=glow(pixelsTwo,TOLERANCE);
			float similarity=0,redpixels=0;
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixelsOne[i]==RED) {
					if(pixelsTwo[i]<=RED&&pixelsTwo[i]>RED-10) {
						if(pixelsTwo[i]!=RED)
							similarity+=1.0/Math.pow(RED-pixelsTwo[i],2);
						else
							similarity++;
					}
					else
						similarity--;
				}
				if(pixelsTwo[i]==RED)
					redpixels++;
			}
			if(redpixels==0)
				return -1;
			return similarity/redpixels;
		}
		catch(InterruptedException e) {
			return 0;
		}
	}
	public int[] glow(int pixels[],int layers) {
		for(int count=0;count<layers;count++) {
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixels[i]==RED-count) {
					for(int k=-1;k<=1;k++) {
						for(int j=-1;j<=1;j++) {
							try {
								if(pixels[i+j+k*CASTWIDTH]>RED||pixels[i+j+k*CASTWIDTH]<RED-count)
									pixels[i+j+k*CASTWIDTH]=RED-count-1;
							}
							catch(ArrayIndexOutOfBoundsException e) {}
						}
					}
				}
			}
		}
		return pixels;
	}
	public Image getImage(String image) {
		Image output;
		tracker.addImage(output=getImage(getCodeBase(),image),0);
		try {
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {}
		return output;
	}
	public void load() {
		try	{
			FileInputStream in=new FileInputStream("Save.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			currentmap=convert(file.readLine());
			self.x=convert(file.readLine());
			self.y=convert(file.readLine());
			state="game";
			in.close();
		}
		catch(IOException exc) {}
	}
	public void save() {
		try	{
			FileOutputStream out=new FileOutputStream("Save.sys");
			PrintStream printer=new PrintStream(out);
			printer.println(currentmap);
			printer.println(self.x);
			printer.println(self.y);
			out.close();
		}
		catch (IOException e) {}
	}
	public void start() {
		if(thread==null) {
			thread=new Thread(this);
			thread.start();
		}
	}
	public void run() {
		Thread current=Thread.currentThread();
		int count=0,lava=0;
		while(thread==current) {
			if(state.equals("menu")||state.equals("options")) {
				menucolor++;
				menucolor2+=2;
				menucolor3+=3;
				if(menucolor>255)
					menucolor=0;
				if(menucolor2>255)
					menucolor2=0;
				if(menucolor3>255)
					menucolor3=0;
			}
			else if(state.equals("game")) {
				if(self.HP<=0)
					state="lose";
				if(messages.size()!=0) {
					count++;
					if(count>=20) {
						messages.remove(0);
						count=0;
					}
				}
				for(int counter=0;counter<map.get(currentmap).stock.size();counter++) {
					item it=map.get(currentmap).stock.get(counter);
					if(it.pickable&&it.x<self.x+self.size&&it.x+it.sizex>self.x&&it.y<self.y+self.size&&it.y+it.sizey>self.y)
						map.get(currentmap).remove(it);
				}
				for(int counter=0;counter<map.get(currentmap).population.size();counter++) {
					Random wheel=new Random();
					creature me=map.get(currentmap).population.get(counter);
					if(me.x<self.x+self.size&&me.x+me.size>self.x&&me.y<self.y+self.size&&me.y+me.size>self.y) {
						if(wheel.nextInt(5)==0)
							self.damage(me.damage);
					}
					if(me.name.equals("zombie")) {
						int move=wheel.nextInt(50);
						if(move<4)
							me.move=move;
						else if(move==4)
							me.move=-1;
					}
					if(me.move!=-1) {
						me.direction=me.move;
						int oldx=me.x,oldy=me.y;
						try {
							if(me.move==UP) {
								me.y-=me.speed;
								for(int countera=0;countera<=(me.size-2*LEEWAY)/FLOORWIDTH;countera++) {
									if(contain(map.get(currentmap).terrain[(me.x+LEEWAY)/FLOORWIDTH+countera][(me.y+me.size/2)/FLOORHEIGHT],blocked))
										me.y=oldy;
								}
								if(contain(map.get(currentmap).terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
									me.y=oldy;
							}
							else if(me.move==DOWN) {
								me.y+=me.speed;
								for(int countera=0;countera<=(me.size-2*LEEWAY)/FLOORWIDTH;countera++) {
									if(contain(map.get(currentmap).terrain[(me.x+LEEWAY)/FLOORWIDTH+countera][(me.y+me.size)/FLOORHEIGHT],blocked))
										me.y=oldy;
								}
								if(contain(map.get(currentmap).terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT],blocked))
									me.y=oldy;
							}
							else if(me.move==LEFT) {
								me.x-=me.speed;
								for(int countera=0;countera<=(me.size-me.size/2)/FLOORHEIGHT;countera++) {
									if(contain(map.get(currentmap).terrain[(me.x+LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT-countera],blocked))
										me.x=oldx;
								}
								if(contain(map.get(currentmap).terrain[(me.x+LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
									me.x=oldx;
							}
							else if(me.move==RIGHT) {
								me.x+=me.speed;
								for(int countera=0;countera<=(me.size-me.size/2)/FLOORHEIGHT;countera++) {
									if(contain(map.get(currentmap).terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT-countera],blocked))
										me.x=oldx;
								}
								if(contain(map.get(currentmap).terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
									me.x=oldx;
							}
							if(me.x<0||me.y+me.size/4<0) {
								me.x=oldx;
								me.y=oldy;
							}
						}
						catch(ArrayIndexOutOfBoundsException e) {
							me.x=oldx;
							me.y=oldy;
						}
					}
				}
				if(self.move!=-1) {
					self.direction=self.move;
					int oldx=self.x,oldy=self.y;
					try {
						if(self.move==UP) {
							self.y-=self.speed;
							for(int counter=0;counter<=(self.size-2*LEEWAY)/FLOORWIDTH;counter++) {
								if(contain(map.get(currentmap).terrain[(self.x+LEEWAY)/FLOORWIDTH+counter][(self.y+self.size/2)/FLOORHEIGHT],blocked))
									self.y=oldy;
							}
							if(contain(map.get(currentmap).terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
								self.y=oldy;
						}
						else if(self.move==DOWN) {
							self.y+=self.speed;
							for(int counter=0;counter<=(self.size-2*LEEWAY)/FLOORWIDTH;counter++) {
								if(contain(map.get(currentmap).terrain[(self.x+LEEWAY)/FLOORWIDTH+counter][(self.y+self.size)/FLOORHEIGHT],blocked))
									self.y=oldy;
							}
							if(contain(map.get(currentmap).terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT],blocked))
								self.y=oldy;
						}
						else if(self.move==LEFT) {
							self.x-=self.speed;
							for(int counter=0;counter<=(self.size-self.size/2)/FLOORHEIGHT;counter++) {
								if(contain(map.get(currentmap).terrain[(self.x+LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT-counter],blocked))
									self.x=oldx;
							}
							if(contain(map.get(currentmap).terrain[(self.x+LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
								self.x=oldx;
						}
						else if(self.move==RIGHT) {
							self.x+=self.speed;
							for(int counter=0;counter<=(self.size-self.size/2)/FLOORHEIGHT;counter++) {
								if(contain(map.get(currentmap).terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT-counter],blocked))
									self.x=oldx;
							}
							if(contain(map.get(currentmap).terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
								self.x=oldx;
						}
						if(self.x<0||self.y+self.size/4<0) {
							if(map.get(currentmap).terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]<0) {
								currentmap=Math.abs(map.get(currentmap).terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]);
								if((oldx+self.size/2)/FLOORWIDTH==0)
									self.x=FLOORWIDTH*MAPWIDTH-self.size;
								else if((oldy+self.size/2)/FLOORHEIGHT==0)
									self.y=FLOORHEIGHT*MAPHEIGHT-self.size;
							}
							else {
								self.x=oldx;
								self.y=oldy;
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e) {
						if(map.get(currentmap).terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]<0) {
							currentmap=Math.abs(map.get(currentmap).terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]);
							if((oldx+self.size/2)/FLOORWIDTH==MAPWIDTH-1)
								self.x=0;
							else if((oldy+self.size/2)/FLOORHEIGHT==MAPHEIGHT-1)
								self.y=0;
						}
						else {
							self.x=oldx;
							self.y=oldy;
						}
					}
				}
				if(map.get(currentmap).terrain[self.grid()[0]][self.grid()[1]]==2) {
					lava++;
					if(lava%5==0) {
						self.HP-=1;
					}
				}
				else
					lava=0;
			}
			repaint();
			try {
				Thread.sleep(50);
			}
			catch(InterruptedException e) {}
		}
	}
	public void stop() {
		thread=null;
	}
	public void paint(Graphics monitor) {
		Image screen=createImage(WIDTH,HEIGHT);
		Graphics2D game=(Graphics2D)screen.getGraphics();
		if(state.equals("loading")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.setColor(Color.white);
			game.drawString("Loading",WIDTH/2-21,HEIGHT/2+5);
		}
		else if(state.equals("lose")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.drawImage(self.icon[DOWN],140,120,this);
			game.setColor(Color.white);
			game.drawString("You have died.",112,155);
		}
		else if(state.equals("menu")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.drawImage(self.icon[DOWN],140,95,this);
			game.setColor(Color.white);
			String[] option={"Start new game","Load game","Settings","Exit"};
			game.drawString("SPELLCRAFT",113,135);
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],110,155+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],110,155+count*15);
			}
		}
		else if(state.equals("options")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.setColor(Color.white);
			String[] toughness={"Easy","Medium","Hard"},switcher={"On","Off"},option={"Music: "+switcher[music],"Difficulty: "+toughness[difficulty],"Return to main menu"};
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],110,135+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],110,135+count*15);
			}
		}
		else if(state.equals("game")) {
			for(int j=0;j<MAPHEIGHT;j++) {
				for(int i=0;i<MAPWIDTH;i++) {
					if(map.get(currentmap).terrain[i][j]>=0)
						game.drawImage(floor[map.get(currentmap).terrain[i][j]],i*FLOORWIDTH,j*FLOORHEIGHT,this);
					else
						game.drawImage(floor[0],i*FLOORWIDTH,j*FLOORHEIGHT,this);
				}
			}
			for(int count=0;count<map.get(currentmap).population.size();count++) {
				creature me=map.get(currentmap).population.get(count);
				game.drawImage(me.icon[me.direction],me.x,me.y,this);
			}
			for(int count=0;count<map.get(currentmap).stock.size();count++) {
				item it=map.get(currentmap).stock.get(count);
				game.drawImage(it.icon,it.x,it.y,this);
			}
			game.drawImage(self.icon[self.direction],self.x,self.y,this);
			game.setColor(Color.black);
			game.fillRect(0,FIELDHEIGHT,WIDTH,HEIGHT-FIELDHEIGHT);
			game.setColor(Color.gray);
			game.fillRect(8,234,184,13);
			game.setColor(Color.red);
			game.fillRect(8,234,184*self.HP/self.maxHP,13);
			game.setColor(Color.white);
			game.drawString("HP: "+self.HP+"/"+self.maxHP,10,245);
			if(!messages.isEmpty())
				game.drawString(messages.get(0),10,260);
			game.drawImage(area,CASTX,CASTY,this);
		}
		monitor.drawImage(screen,0,0,this);
	}
	public void repaint() {
		paint(this.getGraphics());
	}
	public void keyPressed(KeyEvent ev) {
		switch(ev.getKeyCode()) {
		case KeyEvent.VK_ENTER:
			if(state.equals("menu")) {
				if(menuchoice==0)
					state="game";
				else if(menuchoice==1)
					load();
				else if(menuchoice==2) {
					state="options";
					menuchoice=0;
				}
				else if(menuchoice==3)
					System.exit(0);
			}
			else if(state.equals("options")) {
				if(menuchoice==0) {
					if(music==0)
						music=1;
					else
						music=0;
				}
				else if(menuchoice==1) {
					difficulty++;
					if(difficulty>2)
						difficulty=0;
				}
				else if(menuchoice==2) {
					state="menu";
					menuchoice=0;
				}
			}
			break;
		case 37:
		case 38:
		case 39:
		case 40:
			if(state.equals("game"))
				self.move=DIR[ev.getKeyCode()-37];
			else if(state.equals("menu")||state.equals("options")) {
				if(ev.getKeyCode()==38)
					menuchoice--;
				else if(ev.getKeyCode()==40)
					menuchoice++;
				if(menuchoice>3||(menuchoice>2&&state.equals("options")))
					menuchoice=0;
				else if(menuchoice<0) {
					if(state.equals("options"))
						menuchoice=2;
					else
						menuchoice=3;
				}
			}
			break;
		case KeyEvent.VK_A:
			if(state.equals("game"))
				self.move=LEFT;
			break;
		case ',':
			if(state.equals("game"))
				self.move=UP;
			else if(state.equals("menu")||state.equals("options")) {
				menuchoice--;
				if(menuchoice<0) {
					if(state.equals("options"))
						menuchoice=2;
					else
						menuchoice=3;
				}
			}
			break;
		case KeyEvent.VK_E:
			if(state.equals("game"))
				self.move=RIGHT;
			break;
		case KeyEvent.VK_O:
			if(state.equals("game"))
				self.move=DOWN;
			else if(state.equals("menu")||state.equals("options")) {
				menuchoice++;
				if(menuchoice>3||(menuchoice>2&&state.equals("options")))
					menuchoice=0;
			}
			break;
		}
		return;
	}
	public void keyReleased(KeyEvent ev) {
		switch(ev.getKeyCode()) {
		case 37:
		case 38:
		case 39:
		case 40:
			if(self.move==DIR[ev.getKeyCode()-37])
				self.move=-1;
			break;
		case KeyEvent.VK_A:
			if(self.move==LEFT)
				self.move=-1;
			break;
		case ',':
			if(self.move==UP)
				self.move=-1;
			break;
		case KeyEvent.VK_E:
			if(self.move==RIGHT)
				self.move=-1;
			break;
		case KeyEvent.VK_O:
			if(self.move==DOWN)
				self.move=-1;
			break;
		}
	}
	public void keyTyped(KeyEvent ev) {}public void mouseMoved(MouseEvent e) {}public void mouseClicked(MouseEvent e) {}public void mouseEntered(MouseEvent e) {}public void mouseExited(MouseEvent e) {}
}