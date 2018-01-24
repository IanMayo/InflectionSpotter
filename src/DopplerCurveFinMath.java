import java.util.ArrayList;

import net.finmath.optimizer.LevenbergMarquardt;
import net.finmath.optimizer.SolverException;

import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class DopplerCurveFinMath implements IDopplerCurve
{

  /**
   * time stamp at inflection point
   */
  private long _inflectionTime;

  /**
   * frequency at inflection point
   */
  private double _inflectionFreq;

  /**
   * double[4] -> [a,b,c,d] for the sigmoid model: d + (c/(1+e^(a*x+b)))
   */
  private double[] _modelParameters;

  private final Normaliser _timeNormaliser;
  private final Normaliser _freqNormaliser;

  private double[] _times;

  public DopplerCurveFinMath(final ArrayList<Long> times,
      final ArrayList<Double> freqs)
  {
    // do some data testing
    if (times == null || freqs == null)
    {
      throw new IllegalArgumentException("The input datasets cannot be null");
    }

    if (times.size() == 0 || freqs.size() == 0)
    {
      throw new IllegalArgumentException("The input datasets cannot be empty");
    }

    // convert the times to doubles
    ArrayList<Double> dTimes = new ArrayList<Double>();
    for (Long t : times)
    {
      dTimes.add((double) t);
    }
    _timeNormaliser = new Normaliser(dTimes, false);
    _freqNormaliser = new Normaliser(freqs, true);

    final int sampleCount = times.size();

    // ok, collate the data
    final WeightedObservedPoints obs = new WeightedObservedPoints();

    double[] freqArr = new double[sampleCount];

    _times = new double[sampleCount];
    double[] weights = new double[sampleCount];

    for (int i = 0; i < sampleCount; i++)
    {
      double time = _timeNormaliser.normalise(dTimes.get(i));
      double freq = _freqNormaliser.normalise(freqs.get(i));
      obs.add(time, freq);

      _times[i] = time;
      freqArr[i] = freq;

      weights[i] = 1d;
      
      System.out.println(time + ", " + freq);
    }

    LevenbergMarquardt optimizer = new LevenbergMarquardt()
    {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      // Override your objective function here
      public void setValues(double[] params, double[] values)
      {
        double a = params[0];
        double b = params[1];
        double c = params[2];
        double d = params[3];

        for (int i = 0; i < _times.length; i++)
        {
          double thisT = _times[i];
          values[i] = ((a - d) / (1.0 + Math.pow(thisT / c, b))) + d;
        }
      }
    };

    // Set solver parameters
    optimizer.setInitialParameters(new double[]
    {1, 1, 1, 1});
    optimizer.setWeights(weights);
    optimizer.setMaxIteration(10000);
    optimizer.setTargetValues(freqArr);

    try
    {
      optimizer.run();
      _modelParameters = optimizer.getBestFitParameters();
    }
    catch (SolverException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    
     // and store the equation parameters
//     _modelParameters = coeff;
    
     FourParameterLogisticSecondDrivative derivativeFunc =
         new FourParameterLogisticSecondDrivative(); // ***
     derivativeFunc.coeff = _modelParameters;

     // use bisection solver to find the zero crossing point of derivative
     BisectionSolver bs = new BisectionSolver(1.0e-12, 1.0e-8);
     double root = bs.solve(1000000, derivativeFunc, 0, 1, 0.5);

     _inflectionTime = (long) _timeNormaliser.deNormalise(root); 
     _inflectionFreq = valueAt(_inflectionTime);
  }

  public double inflectionFreq()
  {
    return _inflectionFreq;
  }

  public long inflectionTime()
  {
    return _inflectionTime;
  }

  /**
   * calculate the value on the curve at this time
   * 
   * @param t
   *          time
   * @return frequency at this time
   */
  public double valueAt(final long t)
  {
    double normalised = _timeNormaliser.normalise(t);
    double val = new FourParameterLogistic().value(normalised, _modelParameters);
    return _freqNormaliser.deNormalise(val);
  }

  public double[] getCoords()
  {
    return _modelParameters;
  }
}