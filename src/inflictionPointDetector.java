import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;

public class inflictionPointDetector
{

  public static void Assert(Boolean cond, String errMsg)
  {
    if (!cond)
      throw new IllegalArgumentException(errMsg);
  }

  public static void main(String[] args) throws IOException, ParseException
  {

    String line;

    ArrayList<Double> values = new ArrayList<Double>();
    ArrayList<Long> timeStamps = new ArrayList<Long>();

    Assert(args.length == 1,
        "Please provide a single parameter which is the name of the file to process");

    File f = new File(args[0]);
    Assert(f.exists(), String.format("File %s doesn't exist!", args[0]));

    DateFormat df = new SimpleDateFormat("HH:mm:ss");

    BufferedReader br = new BufferedReader(new FileReader(args[0]));
    while ((line = br.readLine()) != null)
    {
      if (line.isEmpty())
        continue;
      String[] cells = line.split(",");
      Assert(cells.length == 2, "Problem with input file format");

      values.add(Double.parseDouble(cells[1]));
      timeStamps.add(df.parse(cells[0]).getTime());
    }
    br.close();

    System.out.format("Processing: %s\n", args[0]);

    DopplerCurve dc = new DopplerCurve(timeStamps, values);

    System.out.format("Inflection time stamp: %dms (%S) \n", dc
        .inflectionTime(), df.format(new java.util.Date(dc.inflectionTime())));
    System.out.format("Inflection frequency: %fHz\n", dc.inflectionFreq());
    System.out.print("Coords:");
    for(int i=0;i<4;i++)
    {
      System.out.print(dc.getCoords()[i] + " , ");
    }
    System.out.println();

    // output sample curve
    long startT = timeStamps.get(0);
    long endT = timeStamps.get(timeStamps.size() - 1);
    long delta = (endT - startT) / 5;
    System.out.println("Coords fitted curve");
    for (long i = startT; i <= endT; i += delta)
    {
      System.out.println(i + ", " + dc.valueAt(i));
    }

  }

}
