import java.applet.Applet;
import java.awt.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class Spellcraft extends Applet implements Runnable {
//Game settings
	Thread gameThread;
	final int WIDTH=500,HEIGHT=330,FLOORWIDTH=25,FLOORHEIGHT=22,CASTHEIGHT=32;
	int state,rumble=0,time=1020,timeSpeed=10;
//Menu elements
	Image menu;
	final int MENU=0,LOAD=1,GAME=2,LOSE=3,MESSAGE=4;
	int menuChoice=0,menuColor=0,menuCount=0,loseColor=255;
//Game elements
	Font spellcraft,timeFont,gameFont;
	FontMetrics metrics,timeMetrics,gameMetrics;
	DecimalFormat format=new DecimalFormat("00");
	Image sidebar,darkness[]=new Image[10],moon,sun;
	final int UP=0,DOWN=1,LEFT=2,RIGHT=3;
	int timeCount=0,timeTarget=-1;
	Random wheel=new Random();
	Creature player,zombie,infected,necromancer;
	Magic heal,fire,zap,quake,counterspell,sunMoon,unearth,raiseDead,blast,teleport,spellbomb,freeze;
	ArrayList<String> messages=new ArrayList<String>();
//Startup
	public void init() {
		resize(WIDTH,HEIGHT);
		spellcraft=new Font("Andalus",Font.BOLD,18);
		metrics=getFontMetrics(spellcraft);
		menu=getImage("Menu.gif");
		state=MENU;
	}
	public void load() {
//Font
		timeFont=new Font("BatangChe",Font.PLAIN,12);
		timeMetrics=getFontMetrics(timeFont);
		gameFont=new Font("Andalus",Font.PLAIN,12);
		gameMetrics=getFontMetrics(gameFont);
//Images
		sidebar=getImage("Sidebar.gif");
		moon=getImage("Moon.gif");
		sun=getImage("Sun.gif");
		for(int count=0;count<10;count++)
			darkness[count]=getImage("Darkness"+count+".png");
//Magic
		freeze=new Magic("Freeze",Color.cyan,1,2);
		freeze.size=35;
		blast=new Magic("Blast",Color.red,5,20);
		blast.selfTargeting=true;
		blast.size=100;
		heal=new Magic("Heal",Color.gray,5,5);
		heal.selfTargeting=true;
		heal.size=35;
		teleport=new Magic("Teleport",Color.blue,2,10);
		teleport.selfTargeting=true;
		teleport.size=35;
		fire=new Magic("Fire",Color.orange,1,2);
		fire.size=20;
		zap=new Magic("Zap",Color.yellow,0,1);
		zap.size=5;
		zap.speed=10;
		zap.lands=false;
		spellbomb=new Magic("Spellbomb",Color.green,4,20);
		spellbomb.size=50;
		spellbomb.lands=false;
		counterspell=new Magic("Counterspell",Color.white,0,3);
		counterspell.size=20;
		counterspell.speed=20;
		counterspell.lands=false;
		quake=new Magic("Quake",new Color(128,64,0),10,30);
		quake.selfTargeting=true;
		quake.size=350;
		sunMoon=new Magic("Sun & Moon",Color.black,5,5);
		sunMoon.selfTargeting=true;
		sunMoon.size=35;
		unearth=new Magic("Unearth",Color.black,5,10);
		unearth.size=35;
		raiseDead=new Magic("Raise Dead",Color.black,50,30);
		raiseDead.size=350;
//Creatures
		player=new Creature("Player",18,19);
		player.unique=true;
		player.friendly=true;
		player.damage=0;
		player.set(30,30);
		player.learn(heal);
		player.learn(fire);
		player.learn(freeze);
		player.learn(zap);
		player.learn(teleport);
		player.learn(spellbomb);
		zombie=new Creature("Zombie",18,14);
		zombie.set(8,0);
		zombie.speed=-1;
		zombie.undead=true;
		infected=new Creature("Infected",20,25);
		infected.set(15,0);
		infected.damage=2;
		infected.undead=true;
		necromancer=new Creature("Necromancer",18,19);
		necromancer.set(8,30);
		necromancer.learn(unearth);
		necromancer.learn(raiseDead);
		necromancer.damage=0;
		necromancer.undead=true;
//Maps
		Map suckTown=new Map("Grass",30,30);
		for(int count=0;count<5;count++)
			suckTown.add(necromancer);
		Map suckCave=new Map("Rock",30,30);
		suckCave.darkness=9;
		for(int count=0;count<100;count++)
			suckCave.add(zombie);
		suckTown.add(player,250,165);
//Start
		player.setSpell=player.spellbook.get(0);
		state=GAME;
	}
//Thread controls
	public void start() {
		if(gameThread==null) {
			gameThread=new Thread(this);
			gameThread.start();
		}
	}
	public void stop() {
		if(gameThread!=null)
			gameThread=null;
	}
	public void run() {
		while(Thread.currentThread()==gameThread) {
			if(state==MENU) {
				if(menuCount++==1) {
					menuCount=0;
					menuColor=change(menuColor,1,-254,255,true);
				}
			}
			else if(state==LOAD) {
				repaint();
				load();
			}
			else if(state==GAME) {
				if(timeTarget==-1) {
					timeCount=change(timeCount,1,0,49-timeSpeed,true);
					if(timeCount==0)
						time=change(time,1,0,1439,true);
				}
				else {
					if(timeTarget==time)
						timeTarget=-1;
					else
						time=change(time,1,0,1439,true);
				}
				for(int count=0;count<player.location.casts.size();count++) {
					player.location.casts.get(count).update();
				}
				for(int count=0;count<player.location.population.size();count++) {
					player.location.population.get(count).update();
				}
				if(messages.size()>0)
					state=MESSAGE;
			}
			else if(state==MESSAGE) {
				if(messages.size()==0) {
					state=GAME;
					player.stop();
				}
			}
			repaint();
			try {
				Thread.sleep(10);
			} catch(InterruptedException e) {
			}
		}
	}
//Paint
	public void paint(Graphics monitor) {
		Image screen=createImage(WIDTH,HEIGHT);
		Graphics2D game=(Graphics2D)screen.getGraphics();
		game.setFont(spellcraft);
		if(state==MENU) {
			int color=Math.abs(menuColor)%256,inverseColor=(255-Math.abs(menuColor))%256;
			game.setColor(new Color(inverseColor,inverseColor,inverseColor));
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.drawImage(menu,0,0,this);
			game.setColor(new Color(color,color,color));
			game.fillRect(0,0,WIDTH,50);
			game.fillRect(0,HEIGHT-50,WIDTH,50);
			game.setColor(Color.cyan);
			String displays[]={"New Game","Continue","Options"};
			String display=(char)8249+" "+displays[menuChoice]+" "+(char)8250;
			int width=250-metrics.stringWidth(display)/2;
			game.drawString(display,width,310);
		}
		else if(state==LOAD) {
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			game.setColor(Color.white);
			int width=250-metrics.stringWidth("LOADING")/2;
			game.drawString("LOADING",width,170);
		}
		else if(state==LOSE) {
			loseColor=change(loseColor,-1,0,255,false);
			game.setColor(new Color(loseColor%256,loseColor%256,loseColor%256));
			int width=250-metrics.stringWidth("You have died.")/2;
			game.drawString("You have died.",width,170);
		}
		else if(state==GAME||state==MESSAGE) {
			int offX=250-player.x,offY=165-player.y;
//Map
			game.setColor(Color.black);
			game.fillRect(0,0,WIDTH,HEIGHT);
			Map map=player.location;
			for(int j=Math.max(-offY/FLOORHEIGHT,0);j<Math.min(-offY/FLOORHEIGHT+16,map.height);j++) {
				for(int i=Math.max(-offX/FLOORWIDTH,0);i<Math.min(-offX/FLOORWIDTH+20,map.width);i++) {
					game.setColor(Color.green);
					game.fillRect(25*i+offX,22*j+offY,25,22);
					game.drawImage(map.terrain[i][j].icon,25*i+offX,22*j+offY,this);
				}
			}
//Explosions
			for(int count=0;count<player.location.casts.size();count++) {
				Magic spell=player.location.casts.get(count);
				game.setColor(spell.color);
				if(spell.explosionCount==0)
					game.fillOval(spell.x-5+offX,spell.y-5+offY,10,10);
				else
					game.fillOval(spell.x+offX-spell.explosionCount,spell.y+offY-spell.explosionCount,spell.explosionCount*2,spell.explosionCount*2);
			}
//Creatures
			for(int count=0;count<map.population.size();count++) {
				Creature me=map.population.get(count);
				if(me.freezeCount>0)
					game.drawImage(me.icon[1][me.direction],250+me.left()-player.x,165+me.up()-player.y,this);
				else
					game.drawImage(me.icon[0][me.direction],250+me.left()-player.x,165+me.up()-player.y,this);
				if(me!=player) {
					game.setColor(Color.gray);
					game.fillRect(250+me.left()-player.x,157+me.up()-player.y,me.sizeX,3);
					game.setColor(Color.red);
					game.fillRect(250+me.left()-player.x,157+me.up()-player.y,me.sizeX*me.HP/me.maxHP,3);
				}
			}
//Casts
			for(int count=0;count<map.population.size();count++) {
				Creature me=map.population.get(count);
				if(me.activeSpell!=null) {
					int charge=me.activeSpell.chargeCount;
					int ratio=0;
					if(me.activeSpell.maxCharge!=0)
						ratio=charge*360/me.activeSpell.maxCharge;
					else
						ratio=360;
					if(me.activeSpell.color==Color.black) {
						game.setColor(Color.white);
						game.fillArc(me.x-12+offX,me.up()+offY-5-CASTHEIGHT,24,24,0,360);
					}
					game.setColor(me.activeSpell.color);
					game.fillArc(me.x-12+offX,me.up()+offY-5-CASTHEIGHT,24,24,0,ratio);
					game.drawImage(me.activeSpell.icon,me.x-7+offX,me.up()+offY-CASTHEIGHT,this);
				}
			}
//Darkness
			if(map.darkness==-1) {
				if(time<=330||time>=1110)
					game.drawImage(darkness[0],0,0,this);
				if(time>330&&time<390)
					game.drawImage(darkness[9-(389-time)/6],0,0,this);
				if(time<1110&&time>1050)
					game.drawImage(darkness[9-(time-1051)/6],0,0,this);
			}
			else
				game.drawImage(darkness[9-map.darkness],0,0,this);
//Sidebars
			game.drawImage(sidebar,0,0,this);
			game.drawImage(sidebar,455,0,this);
			for(int count=0;count<player.spellbook.size();count++) {
				game.setColor(player.spellbook.get(count).color);
				game.fillRect(10,10+count*30,25,25);
				game.drawImage(player.spellbook.get(count).icon,15,15+count*30,this);
				if(player.spellbook.get(count)==player.setSpell) {
					if(player.spellbook.get(count).color!=Color.red)
						game.setColor(Color.red);
					else
						game.setColor(Color.white);
					game.drawRect(10,10+count*30,24,24);
				}
					
			}
			if(time<=360||time>1080)
				game.drawImage(moon,465,10,this);
			else
				game.drawImage(sun,465,10,this);
			game.setColor(Color.lightGray);
			game.fillRect(480,45,10,275);
			game.fillRect(465,45,10,275);
			game.setColor(Color.red);
			game.fillRect(480,45,10,player.HP*275/player.maxHP);
			game.setColor(Color.blue);
			game.fillRect(465,45,10,player.MP*275/player.maxMP);
//Messages
			if(state==MESSAGE) {
				game.setColor(Color.black);
				game.fillRect(50,250,400,75);
				game.setColor(Color.white);
				ArrayList<String> messageArray=cut(messages.get(0),390);
				game.setFont(gameFont);
				for(int count=0;count<messageArray.size();count++)
					game.drawString(messageArray.get(count),55,264+count*14);
			}
		}
		monitor.setColor(Color.black);
		monitor.fillRect(0,0,WIDTH,HEIGHT);
		int screenX=0,screenY=0;
		if(rumble>0) {
			screenX=wheel.nextInt(rumble*2+1)-rumble;
			screenY=wheel.nextInt(rumble*2+1)-rumble;
			if(wheel.nextInt(10)==0)
				rumble--;
		}
		monitor.drawImage(screen,screenX,screenY,this);
	}
	public void update(Graphics monitor) {
		paint(monitor);
	}
//Keyboard input
	public boolean keyDown(Event ev,int key) {
		if(state==MENU) {
			if(key==Event.RIGHT)
				menuChoice=change(menuChoice,1,0,2,true);
			else if(key==Event.LEFT)
				menuChoice=change(menuChoice,-1,0,2,true);
			else if(key==Event.ENTER) {
				switch(menuChoice) {
				case 1:
				case 0:
					state=LOAD;
					break;
				}
			}
		}
		else if(state==GAME) {
			switch(key) {
			case 32:
				int spell=change(player.spellbook.indexOf(player.setSpell),1,0,player.spellbook.size()-1,true);
				if(player.activeSpell!=null) {
					player.activeSpell.chargeCount=0;
					player.activeSpell=null;
				}
				player.setSpell=player.spellbook.get(spell);
				break;
			case (int)'p':
				messages.add("Paused");
				break;
			case Event.UP:
			case (int)',':
				player.up=true;
				break;
			case Event.DOWN:
			case (int)'o':
				player.down=true;
				break;
			case Event.LEFT:
			case (int)'a':
				player.left=true;
				break;
			case Event.RIGHT:
			case (int)'e':
				player.right=true;
				break;
			case 49:
			case 50:
			case 51:
			case 52:
			case 53:
			case 54:
			case 55:
			case 56:
			case 57:
				if(player.spellbook.size()>=key-48) {
					player.stopSpell();
					player.setSpell=player.spellbook.get(key-49);
				}
				break;
			}
		}
		return true;
	}
	public boolean keyUp(Event ev,int key) {
		if(state==GAME) {
			switch(key) {
			case Event.UP:
			case (int)',':
				player.up=false;
				break;
			case Event.DOWN:
			case (int)'o':
				player.down=false;
				break;
			case Event.LEFT:
			case (int)'a':
				player.left=false;
				break;
			case Event.RIGHT:
			case (int)'e':
				player.right=false;
				break;
			}
		}
		else if(state==MESSAGE) {
			switch(key) {
			case 32:
				if(messages.size()>0)
					messages.remove(0);
				break;
			}
		}
		return true;
	}
//Mouse Input
	public boolean mouseDown(Event ev,int x,int y) {
		if(state==GAME) {
			if(player.MP>=player.setSpell.cost)
				player.activeSpell=player.setSpell;
		}
		return true;
	}
	public boolean mouseUp(Event ev,int x,int y) {
		if(state==GAME) {
			if(player.activeSpell!=null) {
				Magic spell=player.activeSpell;
				if(spell.selfTargeting)
					player.cast(spell,player.x,player.y);
				else
					player.cast(spell,x+player.x-250,y+player.y-165);
			}
		}
		return true;
	}
//Methods
	public ArrayList<String> cut(String input,int length) {
		StringTokenizer tokens=new StringTokenizer(input);
		ArrayList<String> outputs=new ArrayList<String>();
		String concat="";
		while(gameMetrics.stringWidth(input)>length) {
			String line=concat;
			try {
				while(gameMetrics.stringWidth(line.concat(concat=tokens.nextToken()+" "))<length)
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
		return outputs;
	}
	Image crop(Image icon,int x,int y,int width,int height) {
		return createImage((ImageProducer) new FilteredImageSource(icon.getSource(),new CropImageFilter(x,y,width,height)));
	}
	Image getImage(String image) {
		MediaTracker tracker=new MediaTracker(this);
		Image output;
		tracker.addImage(output=getImage(this.getCodeBase(),"Images/"+image),0);
		try {
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {}
		return output;
	}
	int change(int input,int change,int lower,int higher,boolean loop) {
		int output=input+change;
		if(loop) {
			if(output<lower)
				output=higher;
			if(output>higher)
				output=lower;
		}
		else {
			if(output<lower)
				output=lower;
			if(output>higher)
				output=higher;
		}
		return output;
	}
//Classes
	class Creature implements Cloneable {
//Attributes
		String name;
		boolean undead,flies,wizard,poisoned,unique,friendly;
		Map location;
		Creature target,evade;
		int direction=DOWN,x,y,sizeX,sizeY,HP,maxHP,speed=1,damage=1,MP,maxMP,provokeRange=150,persistance=50;
		ArrayList<String> description=new ArrayList<String>();
		ArrayList<Item> inventory=new ArrayList<Item>();
		ArrayList<Magic> spellbook=new ArrayList<Magic>();
//Settings
		int RecoverCount=0,walkCount=0,attackCount=0,loseCount=0,freezeCount=0;
		Image icon[][]=new Image[2][4],avatar;
		Magic setSpell,activeSpell;
		boolean up,down,left,right;
//Creating and cloning
		public Creature clone() {
			try {
				direction=wheel.nextInt(4);
				for(int count=0;count<spellbook.size();count++)
					spellbook.set(count,spellbook.get(count));
				return (Creature)super.clone();
			}
			catch(Exception e) {
				throw new InternalError(e.toString());
			}
		}
		Creature(String name,int sizeX,int sizeY) {
			this.name=name;
			this.sizeX=sizeX;
			this.sizeY=sizeY;
			Image icon=getImage("Creatures/"+name+".gif");
			try {
				for(int j=0;j<4;j++) {
					for(int i=0;i<2;i++)
						this.icon[i][j]=crop(icon,i*sizeX,j*sizeY,sizeX,sizeY);
				}
			}
			catch (Exception e) {}
			avatar=this.icon[0][DOWN];
		}
		public void set(int HP,int MP) {
			this.HP=HP;
			this.MP=MP;
			maxHP=HP;
			maxMP=MP;
		}
//AI
		public void AI() {
//Move
			if(Math.hypot(player.x-x,player.y-y)<=provokeRange) {
				if(name.equals("Necromancer"))
					evade=player;
				else
					target=player;
			}
			else if(loseCount++==persistance) {
				loseCount=0;
				target=null;
				evade=null;
			}
//Cast
			if(name.equals("Necromancer")) {
				if(activeSpell==null) {
					if(location.population.size()<50)
						activeSpell=raiseDead.clone();
					else
						activeSpell=unearth.clone();
				}
				if(activeSpell.name.equals("Raise Dead")&&(activeSpell.maxCharge-activeSpell.chargeCount)>50&&evade==player) {
					activeSpell=unearth.clone();
				}
				if(activeSpell.chargeCount==activeSpell.maxCharge) {
					if(activeSpell.name.equals("Unearth")) {
						Creature me=zombie.clone();
						boolean cast=false;
						me.x=wheel.nextInt(100)+x-50;
						me.y=wheel.nextInt(100)+y-50;
						me.location=location;
						if(me.collide()||me.offMap())
							cast=false;
						else
							cast=true;
						if(cast)
							cast(activeSpell,me.x,me.y);
					}
					else if(activeSpell.name.equals("Raise Dead"))
						cast(activeSpell,x,y);
				}
			}
//Move
			if(target==null&&evade==null) {
				if(wheel.nextInt(50)==0)
				{
					int dir=wheel.nextInt(4);
					if(dir==0)
						up=!up;
					else if(dir==1)
						down=!down;
					else if(dir==2)
						left=!left;
					else if(dir==3)
						right=!right;
				}
			}
			else {
				up=false;
				down=false;
				left=false;
				right=false;
				if(target!=null) {
					if(x<target.x)
						right=true;
					else if(x>target.x)
						left=true;
					if(y<target.y)
						down=true;
					else if(y>target.y)
						up=true;
				}
				if(evade!=null) {
					if(x<evade.x)
						left=true;
					else if(x>evade.x)
						right=true;
					if(y<evade.y)
						up=true;
					else if(y>evade.y)
						down=true;
				}
			}
		}
//Functions
		public void stop() {
			up=false;
			down=false;
			left=false;
			right=false;
		}
		public boolean offMap() {
			boolean output=false;
			if(left()<0||right()>location.width*FLOORWIDTH||up()<0||down()>location.height*FLOORHEIGHT)
				output=true;
			return output;
		}
		public boolean collide() {
			boolean output=false;
			for(int count=0;count<location.population.size();count++) {
				Creature me=location.population.get(count);
				if(collide(me))
					output=true;
			}
			return output;
		}
		public boolean collide(Creature me) {
			boolean output=false;
			if(me!=this&&left()<me.right()&&right()>me.left()&&up()<me.down()&&down()>me.up())
				output=true;
			return output;
		}
		public void learn(Magic spell) {
			spellbook.add(spell.clone());
		}
		public void changeHP(int gain) {
			HP=change(HP,gain,0,maxHP,false);
		}
		public void changeMP(int gain) {
			MP=change(MP,gain,0,maxMP,false);
		}
		public void cast(Magic spell,int x,int y) {
			if(MP>=spell.cost&&spell.chargeCount==spell.maxCharge) {
				MP-=spell.cost;
				spell.caster=this;
				spell.x=this.x;
				spell.y=this.up()-CASTHEIGHT+7;
				if(spell.lands) {
					spell.targetX=x;
					spell.targetY=y;
				}
				else {
					spell.targetX=(x-spell.x)*99;
					spell.targetY=(y-spell.y)*99;
				}
				spell.location=location;
				location.casts.add(spell.clone());
			}
			spell.chargeCount=0;
			activeSpell=null;
		}
		public void stopSpell() {
			if(activeSpell!=null)
				activeSpell.chargeCount=0;
			activeSpell=null;
		}
//Movement and position
		public void move(int direction) {
			int oldX=x,oldY=y;
			int moveIt=speed;
			boolean revert=false;
			if(speed<=0)
				moveIt=1;
			this.direction=direction;
			switch(direction) {
			case LEFT:
				if(!(left()-moveIt<=0))
					x-=moveIt;
				break;
			case RIGHT:
				if(!(right()+moveIt>=location.width*FLOORWIDTH))
					x+=moveIt;
				break;
			case UP:
				if(!(up()-moveIt<=0))
					y-=moveIt;
				break;
			case DOWN:
				if(!(down()+moveIt>=location.height*FLOORHEIGHT))
					y+=moveIt;
				break;
			}
			for(int count=0;count<location.population.size();count++) {
				Creature me=location.population.get(count);
				if(collide(me)) {
					if(friendly!=me.friendly) {
						if(attackCount++==10) {
							me.changeHP(-damage);
							attackCount=0;
						}
						if(me.attackCount++==10) {
							changeHP(-me.damage);
							me.attackCount=0;
						}
					}
					revert=true;
				}
			}
			if(revert) {
				x=oldX;
				y=oldY;
			}
		}
		public int up() {
			return y-sizeY/2;
		}
		public int down() {
			return y+sizeY/2;
		}
		public int left() {
			return x-sizeX/2;
		}
		public int right() {
			return x+sizeX/2;
		}
//Updating
		public void update() {
			if(HP<=0) {
				if(this==player)
					state=LOSE;
				else
					location.remove(this);
			}
			else {
				if(activeSpell!=null)
						activeSpell.chargeCount=change(activeSpell.chargeCount,1,0,activeSpell.maxCharge,false);
				if(RecoverCount++==20) {
					MP=change(MP,1,0,maxMP,false);
					RecoverCount=0;
				}
				if(freezeCount>0)
					freezeCount--;
				else {
					if(this!=player)
						AI();
					boolean moveIt=false;
					if(speed>0)
						moveIt=true;
					else if(walkCount++==1-speed)
						moveIt=true;
					if(moveIt) {
						if(left&&!right)
							move(LEFT);
						else if(right&&!left)
							move(RIGHT);
						if(up&&!down)
							move(UP);
						else if(down&&!up)
							move(DOWN);
						walkCount=0;
					}
				}
			}
		}
	}
	class Magic implements Cloneable {
//Attributes
		String name;
		ArrayList<String> description=new ArrayList<String>();
		Creature caster;
		Map location;
		Color color;
		boolean selfTargeting,lands=true;
		int maxCharge,speed=5,cost,size=10;
//Settings
		Image icon;
		int chargeCount=0,x,y,targetX,targetY,explosionCount=0;
//Creating and cloning
		public Magic clone() {
			try {
				return (Magic)super.clone();
			}
			catch(Exception e) {
				throw new InternalError(e.toString());
			}
		}
		Magic(String name,Color color,int charge,int cost) {
			this.name=name;
			this.color=color;
			this.cost=cost;
			maxCharge=charge*25;
			icon=getImage("Magic/"+name+".gif");
		}
		Magic(String name,String description,Color color) {
			this.name=name;
			this.color=color;
			icon=getImage("Magic/"+name+".gif");
			this.description=cut(description,166);
		}
//Effects
		public void effect() {
			if(caster.location!=null) {
				if(name.equals("Fire")) {
					damage(5,true);
				}
				else if(name.equals("Freeze")) {
					damage(1,true);
				}
				else if(name.equals("Raise Dead")) {
					for(int count=0;count<15;count++)
						location.add(zombie);
				}
				else if(name.equals("Counterspell")) {
					Magic spell=magicTarget();
					for(int count=0;count<caster.location.population.size();count++) {
						Creature me=caster.location.population.get(count);
						double distance=Math.sqrt((me.x-x)*(me.x-x)+(me.y-y)*(me.y-y));
						if(distance<size*2)
							me.stopSpell();
					}
					if(spell!=null)
						spell.location.casts.remove(spell);
				}
				else if(name.equals("Blast")) {
					rumble=10;
					damage(30,false);
				}
				else if(name.equals("Spellbomb")) {
					rumble=10;
					damage(30,true);
				}
				else if(name.equals("Quake")) {
					rumble=20;
					damage(50,false);
					caster.changeHP(-20);
				}
				else if(name.equals("Zap")) {
					damage(1,true);
				}
				else if(name.equals("Heal")) {
					caster.changeHP(10);
				}
				else if(name.equals("Teleport")) {
					Creature me=caster.clone();
					boolean loop;
					do {
						me.x=wheel.nextInt(me.location.width*FLOORWIDTH-me.sizeX)+Math.round(me.sizeX/2);
						me.y=wheel.nextInt(me.location.height*FLOORHEIGHT-me.sizeY)+Math.round(me.sizeY/2);
						if(me.collide()||me.offMap())
							loop=true;
						else
							loop=false;
					}
					while(loop);
					caster.x=me.x;
					caster.y=me.y;
				}
				else if(name.equals("Sun & Moon")) {
					if(time<=360||time>1080) {
						time=330;
						timeTarget=390;
					}
					else {
						time=1050;
						timeTarget=1110;
					}
				}
				else if(name.equals("Unearth")) {
					caster.location.add(zombie,x,y);
				}
			}
		}
		public void damage(int blast,boolean selfInjuring) {
			for(int count=0;count<caster.location.population.size();count++) {
				Creature me=caster.location.population.get(count);
				if(me==caster&&!selfInjuring);
				else {
					double distance;
					if(me.left()<x&&me.right()>x&&me.up()<y&&me.down()>y)
						distance=0;
					else
						distance=Math.sqrt(Math.pow(Math.min(Math.abs(me.left()-x),Math.abs(me.right()-x)),2)+Math.pow(Math.min(Math.abs(me.up()-y),Math.abs(me.down()-y)),2));
					if(distance<size) {
						me.changeHP((int)(-blast*(1-distance/size)));
						if(name.equals("Freeze")&&me.freezeCount<250)
							me.freezeCount=250;
						else if(name.equals("Fire"))
							me.freezeCount=0;
						else if(name.equals("Blast"))
							me.freezeCount=0;
					}
				}
			}
		}
		public Creature creatureTarget() {
			Creature me=null;
			for(int count=0;count<location.population.size();count++) {
				Creature suspect=location.population.get(count);
				if(suspect.left()<x&&suspect.right()>x&&suspect.up()<y&&suspect.down()>y)
					me=suspect;
			}
			return me;
		}
		public Magic magicTarget() {
			Magic me=null;
			for(int count=0;count<location.casts.size();count++) {
				Magic suspect=location.casts.get(count);
				if(suspect.x-5<x&&suspect.x+5>x&&suspect.y-5<y&&suspect.y+5>y)
					me=suspect;
			}
			return me;
		}
//Updating
		public void update() {
			double hypotenuse=Math.sqrt((targetX-x)*(targetX-x)+(targetY-y)*(targetY-y));
			double deltaX=speed/hypotenuse*(targetX-x),deltaY=speed/hypotenuse*(targetY-y);
			if(x<targetX)
				x=change(x,(int)deltaX,-999999,targetX,false);
			else
				x=change(x,(int)deltaX,targetX,999999,false);
			if(y<targetY)
				y=change(y,(int)deltaY,-999999,targetY,false);
			else
				y=change(y,(int)deltaY,targetY,999999,false);
			if(!lands) {
				Creature me=creatureTarget();
				if(me!=null&&me!=caster) {
					targetX=x;
					targetY=y;
				}
			}
			if(x==targetX&&y==targetY) {
				if(explosionCount==0) {
					effect();
				}
				if(size<10)
					explosionCount++;
				else
					explosionCount+=size/10;
				if(explosionCount>=size)
					location.casts.remove(this);
			}
			else if(x<-200||x>location.width*FLOORWIDTH+200||y<-200||y>location.height*FLOORHEIGHT+200) {
				location.casts.remove(this);
			}
		}
	}
	class Map {
		ArrayList<Creature> population=new ArrayList<Creature>();
		ArrayList<Item> stock=new ArrayList<Item>();
		ArrayList<Magic> casts=new ArrayList<Magic>();
		int width,height,darkness=-1;
		Floor terrain[][];
		int link[]=new int[4];
		void add(Creature oldMe,int x,int y) {
			Creature me=oldMe;
			if(!oldMe.unique)
				me=oldMe.clone();
			me.x=x;
			me.y=y;
			me.location=this;
			population.add(me);
			if(me.offMap()||me.collide())
				remove(me);
		}
		void add(Creature oldMe) {
			Creature me=oldMe;
			if(!oldMe.unique)
				me=oldMe.clone();
			boolean loop=false;
			me.location=this;
			do {
				me.x=wheel.nextInt(width*FLOORWIDTH-me.sizeX)+Math.round(me.sizeX/2);
				me.y=wheel.nextInt(height*FLOORHEIGHT-me.sizeY)+Math.round(me.sizeY/2);
				if(me.collide())
					loop=true;
				else
					loop=false;
			}
			while(loop);
			population.add(me);
			if(me.offMap()||me.collide())
				remove(me);
		}
		void add(Item it,int x,int y) {
			it.x=x;
			it.y=y;
			stock.add((Item)it.clone());
		}
		void remove(Creature me) {
			population.remove(me);
			me.location=null;
		}
		void remove(Item it) {
			stock.remove(it);
		}
		void layout(int link[]) {
			this.link=link;
		}
		Map(String ground,int x,int y) {
			width=x;
			height=y;
			terrain=new Floor[x][y];
			for(int j=0;j<y;j++) {
				for(int i=0;i<x;i++) {
					terrain[i][j]=new Floor(ground);
				}
			}
		}
		class Floor implements Cloneable {
			String name;
			boolean impassable;
			Item item;
			Image icon;
			public Floor clone() {
				try {
					return (Floor)super.clone();
				}
				catch(Exception e) {
					throw new InternalError(e.toString());
				}
			}
			Floor(String name) {
				this.name=name;
				this.icon=getImage("Floors/"+name+".gif");
			}
		}
	}
}