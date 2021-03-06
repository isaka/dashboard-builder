/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dashboard.displayer.chart;

public abstract class AbstractXAxisDisplayer extends AbstractChartDisplayer {

    /** The flag indicating if the X-aAxis labels should be displayed. */
    protected boolean showLabelsXAxis;

    /** The display angle for the X-axis labels. */
    protected int labelAngleXAxis;

    /** Display area below lines*/
    protected boolean showLinesArea;

    public AbstractXAxisDisplayer() {
        super();
        showLabelsXAxis = true;
        labelAngleXAxis = -45;
        showLinesArea = false;
    }

    public boolean isShowLabelsXAxis() {
        return showLabelsXAxis;
    }

    public void setShowLabelsXAxis(boolean showLabelsXAxis) {
        this.showLabelsXAxis = showLabelsXAxis;
    }

    public int getLabelAngleXAxis() {
        return labelAngleXAxis;
    }

    public void setLabelAngleXAxis(int labelAngleXAxis) {
        this.labelAngleXAxis = labelAngleXAxis;
    }

    public boolean isShowLinesArea() {
        return showLinesArea;
    }

    public void setShowLinesArea(boolean showLinesArea) {
        this.showLinesArea = showLinesArea;
    }
}
