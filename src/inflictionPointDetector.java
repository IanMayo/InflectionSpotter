import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class inflictionPointDetector {
	
	public static void Assert(Boolean cond,String errMsg) {
		if(!cond) {
			System.out.println(errMsg);
			System.exit(0);
		}
	}
	
	public static double[] smooth(double[] data,int span) {
		double[] smoothed = new double[data.length];
		double total;
		int posleft,posright,elementCount;
		
		for(int i = 0;i<smoothed.length;i++) {
			total = 0;
			posleft = i-span;
			posleft = posleft<0 ? 0 : posleft;
			posright = i+span;
			posright = posright>=data.length ? data.length-1 : posright;					
			elementCount = 0;
			for(int j=posleft;j<(posright+1);j++) {
				total+=data[j];
				elementCount++;
			}
			smoothed[i] = total/elementCount;
		}
		
		return smoothed;		
	}
	

	public static double[] diff(double[] data) {
		double[] diffed = new double[data.length-1];
		for(int i =0;i<diffed.length;i++)
			diffed[i] = data[i+1]-data[i];
		return diffed;
	}
	
	public static void main(String[] args) throws IOException, ParseException {

		//  ---  File Parsing
		String line;
		ArrayList<Double> values = new ArrayList<Double>();
		ArrayList<Long> timeStamps = new ArrayList<Long>();
		
		
		Assert(args.length == 1,"Please provide a single parameter which is the name of the file to process");
		
		File f = new File(args[0]);
		Assert(f.exists(),String.format( "File %s doesn't exist!",args[0]));
		
		final DateFormat df = new SimpleDateFormat("HH:mm:ss");
		
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		while((line= br.readLine()) != null ) {
			if (line.isEmpty()) continue;
			String[] cells = line.split(",");
			Assert(cells.length == 2,"Problem with input file format");
			
			values.add(Double.parseDouble(cells[1]));
			
			String timeStr = cells[0];
			Date date = df.parse(timeStr);
			
			timeStamps.add(date.getTime());						
		}
		br.close();


		//  ---  Numerical computation
		// approximation of first derivative
		double[] d1 = new double[values.size()-1];
		double tmp = values.get(0); 
		for(int i = 0;i<d1.length;i++) {
			d1[i] = values.get(i+1)-tmp;
			tmp += d1[i]; 
		}
					
		double[] d2 = smooth(diff(smooth(d1,4)),4);		// second derivative smoothed with box filter
		
		boolean pointFound = false;
		for(int i=0;i<d2.length-1;i++) {
			if((d2[i]*d2[i+1])<0) {				//  looking for sign change on the smoothed second derivative
				System.out.format("Inflection point detected at %s\n", df.format(new Date(timeStamps.get(i+1))));
				pointFound  = true;
			}
		}
		
		if(!pointFound) System.out.println("No inflection point found");
		
	}

}
