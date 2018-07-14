//VERSION CHANGES
//Added unlock spell
//Added impermeable objects
//Added storyline
//Made scroll bigger
//Added shock spell
//Added HP bars
//Added spellbook viewer
//Watched Jiang Xie Shen Jian
//Increased map
//Added zap spell
//Added infected ground
//Added AI
//Clears casting when changing maps
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
@SuppressWarnings("serial")
public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener,KeyListener {
	Thread thread=null;
	MediaTracker tracker=new MediaTracker(this);
	Font spellcraft;
	FontMetrics metrics;
	Image area,target,ground,book,up,down;
	Graphics2D cast;
	ArrayList<magic> grimoire=new ArrayList<magic>(),spellbook=new ArrayList<magic>();
	magic heal,freeze,unlock,kill,shock,zap;
	ArrayList<creature> bestiary=new ArrayList<creature>();
	creature self,person,zombie,infected;
	ArrayList<item> inventory=new ArrayList<item>();
	ArrayList<item> scrolls=new ArrayList<item>();
	item savespot,lock,tree;
	ArrayList<layout> map=new ArrayList<layout>();
	ArrayList<ground> floors=new ArrayList<ground>();
	int lastx,lasty,currentmap=6,difficulty=0,menuchoice=0,menucolor=0,menucolor2=0,menucolor3=0,music=0
		,spellbookcount=0;
	String state="loading",display="default";
	boolean triggered[]=new boolean[100];
	ArrayList<String> messages=new ArrayList<String>();
	ArrayList<action> cinema=new ArrayList<action>();
	final int WIDTH=300,HEIGHT=325,CASTX=201,CASTY=226,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,FIELDWIDTH=300,FIELDHEIGHT=225,MAPWIDTH=15,MAPHEIGHT=15,FLOORWIDTH=20,LEEWAY=3,TOLERANCE=3
		,FLOORHEIGHT=15,LEFT=0,UP=1,RIGHT=2,DOWN=3,BOOKX=181,BOOKY=240,UPX=181,UPY=267,DOWNX=181
		,DOWNY=294;
	public void cast(creature caster,magic spell,int x,int y) {
		ground terrain=null;
		ArrayList<creature> targets=new ArrayList<creature>();
		ArrayList<item> itemtargets=new ArrayList<item>();
		try{
			terrain=map.get(currentmap).terrain[x/FLOORWIDTH][y/FLOORHEIGHT];
		}
		catch(ArrayIndexOutOfBoundsException e) {}
		for(int count=0;count<map.get(currentmap).population.size();count++) {
			creature me=map.get(currentmap).population.get(count);
			if(me.leftx()<x&&me.rightx()>x&&me.upy()<y&&me.downy()>y)
				targets.add(me);
		}
		for(int count=0;count<map.get(currentmap).stock.size();count++) {
			item it=map.get(currentmap).stock.get(count);
			if(it.leftx()<x&&it.rightx()>x&&it.upy()<y&&it.downy()>y)
				itemtargets.add(it);
		}
		if(spell==heal)
			caster.recover(10);
		else if(spell==shock) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(5);
		}
		else if(spell==zap) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(10);
		}
		else if(spell==freeze) {
			if(terrain.ID==1)
				terrain.set(4);
			for(int count=0;count<targets.size();count++)
				targets.get(count).frozen+=200;
		}
		else if(spell==unlock) {
			for(int count=0;count<itemtargets.size();count++)
				if(itemtargets.get(count).name.equals("lock"))
					map.get(currentmap).remove(itemtargets.get(count));
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
		kill=new magic("Kill","slash.gif",1,"Powerful dark magic immediately kills one enemy on contact.");
		heal=new magic("Heal","plus.gif",0,"This low powered healing spell will allow its caster to recover some HP.");
		freeze=new magic("Freeze","flake.gif",1,"Basic freeze spell. Can immobilize enemies for a short time.");
		shock=new magic("Shock","seven.gif",1,"A standard low powered attack spell first invented to zap mosquitos.");
		unlock=new magic("Unlock","key.gif",1,"Works better than a key on standard locks!");
		zap=new magic("Zap","Z.gif",1,"A stronger version of shock, this spell does a bit more damage.");
	}
	public void creature() {
		self=new creature("player",20,20,50,0);
		person=new creature("person",20,20,20,0);
		zombie=new creature("zombie",20,20,5,1);
		zombie.speed=1;
		infected=new creature("infected",20,20,15,3);
		infected.speed=1;
	}
	public void item() {
		savespot=new item("savespot",0,59,44);
		for(int count=0;count<grimoire.size();count++) {
			scrolls.add(new item("scroll",1,20,6));
			scrolls.get(count).spell=grimoire.get(count);
		}
		lock=new item("lock",2,60,15);
		tree=new item("tree",2,25,40);
	}
	public void map() {
		try {
			int blocked[]={1,3};
			for(int count=0;count<ground.getHeight(this)/FLOORHEIGHT;count++)
				floors.add(new ground(count));
			for(int count=0;count<blocked.length;count++)
				floors.get(blocked[count]).blocked=true;
			FileInputStream in=new FileInputStream("maps.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			map.add(new layout(new int[4]));
			while(true) {
				String next=file.readLine();
				StringTokenizer chopper=new StringTokenizer(next);
				int mapcount=Integer.parseInt(chopper.nextToken());
				int links[]={Integer.parseInt(chopper.nextToken()),Integer.parseInt(chopper.nextToken())
						,Integer.parseInt(chopper.nextToken()),Integer.parseInt(chopper.nextToken())};
				map.add(new layout(links));
				for(int j=0;j<MAPHEIGHT;j++) {
					next=file.readLine();
					for(int i=0;i<MAPWIDTH;i++) {
						int ID=Integer.parseInt(next.substring(i*4,i*4+3));
						map.get(mapcount).terrain[i][j]=(ground)floors.get(ID).clone();
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
		map.get(1).add(zombie,130,150);
		map.get(1).add(zombie,60,80);
		map.get(1).add(zombie,220,100);
		map.get(6).add(scrolls.get(3),40,50);
		map.get(14).add(scrolls.get(5),150,112);
		map.get(5).add(lock,150,217);
		map.get(8).add(lock,190,7);
		map.get(11).add(tree,190,70);
		map.get(16).add(zombie,220,140);
		map.get(16).add(zombie,250,180);
		map.get(16).add(zombie,210,190);
		map.get(17).add(infected,250,127);
		map.get(18).add(zombie,120,150);
		map.get(18).add(zombie,142,40);
		map.get(18).add(zombie,231,50);
		map.get(18).add(infected,50,27);
		map.get(currentmap).add(self,110,80);
	}
	public void triggers() {
		layout place=map.get(currentmap);
		switch(currentmap) {
		case 6:
			if(place.stock.size()==0&&!triggered[6]) {
				cutscene(3);
				triggered[6]=true;
			}
			break;
		case 3:
			if(spellbook.size()==0)
				cutscene(4);
			break;
		case 1:
			if(place.population.size()==1&&!triggered[2]) {
				cutscene(2);
				triggered[2]=true;
			}
			break;
		}
	}
	public void cutscene (int ID) {
		state="cutscene";
		ArrayList <creature>scene=map.get(currentmap).population;
		switch(ID) {
		case 4:
			cinema.add(new action(1,"notify","I'd better prepare first.",1));
			cinema.add(new action(0,"notify","Good thing I have a spell scroll here.",2));
			cinema.add(new action(1,"move",self,LEFT,20));
			break;
		case 1:
			cinema.add(new action(1,"notify","Yay, another day of not having to do anything!",1));
			cinema.add(new action(0,"notify","Because I'm a wizard, that is.",2));
			cinema.add(new action(1,"notify","And wizards don't have to do anything.",2));
			cinema.add(new action(0,"notify","~CRASHING SOUNDS OFF SCREEN~",1));
			cinema.add(new action(1,"move",self,RIGHT,0));
			cinema.add(new action(1,"notify","What was that?",1));
			cinema.add(new action(2,"notify","I'd better go check it out.",2));
			break;
		case 0:
			map.get(1).population.clear();
			map.get(1).add(self,150,60);
			map.get(1).add(zombie,120,60);
			map.get(1).add(zombie,180,60);
			cinema.add(new action(0,"move",scene.get(2),DOWN,0));
			cinema.add(new action(0,"move",scene.get(1),DOWN,0));
			cinema.add(new action(1,"notify","Time to dance!",1));
			cinema.add(new action(0,"move",scene.get(2),DOWN,20));
			cinema.add(new action(0,"move",scene.get(1),DOWN,20));
			cinema.add(new action(0,"move",scene.get(0),DOWN,50));
			cinema.add(new action(1,"move",scene.get(1),LEFT,20));
			cinema.add(new action(1,"move",scene.get(2),RIGHT,20));
			cinema.add(new action(0,"notify","Oh yeah!",1));
			cinema.add(new action(1,"move",scene.get(0),LEFT,80));
			cinema.add(new action(1,"move",scene.get(1),LEFT,20));
			cinema.add(new action(1,"move",scene.get(2),LEFT,20));
			cinema.add(new action(0,"notify","Move it!",1));
			cinema.add(new action(0,"move",scene.get(2),RIGHT,40));
			cinema.add(new action(0,"move",scene.get(1),RIGHT,40));
			cinema.add(new action(0,"move",scene.get(0),RIGHT,160));
			cinema.add(new action(1,"move",scene.get(2),LEFT,20));
			cinema.add(new action(1,"move",scene.get(1),LEFT,20));
			cinema.add(new action(1,"move",scene.get(0),LEFT,80));
			cinema.add(new action(0,"move",scene.get(2),DOWN,10));
			cinema.add(new action(0,"move",scene.get(1),DOWN,10));
			cinema.add(new action(0,"move",scene.get(0),DOWN,0));
			cinema.add(new action(1,"move",scene.get(2),DOWN,10));
			cinema.add(new action(1,"move",scene.get(1),DOWN,10));
			cinema.add(new action(1,"move",scene.get(0),LEFT,0));
			cinema.add(new action(0,"move",scene.get(2),DOWN,10));
			cinema.add(new action(0,"move",scene.get(1),DOWN,10));
			cinema.add(new action(0,"move",scene.get(0),RIGHT,0));
			cinema.add(new action(1,"move",scene.get(2),LEFT,10));
			cinema.add(new action(1,"move",scene.get(1),RIGHT,10));
			cinema.add(new action(1,"move",scene.get(0),UP,40));
			cinema.add(new action(0,"move",scene.get(2),RIGHT,10));
			cinema.add(new action(0,"move",scene.get(1),LEFT,10));
			cinema.add(new action(0,"move",scene.get(0),DOWN,40));
			cinema.add(new action(1,"move",scene.get(2),DOWN,0));
			cinema.add(new action(1,"move",scene.get(1),DOWN,0));
			cinema.add(new action(1,"move",scene.get(0),DOWN,0));
			cinema.add(new action(1,"notify","Dancing time is over.",1));
			cinema.add(new action(0,"clear"));
			cinema.add(new action(0,"add",zombie,130,150));
			cinema.add(new action(0,"add",zombie,60,80));
			cinema.add(new action(0,"add",zombie,220,100));
			cinema.add(new action(0,"add",self,150,30));
			cinema.add(new action(0,"menu"));
			break;
		case 3:
			scene=map.get(1).population;
			cinema.add(new action(0,"move",self,'y',52));
			cinema.add(new action(1,"move",self,RIGHT,300));
			cinema.add(new action(0,"move",self,'x',150));
			cinema.add(new action(1,"move",self,DOWN,200));
			cinema.add(new action(0,"notify","What's this!",1));
			cinema.add(new action(2,"move",scene.get(1),RIGHT,20));
			cinema.add(new action(2,"move",scene.get(2),UP,20));
			cinema.add(new action(1,"notify","Zombies in my palace!",1));
			cinema.add(new action(0,"move",scene.get(0),RIGHT,20));
			cinema.add(new action(1,"notify","I must get rid of them.",1));
			cinema.add(new action(0,"move",scene.get(0),UP,20));
			cinema.add(new action(0,"move",scene.get(1),RIGHT,20));
			cinema.add(new action(0,"move",scene.get(2),LEFT,20));
			cinema.add(new action(0,"move",self,DOWN,50));
			break;
		case 2:
			self.direction=LEFT;
			cinema.add(new action(0,"pause",1));
			self.direction=UP;
			cinema.add(new action(1,"pause",1));
			self.direction=RIGHT;
			cinema.add(new action(0,"pause",1));
			cinema.add(new action(1,"notify","These zombies have stolen my spell scrolls!",2));
			cinema.add(new action(0,"notify","And because of my extreme forgetfullness,",2));
			cinema.add(new action(1,"notify","I've forgotten all the spells.",2));
			cinema.add(new action(0,"notify","I'll have to go get all of them back.",2));
			cinema.add(new action(2,"move",self,'x',150));
			cinema.add(new action(1,"move",self,DOWN,250));
			cinema.add(new action(0,"move",self,DOWN,100));
			cinema.add(new action(1,"notify","Wait, I still remember the unlock spell.",2));
			cinema.add(new action(2,"cast",self,unlock,150,217));
			cinema.add(new action(0,"move",self,DOWN,150));
			spellbook.add(unlock);
			break;
		}
	}
	public void init() {
		resize(WIDTH,HEIGHT);
		repaint();
		spellcraft=new Font("Times New Roman",Font.PLAIN,12);
		metrics=getFontMetrics(spellcraft);
		target=getImage("target.gif",null);
		ground=getImage("floors.gif",null);
		book=getImage("book.gif",null);
		up=getImage("up.gif",null);
		down=getImage("down.gif",null);
		magic();
		creature();
		item();
		map();
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
		int pickable,direction=DOWN;
		int x,y,sizex,sizey,count=0;
		magic spell;
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
				direction=new Random().nextInt(4);
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
		item(String name,int pickable,int sizex,int sizey) {
			this.name=name;
			this.pickable=pickable;
			this.sizex=sizex;
			this.sizey=sizey;
			icon=target;
			icon=getImage(name+".gif","Items");
		}
	}
	class creature extends item {
		int HP=50,maxHP=50,move=-1,speed=4,damage=1,lava=0,frozen=0;
		Image icon[][]=new Image[2][4];
		boolean targeted=true;
		creature(String name,int sizex,int sizey,int HP,int damage) {
			super(name,2,sizex,sizey);
			super.icon=getImage(name+".gif","Creatures");
			maxHP=HP;
			this.HP=HP;
			this.damage=damage;
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
		String name,description[]=new String[5];
		int target;
		magic(String name,String image,int target,String description) {
			this.name=name;
			icon=getImage(image,"Magic");
			this.target=target;
			grimoire.add(this);
			this.description=cut(description,166);
		}
	}
	class layout {
		ArrayList<creature> population=new ArrayList<creature>();
		ArrayList<item> stock=new ArrayList<item>();
		ground terrain[][]=new ground[MAPWIDTH][MAPHEIGHT];
		ground defaultground=floors.get(0);
		int link[]=new int[4];
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
		layout(int link[]) {
			this.link=link;
		}
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
			this.image=createImage(new FilteredImageSource(ground.getSource(),new CropImageFilter(0,ID*FLOORHEIGHT,FLOORWIDTH,FLOORHEIGHT)));
		}
	}
	class action {
		String act,notice;
		creature actor;
		boolean acting=false;
		int direction,targetx,targety,count,order,pixels,start,x,y;
		char axis=' ';
		magic spell;
		action(int order,String notify,String notice,int count) {
			this.order=order;
			act=notify;
			this.notice=notice;
			this.count=count;
		}
		action(int order,String cast,creature actor,magic spell,int x,int y) {
			this.order=order;
			this.actor=actor;
			act=cast;
			this.spell=spell;
			this.x=x;
			this.y=y;
		}
		action(int order,String act) {
			this.order=order;
			this.act=act;
		}
		action(int order,String notify,int count) {
			this.order=order;
			act=notify;
			this.count=count;
		}
		action(int order,String move,creature me,int direction,int pixels) {
			this.order=order;
			act=move;
			actor=me;
			this.direction=direction;
			targetx=direction;
			this.pixels=pixels;
			targety=pixels;
		}
		action(int order,String move,creature me,char axis,int pixels) {
			this.order=order;
			act=move;
			actor=me;
			this.axis=axis;
			this.pixels=pixels;
		}
	}
	public String[] cut(String input,int length) {
		StringTokenizer tokens=new StringTokenizer(input);
		ArrayList<String> outputs=new ArrayList<String>();
		String concat="";
		while(metrics.stringWidth(input)>length) {
			String line=concat;
			try {
				while(metrics.stringWidth(line.concat(concat=tokens.nextToken()+" "))<length)
					line=line.concat(concat);
			}
			catch(Exception e) {}
			outputs.add(line);
			input=input.substring(line.length());
		}
		outputs.add(input);
		String[] output=new String[outputs.size()];
		for(int count=0;count<outputs.size();count++)
			output[count]=outputs.get(count);
		return output;
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
		if(e.getButton()==1&&state.equals("game")) {
			if(e.getX()>=BOOKX&&e.getX()<=BOOKX+15&&e.getY()>=BOOKY&&e.getY()<=BOOKY+15) {
				if(spellbook.size()>0&&display.equals("default"))
					display="spellbook";
				else if(display.equals("spellbook"))
					display="default";
			}
			else if(e.getX()>=UPX&&e.getX()<=UPX+15&&e.getY()>=UPY&&e.getY()<=UPY+15) {
				if(display.equals("spellbook")) {
					spellbookcount++;
					if(spellbookcount>=spellbook.size())
						spellbookcount=0;
				}
			}
			else if(e.getX()>=DOWNX&&e.getX()<=DOWNX+15&&e.getY()>=DOWNY&&e.getY()<=DOWNY+15) {
				if(display.equals("spellbook")) {
					spellbookcount--;
					if(spellbookcount<0)
						spellbookcount=spellbook.size()-1;
				}
			}
		}
		else if(e.getButton()==3&&state.equals("game")) {
			int spell=match();
			if(spell>=0) {
				messages.add("Cast "+spellbook.get(spell).name);
				cast(self,spellbook.get(spell),e.getX(),e.getY());
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
	public Image getImage(String image,String source) {
		Image output;
		if(source!=null)
			tracker.addImage(output=getImage(getCodeBase(),"Images/"+source+"/"+image),0);
		else
			tracker.addImage(output=getImage(getCodeBase(),"Images/"+image),0);
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
			if(messages.size()!=0)
				count++;
			if(count>=20) {
				count=0;
				messages.remove(0);
			}
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
				if(menucolor<=10&&menucolor2<=10&&menucolor3==0)
					cutscene(0);
			}
			else if(state.equals("cutscene")) {
				if(cinema.isEmpty())
					state="game";
				else for(int counter=0;counter<cinema.size();counter++) {
					action act=cinema.get(counter);
					boolean done=false;
					if(!act.acting) {
						if(act.act.equals("move")) {
							if(act.axis==' ') {
								act.targetx=act.actor.x;
								act.targety=act.actor.y;
								if(act.direction==UP)
									act.targety-=act.pixels;
								else if(act.direction==LEFT)
									act.targetx-=act.pixels;
								else if(act.direction==DOWN)
									act.targety+=act.pixels;
								else if(act.direction==RIGHT)
									act.targetx+=act.pixels;
							}
							else {
								if(act.axis=='x') {
									act.targetx=act.pixels;
									act.targety=act.actor.y;
									if(act.targetx<act.actor.x)
										act.direction=LEFT;
									else
										act.direction=RIGHT;
								}
								else if(act.axis=='y') {
									act.targetx=act.actor.x;
									act.targety=act.pixels;
									if(act.targety<act.actor.y)
										act.direction=UP;
									else
										act.direction=DOWN;
								}
							}
							act.actor.direction=act.direction;
							act.actor.move=act.direction;
							act.start=currentmap;
						}
						else if(act.act.equals("notify")) {
							messages.clear();
							for(int countera=0;countera<act.count;countera++)
								messages.add(act.notice);
						}
						act.acting=true;
					}
					if(act.act.equals("move")) {
						move(act.actor);
						if(act.actor.direction==UP&&act.actor.y<=act.targety)
							done=true;
						else if(act.actor.direction==LEFT&&act.actor.x<=act.targetx)
							done=true;
						else if(act.actor.direction==DOWN&&act.actor.y>=act.targety)
							done=true;
						else if(act.actor.direction==RIGHT&&act.actor.x>=act.targetx)
							done=true;
						if(act.start!=currentmap)
							done=true;
						if(done)
							act.actor.move=-1;
					}
					else if(act.act.equals("notify")) {
						if(messages.isEmpty())
							done=true;
					}
					else if(act.act.equals("cast")) {
						cast(act.actor,act.spell,act.x,act.y);
						done=true;
					}
					else if(act.act.equals("add")) {
						map.get(currentmap).add(act.actor,act.targetx,act.targety);
						done=true;
					}
					else if(act.act.equals("clear")) {
						map.get(currentmap).population.clear();
						done=true;
					}
					else if(act.act.equals("menu")) {
						state="menu";
						menucolor=0;
						menucolor2=0;
						menucolor3=0;
						done=true;
					}
					else if(act.act.equals("pause")) {
						if(act.count<=0)
							done=true;
						else
							act.count--;
					}
					if(done)
						cinema.remove(counter--);
					try {
						if(cinema.get(counter+1).order!=act.order)
							counter=cinema.size()-1;
					}
					catch(IndexOutOfBoundsException e) {}
				}
			}
			else if(state.equals("game")) {
				if(self.HP<=0)
					state="lose";
				else
					triggers();
				for(int counter=0;counter<map.get(currentmap).stock.size();counter++) {
					item it=map.get(currentmap).stock.get(counter);
					if(it.pickable==1&&collide(it,self)) {
						if(it.name.equals("scroll")) {
							messages.add("Learned "+it.spell.name);
							spellbook.add(it.spell);
						}
						map.get(currentmap).remove(it);
					}
				}
				for(int counter=0;counter<map.get(currentmap).population.size();counter++) {
					Random wheel=new Random();
					creature me;
					me=map.get(currentmap).population.get(counter);
					if(me.name.equals("zombie")) {
						if(map.get(currentmap).terrain[me.grid()[0]][me.grid()[1]].ID==6) {
							me.speed=2;
							me.damage=2;
						}
						else {
							me.speed=1;
							me.damage=1;
						}
					}
					if(me.frozen>0)
						me.frozen--;
					if(me.frozen==0) {
						if(me!=self) {
							if(collide(me,self)) {
								if(wheel.nextInt(5)==0)
									self.damage(me.damage);
							}
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
					if(me.name.equals("infected"))
						map.get(currentmap).terrain[me.grid()[0]][me.grid()[1]]=floors.get(6);
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
		Random wheel=new Random();
		if(me!=self) {
			if(!me.targeted) {
				int move=wheel.nextInt(50);
				if(move<4)
					me.move=move;
				else if(move==4)
					me.move=-1;
			}
			else if(me.targeted&&wheel.nextInt(5)==0) {
				if(wheel.nextInt(2)==1) {
					if(me.rightx()<self.leftx())
						me.move=RIGHT;
					else if(me.leftx()>self.rightx())
						me.move=LEFT;
					else if(me.downy()<self.upy())
						me.move=DOWN;
					else if(me.upy()>self.downy())
						me.move=UP;
				}
				else {
					if(me.downy()<self.upy())
						me.move=DOWN;
					else if(me.upy()>self.downy())
						me.move=UP;
					else if(me.rightx()<self.leftx())
						me.move=RIGHT;
					else if(me.leftx()>self.rightx())
						me.move=LEFT;
				}
			}
		}
		if(me.move!=-1) {
			me.direction=me.move;
			int oldmap=currentmap;
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
				for(int counter=0;counter<map.get(currentmap).stock.size();counter++) {
					item it=map.get(currentmap).stock.get(counter);
					if(it.pickable==2&&collide(it,self)) {
						me.x=oldx;
						me.y=oldy;
					}
				}
				if(me.leftx()<0) {
					if(me==self) {
						map.get(currentmap).remove(me);
						currentmap=map.get(currentmap).link[LEFT];
						me.x=FLOORWIDTH*MAPWIDTH-me.sizex/2;
						map.get(currentmap).add(me,me.x,me.y);
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
				else if(me.upy()+me.sizey/2<0) {
					if(me==self) {
						map.get(currentmap).remove(me);
						currentmap=map.get(currentmap).link[UP];
						me.y=FLOORHEIGHT*MAPHEIGHT-me.sizey/2;
						map.get(currentmap).add(me,me.x,me.y);
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				if(me.rightx()/FLOORWIDTH>MAPWIDTH-1) {
					if(me==self) {
						map.get(currentmap).remove(me);
						currentmap=map.get(currentmap).link[RIGHT];
						me.x=me.sizex/2;
						map.get(currentmap).add(me,me.x,me.y);
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
				else if(me.downy()/FLOORHEIGHT>MAPHEIGHT-1) {
					if(me==self) {
						map.get(currentmap).remove(me);
						currentmap=map.get(currentmap).link[DOWN];
						me.y=me.sizey/2;
						map.get(currentmap).add(me,me.x,me.y);
					}
					else {
						me.x=oldx;
						me.y=oldy;
					}
				}
			}
			if(me==self&&currentmap!=oldmap)
				cast.drawImage(target,0,0,this);
		}
	}
	public void paint(Graphics monitor) {
		Image screen=createImage(WIDTH,HEIGHT);
		Graphics2D game=(Graphics2D)screen.getGraphics();
		game.setFont(spellcraft);
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
			game.drawString("You have died.",150-metrics.stringWidth("You have died.")/2,155);
		}
		else if(state.equals("menu")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.drawImage(self.icon[0][DOWN],140,95,this);
			game.setColor(Color.white);
			String[] option={"START NEW GAME","LOAD GAME","SETTINGS","EXIT"};
			game.drawString("SPELLCRAFT",111,135);
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],150-metrics.stringWidth(option[count])/2,155+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],150-metrics.stringWidth(option[count])/2,155+count*15);
			}
		}
		else if(state.equals("options")) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.setColor(Color.white);
			String[] toughness={"EASY","MEDIUM","HARD"},switcher={"ON","OFF"},option={"MUSIC: "+switcher[music],"DIFFICULTY: "+toughness[difficulty],"RETURN TO MAIN MENU"};
			game.setColor(Color.gray);
			for(int count=0;count<option.length;count++) {
				if(count==menuchoice) {
					game.setColor(new Color(menucolor,menucolor2,menucolor3));
					game.drawString(option[count],150-metrics.stringWidth(option[count])/2,135+count*15);
					game.setColor(Color.gray);
				}
				else
					game.drawString(option[count],150-metrics.stringWidth(option[count])/2,135+count*15);
			}
		}
		else if(state.equals("game")||state.equals("cutscene")) {
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
				if(me!=self) {
					game.setColor(Color.gray);
					game.fillRect(me.x-5,me.upy()-5,10,2);
					game.setColor(Color.red);
					game.fillRect(me.x-5,me.upy()-5,10*me.HP/me.maxHP,2);
				}
			}
			game.setColor(Color.black);
			game.fillRect(0,FIELDHEIGHT,WIDTH,HEIGHT-FIELDHEIGHT);
			if(state.equals("game")) {
				game.setColor(Color.gray);
				game.fillRect(8,234,166,13);
				game.setColor(Color.red);
				game.fillRect(8,234,166*self.HP/self.maxHP,13);
				game.setColor(Color.white);
				game.drawString("HP: "+self.HP+"/"+self.maxHP,10,245);
				game.drawImage(book,BOOKX,BOOKY,this);
				game.drawImage(up,UPX,UPY,this);
				game.drawImage(down,DOWNX,DOWNY,this);
				game.setColor(Color.white);
				if(display.equals("default")) {
					game.drawImage(area,CASTX,CASTY,this);
					if(!messages.isEmpty())
						game.drawString(messages.get(0),10,265);
				}
				else if(display.equals("spellbook")) {
					game.drawImage(spellbook.get(spellbookcount).icon,CASTX,CASTY,this);
					game.drawString(spellbook.get(spellbookcount).name,10,265);
					for(int count=0;count<spellbook.get(spellbookcount).description.length;count++)
						game.drawString(spellbook.get(spellbookcount).description[count],10,265+(metrics.getHeight()+3)*(count+1));
				}
			}
			else if(state.equals("cutscene")) {
				game.setColor(Color.white);
				if(!messages.isEmpty())
					game.drawString(messages.get(0),150-metrics.stringWidth(messages.get(0))/2,275+metrics.getHeight()/2);
			}
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
				if(menuchoice==0) {
					if(currentmap==6)
						cutscene(1);
					else
						state="game";
				}
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
				self.move=ev.getKeyCode()-37;
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
			if(self.move==ev.getKeyCode()-37)
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