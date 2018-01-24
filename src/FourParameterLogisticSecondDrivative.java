import org.apache.commons.math3.analysis.UnivariateFunction;

public class FourParameterLogisticSecondDrivative implements UnivariateFunction {

	public double[] coeff;
	
	@Override
	public double value(double x) {
		double a = coeff[0];
		double b = coeff[1];
		double c = coeff[2];
		double d = coeff[3];
		double cb = Math.pow(c, b);
		double xb = Math.pow(x, b);
		return -(b*cb*Math.pow(x, b-2)*(b*cb-cb-b*xb-xb)*(a-d))/Math.pow(cb+xb,3);
		
	}

}
