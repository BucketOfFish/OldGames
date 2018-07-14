import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.PixelGrabber;
import java.io.*;

public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener {
	Image area,target;
	Image spell[]=new Image[20];
	Graphics2D cast;
	Thread thread=null;
	int lastx,lasty,spells=3,HP=20,maxHP=20;
	layout map[]=new layout[50];
	float test;
	final int WIDTH=300,HEIGHT=300,CASTX=201,CASTY=201,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50,RED=-65536
		,LIGHTRED=-60000,FIELDWIDTH=300,FIELDHEIGHT=200,PINK=-50000,MAPWIDTH=20,MAPHEIGHT=15;
	public void init() {
		spell[0]=getImage(getCodeBase(),"X.gif");
		spell[1]=getImage(getCodeBase(),"circle.gif");
		spell[2]=getImage(getCodeBase(),"complex.gif");
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
		target=getImage(getCodeBase(),"target.gif");
		area=createImage(CASTWIDTH,CASTHEIGHT);
		cast=(Graphics2D)area.getGraphics();
		cast.drawImage(target,0,0,this);
		cast.setColor(Color.red);
		cast.setStroke(new BasicStroke(3));
		addMouseListener(this);
		addMouseMotionListener(this);
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
	public void effect(int spell) {
		switch(spell){
		case 0:
			HP+=5;
			if(HP>maxHP)
				HP=maxHP;
			break;
		case 1:
			maxHP+=5;
			break;
		case 2:
			maxHP+=50;
			HP+=50;
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
			if(spell>=0)
				effect(spell);
			else if(spell==-2)
				maxHP-=5;
			else if(spell==-1)
				HP-=5;
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
			float similarity=compare(area,spell[count]);
			test=similarity;
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
		game.setColor(Color.black);
		game.fillRect(0,FIELDHEIGHT,WIDTH,HEIGHT-FIELDHEIGHT);
		game.setColor(Color.white);
		game.drawString(HP+"/"+maxHP,10,215);
		game.drawString(test+"",10,225);
		game.drawImage(area,CASTX,CASTY,this);
		monitor.drawImage(screen,0,0,this);
	}
	public void start() {
		if(thread==null) {
			thread=new Thread(this);
			thread.start();
		}
	}
	public void run() {
		Thread current=Thread.currentThread();
		while(thread==current) {
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
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}