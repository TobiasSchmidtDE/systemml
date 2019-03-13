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

import java.util.Collection;
import java.util.Collections;

public class ArimaTrainingTest extends ArimaTest {

	protected final static String TEST_NAME = "arima_training";
	protected String TEST_CLASS_DIR = TEST_DIR + ArimaTrainingTest.class.getSimpleName() + "/";

	public ArimaTrainingTest(int p, int d, int q, int P, int D, int Q, int s) {
		super(p, d, q, P, D, Q, s);
	}

	@Override
	public void setUp() {
		this.addTestConfiguration(TEST_CLASS_DIR, TEST_NAME);
		this.disableOutAndExpectedDeletion();
	}

	@Override
	protected String getTestName() {
		return TEST_NAME;
	}

	@Override
	protected Collection<? extends String> getOptionalDMLProgrammArgs() {
		return Collections.emptySet();
	}

	@Override
	protected Collection<? extends String> getOptionalRProgrammArgs() {
		return Collections.emptySet();
	}

}
