package se.sics.stochastic.kriging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import se.sics.kriging.core.Kriging;
import se.sics.stochastic.helpers.DataConverter;

public class StochasticKriging {
	Logger logger = LoggerFactory.getLogger(StochasticKriging.class);

	private SKSettings settings;
	private String fname;
	private String fmodel;

	public StochasticKriging() {
		// create the SK object with the default settings
		settings = new SKSettings();
	}

	public void reset(SKSettings settings) {
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
			wr.write(Integer.toString(settings.getGammaP()) + "\n");
			wr.write(Integer.toString(settings.getAlgor()) + "\n");
			wr.write(Integer.toString(settings.getMaxEval()) + "\n");
			wr.flush();
			wr.close();
			logger.debug("settings loaded. filename = {}", fname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String SKfit(double[][] designPoints, double[] valuesAtDPs,
			double[] stdvAtDPs) throws MWException {

		MWNumericArray mDesignPoints = DataConverter.J2MDouble2(designPoints), mValues = DataConverter
				.J2MDouble(valuesAtDPs), mStdv = DataConverter
				.J2MDouble(stdvAtDPs);
		Object[] mResult;

		Kriging mKrg = new Kriging();
		mResult = mKrg.SKfiting(1, mDesignPoints, mValues, mStdv,
				DataConverter.JString2M(fname));
		mKrg.dispose();

		MWCharArray output = (MWCharArray) mResult[0];
		fmodel = new String((char[]) output.getData());
		return fmodel;
	}

	public void visualize(double[][] designPoints, double[] valuesAtDPs,
			double[] maxX, double[] minX) throws MWException, IOException {
		if (maxX.length != 2) {
			System.out.println("Unable to draw graph visualization.");
			return;
		}
		MWNumericArray mDesignPoints = DataConverter.J2MDouble2(designPoints), mValues = DataConverter
				.J2MDouble(valuesAtDPs), mMax = DataConverter.J2MDouble(maxX), mMin = DataConverter
				.J2MDouble(minX);
		Kriging mKrg = new Kriging();
		mKrg.drawGraph(0, DataConverter.JString2M(fmodel), mMax, mMin,
				mDesignPoints, mValues);
//		System.in.read();
//		mKrg.dispose();
	}
	

	public SKSettings getSettings() {
		return settings;
	}

	public String getFname() {
		return fname;
	}

}
