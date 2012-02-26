package se.sics.stochastic.kriging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kriging.core.Kriging;
import se.sics.stochastic.helpers.DataConverter;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

public class SimulatedAnnealing {
	Logger logger = LoggerFactory.getLogger(SimulatedAnnealing.class);

	private SASettings settings;
	private String fname;

	public SimulatedAnnealing() {
		// create the SK object with the default settings
		settings = new SASettings();
	}

	public void reset(SASettings settings) {
		this.settings = settings;
	}

	public void load() throws MWException {
		// serialize the setting to temp file and save the filename
		String tmpProp = "java.io.tmpdir";
		String tmpDir = System.getProperty(tmpProp);
		Random r = new Random();
		fname = tmpDir + "/" + r.nextInt(Integer.MAX_VALUE);
		try {
			Writer wr = new FileWriter(fname, false);
			wr.write(Integer.toString(settings.getNt()) + "\n");
			wr.write(Double.toString(settings.getFunctol()) + "\n");
			wr.write(Double.toString(settings.getParamtol()) + "\n");
			wr.write(Integer.toString(settings.getMaxEval()) + "\n");
			wr.write(Integer.toString(settings.getNeps()) + "\n");
			wr.flush();
			wr.close();
			logger.debug("settings loaded. filename={}", fname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//TODO: this method is to be removed because the implementation was changed to getPartitionMSE using pMSE.
	public double[][] mseMin(String model, double[] maxX, double[] minX,
			double threshold) throws MWException {
		double[][] result;
		MWNumericArray mMax = DataConverter.J2MDouble(maxX), mMin = DataConverter
				.J2MDouble(minX);
		Kriging mKrg = new Kriging();
		Object[] mResults = mKrg.mseMin(1, DataConverter.JString2M(model),
				mMax, mMin, threshold, DataConverter.JString2M(fname));

		result = DataConverter.M2JDouble2((MWNumericArray) mResults[0]);
		return result;
	}

	public ExpPoint getPartitionMSE(String model, double[] ub, double[] lb)
			throws MWException {
		MWNumericArray mUb = DataConverter.J2MDouble(ub), mLb = DataConverter
				.J2MDouble(lb);
		Kriging mKrg = new Kriging();
		Object[] mResults = mKrg.pMSE(2, DataConverter.JString2M(model), mUb,
				mLb, DataConverter.JString2M(fname));
		double[] oPoint = DataConverter
				.M2JDouble2((MWNumericArray) mResults[0])[0];
		double oF = DataConverter.M2JDouble((MWNumericArray) mResults[1])[0];
		mKrg.dispose();
		return (new ExpPoint(oF, oPoint));
	}

	public ExpPoint getOptimal(String model, double[] maxX, double[] minX)
			throws MWException {
		MWNumericArray mMax = DataConverter.J2MDouble(maxX), mMin = DataConverter
				.J2MDouble(minX);
		Kriging mKrg = new Kriging();
		Object[] mResults = mKrg.predictMin(2, DataConverter.JString2M(model),
				mMax, mMin, DataConverter.JString2M(fname));
		double[] oPoint = DataConverter
				.M2JDouble2((MWNumericArray) mResults[0])[0];
		double oF = DataConverter.M2JDouble((MWNumericArray) mResults[1])[0];
		mKrg.dispose();
		return (new ExpPoint(oF, oPoint));

	}
}
