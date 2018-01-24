import java.util.ArrayList;

import junit.framework.TestCase;

public class Normaliser
{
  double _min;
  double _max;
  double _range;
  private boolean _flipAxis;


  public Normaliser(ArrayList<Double> items, boolean flipAxis)
  {
    boolean first = true;
    for (double num : items)
    {
      if (first)
      {
        _min = num;
        _max = num;
        first = false;
      }
      else
      {
        if (num < _min)
        {
          _min = num;
        }
        if (num > _max)
        {
          _max = num;
        }
      }
    }

    _range = _max - _min;
    
    _flipAxis = flipAxis;
  }

  public double normalise(double val)
  {
    if(_flipAxis)
    {
      return 1 - ((val - _min) / _range);
    }
    else
    {
      return (val - _min) / _range;
    }
  }

  public double deNormalise(double val)
  {
    if(_flipAxis)
    {
      return (((1 - val) * _range) + _min);
    }
    else
    {
      return (val * _range) + _min;
    }
  }

  public static class TestNormaliser extends TestCase
  {
    public void testNormal()
    {
      ArrayList<Double> data = new ArrayList<Double>();
      data.add(12d);
      data.add(3d);
      data.add(5d);
      data.add(103d);
      Normaliser norm = new Normaliser(data, false);
      
      assertEquals(3d, norm._min, 0d);
      assertEquals(103d, norm._max, 0d);
      assertEquals(100d, norm._range, 0d);
      
      assertEquals(0d, norm.normalise(3d), 0d);
      assertEquals(1d, norm.normalise(103d), 0d);
      assertEquals(0.5d, norm.normalise(53d), 0d);
      
      assertEquals(3d, norm.deNormalise(0d), 0d);
      assertEquals(103d, norm.deNormalise(1d), 0d);
      assertEquals(53d, norm.deNormalise(0.5d), 0d);
      
    }

    public void testFlipped()
    {
      ArrayList<Double> data = new ArrayList<Double>();
      data.add(12d);
      data.add(3d);
      data.add(5d);
      data.add(103d);
      Normaliser norm = new Normaliser(data, true);
      
      assertEquals(3d, norm._min, 0d);
      assertEquals(103d, norm._max, 0d);
      assertEquals(100d, norm._range, 0d);
      
      assertEquals(1d, norm.normalise(3d), 0d);
      assertEquals(0d, norm.normalise(103d), 0d);
      assertEquals(0.5d, norm.normalise(53d), 0d);
      
      assertEquals(3d, norm.deNormalise(1d), 0d);
      assertEquals(103d, norm.deNormalise(0d), 0d);
      assertEquals(53d, norm.deNormalise(0.5d), 0d);
      
    }

  }

}
