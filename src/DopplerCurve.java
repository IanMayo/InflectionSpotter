import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

public class DopplerCurve
{

  /**
   * our function definition for a doppler curve
   * 
   * @author Ian
   * 
   */
  private static class DopplerCurveFitter extends AbstractCurveFitter
  {

    @Override
    protected LeastSquaresProblem getProblem(
        final Collection<WeightedObservedPoint> points)
    {
      final int len = points.size();
      final double[] target = new double[len];
      final double[] weights = new double[len];

      int i = 0;
      for (WeightedObservedPoint point : points)
      {
        target[i] = point.getY();
        weights[i] = point.getWeight();
        i += 1;
      }

      AbstractCurveFitter.TheoreticalValuesFunction model =
          new AbstractCurveFitter.TheoreticalValuesFunction(
              new ScalableSigmoid(), points);

      LeastSquaresBuilder lsb = new LeastSquaresBuilder();
      lsb.maxEvaluations(1000000);
      lsb.maxIterations(1000000);
      lsb.start(new double[]
      {1.0, -0.5, 1.0, 1.0});
      lsb.target(target);
      lsb.weight(new DiagonalMatrix(weights));
      lsb.model(model.getModelFunction(), model.getModelFunctionJacobian());
      return lsb.build();
    }

  }

  
  
  private static class FourPLCurveFitter extends AbstractCurveFitter
  {

    @Override
    protected LeastSquaresProblem getProblem(
        final Collection<WeightedObservedPoint> points)
    {
      final int len = points.size();
      final double[] target = new double[len];
      final double[] weights = new double[len];

      int i = 0;
      for (WeightedObservedPoint point : points)
      {
        target[i] = point.getY();
        weights[i] = point.getWeight();
        i += 1;
      }

      AbstractCurveFitter.TheoreticalValuesFunction model =
          new AbstractCurveFitter.TheoreticalValuesFunction(
              new FourParameterLogistic(), points);

      LeastSquaresBuilder lsb = new LeastSquaresBuilder();
      lsb.maxEvaluations(1000000);
      lsb.maxIterations(1000000);
      lsb.start(new double[]
      {1.0, -0.5, 1.0, 1.0});
      lsb.target(target);
      lsb.weight(new DiagonalMatrix(weights));
      lsb.model(model.getModelFunction(), model.getModelFunctionJacobian());
      return lsb.build();
    }

  }
  
  
  
  /**
   * times of samples
   * 
   */
  private final ArrayList<Long> _times;

  /**
   * times normalised to 0..1 range
   */
  private final double[] _normalizedTimes;

  /**
   * number of milliseconds from first pointto last point
   */
  private final long _timeStampSpan;

  /**
   * measured frequencies
   * 
   */
  private final ArrayList<Double> _freqs;

  /**
   * use the first time value as an offset
   * 
   */
  private final long _startTime;

  /**
   * time stamp at inflection point
   */
  private final long _inflectionTime;

  /**
   * frequency at inflection point
   */
  private final double _inflectionFreq;

  /**
   * double[4] -> [a,b,c,d] for the sigmoid model: d + (c/(1+e^(a*x+b)))
   */
  private final double[] _modelParameters;

  public DopplerCurve(final ArrayList<Long> times, final ArrayList<Double> freqs)
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

    _times = times;
    _freqs = freqs;
    final int sampleCount = times.size();

    _startTime = _times.get(0);
    _normalizedTimes = new double[sampleCount];
    _timeStampSpan = _times.get(_times.size() - 1) - _startTime;

    // normalize time span to 0..1
    for (int i = 0; i < sampleCount; i++)
    {
      // time is reversed after normalization by (1-x) to make the shape of sigmodi match the data
      //                           _____
      // shape of sigmoid : ______/
      //                    ______
      // shape of our data:       \_____
      _normalizedTimes[i] =
          1 - (((double) (times.get(i) - _startTime)) / _timeStampSpan);
    }

    // ok, collate the data
    final WeightedObservedPoints obs = new WeightedObservedPoints();

    
    
    // add the first sample manually as the loop will add pairs of (midpoint,sample)
    obs.add(_normalizedTimes[sampleCount-1], _freqs.get(sampleCount-1));
    System.out.println(_normalizedTimes[sampleCount-1] + ", " + _freqs.get(sampleCount-1));
    
    // use reverse counter, so the curve still appears in chronological order
    for (int i = sampleCount - 2; i >= 0; i--)
    {
      // add sample midpoints too as samples to curve fitter
      //obs.add((_normalizedTimes[i]+_normalizedTimes[i+1])/2.0, (_freqs.get(i)+_freqs.get(i+1))/2.0);
      //System.out.println((_normalizedTimes[i]+_normalizedTimes[i+1])/2.0+","+ (_freqs.get(i)+_freqs.get(i+1))/2.0);
      
      obs.add(_normalizedTimes[i], _freqs.get(i)); 
      System.out.println(_normalizedTimes[i] + ", " + _freqs.get(i));
    }
    
     
    // now Instantiate a parametric sigmoid fitter.
    //final AbstractCurveFitter fitter = new DopplerCurveFitter();   // ***
    final AbstractCurveFitter fitter = new FourPLCurveFitter();   // ***

    // Retrieve fitted parameters (a,b,c,d) for the sigmoid model: d + (c/(1+e^(a*x+b)))
    final double[] coeff = fitter.fit(obs.toList());

    // --- checking for inflection point ---
    // construct the second order derivative of the sigmoid with this parameters
    //SigmoidSecondDerivative derivativeFunc = new SigmoidSecondDerivative();   // ***
    FourParameterLogisticSecondDrivative derivativeFunc = new FourParameterLogisticSecondDrivative();   // ***
    derivativeFunc.coeff = coeff;

    // use bisection solver to find the zero crossing point of derivative
    BisectionSolver bs = new BisectionSolver(1.0e-12, 1.0e-8);
    double root = bs.solve(10000, derivativeFunc, 0, 1, 0.5);

    // and store the equation parameters
    _modelParameters = coeff;

    _inflectionTime = _startTime + (long) ((_timeStampSpan * (1 - root))); // taking into account
                                                                           // that time is reversed
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
    return new ScalableSigmoid().value(
        1 - (((double) (t - _startTime)) / _timeStampSpan), _modelParameters); // taking into
                                                                               // account that time
                                                                               // is reversed
  }

  public double[] getCoords()
  {
    return _modelParameters;
  }
}