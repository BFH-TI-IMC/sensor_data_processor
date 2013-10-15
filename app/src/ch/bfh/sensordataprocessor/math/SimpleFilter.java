/*
 * Copyright 2013 Bern University of Applied Sciences BFH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.bfh.sensordataprocessor.math;

public class SimpleFilter {

	static final float LOW_PASS_A = 0.1f;

	static final float HIGH_PASS_A = 0.7f;

	static float lowPass(float current, float last) {
		return last * (1.0f - LOW_PASS_A) + current * LOW_PASS_A;
	}

	static float highPass(float current, float last, float filtered) {
		return HIGH_PASS_A * (filtered + current - last);
	}

}
