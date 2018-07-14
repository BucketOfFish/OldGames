//VERSION CHANGES
//Changed X-Y system
//Simplified moving and collision detection
//Changed things to ArrayLists
//Added ground class
//Casting now passes grid values to function
//Removed convert function
//Added creature freezing
//Suckier graphics
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
	Image area,target;
	Graphics2D cast;
	ArrayList<magic> grimoire=new ArrayList<magic>(),spellbook=new ArrayList<magic>();
	magic heal,freeze,unlock,kill;
	ArrayList<creature> bestiary=new ArrayList<creature>();
	creature self,person,zombie;
	ArrayList<item> inventory=new ArrayList<item>();
	item savespot;
	ArrayList<layout> map=new ArrayList<layout>();
	ArrayList<ground> floors=new ArrayList<ground>();
	int lastx,lasty,currentmap=1,difficulty=0,menuchoice=0,menucolor=0,menucolor2=0,menucolor3=0,music=0;
	String state="loading";
	ArrayList<String> messages=new ArrayList<String>();
	final int WIDTH=300,HEIGHT=325,CASTX=201,CASTY=226,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,FIELDWIDTH=300,FIELDHEIGHT=225,MAPWIDTH=15,MAPHEIGHT=15,FLOORWIDTH=20,LEEWAY=3,TOLERANCE=3
		,FLOORHEIGHT=15,RIGHT=0,DOWN=1,LEFT=2,UP=3;
	final int DIR[]={LEFT,UP,RIGHT,DOWN};
	public void effect(creature caster,magic spell,int x,int y) {
		ground terrain=null;
		ArrayList<creature> targets=new ArrayList<creature>();
		try{
			terrain=map.get(currentmap).terrain[x/FLOORWIDTH][y/FLOORHEIGHT];
		}
		catch(ArrayIndexOutOfBoundsException e) {}
		for(int count=0;count<map.get(currentmap).population.size();count++) {
			creature me=map.get(currentmap).population.get(count);
			if(me.leftx()<x&&me.rightx()>x&&me.upy()<y&&me.downy()>y)
				targets.add(me);
		}
		if(spell==heal)
			caster.recover(10);
		else if(spell==freeze) {
			if(terrain.ID==1)
				terrain.set(4);
			for(int count=0;count<targets.size();count++)
				targets.get(count).frozen+=200;
		}
		else if(spell==unlock) {
			if(terrain.ID==5)
				terrain.set(0);
		}
		else if(spell==kill) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(targets.get(count).HP);
		}
	}
	public void action() {
		for(int count=0;count<map.get(currentmap).stock.size();count++) {
			item it=map.get(currentmap).stock.get(count);
			if(it.name.equals("savespot")) {
				save();
				messages.add("Game saved");
			}
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
		self=new creature("player",20,20);
		person=new creature("person",20,20);
		zombie=new creature("zombie",20,20);
		zombie.speed=1;
	}
	public void item() {
		savespot=new item("savespot",false,59,44);
	}
	public void map() {
		try {
			int blocked[]={1,3,5};
			for(int count=0;count<getImage("floors.gif").getHeight(this)/FLOORHEIGHT;count++)
				floors.add(new ground(count));
			for(int count=0;count<blocked.length;count++)
				floors.get(blocked[count]).blocked=true;
			FileInputStream in=new FileInputStream("maps.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			map.add(new layout());
			while(true) {
				String next=file.readLine();
				int mapcount=Integer.parseInt(next);
				map.add(new layout());
				for(int j=0;j<MAPHEIGHT;j++) {
					next=file.readLine();
					for(int i=0;i<MAPWIDTH;i++) {
						int ID=Integer.parseInt(next.substring(i*4,i*4+3));
						if(ID>=0)
							map.get(mapcount).terrain[i][j]=(ground)floors.get(ID).clone();
						else {
							map.get(mapcount).terrain[i][j]=(ground)map.get(mapcount).defaultground.clone();
							map.get(mapcount).terrain[i][j].ID=ID;
						}
					}
				}
				file.readLine();
			}
		}
		catch(IOException e) {
			System.exit(0);
		}
		catch(Exception e) {}
	}
	public void populate() {
		map.get(currentmap).add(self,150,100);
		map.get(4).add(zombie,140,125);
		map.get(2).add(savespot,140,105);
		map.get(7).add(zombie,120,85);
		map.get(7).add(zombie,160,125);
		map.get(7).add(zombie,120,125);
		map.get(7).add(zombie,160,85);
		Random wheel=new Random();
		for(int count=0;count<10;count++)
			map.get(8).add(zombie,wheel.nextInt(240)+30,wheel.nextInt(170)+15);
		for(int count=0;count<50;count++)
			map.get(9).add(zombie,wheel.nextInt(240)+30,wheel.nextInt(170)+15);
	}
	public void init() {
		resize(WIDTH,HEIGHT);
		repaint();
		target=getImage("target.gif");
		magic();
		creature();
		item();
		map();
		populate();
		self.x=150;
		self.y=100;
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
		public int[] grid() {
			int output[]={(x)/FLOORWIDTH,(y)/FLOORHEIGHT};
			return output;
		}
		public int[] standing(int allowance) {
			int output[]={(leftx()-allowance)/FLOORWIDTH,(rightx()+allowance)/FLOORWIDTH,(upy()-allowance*3/4+sizey/4)/FLOORHEIGHT,(downy()+allowance*3/4)/FLOORHEIGHT};
			return output;
		}
		public Object clone() {
			try {
				return super.clone();
			}
			catch(Exception e) {
				throw new InternalError(e.toString());
			}
		}
		public int upy() {
			return y-sizey/2;
		}
		public int downy() {
			return y+sizey/2;
		}
		public int leftx() {
			return x-sizex/2;
		}
		public int rightx() {
			return x+sizex/2;
		}
		public void setsize(int sizex,int sizey) {
			this.sizex=sizex;
			this.sizey=sizey;
		}
		item(String name,boolean pickable,int sizex,int sizey) {
			this.name=name;
			this.pickable=pickable;
			this.sizex=sizex;
			this.sizey=sizey;
			icon=target;
			icon=getImage(name+".gif");
		}
	}
	class creature extends item {
		int HP=50,maxHP=50,move=-1,speed=4,direction=DOWN,damage=1,lava=0,frozen=0;
		Image icon[][]=new Image[2][4];
		creature(String name,int sizex,int sizey) {
			super(name,false,sizex,sizey);
			try {
				for(int j=0;j<4;j++) {
					for(int i=0;i<2;i++) {
						icon[i][j]=createImage(new FilteredImageSource(super.icon.getSource(),new CropImageFilter(i*sizex,j*sizey,sizex,sizey)));
						tracker.addImage(icon[i][j],0);
						tracker.waitForID(0);
					}
				}
			}
			catch (Exception e) {}
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
		ground terrain[][]=new ground[MAPWIDTH][MAPHEIGHT];
		ground defaultground=floors.get(0);
		void add(creature me,int x,int y) {
			me.x=x;
			me.y=y;
			if(me==self)
				population.add(self);
			else
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
	class ground implements Cloneable {
		int ID;
		boolean blocked=false;
		Image image;
		public void set(int ID) {
			this.ID=ID;
			this.image=floors.get(ID).image;
			this.blocked=floors.get(ID).blocked;
		}
		public Object clone() {
			try {
				return super.clone();
			}
			catch(Exception e) {
				throw new InternalError(e.toString());
			}
		}
		ground(int ID) {
			this.ID=ID;
			this.image=createImage(new FilteredImageSource(getImage("floors.gif").getSource(),new CropImageFilter(0,ID*FLOORHEIGHT,FLOORWIDTH,FLOORHEIGHT)));
		}
	}
	public boolean collide(Object c, Object d) {
		item a=(item)c,b=(item)d;
		if(a.leftx()<=b.rightx()&&a.rightx()>=b.leftx()&&a.downy()>=b.upy()&&a.upy()<=b.downy())
			return true;
		else
			return false;
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
			if(spell>=0) {
				messages.add("Cast "+spellbook.get(spell).name);
				effect(self,spellbook.get(spell),e.getX(),e.getY());
			}
			else if(spell==-2)
				messages.add("Casting failed");
			else if(spell==-1)
				action();
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
			boolean empty=true;
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixelsOne[i]==RED) {
					empty=false;
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
			if(empty)
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
			currentmap=Integer.parseInt(file.readLine());
			map.get(currentmap).add(self,Integer.parseInt(file.readLine()),Integer.parseInt(file.readLine()));
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
		int count=0;
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
					if(it.pickable&&collide(it,self))
						map.get(currentmap).remove(it);
				}
				for(int counter=0;counter<map.get(currentmap).population.size();counter++) {
					Random wheel=new Random();
					creature me;
					me=map.get(currentmap).population.get(counter);
					if(me.frozen>0)
						me.frozen--;
					if(me.frozen==0) {
						if(me!=self&&collide(me,self)) {
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
						move(me);
					}
					for(int j=me.standing(0)[2];j<me.standing(0)[3];j++) {
						for(int i=me.standing(0)[0];i<me.standing(0)[1];i++) {
							if(map.get(currentmap).terrain[i][j].ID==2) {
								me.lava++;
								if(me.lava%5==0) {
									me.HP-=1;
								}
							}
							else
								me.lava=0;
						}
					}
				}
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
	public void move(creature me) {
		if(me.move!=-1) {
			me.direction=me.move;
			int oldx=me.x,oldy=me.y;
			try {
				if(me.move==UP)
					me.y-=me.speed;
				else if(me.move==DOWN)
					me.y+=me.speed;
				else if(me.move==LEFT)
					me.x-=me.speed;
				else if(me.move==RIGHT)
					me.x+=me.speed;
				for(int j=me.standing(-TOLERANCE)[2];j<=me.standing(-TOLERANCE)[3];j++) {
					for(int i=me.standing(-TOLERANCE)[0];i<=me.standing(-TOLERANCE)[1];i++) {
						if(map.get(currentmap).terrain[i][j].blocked) {
							me.x=oldx;
							me.y=oldy;
						}
					}
				}
				if(me.leftx()<0||me.upy()+me.sizey/2<0) {
					if(me==self) {
						if(map.get(currentmap).terrain[(oldx)/FLOORWIDTH][(oldy)/FLOORHEIGHT].ID<0) {
							map.get(currentmap).remove(me);
							currentmap=Math.abs(map.get(currentmap).terrain[(oldx)/FLOORWIDTH][(oldy)/FLOORHEIGHT].ID);
							if((oldx)/FLOORWIDTH==0)
								me.x=FLOORWIDTH*MAPWIDTH-me.sizex/2;
							else if((oldy)/FLOORHEIGHT==0)
								me.y=FLOORHEIGHT*MAPHEIGHT-me.sizey/2;
							map.get(currentmap).add(me,me.x,me.y);
						}
						else {
							me.x=oldx;
							me.y=oldy;
						}
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				if(me==self) {
					if(map.get(currentmap).terrain[(oldx)/FLOORWIDTH][(oldy)/FLOORHEIGHT].ID<0) {
						map.get(currentmap).remove(me);
						currentmap=Math.abs(map.get(currentmap).terrain[(oldx)/FLOORWIDTH][(oldy)/FLOORHEIGHT].ID);
						if((oldx)/FLOORWIDTH==MAPWIDTH-1)
							me.x=me.sizex/2;
						else if((oldy)/FLOORHEIGHT==MAPHEIGHT-1)
							me.y=me.sizey/2;
						map.get(currentmap).add(me,me.x,me.y);
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
				else {
					me.x=oldx;
					me.y=oldy;
				}
			}
		}
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
			game.drawImage(self.icon[0][DOWN],140,120,this);
			game.setColor(Color.white);
			game.drawString("You have died.",112,155);
		}
		else if(state.equals("menu")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.drawImage(self.icon[0][DOWN],140,95,this);
			game.setColor(Color.white);
			int position[]={97,116,120,137};
			String[] option={"START NEW GAME","LOAD GAME","SETTINGS","EXIT"};
			game.drawString("SPELLCRAFT",111,135);
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],position[count],155+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],position[count],155+count*15);
			}
		}
		else if(state.equals("options")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.setColor(Color.white);
			int position[]={123,110,92};
			String[] toughness={"EASY","MEDIUM","HARD"},switcher={"ON","OFF"},option={"MUSIC: "+switcher[music],"DIFFICULTY: "+toughness[difficulty],"RETURN TO MAIN MENU"};
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],position[count],135+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],position[count],135+count*15);
			}
		}
		else if(state.equals("game")) {
			for(int j=0;j<MAPHEIGHT;j++) {
				for(int i=0;i<MAPWIDTH;i++) {
					game.drawImage(map.get(currentmap).terrain[i][j].image,i*FLOORWIDTH,j*FLOORHEIGHT,this);
				}
			}
			for(int count=0;count<map.get(currentmap).stock.size();count++) {
				item it=map.get(currentmap).stock.get(count);
				game.drawImage(it.icon,it.leftx(),it.upy(),this);
			}
			for(int count=0;count<map.get(currentmap).population.size();count++) {
				creature me=map.get(currentmap).population.get(count);
				if(me.frozen!=0)
					game.drawImage(me.icon[1][me.direction],me.leftx(),me.upy(),this);
				else
					game.drawImage(me.icon[0][me.direction],me.leftx(),me.upy(),this);
			}
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