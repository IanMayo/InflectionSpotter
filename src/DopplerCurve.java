
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DopplerCurve
{
  
  /** our function definition for a doppler curve
   * 
   * @author Ian
   *
   */
  private static class DopplerCurveFitter extends AbstractCurveFitter
  {

    @Override
    protected LeastSquaresProblem getProblem(
        final Collection<WeightedObservedPoint> arg0)
    {
      throw new NotImplementedException();
    }

  }

  /**
   * times of samples
   * 
   */
  private final ArrayList<Long> _times;

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

    // change the times, so they start at zero (to keep time parameters small)
    _startTime = _times.get(0);

    // remove this from the time values
    for (int i = 0; i < times.size(); i++)
    {
      times.set(i, times.get(i) - _startTime);
    }

    // ok, collate the data

    final WeightedObservedPoints obs = new WeightedObservedPoints();
    for (int i = 0; i < times.size(); i++)
    {
      obs.add(_times.get(i), _freqs.get(i));
    }

    // now Instantiate a third-degree polynomial fitter.
    final AbstractCurveFitter fitter = new DopplerCurveFitter();

    // Retrieve fitted parameters (coefficients of the polynomial function).
    final double[] coeff = fitter.fit(obs.toList());

    System.out.println(coeff[0]);

    // now check if there's an inflection point

    // and store the equation parameters
  }

  public boolean hasInflection()
  {
    return false;
  }

  public double inflectionFreq()
  {
    return -1;
  }

  public long inflectionTime()
  {
    return -1;
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
    return 149.5 + Math.random() * 1d;
  }
}
