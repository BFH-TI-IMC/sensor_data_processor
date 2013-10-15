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

public class SimpleMovingAverageFilter {
	
	private final float circularBuffer[];
	private float avg;
	private int circularIndex;
	private int count;
	
	public SimpleMovingAverageFilter(int k)
	{
		circularBuffer = new float[k];
		count = 0;
		circularIndex = 0;
		avg = 0;
	}

	
	public float getValue() {
		return avg;
	}

	public void pushValue(float x) {
		if (count++ == 0) {
			primeBuffer(x);
		}
		float lastValue = circularBuffer[circularIndex];
		avg = avg + (x - lastValue) / circularBuffer.length;
		circularBuffer[circularIndex] = x;
		circularIndex = nextIndex(circularIndex);
	}

	public long getCount() {
		return count;
	}

	private void primeBuffer(float val) {
		for (int i = 0; i < circularBuffer.length; ++i) {
			circularBuffer[i] = val;
		}
		avg = val;
	}

	private int nextIndex(int curIndex) {
		if (curIndex + 1 >= circularBuffer.length) {
			return 0;
		}
		return curIndex + 1;
	}

}
