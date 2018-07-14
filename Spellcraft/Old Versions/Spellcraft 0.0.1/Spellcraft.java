import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.PixelGrabber;
import java.util.*;

public class Spellcraft extends Applet implements Runnable,MouseListener,MouseMotionListener {

	Image area,target,X,circle;
	Graphics2D cast;
	Thread thread=null;
	int lastx,lasty;
	float similarity,sim2;
	final int WIDTH=300,HEIGHT=300,CASTX=50,CASTY=50,CASTWIDTH=97,CASTHEIGHT=97,DELAY=50;

	public void init() {
		resize(WIDTH,HEIGHT);
		target=getImage(getCodeBase(),"target.gif");
		X=getImage(getCodeBase(),"X.gif");
		circle=getImage(getCodeBase(),"circle.gif");
		area=createImage(CASTWIDTH,CASTHEIGHT);
		cast=(Graphics2D)area.getGraphics();
		cast.setColor(Color.white);
		cast.fillRect(0,0,CASTWIDTH,CASTHEIGHT);
		cast.setColor(Color.red);
		cast.setStroke(new BasicStroke(3));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==3) {
			similarity=compare(area,X);
			sim2=compare(area,circle);
			cast.setColor(Color.white);
			cast.fillRect(0,0,CASTWIDTH,CASTHEIGHT);
			cast.setColor(Color.red);
		}
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton()==1) {
			lastx=e.getX();
			lasty=e.getY();
		}
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
			float similarity=0,redpixels=0;
			for(int j=0;j<CASTHEIGHT;j++) {
				for(int i=0;i<CASTWIDTH;i++) {
					if(pixelsOne[(j*CASTWIDTH)+i]==-65536) {
						redpixels++;
						if(pixelsOne[(j*CASTWIDTH)+i]==pixelsTwo[(j*CASTWIDTH)+i]) {
							similarity++;
						}
					}
				}
			}
			return similarity/redpixels;
		}
		catch(InterruptedException e) {
			return 0;
		}
	}

	public void paint(Graphics monitor) {
		Image screen=createImage(WIDTH,HEIGHT);
		Graphics2D game=(Graphics2D)screen.getGraphics();
		game.drawString(similarity+", "+sim2,10,10);
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