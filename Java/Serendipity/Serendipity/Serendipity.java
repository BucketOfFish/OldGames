import java.util.*;
import java.io.*;
import java.text.*;

class Serendipity {

    role role[] = new role[18];
    int n = 0, x;

    public void init() {
		role[n] = new role("a writer");
			role[x].add("Read 2 chapters", 2);
			role[x].add("Read poetry", 1);
			role[x].add("Learn/make up and use a new word/phrase", 1);
			role[x].add("Write a poem", 3);
			role[x].add("Write 200 words of prose", 3);
			role[x].add("", 2, "Writing Exercises");
		role[n] = new role("an artist");
			role[x].add("Make a sketch", 3);
			role[x].add("Plan 2 art projects", 1);
			role[x].add("", 4, "Art Projects");
			role[x].add("Look at art", 1);
			role[x].add("Learn Photoshop", 3);
			role[x].add("Take a picture", 2);
		role[n] = new role("a scholar");
			role[x].add("Read Wikipedia article", 2);
			role[x].add("Read news", 2);
			role[x].add("Research ", 2, "Research");
			role[x].add("Discuss ", 2, "Discuss");
			role[x].add("Read a foreign short story", 2);
		role[n] = new role("culturally aware");
			role[x].add("Watch a movie", 8);
			role[x].add("Watch 30 minutes of TV", 2);
			role[x].add("Listen to an album", 2);
			role[x].add("Practice guitar 30 minutes", 2);
		role[n] = new role("guileful");
			role[x].add("Work on sleight of hand", 1);
			role[x].add("Learn a new trick", 2);
			role[x].add("Pick a lock", 1);
			role[x].add("Plan a prank", 1);
		role[n] = new role("cleansed");
			role[x].add("Clear out 2 bookmarks", 2);
			role[x].add("People watch", 2);
			role[x].add("Hang out", 4);
			role[x].add("Clear 1 to-do item", 2);
			role[x].add("Do something active", 4);
 			role[x].add("Meditate and freethink", 1);
   }

    public static void main(String[] args) {
		Serendipity moriae = new Serendipity();
		moriae.init();
//		Scanner reader = new Scanner(System.in);
//		System.out.println("\nPassword?\n");
//		String password = reader.next();
//		if (!password.equals("sadcat")) {
//			System.exit(0);
//		}
		System.out.println("\n~Relearning Everything~\n");
//	    System.out.println("\n~Relearning Everything~\n\n1. Determine Fate\n2. Quit\n");
//		int choice = reader.nextInt();
		int choice = 1;
		   if (choice == 1)
			   moriae.determine();
		   else
			   System.exit(0);
    }

    public void determine() {
    	boolean existing = false;
		try	{
			FileInputStream in = new FileInputStream("Serendipity$schedule.class");
			BufferedReader file = new BufferedReader(new InputStreamReader(in));
			String read = file.readLine();
			Date today = new java.util.Date();
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
			String date = format.format(today);
			if (read.equals("Schedule for " + date)) {
				System.out.print("\nSchedule for today already exists:\n\n------------------------------");
				while (!(read = file.readLine()).equals("------------------------------")) {
					System.out.println(read);
				}
				System.out.println("------------------------------");
				System.out.println("\n1. Exit\n2. New Schedule\n");
				Scanner reader = new Scanner(System.in);
				int choice = reader.nextInt();
				if (choice == 1)
					existing = true;
				else {}
			}
			in.close();
		}
		catch(IOException ex) {}
    	if (existing) {
    		System.exit(0);
    	}
        Scanner scan = new Scanner(System.in);
		System.out.println("\nHours to spare?\n");
		int choice = scan.nextInt();
		Random wheel = new Random();
		int fate = wheel.nextInt(n);
		String occupation = role[fate].name;
//		System.out.println("\nMixed tasks?\n\n1. Yes\n2. No\n");
//		int mixed = scan.nextInt();
		int mixed = 1;
		boolean mix = false;
		if (mixed == 1)
			mix = true;
//		System.out.println("\nSort results?\n\n1. Yes\n2. No\n");
//		int sort = scan.nextInt();
		int sort = 1;
		if (!mix)
			System.out.println("\nToday you are " + occupation + ".\nYour tasks are:\n");
		else
			System.out.println("");
		int time;
		time = choice*4;
		int determined = 0, actions = 0, loops = 0;
		action action[] = new action[40];
		ArrayList<String> schedule = new ArrayList<String>();
		while (determined < time && loops < 30) {
			if (mix)
				fate = wheel.nextInt(n);
			action[actions] = new action (role[fate].action[wheel.nextInt(role[fate].actions)]);
			if (determined+action[actions].time <= time) {
				determined+=action[actions].time;
				if (action[actions].file == null)
					schedule.add(action[actions].action);
				else
					schedule.add(action[actions].action + action[actions].read());
				actions++;
				loops = 0;
			}
			else
				loops++;
		}
		if (sort == 1) {
			Collections.sort(schedule, String.CASE_INSENSITIVE_ORDER);
			for (int count = 0, instances = 1; count < schedule.size()-1; count++) {
				if (schedule.get(count).equals(schedule.get(count+1))) {
					instances++;
					schedule.remove(count+1);
					count--;
				}
				else if (instances != 1) {
					schedule.set(count, schedule.get(count) + " (" + instances + ")");
					instances = 1;
				}
				else {}
				if (count == schedule.size()-2 && instances != 1)
					schedule.set(count+1, schedule.get(count+1) + " (" + instances + ")");
			}
		}
		for (int count = 0; count < schedule.size(); count++) {
			System.out.println(schedule.get(count));
		}
		try	{
			Date today = new java.util.Date();
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
			String date = format.format(today);
		    FileOutputStream out = new FileOutputStream("Serendipity$schedule.class");
		    PrintStream printer = new PrintStream(out);
		    if (!mix)
		    	printer.println("Schedule for " + date + "\n\nToday you are " + occupation + ".\nYour tasks are:\n");
		    else
		    	printer.println("Schedule for " + date + "\n");
		    for (int count = 0; count < schedule.size(); count++) {
				printer.println(schedule.get(count));
			}
			printer.print("------------------------------");
		    out.close();
		}
		catch (IOException e) {}
		System.out.println("\n1. Exit\n");
		scan.next();

    }

    class role {
		String name;
		int actions = 0;
		action action[] = new action[10];

		public role(String name) {
			this.name = name;
			x = n;
			n++;
		}

		public void add(String action, int time) {
			this.action[actions] = new action(action, time);
			actions++;
		}

		public void add(String action, int time, String file) {
			this.action[actions] = new action(action, time, file);
			actions++;
		}
    }

	class action {
		String action, file;
		int time;

		public action(String action, int time) {
			this.action = action;
			this.time = time;
		}

		public action(String action, int time, String file) {
			this.action = action;
			this.time = time;
			this.file = file + ".txt";
		}

		public action(action action) {
			this.action = action.action;
			this.time = action.time;
			this.file = action.file;
		}

		public String read() {
			try	{
				FileInputStream in = new FileInputStream("C:/Documents and Settings/Matt/My Documents/Stuff/To Do/" + this.file);
				BufferedReader file = new BufferedReader(new InputStreamReader(in));
				String read = file.readLine();
			    FileOutputStream out = new FileOutputStream("C:/Documents and Settings/Matt/My Documents/Stuff/To Do/Temp.txt");
			    PrintStream printer = new PrintStream(out);
			    String next = read;
			    try {
					while (!(next = file.readLine()).equals(null)) {
						printer.println(next);
					}
			    }
			    catch (NullPointerException ex) {}
			    out.close();
				in.close();
			    File temp = new File("C:/Documents and Settings/Matt/My Documents/Stuff/To Do/Temp.txt");
			    File permanent = new File("C:/Documents and Settings/Matt/My Documents/Stuff/To Do/" + this.file);
			    permanent.delete();
			    if (!temp.renameTo(permanent))
			    	System.out.println("Could not rename file");
				return read;
			}
			catch(IOException ex) {
				return "something";
			}
		}
	}
}
