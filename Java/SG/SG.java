import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.applet.*;

public class SG extends Applet implements KeyListener {

	Image Gary,floors;
	final int LEFT=37,UP=38,RIGHT=39,DOWN=40;
	int positionx=1,positiony=1;
	
	public void init() {
		Gary=getImage(getCodeBase(),"Gary.gif");
		floors=getImage(getCodeBase(),"floors.gif");
		addKeyListener(this);
	}

	public void paint(Graphics gr) {
		for(int countx=1;countx<10;countx++) {
			for(int county=1;county<10;county++) {
				gr.drawImage(floors,(countx-county)*25,(county+countx)*13,this);
			}
		}
		gr.drawImage(Gary,positionx,positiony,this);
	}

	public void move(int direction,int spaces) {
		slide(direction,spaces);
	}

	public void slide(int direction,int spaces) {
		for(int count=0;count<spaces;count++) {
			switch(direction) {
				case LEFT:
					positionx-=25;
					positiony-=13;
					break;
				case UP:
					positionx+=25;
					positiony-=13;
					break;
				case RIGHT:
					positionx+=25;
					positiony+=13;
					break;
				case DOWN:
					positionx-=25;
					positiony+=13;
					break;
			}
			repaint();
		}		
	}

	public void keyPressed(KeyEvent ev) {
		int key=ev.getKeyCode();
		move(key,1);
		repaint();
		return;
	}

	public void keyTyped(KeyEvent ev) {}
    public void keyReleased(KeyEvent ev) {}

}