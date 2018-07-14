class Action extends Spellcraft {
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