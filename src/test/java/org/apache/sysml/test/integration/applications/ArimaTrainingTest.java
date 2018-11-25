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

import org.apache.sysml.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.utils.TestUtils;
import org.junit.runners.Parameterized.Parameters;

public abstract class ArimaTrainingTest extends AutomatedTestBase {

	protected final static String TEST_DIR = "applications/arima_box-jenkins/";
	protected final static String TEST_NAME = "arima_training";
	protected String TEST_CLASS_DIR = TEST_DIR + ArimaTrainingTest.class.getSimpleName() + "/";

	private final static double ACCURACY_TOL = Math.pow(10, -4);
	private final static int DEFAULT_LOWER_P = 0;
	private final static int DEFAULT_LOWER_D = 0;
	private final static int DEFAULT_LOWER_Q = 0;
	private final static int DEFAULT_CAPITAL_P = 0;
	private final static int DEFAULT_CAPITAL_D = 0;
	private final static int DEFAULT_CAPITAL_Q = 0;
	private final static int DEFAULT_S = 7;

	private static final String PREDEFINED_TEST_TIMESERIES_PATH = "./src/test/scripts/applications/arima_box-jenkins/testdata/pythondump.csv";

	protected int max_func_invoc, p, d, q, P, D, Q, s, include_mean, useJacobi;

	public ArimaTrainingTest(int p, int d, int q, int P, int D, int Q, int s) {
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

	private static Object[] AR(int p) {
		return ARMA(p, DEFAULT_LOWER_Q);
	}

	private static Object[] MA(int q) {
		return ARMA(DEFAULT_LOWER_P, q);
	}

	private static Object[] ARMA(int p, int q) {
		return ARIMA(p, DEFAULT_LOWER_D, q);
	}

	private static Object[] ARIMA(int p, int d, int q) {
		return SARIMA(p, d, q, DEFAULT_CAPITAL_P, DEFAULT_CAPITAL_D, DEFAULT_CAPITAL_Q, DEFAULT_S);
	}

	private static Object[] SAR(int P, int s) {
		return SARMA(P, DEFAULT_LOWER_Q, DEFAULT_S);
	}

	private static Object[] SMA(int Q, int s) {
		return SARMA(DEFAULT_LOWER_P, Q, DEFAULT_S);
	}

	private static Object[] SARMA(int P, int Q, int s) {
		return SARIMA(DEFAULT_LOWER_P, DEFAULT_LOWER_D, DEFAULT_LOWER_Q, P, DEFAULT_CAPITAL_D, Q, s);
	}

	private static Object[] SARIMA(int p, int d, int q, int P, int D, int Q, int s) {
		return new Object[] { p, d, q, P, D, Q, s };
	}

	@Override
	public void setUp() {
		this.addTestConfiguration(TEST_CLASS_DIR, TEST_NAME);
	}

	protected void testArima(ScriptType scriptType) {
		if (this.shouldSkipTest())
			return;

		System.out.println("------------ BEGIN " + TEST_NAME + " " + scriptType + " TEST WITH {" + max_func_invoc + ", "
				+ p + ", " + d + ", " + q + ", " + P + ", " + D + ", " + Q + ", " + s + ", " + include_mean + ", "
				+ useJacobi + "} ------------");

		this.scriptType = scriptType;

		this.getAndLoadTestConfiguration(TEST_NAME);
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

	private String[] getRscriptProgramArgs() {
		String[] args = new String[5];

		args[0] = PREDEFINED_TEST_TIMESERIES_PATH;
		args[1] = Integer.toString(this.p) + "," + Integer.toString(this.d) + "," + Integer.toString(this.q);
		args[2] = Integer.toString(this.P) + "," + Integer.toString(this.D) + "," + Integer.toString(this.Q);
		args[3] = Integer.toString(this.s);
		args[4] = expected("learnt.model");

		return args;
	}

	private String[] getDMLProgramArgs(ScriptType scriptType) {
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
		return proArgs.toArray(new String[proArgs.size()]);
	}
}
