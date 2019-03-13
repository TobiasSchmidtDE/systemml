package org.apache.sysml.test.integration.applications;

import java.util.Collection;

public class ArimaResidualsTest extends ArimaTest {

	private int[] weights;

	protected final static String TEST_NAME = "arima_residuals";
	protected String TEST_CLASS_DIR = TEST_DIR + ArimaResidualsTest.class.getSimpleName() + "/";

	public ArimaResidualsTest(int p, int d, int q, int P, int D, int Q, int s) {
		super(p, d, q, P, D, Q, s);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getTestName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<? extends String> getOptionalDMLProgrammArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<? extends String> getOptionalRProgrammArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub

	}

}
