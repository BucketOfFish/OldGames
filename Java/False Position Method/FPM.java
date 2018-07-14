public class FPM {
	public static void main(String args[]) {
		double l=.008,r=.08;
		while(r-l>.001) {
			double app=(l+r)/2;
			if(f(app)<0)
				l=app;
			else
				r=app;
			System.out.println(l+", "+r+", "+app);
		}
	}
	public static double f(double x) {
		return -.5*Math.pow(x,-1.5)-(Math.pow(x,-1.5)/(.000081+.000183*Math.pow(x,.5)));
	}
}