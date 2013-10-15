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

public abstract class AbstractBufferedFilter implements IBufferedFilter {

    private static final int DEFAULT_BUFFER_SIZE = 128;

    protected float filteredValue;
    private final float circularBuffer[];
    private int circularIndex;
    private int count;

    public AbstractBufferedFilter(int bufferSize) {
        circularBuffer = new float[bufferSize];
        count = 0;
        circularIndex = 0;
        filteredValue = 0;
    }

    public AbstractBufferedFilter() {
        this(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public float processValue(float newValue) {
        pushValue(newValue);
        return getValue();
    }

    abstract protected void processNewValue(float newValue);

    protected float getValue() {
        return filteredValue;
    }
    
    protected void setValue(float value) {
        filteredValue = value;
    }

    protected void pushValue(float newValue) {
        if (count++ == 0) {
            primeBuffer(newValue);
        }
        processNewValue(newValue);
        circularBuffer[circularIndex] = newValue;
        circularIndex = nextIndex(circularIndex);
    }

    protected float getLastValue() {
        return circularBuffer[circularIndex];
    }

    protected int getBufferLength() {
        return circularBuffer.length;
    }

    protected void primeBuffer(float val) {
        for (int i = 0; i < circularBuffer.length; ++i) {
            circularBuffer[i] = val;
        }
        filteredValue = val;
    }

    protected int nextIndex(int curIndex) {
        if (curIndex + 1 >= circularBuffer.length) {
            return 0;
        }
        return curIndex + 1;
    }

}
