import java.util.*;
import java.io.*;

class TextAdventure {

	frame frame[]=new frame[500];
	int current=0,supress=0,links=0;
	boolean firstrun=true;
	final int NULL=-1,OVER=999,TOTALCHOICES=20;
	int variable[]=new int[58],sysvariable[]=new int[58];
	String string[]=new String[58];
	choice option[]=new choice[10];
	String gamefile;
	Scanner reader=new Scanner(System.in);

	public boolean switchboard(int tokens,String[] command) {
    	boolean print=true;
    	if(tokens!=0)
    		print=false;
    	if(gamefile.toLowerCase().equals("sgzero")) {
    		print=SGZero(tokens,command);
    	}
    	return print;
    }

//Contains game-specific actions

	public boolean SGZero(int tokens,String[] command) {
		boolean print=true;
		if(tokens==0) {
			switch(current) {
			case 86:
				frame[86].line[0]="\"I want to go to the "+s('a')+",\" said Gary. \"Ok,\" replied mom, \"the science labs it is! This will be";
				break;
			}
		}
		else {
			final int LOLLIPOPS=85;
			if(tokens==1&&(command[0].toLowerCase().equals("suck"))) {
				System.out.println("No, YOU suck!");
			}
			else if(tokens==1&&(command[0].toLowerCase().equals("help"))) {
				System.out.println("I'll help you die!");
			}
			else if(LOLLIPOPS>=current&&current>56&&tokens==1&&(command[0].toLowerCase().equals("lollipop")||command[0].toLowerCase().equals("lollipops"))) {
				System.out.println("Gary has "+(v('c')+v('d'))+" lollipops.");
			}
			else if(tokens==1&&(command[0].toLowerCase().equals("refresh")||command[0].toLowerCase().equals("repeat")||command[0].toLowerCase().equals("what")||command[0].equals("?"))) {
				System.out.println();
				supress=1;
			}
			else if(command[0].toLowerCase().equals("use")||command[0].toLowerCase().equals("punch")||command[0].toLowerCase().equals("eat")) {
				if(tokens==1) {
					if(command[0].toLowerCase().equals("eat"))
						System.out.println("Gary ate.");
					else
						System.out.println("Gary "+command[0].toLowerCase()+"ed.");
				}
				else
					System.out.println("Gary cannot "+command[0].toLowerCase()+" "+combine(1,tokens,command)+".");
			}
			else if(command[0].toLowerCase().equals("magic")) {
				System.out.println("Gary doesn't know magic.");
			}
			else if(command[0].toLowerCase().equals("code")||command[0].toLowerCase().equals("cheat")||command[0].toLowerCase().equals("password")) {
				String code;
				if(tokens==1) {
					System.out.println("Enter four digit code.");
					code=reader.next();
				}
				else
					code=command[1];
				if(code.length()==4) {
					int codeID=convert(code);
					if(codeID==-1)
						System.out.println("Invalid code.");
					else if(codeID==1011) {
						System.out.println("Code accepted: 99 lollipops.");
						if(LOLLIPOPS>=current&&current>56) {
							variable['c'-65]=99;
							variable['d'-65]=0;
							variable['l'-65]=99;
						}
					}
					else {
						Random wheel=new Random();
						int effect=10;
						boolean loop=false;
						do {
							effect=wheel.nextInt(100);
							if(effect<10) {
								if(sysvariable[effect]==0)
									sysvariable[effect]=1;
								else
									loop=true;
							}
							else
								loop=false;
						}
						while(loop);
						switch(effect) {
						case 0:
							System.out.println("Code accepted: Infinite mana.");
							break;
						case 1:
							System.out.println("Code accepted: Infinite bananas.");
							break;
						case 2:
							System.out.println("Code accepted: Difficulty increased.");
							break;
						case 3:
							System.out.println("Code accepted: Game restarted.");
							init(false);
							supress=0;
							break;
						case 4:
							System.out.println("Code accepted: Dam doors unlocked.");
							break;
						case 5:
							System.out.println("Code accepted: Self destruct.\n\nGame Over");
							System.exit(0);
							break;
						case 6:
							System.out.println("Code accepted: Graphics quality set to high.");
							break;
						case 7:
							System.out.println("Code accepted: Speed increased.");
							break;
						case 8:
							System.out.println("Code accepted: Level increased.");
							break;
						case 9:
							System.out.println("Code accepted: Old biscuit added to inventory.");
							break;
						default:
							System.out.println("Invalid code.");
							break;
						}
					}
				}
				else {
					System.out.println("Invalid code.");
				}
			}
			else
				print=false;
		}
		return print;
	}

//Contains basic actions

	public void command() {
		supress=2;
		int tokens=0;
		String[] command=new String[5];
		StringTokenizer tokenizer = new StringTokenizer(reader.nextLine());
		while(tokenizer.hasMoreTokens()) {
			command[tokens++]=tokenizer.nextToken();
		}
		if(command[0].toLowerCase().equals("save")) {
			String name;
			if(tokens==1) {
				System.out.println("Name of save file?");
				name=reader.next();
			}
			else
				name=command[1];
			save(name);
		}
		else if(command[0].toLowerCase().equals("change")||command[0].toLowerCase().equals("switch")) {
			String name;
			if(tokens==1) {
				System.out.println("Enter name of game file.");
				name=reader.next();
			}
			else
				name=command[1];
			change(name);
		}
		else if(command[0].toLowerCase().equals("load")) {
			String name;
			if(tokens==1) {
				System.out.println("Name of saved file?");
				name=reader.next();
			}
			else
				name=command[1];
			load(name);
		}
		else if(command[0].toLowerCase().equals("reload")) {
			save("tempsavefile");
			init(false);
			load("tempsavefile");
			File permanent=new File("Saves/"+gamefile+"/tempsavefile.save");
			permanent.delete();
		}
		else if(command[0].toLowerCase().equals("jump")) {
			current=convert(command[1]);
			supress=0;
		}
		else if(command[0].toLowerCase().equals("variable")) {
			System.out.println(v(command[1].charAt(0)));
		}
		else if(command[0].toLowerCase().equals("clear")&&command[1].toLowerCase().equals("variables")) {
			decode("&");
			System.out.println("Variables cleared.");
		}
		else if(command[0].toLowerCase().equals("varset")) {
			variable[command[1].charAt(0)-65]=convert(command[2]);
			System.out.println("Variable set.");
		}
		else if((tokens==1||command[1].toLowerCase().equals("game"))&&(command[0].toLowerCase().equals("exit")||command[0].toLowerCase().equals("quit"))) {
			System.exit(0);
		}
		else if(command[0].toLowerCase().equals("restart")) {
			System.out.println();
			init(false);
			supress=0;
		}
		else if(tokens==1&&command[0].toLowerCase().equals("frame")) {
			System.out.println("You are on frame "+current);
		}
		else {
			if(!switchboard(tokens,command)) {
				System.out.println("Unrecognized command.");
				log(combine(0,tokens,command));
			}
		}
	}

	public void run() {
		while(true) {
			int input=NULL,choice=NULL;
			if(supress!=2)
				frame[current].display();
			supress=0;
			try {
				input=reader.nextInt();
				try {
					choice=option[input-1].link;
					if(option[input-1].after)
						decode(option[input-1].aftercode);
				}
				catch(ArrayIndexOutOfBoundsException ex) {}
				switch(choice) {
				case OVER:
					System.out.println("\nGame Over");
					System.exit(0);
				case NULL:
					System.out.println("Invalid choice. Choose again.");
					supress=2;
					break;
				default:
		    		System.out.println();
					current=choice;
					break;
				}
			}
			catch(InputMismatchException ex) {
				command();
			}
		}
	}

	public void init(boolean run) {
    	try {
    		FileInputStream in=new FileInputStream("Configuration.sys");
    		BufferedReader file=new BufferedReader(new InputStreamReader(in));
    		String next=file.readLine();
	       	gamefile=next;
	       	in.close();
	    }
	    catch(Exception exc) {
	    	System.out.println("Corrupt configuration file.");
	    	System.exit(0);
	    }
	    current=0;
    	for(int count=0;count<58;count++) {
    		variable[count]=0;
    		sysvariable[count]=0;
    		string[count]=null;
    	}
		try	{
			FileInputStream in=new FileInputStream("Games/"+gamefile+".txa");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			String next;
		    try {
				while(!(next=file.readLine()).equals(null)) {
			    	int linecount=0;
					int ID=convert(next);
					if(firstrun) {
						try {
							System.out.println("WARNING: Frame "+frame[ID].ID+" is a duplicate.");
						}
						catch(Exception ex) {}
					}
					frame[ID]=new frame(ID);
					if((next=file.readLine()).equals("-")) {
						while(!(next=file.readLine()).equals("-")) {
							frame[ID].code[frame[ID].codes]=next;
							frame[ID].codes++;
						}
					}
					else
						frame[ID].line[linecount++]=next;					
					while(!(next=file.readLine()).equals("~")) {
						frame[ID].line[linecount++]=next;
					}
					while(!(next=file.readLine()).equals("")) {
						boolean set=true;
						if(next.charAt(0)>=48&&next.charAt(0)<=57) {
							int charcount=0,charcount2=0;
							while(next.charAt(charcount++)!=' ') {}
							charcount2=charcount;
							try {
								while(next.charAt(charcount2++)!='|') {}
							}
							catch(StringIndexOutOfBoundsException ex) {
								set=false;
								frame[ID].choice[frame[ID].choices++]=new choice(false,next.substring(charcount),convert(next.substring(0,charcount-1)),null);
							}
							if(set)
								frame[ID].choice[frame[ID].choices++]=new choice(true,next.substring(charcount,charcount2-1),convert(next.substring(0,charcount-1)),next.substring(charcount2));
						}
						else {
							int charcount=0,charcount2=0,charcount3=0;
							while(next.charAt(charcount++)!=' ') {}
							charcount2=charcount;
							while(next.charAt(charcount2++)!=' ') {}
							charcount3=charcount2;
							try {
								while(next.charAt(charcount3++)!='|') {}
							}
							catch(StringIndexOutOfBoundsException ex) {
								set=false;
								frame[ID].choice[frame[ID].choices++]=new choice(false,next.substring(charcount2),convert(next.substring(charcount,charcount2-1)),next.substring(0,charcount-1),null);
							}
							if(set)
								frame[ID].choice[frame[ID].choices++]=new choice(true,next.substring(charcount2,charcount3-1),convert(next.substring(charcount,charcount2-1)),next.substring(0,charcount-1),next.substring(charcount3));
						}
					}
				}
		    }
		    catch(NullPointerException ex) {}
			in.close();
		}
		catch(IOException ex) {}
    	firstrun=false;
		if(run)
			run();
	}

	public boolean decode(String code) {
		int varID=NULL,var2ID=NULL,value=NULL,operation=NULL;
		boolean returnvalue=true;
		for(int counter=0;counter<code.length();counter++) {
			int charID=code.charAt(counter);
			if((charID>='A'&&charID<='Z')||(charID>='a'&&charID<='z')) {
				if(varID==NULL)
					varID=charID-65;
				else
					var2ID=charID-65;
			}
			else if(charID>='0'&&charID<='9') {
				int length=1;
				try {
					while(code.charAt(counter+length)>='0'&&code.charAt(counter+length)<='9') {
						length++;
					}
				}
				catch(StringIndexOutOfBoundsException e) {}
				value=convert(code.substring(counter,counter+length));
				counter+=(length-1);
			}
			else if(charID==(int)',') {
				varID=NULL;
				var2ID=NULL;
				value=NULL;
				operation=NULL;
			}
			else if(charID==(int)'%') {
				string[code.charAt(counter+1)-65]=code.substring(counter+3);
				counter=code.length();
			}
			else {
				operation=charID;
			}
			if((varID!=NULL&&(var2ID!=NULL||value!=NULL)&&operation!=NULL)||(operation=='&')) {
				if(var2ID!=NULL)
					value=variable[var2ID];
				switch(operation) {
				case (int)':':
					if(variable[varID]!=value)
						returnvalue=false;
					break;
				case (int)'<':
					if(!(variable[varID]<value))
						returnvalue=false;
					break;
				case (int)'>':
					if(!(variable[varID]>value))
						returnvalue=false;
					break;
				case (int)'=':
					variable[varID]=value;
					break;
				case (int)'-':
					variable[varID]-=value;
					break;
				case (int)'+':
					variable[varID]+=value;
					break;
				case (int)'&':
					for(int count=0;count<=57;count++) {
						variable[count]=0;
					}
					break;
				}
				if(returnvalue==false)
					counter=code.length();
			}
		}
		return returnvalue;
	}

	public void interpret(int ID) {
		if(supress==0) {
			for(int codecount=0;codecount<frame[ID].codes;codecount++) {
				decode(frame[ID].code[codecount]);
			}
		}
		links=0;
		for(int choicecount=0;choicecount<frame[ID].choices;choicecount++) {
			if(frame[ID].choice[choicecount].hidden) {
				if(frame[ID].choice[choicecount].revealed=decode(frame[ID].choice[choicecount].code))
					option[links++]=frame[ID].choice[choicecount];
			}
			else
				option[links++]=frame[ID].choice[choicecount];
		}
		for(int count=links;count<10;count++) {
			option[count]=null;
		}
	}

	public void save(String name) {
		boolean write=true;
		try	{
			FileInputStream in=new FileInputStream("Saves/"+gamefile+"/"+name+".save");
			System.out.println("File already exists. Overwrite? (Y/N)");
			String confirm=reader.next();
			if(confirm.toLowerCase().equals("n")||confirm.toLowerCase().equals("no"))
				write=false;
			else if(!confirm.toLowerCase().equals("y")&&!confirm.toLowerCase().equals("yes")) {
				System.out.println("Unrecognized command.");
				write=false;
			}
			in.close();
		}
		catch(IOException exc) {}
		if(write) {
			try	{
				File file=new File("Saves/"+gamefile+"/");
				file.mkdir();
			    FileOutputStream out=new FileOutputStream("Saves/"+gamefile+"/"+name+".save");
			    PrintStream printer=new PrintStream(out);
		    	printer.println(gamefile);
		    	printer.println(current);
			    for (int count=0;count<58;count++) {
					printer.println(variable[count]);
					printer.println(sysvariable[count]);
					printer.println(string[count]);
		    	}
			    out.close();
				System.out.println("File saved.");
			}
			catch (IOException e) {
				System.out.println("File not saved.");
			}
		}
		else
			System.out.println("File not saved.");
	}

	public void load(String name) {
		try	{
			FileInputStream in=new FileInputStream("Saves/"+gamefile+"/"+name+".save");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			if(file.readLine().toLowerCase().equals(gamefile)) {
				System.out.println("Game loaded.\n");
				supress=1;
				init(false);
				current=convert(file.readLine());
			    for (int count=0;count<58;count++) {
					variable[count]=convert(file.readLine());
					sysvariable[count]=convert(file.readLine());
					string[count]=file.readLine();
				}
			}
			else
				System.out.println("Save file is for different game");
			in.close();
		}
		catch(IOException exc) {
			System.out.println("File does not exist.");
		}
	}

	public void change(String name) {
		try	{
			FileInputStream in=new FileInputStream("Games/"+name+".txa");
			in=new FileInputStream("Configuration.sys");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			String next=file.readLine();
		    FileOutputStream out=new FileOutputStream("Configuration.temp");
		    PrintStream printer=new PrintStream(out);
		    try {
		    	printer.println(name);
				while(!(next=file.readLine()).equals(null)) {
					printer.println(next);
				}
		    }
		    catch(NullPointerException exc) {}
		    out.close();
			in.close();
		    File temp=new File("Configuration.temp");
		    File permanent=new File("Configuration.sys");
		    permanent.delete();
		    if (!temp.renameTo(permanent))
		    	System.out.println("Could not rename file");
		    init(false);
		    System.out.println();
		    supress=0;
		}
		catch(IOException exc) {
			System.out.println("Game does not exist");
		}
	}

	public String combine(int start,int tokens,String[] command) {
    	String combination="";
    	for(int count=start;count<tokens;count++) {
    		if(count!=start)
    			combination+=(" ");
    		combination+=(command[count]);
    	}
    	return combination;
    }

	public int convert(String input) {
		int number=0;
		for(int count=input.length();count>0;count--) {
			if(input.charAt(count-1)>57||input.charAt(count-1)<48) {
				number=-1;
				count=0;
			}
			else
				number+=(input.charAt(count-1)-48)*Math.pow(10,input.length()-count);
		}
		return number;
	}

	public int v(char ID) {
    	return variable[ID-65];
    }

	public String s(char ID) {
    	return string[ID-65];
    }

	public void log(String line) {
		line=gamefile+": "+line;
		try	{
			FileInputStream in=new FileInputStream("Error.log");
			BufferedReader file=new BufferedReader(new InputStreamReader(in));
			String next;
			FileOutputStream out=new FileOutputStream("Error.temp");
			PrintStream printer=new PrintStream(out);
			try {
				while(!(next=file.readLine()).equals(null)) {
					printer.println(next);
				}
			}
			catch(NullPointerException exce) {
				printer.println(line);
			}
			out.close();
			in.close();
			File temp=new File("Error.temp");
			File permanent=new File("Error.log");
			permanent.delete();
			temp.renameTo(permanent);
		}
		catch(IOException exc) {
			try {
				FileOutputStream out=new FileOutputStream("Error.log");
				PrintStream printer=new PrintStream(out);
				printer.println(line);
				out.close();
			}
			catch(Exception exce) {}
		}
	}

	public static void main(String[] args) {
		TextAdventure game=new TextAdventure();
		game.init(true);
	}

	class frame {
		int ID,codes=0,choices=0;
		String line[]=new String[20];
		String code[]=new String[10];
		choice choice[]=new choice[20];
		
		public frame(int newID) {
			ID=newID;
		}

		public void display() {
			interpret(ID);
			if(switchboard(0,null)) {
				try {
					int linecount=0;
					while(!(frame[ID].line[linecount]).equals(null)) {
						System.out.println(frame[ID].line[linecount++]);
					}
				}
				catch(Exception ex) {
					try {
						int choicecount=0,revealed=0;
						System.out.println();
						while(true) {
							if(frame[ID].choice[choicecount].revealed==true)
								System.out.println((revealed++)+1+". "+frame[ID].choice[choicecount++].line);
							else
								choicecount++;
						}
					}
					catch(Exception exc) {
						System.out.println();
					}
				}
			}
		}
	}

	class choice {
		boolean hidden=false,revealed=true,after=false;
		int link=NULL;
		String code,line,aftercode;

		public choice(boolean after,String line,int link,String aftercode) {
			this.after=after;
			this.aftercode=aftercode;
			this.line=line;
			this.link=link;
		}

		public choice(boolean after,String line,int link,String code,String aftercode) {
			this.after=after;
			this.aftercode=aftercode;
			this.hidden=true;
			this.line=line;
			this.link=link;
			this.code=code;
		}
	}
}