package org.apache.sysml.test.integration.applications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.sysml.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.utils.TestUtils;
import org.junit.runners.Parameterized.Parameters;

public abstract class ArimaTest extends AutomatedTestBase {

	protected final static String TEST_DIR = "applications/arima_box-jenkins/";

	protected final static double ACCURACY_TOL = Math.pow(10, -4);
	protected final static int DEFAULT_LOWER_P = 0;
	protected final static int DEFAULT_LOWER_D = 0;
	protected final static int DEFAULT_LOWER_Q = 0;
	protected final static int DEFAULT_CAPITAL_P = 0;
	protected final static int DEFAULT_CAPITAL_D = 0;
	protected final static int DEFAULT_CAPITAL_Q = 0;
	protected final static int DEFAULT_S = 7;

	protected static final String PREDEFINED_TEST_TIMESERIES_PATH = "./src/test/scripts/applications/arima_box-jenkins/testdata/pythondump.csv";

	protected int p, d, q, P, D, Q, s;

	public ArimaTest(int p, int d, int q, int P, int D, int Q, int s) {
		this.p = p;
		this.d = d;
		this.q = q;
		this.P = P;
		this.D = D;
		this.Q = Q;
		this.s = s;
	}

	@Parameters
	public static Collection<Object[]> data() {
		// TODO add more testcases for MA, ARMA, and seasonal models
		// Currently tested: AR(1), AR(5), AR(20)
		return Arrays.asList(new Object[][] { AR(1), AR(5), AR(20) });
	}

	protected static Object[] AR(int p) {
		return ARMA(p, DEFAULT_LOWER_Q);
	}

	protected static Object[] MA(int q) {
		return ARMA(DEFAULT_LOWER_P, q);
	}

	protected static Object[] ARMA(int p, int q) {
		return ARIMA(p, DEFAULT_LOWER_D, q);
	}

	protected static Object[] ARIMA(int p, int d, int q) {
		return SARIMA(p, d, q, DEFAULT_CAPITAL_P, DEFAULT_CAPITAL_D, DEFAULT_CAPITAL_Q, DEFAULT_S);
	}

	protected static Object[] SAR(int P, int s) {
		return SARMA(P, DEFAULT_LOWER_Q, DEFAULT_S);
	}

	protected static Object[] SMA(int Q, int s) {
		return SARMA(DEFAULT_LOWER_P, Q, DEFAULT_S);
	}

	protected static Object[] SARMA(int P, int Q, int s) {
		return SARIMA(DEFAULT_LOWER_P, DEFAULT_LOWER_D, DEFAULT_LOWER_Q, P, DEFAULT_CAPITAL_D, Q, s);
	}

	protected static Object[] SARIMA(int p, int d, int q, int P, int D, int Q, int s) {
		return new Object[] { p, d, q, P, D, Q, s };
	}

	protected String[] getRscriptProgramArgs() {
		List<String> args = new ArrayList<String>();

		args.add(PREDEFINED_TEST_TIMESERIES_PATH);
		args.add(Integer.toString(this.p) + "," + Integer.toString(this.d) + "," + Integer.toString(this.q));
		args.add(Integer.toString(this.P) + "," + Integer.toString(this.D) + "," + Integer.toString(this.Q));
		args.add(Integer.toString(this.s));
		args.addAll(getOptionalRProgrammArgs());
		args.add(expected("learnt.model"));

		return args.toArray(new String[args.size()]);
	}

	protected String[] getDMLProgramArgs(ScriptType scriptType) {
		List<String> proArgs = new ArrayList<String>();

		if (scriptType == ScriptType.PYDML) {
			proArgs.add("-python");
		}

		proArgs.add("-nvargs");
		proArgs.add("X=" + PREDEFINED_TEST_TIMESERIES_PATH);
		proArgs.add("p=" + Integer.toString(p));
		proArgs.add("d=" + Integer.toString(d));
		proArgs.add("q=" + Integer.toString(q));
		proArgs.add("P=" + Integer.toString(P));
		proArgs.add("D=" + Integer.toString(D));
		proArgs.add("Q=" + Integer.toString(Q));
		proArgs.add("s=" + Integer.toString(s));
		proArgs.add("dest=" + output("learnt.model"));
		proArgs.addAll(getOptionalDMLProgrammArgs());
		return proArgs.toArray(new String[proArgs.size()]);
	}

	protected void printTestInformation() {
		System.out.println("------------ BEGIN " + this.getTestName() + " " + scriptType + " TEST WITH {" + p + ", " + d
				+ ", " + q + ", " + P + ", " + D + ", " + Q + ", " + s + "} ------------");
	}

	protected void testArima(ScriptType scriptType) {
		if (this.shouldSkipTest())
			return;

		this.printTestInformation();

		this.scriptType = scriptType;

		this.getAndLoadTestConfiguration(this.getTestName());
		this.fullDMLScriptName = getScript();

		this.programArgs = this.getDMLProgramArgs(scriptType);
		this.rCmd = getRCmd(getRscriptProgramArgs());

		this.runTest(true, EXCEPTION_NOT_EXPECTED, null, -1);
		this.runRScript(true);

		HashMap<CellIndex, Double> arima_model_R = readRMatrixFromFS("learnt.model");
		HashMap<CellIndex, Double> arima_model_SYSTEMML = readDMLMatrixFromHDFS("learnt.model");
		TestUtils.compareMatrices(arima_model_R, arima_model_SYSTEMML, ACCURACY_TOL, "arima_model_R",
				"arima_model_SYSTEMML");
	}

	protected abstract String getTestName();

	protected abstract Collection<? extends String> getOptionalDMLProgrammArgs();

	protected abstract Collection<? extends String> getOptionalRProgrammArgs();
}
