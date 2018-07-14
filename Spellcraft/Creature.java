import java.awt.Image;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Random;

class Creature extends Spellcraft implements Cloneable,Runnable {
	Creature target;
	boolean undead,flies,wizard,poisoned,frozen;
	String name;
	Polygon outline=new Polygon();
	ArrayList<String> description=new ArrayList<String>();
	ArrayList<Item> inventory=new ArrayList<Item>();
	ArrayList<Magic> spellbook=new ArrayList<Magic>();
	Map location;
	int direction=DOWN,x,y,sizex,sizey,HP,maxHP,speed=4,move=-1,damage=1,MP,maxMP;
	Image icon[][]=new Image[2][4];
	Thread moving;
	public Creature clone() {
		try {
			direction=new Random().nextInt(4);
			return (Creature)super.clone();
		}
		catch(Exception e) {
			throw new InternalError(e.toString());
		}
	}
	public Creature clone(int direction) {
		try {
			this.direction=direction;
			return (Creature)super.clone();
		}
		catch(Exception e) {
			throw new InternalError(e.toString());
		}
	}
	Creature(String name,int sizex,int sizey) {
		this.name=name;
		this.sizex=sizex;
		this.sizey=sizey;
		Image icon=getImage("player.gif","Creatures");
		try {
			for(int j=0;j<4;j++) {
				for(int i=0;i<1;i++)
					this.icon[i][j]=crop(icon,i*sizex,j*sizey,sizex,sizey);
			}
		}
		catch (Exception e) {}
	}
	public void setsize(int sizex,int sizey) {
		this.sizex=sizex;
		this.sizey=sizey;
	}
	public void recover(int gain) {
		HP+=gain;
		if(HP>maxHP)
			HP=maxHP;
	}
	public void damage(int lose) {
		HP-=lose;
		if(HP<=0)
			location.remove(this);
	}
	public void move() {
		moving.start();
/*		Random wheel=new Random();
		if(me!=self) {
			if(!targeted) {
				int move=wheel.nextInt(50);
				if(move<4)
					move=move;
				else if(move==4)
					move=-1;
			}
			else if(targeted&&wheel.nextInt(5)==0) {
				if(wheel.nextInt(2)==1) {
					if(rightx()<self.leftx())
						move=RIGHT;
					else if(leftx()>self.rightx())
						move=LEFT;
					else if(downy()<self.upy())
						move=DOWN;
					else if(upy()>self.downy())
						move=UP;
				}
				else {
					if(downy()<self.upy())
						move=DOWN;
					else if(upy()>self.downy())
						move=UP;
					else if(rightx()<self.leftx())
						move=RIGHT;
					else if(leftx()>self.rightx())
						move=LEFT;
				}
			}
		}
			if(me==self&&currentmap!=oldmap)
				cast.drawImage(target,0,0,this);
		}*/
	}
	@SuppressWarnings("deprecation")
	public void stop() {
		moving.suspend();
//		moving.resume();
	}
	public void Action() {
/*		for(int count=0;count<map.get(currentmap).stock.size();count++) {
			Item it=map.get(currentmap).stock.get(count);
			if(it.name.equals("savespot")) {
				save();
				messages.add("Game saved");
			}
		}*/
	}
	public void run() {
	}
}