/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.test.integration.applications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.sysml.runtime.matrix.MatrixCharacteristics;
import org.apache.sysml.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.utils.TestUtils;
import org.junit.runners.Parameterized.Parameters;

public abstract class ArimaCSSTest extends AutomatedTestBase {

	protected final static String TEST_DIR = "applications/arima_box-jenkins/";
	protected final static String TEST_NAME = "arima_css";
	protected final static String X_NAME = "testdata";
	protected final static String WEIGHTS_NAME = "testweights";
	protected final static String OUTPUT_NAME = "learnt.model";
	protected final static int DEFAULT_TIMESERIES_LENGTH = 120;

	protected String TEST_CLASS_DIR = TEST_DIR + ArimaTest.class.getSimpleName() + "/";

	protected int timeSeriesLength, p, d, q, P, D, Q, s;
	protected String solver;

	public ArimaCSSTest(int timeSeriesLength, int p, int d, int q, int P, int D, int Q, int s, String solver) {
		this.timeSeriesLength = timeSeriesLength;
		this.p = p;
		this.d = d;
		this.q = q;
		this.P = P;
		this.D = D;
		this.Q = Q;
		this.s = s;
		this.solver = solver;
	}

	@Parameters
	public static Collection<Object[]> data() {
		// return Arrays.asList(new Object[][] { SAR(3, 12) });
		return Arrays.asList(new Object[][] { AR(5), MA(7), ARMA(4, 6), ARIMA(3, 2, 3), SAR(3, 12), SMA(4, 6),
				SARMA(2, 6, 3), SARIMA(3, 2, 4, 2, 1, 3, 12), SARIMA(1, 0, 2, 0, 1, 4, 7, "forwardsub"),
				SARIMA(10, 3, 1, 4, 2, 4, 2, "forwardsub") });
	}

	protected static Object[] AR(int p) {
		return ARMA(p, 0);
	}

	protected static Object[] MA(int q) {
		return ARMA(0, q);
	}

	protected static Object[] ARMA(int p, int q) {
		return ARIMA(p, 0, q);
	}

	protected static Object[] ARIMA(int p, int d, int q) {
		return SARIMA(p, d, q, 0, 0, 0, 0);
	}

	protected static Object[] SAR(int P, int s) {
		return SARMA(P, 0, s);
	}

	protected static Object[] SMA(int Q, int s) {
		return SARMA(0, Q, s);
	}

	protected static Object[] SARMA(int P, int Q, int s) {
		return SARIMA(0, 0, 0, P, 0, Q, s);
	}

	protected static Object[] SARIMA(int p, int d, int q, int P, int D, int Q, int s) {
		return SARIMA(p, d, q, P, D, Q, s, "jacobi");
	}

	protected static Object[] SARIMA(int p, int d, int q, int P, int D, int Q, int s, String solver) {
		return customSARIMA(DEFAULT_TIMESERIES_LENGTH, p, d, q, P, D, Q, s, solver);
	}

	protected static Object[] customSARIMA(int timeSeriesLength, int p, int d, int q, int P, int D, int Q, int s,
			String solver) {
		return new Object[] { timeSeriesLength, p, d, q, P, D, Q, s, solver };
	}

	@Override
	public void setUp() {
		addTestConfiguration(TEST_CLASS_DIR, TEST_NAME);
	}

	protected void testArimaCSS(ScriptType scriptType) {
		if (shouldSkipTest())
			return;

		System.out.println("------------ BEGIN " + TEST_NAME + " " + scriptType + " TEST WITH {" + p + ", " + d + ", "
				+ q + ", " + P + ", " + D + ", " + Q + ", " + s + ", " + solver + "} ------------");
		this.scriptType = scriptType;

		getAndLoadTestConfiguration(TEST_NAME);

		generateTestData(this.timeSeriesLength, p + q + P + Q);

		String x_file = input(X_NAME + ".mtx");
		String weights_file = input(WEIGHTS_NAME + ".mtx");

		List<String> proArgs = new ArrayList<String>();
		proArgs.add("-nvargs");
		proArgs.add("X=" + x_file);
		proArgs.add("weights_src=" + weights_file);
		proArgs.add("p=" + Integer.toString(p));
		proArgs.add("d=" + Integer.toString(d));
		proArgs.add("q=" + Integer.toString(q));
		proArgs.add("P=" + Integer.toString(P));
		proArgs.add("D=" + Integer.toString(D));
		proArgs.add("Q=" + Integer.toString(Q));
		proArgs.add("s=" + Integer.toString(s));
		proArgs.add("solver=" + solver);
		proArgs.add("result_formate=MM");
		proArgs.add("dest=" + output(OUTPUT_NAME));

		programArgs = proArgs.toArray(new String[proArgs.size()]);
		fullDMLScriptName = getScript();

		rCmd = getRCmd(x_file, weights_file, expected(OUTPUT_NAME), Integer.toString(p), Integer.toString(d),
				Integer.toString(q), Integer.toString(P), Integer.toString(D), Integer.toString(Q),
				Integer.toString(s));

		runTest(true, EXCEPTION_NOT_EXPECTED, null, -1);

		runRScript(true);

		double tol = Math.pow(10, -8);
		HashMap<CellIndex, Double> arima_css_SYSTEMML = readDMLScalarFromHDFS("learnt.model");
		HashMap<CellIndex, Double> arima_css_R = readRScalarFromFS("learnt.model");
		TestUtils.compareMatrices(arima_css_R, arima_css_SYSTEMML, tol, "arima_css_R", "arima_css_SYSTEMML");
	}

	protected void generateTestData(int timeSeriesLength, int weightsLength) {
		double[][] timeSeries = getRandomMatrix(timeSeriesLength, 1, 1, 5, 1, System.currentTimeMillis());
		MatrixCharacteristics timeSeries_mc = new MatrixCharacteristics(timeSeriesLength, 1, -1, -1);
		writeInputMatrixWithMTD(X_NAME, timeSeries, true, timeSeries_mc);

		double[][] weights = getNonZeroRandomMatrix(weightsLength, 1, -2, 2, System.currentTimeMillis());
		MatrixCharacteristics weights_mc = new MatrixCharacteristics(weightsLength, 1, 0);
		writeInputMatrixWithMTD(WEIGHTS_NAME, weights, true, weights_mc);
	}
}
