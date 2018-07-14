import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Random;

public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener,KeyListener {
	Image area,target;
	magic spell[]=new magic[20];
	Image floor[]=new Image[20];
	Graphics2D cast;
	Thread thread=null;
	int lastx,lasty,spells=8,currentmap=1,tolerance=4,castingx,castingy;
	float[] test=new float[6];
	character self,person,zombie;
	String state="loading",casting;
	int blocked[]={1,3,5};
	layout map[]=new layout[50];
	final int WIDTH=300,HEIGHT=325,CASTX=201,CASTY=226,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,FIELDWIDTH=300,FIELDHEIGHT=225,MAPWIDTH=15,MAPHEIGHT=15,FLOORWIDTH=20,LEEWAY=3
		,FLOORHEIGHT=15,RIGHT=0,DOWN=1,LEFT=2,UP=3;
	MediaTracker tracker=new MediaTracker(this);
	final int DIR[]={LEFT,UP,RIGHT,DOWN};
	public void effect(character target,int spell) {
		switch(spell){
		case 0:
			target.HP+=5;
			if(target.HP>target.maxHP)
				target.HP=target.maxHP;
			break;
		case 1:
			target.x=castingx-target.size/2;
			target.y=castingy-target.size/2;
			target.direction=DOWN;
			break;
		case 2:
			target.HP+=50;
			if(target.HP>target.maxHP)
				target.HP=target.maxHP;
			break;
		case 3:
			target.speed+=2;
			break;
		case 4:
			try{
				if(map[currentmap].terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]==1)
					map[currentmap].terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]=4;
			}
			catch(ArrayIndexOutOfBoundsException e) {}
			break;
		case 5:
			try{
				if(map[currentmap].terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]==5)
					map[currentmap].terrain[castingx/FLOORWIDTH][castingy/FLOORHEIGHT]=0;
			}
			catch(ArrayIndexOutOfBoundsException e) {}
			break;
		case 6:
			for(int count=0;count<map[currentmap].characters;count++)
				map[currentmap].population[count].HP=0;
			break;
		case 7:
			for(int count=0;count<map[currentmap].characters;count++)
				if(map[currentmap].population[count].x<=castingx&&map[currentmap].population[count].x+map[currentmap].population[count].size>=castingx&&map[currentmap].population[count].y<=castingy&&map[currentmap].population[count].y+map[currentmap].population[count].size>=castingy)
					map[currentmap].population[count].HP=0;
			break;
		}
	}
	public void init() {
		repaint();
		spell[0]=new magic("Heal","X.gif",0);
		spell[1]=new magic("Teleport","circle.gif",0);
		spell[2]=new magic("Super Heal","complex.gif",0);
		spell[3]=new magic("Speed Up","hourglass.gif",0);
		spell[4]=new magic("Freeze","flake.gif",1);
		spell[5]=new magic("Unlock","key.gif",1);
		spell[6]=new magic("Apocalypse","moon.gif",0);
		spell[7]=new magic("Kill","slash.gif",1);
		resize(WIDTH,HEIGHT);
		try {
			FileInputStream in=new FileInputStream("maps.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			while(true) {
				String next=file.readLine();
				int mapcount=convert(next);
				map[mapcount]=new layout(mapcount);
				for(int j=0;j<MAPHEIGHT;j++) {
					next=file.readLine();
					for(int i=0;i<MAPWIDTH;i++) {
						map[mapcount].terrain[i][j]=convert(next.substring(i*4,i*4+3));
					}
				}
				file.readLine();
			}
		}
		catch(IOException e) {
			System.exit(0);
		}
		catch(Exception e) {}
		target=getImage("target.gif");
		self=new character("player");
		person=new character("person");
		zombie=new character("zombie");
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
		state="game";
	}
	public void populate() {
		map[7].add(zombie,120,85);
		map[7].add(zombie,160,125);
		map[7].add(zombie,120,125);
		map[7].add(zombie,160,85);
	}
	class magic {
		Image icon;
		String name;
		int target;
		magic(String name,String image,int target) {
			this.name=name;
			icon=getImage(image);
			this.target=target;
		}
	}
	class character {
		int HP=50,maxHP=50,x=140,y=105,move=-1,speed=2,direction=DOWN,size=20,damage=1;
		Image icon[]=new Image[4];
		String name;
		character(String image) {
			this.name=image;
			for(int count=0;count<4;count++) {
				icon[count]=createImage(new FilteredImageSource(getImage(image+".gif").getSource(),new CropImageFilter(0,count*20,20,20)));
				tracker.addImage(icon[count],0);
				try {
					tracker.waitForID(0);
				}
				catch (InterruptedException e) {}
			}
		}
		public int gridx() {
			return (x+size/2)/FLOORWIDTH;
		}
		public int gridy() {
			return (y+size/2)/FLOORHEIGHT;
		}
	}
	public int bigger(int a,int b) {
		if(a>b)
			return a;
		else
			return b;
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
	public class layout {
		int ID,characters=0;
		character population[]=new character[20];
		int terrain[][]=new int[MAPWIDTH][MAPHEIGHT];
		void add(character me,int x,int y) {
			population[characters]=new character(me.name);
			population[characters].x=x;
			population[characters++].y=y;
		}
		void remove(character me) {
			for(int count=0;count<characters;count++) {
				if(population[count]==me) {
					for(int counter=count;counter<characters-1;counter++) {
						population[counter]=population[counter+1];
					}
					population[--characters]=null;
					count=characters;
				}
			}
		}
		layout(int ID) {
			this.ID=ID;
		}
	}
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==3) {
			int spell=match();
			castingx=e.getX();
			castingy=e.getY();
			if(spell>=0) {
				casting=this.spell[spell].name;
				effect(self,spell);
			}
			else if(spell==-2)
				casting="failed";
			cast.drawImage(target,0,0,this);
			cast.setColor(Color.red);
		}
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
	public float compare(Image a,Image b) {
		try {
			int pixelsOne[]=new int[CASTWIDTH*CASTHEIGHT];
			PixelGrabber grabber=new PixelGrabber(a,0,0,CASTWIDTH,CASTHEIGHT,pixelsOne,0,CASTWIDTH);
			grabber.grabPixels();
			int pixelsTwo[]=new int[CASTWIDTH*CASTHEIGHT];
			PixelGrabber grabber2=new PixelGrabber(b,0,0,CASTWIDTH,CASTHEIGHT,pixelsTwo,0,CASTWIDTH);
			grabber2.grabPixels();
//			pixelsOne=glow(pixelsOne);
			pixelsTwo=glow(pixelsTwo,tolerance);
			int pixels[]=pixelsTwo;
			/*for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				try {
					cast.setColor(new Color(20*((pixels[i]+12-RED)),0,0));
				}
				catch(Exception e) {
					cast.setColor(Color.white);
				}
				cast.drawLine(i%CASTWIDTH+1,i/CASTWIDTH+1,i%CASTWIDTH+1,i/CASTWIDTH+1);
			}*/
			float similarity=0,redpixels=0;
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixelsOne[i]==RED) {
					if(pixelsTwo[i]<=RED&&pixelsTwo[i]>RED-10) {
						similarity+=1.0/Math.pow(1+RED-pixelsTwo[i],2);
					}
					else
						similarity--;
				}
				if(pixelsTwo[i]==RED) {
					redpixels++;
//					if(pixelsTwo[i]>RED||pixelsTwo[i]<=RED-10)
//						similarity--;
				}
			}
			if(redpixels==0)
				return -1;
			return similarity/redpixels;
		}
		catch(InterruptedException e) {
			return 0;
		}
	}
	public int match() {
		for(int count=0;count<spells;count++) {
			float similarity=compare(area,spell[count].icon);
//			test[count]=similarity;
			if(count==6)
				test[0]=similarity;
			else if(count<6)
				test[0]=0;
			if(similarity>=.7) {
				return count;
			}
			else if(similarity==-1)
				return -1;
		}
		return -2;
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
			game.drawImage(self.icon[DOWN],140,120,this);
			game.setColor(Color.white);
			game.drawString("You have died.",112,155);
		}
		else if(state.equals("game")) {
			for(int j=0;j<MAPHEIGHT;j++) {
				for(int i=0;i<MAPWIDTH;i++) {
					if(map[currentmap].terrain[i][j]>=0)
						game.drawImage(floor[map[currentmap].terrain[i][j]],i*FLOORWIDTH,j*FLOORHEIGHT,this);
					else
						game.drawImage(floor[0],i*FLOORWIDTH,j*FLOORHEIGHT,this);
				}
			}
			for(int count=0;count<map[currentmap].characters;count++) {
				game.drawImage(map[currentmap].population[count].icon[map[currentmap].population[count].direction],map[currentmap].population[count].x,map[currentmap].population[count].y,this);
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
			if(casting!=null)
				game.drawString("Cast "+casting,10,260);
/*			for(int count=0;count<1;count++) {
				game.drawString(test[count]+"",10,275+count*15);
			}*/
			game.drawImage(area,CASTX,CASTY,this);
		}
		monitor.drawImage(screen,0,0,this);
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
			if(self.HP<=0)
				state="lose";
			if(casting!=null)
				count++;
			if(count>=20) {
				casting=null;
				count=0;
			}
			for(int counter=0;counter<map[currentmap].characters;counter++) {
				Random wheel=new Random();
				character me=map[currentmap].population[counter];
				if(me.HP<=0)
					map[currentmap].remove(me);
				if(me.x<self.x+self.size&&me.x+me.size>self.x&&me.y<self.y+self.size&&me.y+me.size>self.y) {
					if(wheel.nextInt(5)==0)
						self.HP-=me.damage;
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
								if(contain(map[currentmap].terrain[(me.x+LEEWAY)/FLOORWIDTH+countera][(me.y+me.size/2)/FLOORHEIGHT],blocked))
									me.y=oldy;
							}
							if(contain(map[currentmap].terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
								me.y=oldy;
						}
						else if(me.move==DOWN) {
							me.y+=me.speed;
							for(int countera=0;countera<=(me.size-2*LEEWAY)/FLOORWIDTH;countera++) {
								if(contain(map[currentmap].terrain[(me.x+LEEWAY)/FLOORWIDTH+countera][(me.y+me.size)/FLOORHEIGHT],blocked))
									me.y=oldy;
							}
							if(contain(map[currentmap].terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT],blocked))
								me.y=oldy;
						}
						else if(me.move==LEFT) {
							me.x-=me.speed;
							for(int countera=0;countera<=(me.size-me.size/2)/FLOORHEIGHT;countera++) {
								if(contain(map[currentmap].terrain[(me.x+LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT-countera],blocked))
									me.x=oldx;
							}
							if(contain(map[currentmap].terrain[(me.x+LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
								me.x=oldx;
						}
						else if(me.move==RIGHT) {
							me.x+=me.speed;
							for(int countera=0;countera<=(me.size-me.size/2)/FLOORHEIGHT;countera++) {
								if(contain(map[currentmap].terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size)/FLOORHEIGHT-countera],blocked))
									me.x=oldx;
							}
							if(contain(map[currentmap].terrain[(me.x+me.size-LEEWAY)/FLOORWIDTH][(me.y+me.size/2)/FLOORHEIGHT],blocked))
								me.x=oldx;
						}
						if(me.x<0||me.y+me.size/4<0) {
/*							if(map[currentmap].terrain[(oldx+me.size/2)/FLOORWIDTH][(oldy+me.size/2)/FLOORHEIGHT]<0) {
								map[currentmap].remove(me);
								currentmap=Math.abs(map[currentmap].terrain[(oldx+me.size/2)/FLOORWIDTH][(oldy+me.size/2)/FLOORHEIGHT]);
								map[currentmap].add(me,);
								if((oldx+me.size/2)/FLOORWIDTH==0)
									me.x=FLOORWIDTH*MAPWIDTH-me.size;
								else if((oldy+me.size/2)/FLOORHEIGHT==0)
									me.y=FLOORHEIGHT*MAPHEIGHT-me.size;
								map[currentmap].add(me,me.x,me.y);
							}
							else {*/
							me.x=oldx;
							me.y=oldy;
						}
					}
					catch(ArrayIndexOutOfBoundsException e) {
/*						if(map[currentmap].terrain[(oldx+me.size/2)/FLOORWIDTH][(oldy+me.size/2)/FLOORHEIGHT]<0) {
							map[currentmap].remove(me);
							currentmap=Math.abs(map[currentmap].terrain[(oldx+me.size/2)/FLOORWIDTH][(oldy+me.size/2)/FLOORHEIGHT]);
							if((oldx+me.size/2)/FLOORWIDTH==MAPWIDTH-1)
								me.x=0;
							else if((oldy+me.size/2)/FLOORHEIGHT==MAPHEIGHT-1)
								me.y=0;
							map[currentmap].add(me,me.x,me.y);
						}
						else {*/
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
							if(contain(map[currentmap].terrain[(self.x+LEEWAY)/FLOORWIDTH+counter][(self.y+self.size/2)/FLOORHEIGHT],blocked))
								self.y=oldy;
						}
						if(contain(map[currentmap].terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
							self.y=oldy;
					}
					else if(self.move==DOWN) {
						self.y+=self.speed;
						for(int counter=0;counter<=(self.size-2*LEEWAY)/FLOORWIDTH;counter++) {
							if(contain(map[currentmap].terrain[(self.x+LEEWAY)/FLOORWIDTH+counter][(self.y+self.size)/FLOORHEIGHT],blocked))
								self.y=oldy;
						}
						if(contain(map[currentmap].terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT],blocked))
							self.y=oldy;
					}
					else if(self.move==LEFT) {
						self.x-=self.speed;
						for(int counter=0;counter<=(self.size-self.size/2)/FLOORHEIGHT;counter++) {
							if(contain(map[currentmap].terrain[(self.x+LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT-counter],blocked))
								self.x=oldx;
						}
						if(contain(map[currentmap].terrain[(self.x+LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
							self.x=oldx;
					}
					else if(self.move==RIGHT) {
						self.x+=self.speed;
						for(int counter=0;counter<=(self.size-self.size/2)/FLOORHEIGHT;counter++) {
							if(contain(map[currentmap].terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size)/FLOORHEIGHT-counter],blocked))
								self.x=oldx;
						}
						if(contain(map[currentmap].terrain[(self.x+self.size-LEEWAY)/FLOORWIDTH][(self.y+self.size/2)/FLOORHEIGHT],blocked))
							self.x=oldx;
					}
					if(self.x<0||self.y+self.size/4<0) {
						if(map[currentmap].terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]<0) {
							currentmap=Math.abs(map[currentmap].terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]);
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
					if(map[currentmap].terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]<0) {
						currentmap=Math.abs(map[currentmap].terrain[(oldx+self.size/2)/FLOORWIDTH][(oldy+self.size/2)/FLOORHEIGHT]);
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
			if(map[currentmap].terrain[self.gridx()][self.gridy()]==2) {
				lava++;
				if(lava%5==0) {
					self.HP-=1;
				}
			}
			else
				lava=0;
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
	public void repaint() {
		paint(this.getGraphics());
	}
	public void keyPressed(KeyEvent ev) {
		switch(ev.getKeyCode()) {
		case 37:
		case 38:
		case 39:
		case 40:
			self.move=DIR[ev.getKeyCode()-37];
			break;
		}
		return;
	}
	public void keyTyped(KeyEvent ev) {}
	public void keyReleased(KeyEvent ev) {
		switch(ev.getKeyCode()) {
		case 37:
		case 38:
		case 39:
		case 40:
			if(self.move==DIR[ev.getKeyCode()-37])
				self.move=-1;
			break;
		}
	}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}