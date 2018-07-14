import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener,KeyListener {
	Image area,target;
	magic spell[]=new magic[20];
	Image floor[]=new Image[20];
	Graphics2D cast;
	Thread thread=null;
	int lastx,lasty,spells=4,currentmap;
	character self;
	String state="loading",casting;
	layout map[]=new layout[50];
	final int WIDTH=300,HEIGHT=325,CASTX=201,CASTY=226,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,LIGHTRED=-60000,FIELDWIDTH=300,FIELDHEIGHT=225,PINK=-50000,MAPWIDTH=15,MAPHEIGHT=15,FLOORWIDTH=20
		,FLOORHEIGHT=15,RIGHT=0,DOWN=1,LEFT=2,UP=3;
	final int DIR[]={LEFT,UP,RIGHT,DOWN};
	public void init() {
		repaint();
		spell[0]=new magic("Heal","X.gif");
		spell[1]=new magic("Teleport","circle.gif");
		spell[2]=new magic("Super Heal","complex.gif");
		spell[3]=new magic("Speed Up","hourglass.gif");
		resize(WIDTH,HEIGHT);
		try {
			FileInputStream in=new FileInputStream("maps.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			String next=file.readLine();
			int mapcount=0;
			map[mapcount]=new layout(convert(next));
			for(int j=0;j<MAPHEIGHT;j++) {
				next=file.readLine();
				for(int i=0;i<MAPWIDTH;i++) {
					map[mapcount].terrain[i][j]=convert(next.substring(i*4,i*4+3));
				}
			}
			in.close();
		}
		catch(IOException e) {
			System.exit(0);
		}
		target=getImage("target.gif");
		self=new character("player");
		for(int count=0;count<getImage("floors.gif").getHeight(this)/FLOORHEIGHT;count++) {
			floor[count]=createImage(new FilteredImageSource(getImage("floors.gif").getSource(),new CropImageFilter(0,count*FLOORHEIGHT,FLOORWIDTH,FLOORHEIGHT)));
		}
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
	class magic {
		Image icon;
		String name;
		magic(String name, String image) {
			this.name=name;
			icon=getImage(image);
		}
	}
	class character {
		int HP=20,maxHP=100,x=140,y=105,move=-1,speed=2,direction=DOWN;
		Image icon[]=new Image[4];
		character(String image) {
			for(int count=0;count<4;count++) {
				icon[count]=createImage(new FilteredImageSource(getImage(image+".gif").getSource(),new CropImageFilter(0,count*20,20,20)));
			}
		}
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
	public void effect(character target,int spell) {
		switch(spell){
		case 0:
			target.HP+=5;
			if(target.HP>target.maxHP)
				target.HP=target.maxHP;
			break;
		case 1:
			target.x=140;
			target.y=105;
			target.direction=DOWN;
			break;
		case 2:
			target.maxHP+=50;
			target.HP+=50;
			break;
		case 3:
			target.speed+=2;
			break;
		}
	}
	public class layout {
		int ID;
		int terrain[][]=new int[MAPWIDTH][MAPHEIGHT];
		layout(int ID) {
			this.ID=ID;
		}
	}
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==3) {
			int spell=match();
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
			pixelsOne=glow(pixelsOne);
			pixelsTwo=glow(pixelsTwo);
			float similarity=0,redpixels=0,similarity2=0,redpixels2=0;
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixelsOne[i]==RED) {
					redpixels++;
					if(pixelsTwo[i]==RED||pixelsTwo[i]==LIGHTRED||pixelsTwo[i]==PINK) {
						similarity++;
					}
				}
			}
			for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
				if(pixelsTwo[i]==RED) {
					redpixels2++;
					if(pixelsOne[i]==RED||pixelsOne[i]==LIGHTRED||pixelsTwo[i]==PINK) {
						similarity2++;
					}
				}
			}
			if(redpixels==0)
				return -1;
			return (similarity/redpixels+similarity2/redpixels2)/2;
		}
		catch(InterruptedException e) {
			return 0;
		}
	}
	public int match() {
		for(int count=0;count<spells;count++) {
			float similarity=compare(area,spell[count].icon);
			if(similarity>=.75)
				return count;
			else if(similarity==-1)
				return -1;
		}
		return -2;
	}
	public int[] glow(int pixels[]) {
		for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
			if(pixels[i]==RED) {
				boolean glow[][]={{true,true,true},{true,true,true},{true,true,true}};
				if(i%CASTWIDTH==0) {
					glow[0][0]=false;
					glow[0][1]=false;
					glow[0][2]=false;
				}
				else if((i+1)%CASTWIDTH==0) {
					glow[2][0]=false;
					glow[2][1]=false;
					glow[2][2]=false;
				}
				if(i<CASTWIDTH) {
					glow[0][0]=false;
					glow[1][0]=false;
					glow[2][0]=false;
				}
				else if(i>=CASTWIDTH*(CASTHEIGHT-1)) {
					glow[0][2]=false;
					glow[1][2]=false;
					glow[2][2]=false;
				}
				if(glow[0][0]&&pixels[i-1-CASTWIDTH]!=RED)
					pixels[i-1-CASTWIDTH]=LIGHTRED;
				if(glow[1][0]&&pixels[i-CASTWIDTH]!=RED)
					pixels[i-CASTWIDTH]=LIGHTRED;
				if(glow[2][0]&&pixels[i+1-CASTWIDTH]!=RED)
					pixels[i+1-CASTWIDTH]=LIGHTRED;
				if(glow[0][1]&&pixels[i-1]!=RED)
					pixels[i-1]=LIGHTRED;
				if(glow[1][1]&&pixels[i]!=RED)
					pixels[i]=LIGHTRED;
				if(glow[2][1]&&pixels[i+1]!=RED)
					pixels[i+1]=LIGHTRED;
				if(glow[0][2]&&pixels[i-1+CASTWIDTH]!=RED)
					pixels[i-1+CASTWIDTH]=LIGHTRED;
				if(glow[1][2]&&pixels[i+CASTWIDTH]!=RED)
					pixels[i+CASTWIDTH]=LIGHTRED;
				if(glow[2][2]&&pixels[i+1+CASTWIDTH]!=RED)
					pixels[i+1+CASTWIDTH]=LIGHTRED;
			}
		}
		pixels=reglow(pixels);
		return pixels;
	}
	public int[] reglow(int pixels[]) {
		for(int i=0;i<CASTHEIGHT*CASTWIDTH;i++) {
			if(pixels[i]==LIGHTRED) {
				boolean glow[][]={{true,true,true},{true,true,true},{true,true,true}};
				if(i%CASTWIDTH==0) {
					glow[0][0]=false;
					glow[0][1]=false;
					glow[0][2]=false;
				}
				else if((i+1)%CASTWIDTH==0) {
					glow[2][0]=false;
					glow[2][1]=false;
					glow[2][2]=false;
				}
				if(i<CASTWIDTH) {
					glow[0][0]=false;
					glow[1][0]=false;
					glow[2][0]=false;
				}
				else if(i>=CASTWIDTH*(CASTHEIGHT-1)) {
					glow[0][2]=false;
					glow[1][2]=false;
					glow[2][2]=false;
				}
				if(glow[0][0]&&pixels[i-1-CASTWIDTH]!=LIGHTRED)
					pixels[i-1-CASTWIDTH]=PINK;
				if(glow[1][0]&&pixels[i-CASTWIDTH]!=LIGHTRED)
					pixels[i-CASTWIDTH]=PINK;
				if(glow[2][0]&&pixels[i+1-CASTWIDTH]!=LIGHTRED)
					pixels[i+1-CASTWIDTH]=PINK;
				if(glow[0][1]&&pixels[i-1]!=LIGHTRED)
					pixels[i-1]=PINK;
				if(glow[1][1]&&pixels[i]!=LIGHTRED)
					pixels[i]=PINK;
				if(glow[2][1]&&pixels[i+1]!=LIGHTRED)
					pixels[i+1]=PINK;
				if(glow[0][2]&&pixels[i-1+CASTWIDTH]!=LIGHTRED)
					pixels[i-1+CASTWIDTH]=PINK;
				if(glow[1][2]&&pixels[i+CASTWIDTH]!=LIGHTRED)
					pixels[i+CASTWIDTH]=PINK;
				if(glow[2][2]&&pixels[i+1+CASTWIDTH]!=LIGHTRED)
					pixels[i+1+CASTWIDTH]=PINK;
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
		else if(state.equals("game")) {
			for(int j=0;j<MAPHEIGHT;j++) {
				for(int i=0;i<MAPWIDTH;i++) {
					game.drawImage(floor[map[currentmap].terrain[i][j]],i*FLOORWIDTH,j*FLOORHEIGHT,this);
				}
			}
			game.drawImage(self.icon[self.direction],self.x,self.y,this);
			game.setColor(Color.black);
			game.fillRect(0,FIELDHEIGHT,WIDTH,HEIGHT-FIELDHEIGHT);
			game.setColor(Color.white);
			game.drawString("HP: "+self.HP+"/"+self.maxHP,10,245);
			if(casting!=null)
				game.drawString("Cast "+casting,10,260);
			game.drawImage(area,CASTX,CASTY,this);
		}
		monitor.drawImage(screen,0,0,this);
	}
	public Image getImage(String image) {
		MediaTracker tracker=new MediaTracker(this);
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
		int count=0;
		while(thread==current) {
			if(casting!=null)
				count++;
			if(count>=20) {
				casting=null;
				count=0;
			}
			if(self.move!=-1) {
				self.direction=self.move;
				if(self.move==UP) {
					self.y-=self.speed;
				}
				else if(self.move==DOWN) {
					self.y+=self.speed;
				}
				else if(self.move==LEFT) {
					self.x-=self.speed;
				}
				else if(self.move==RIGHT) {
					self.x+=self.speed;
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