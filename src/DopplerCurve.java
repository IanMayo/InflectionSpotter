

import java.util.ArrayList;

public class DopplerCurve
{
  /** times of samples
   * 
   */
  private final ArrayList<Long> _times;
  
  /** measured frequencies
   * 
   */
  private final ArrayList<Double> _freqs;

  private final long _startTime;

  public DopplerCurve(final ArrayList<Long> times, final ArrayList<Double> freqs)
  {
    _times = times;
    _freqs = freqs;
    
    // change the times, so they start at zero (to keep time parameters small)
    _startTime = _times.get(0);
    
    // remove this from the time values
    for(int i=0;i<times.size();i++)
    {
      times.set(i, times.get(i) - _startTime);
    }
    
    // ok, fit the data
    
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
  
  public double inflectionTime()
  {
    return -1;
  }

  /** calculate the value on the curve at this time
   * 
   * @param t time
   * @return frequency at this time
   */
  public double valueAt(final long t)
  {
    return 149.5 + Math.random() * 1d;
  }
}
