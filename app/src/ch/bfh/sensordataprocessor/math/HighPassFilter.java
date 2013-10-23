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

public class HighPassFilter extends AbstractFilter implements IFilter {

    static final float STANDARD_HIGH_PASS_ALPHA = 0.7f;

    private final float alpha;

    private float filteredValue = 0;

    public HighPassFilter(float alpha) {
        this.alpha = alpha;
    }

    public HighPassFilter() {
        this(STANDARD_HIGH_PASS_ALPHA);
    }

    @Override
    public float processValue(float newValue) {
        float val = alpha * (filteredValue + newValue - getLastValue());
        setLastValue(newValue);
        filteredValue = val;
        return val;
    }

}
