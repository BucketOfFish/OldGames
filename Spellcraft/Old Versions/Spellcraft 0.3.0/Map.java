import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

class Map extends Spellcraft {
	ArrayList<Creature> population=new ArrayList<Creature>();
	ArrayList<Item> stock=new ArrayList<Item>();
	ArrayList<Magic> casts=new ArrayList<Magic>();
	int width,height;
	Floor terrain[][];
	int link[]=new int[4];
	void add(Creature me,int x,int y) {
		me.x=x;
		me.y=y;
//		if(me==self)
//			population.add(self);
//		else
//			population.add((creature)me.clone());
	}
	void add(Item it,int x,int y) {
		it.x=x;
		it.y=y;
		stock.add((Item)it.clone());
	}
	void remove(Creature me) {
		population.remove(me);
	}
	void remove(Item it) {
		stock.remove(it);
	}
	void layout(int link[]) {
		this.link=link;
	}
	Map(int x,int y) {
		width=x;
		height=y;
		terrain=new Floor[x][y];
		for(int j=0;j<y;j++) {
			for(int i=0;i<x;i++) {
				terrain[i][j]=new Floor("Grass");
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