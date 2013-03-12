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
package org.jboss.dashboard.displayer.table;

import org.jboss.dashboard.commons.comparator.ComparatorByCriteria;
import org.jboss.dashboard.commons.filter.FilterByCriteria;

public interface TableModel {

    int getColumnPosition(String columnName);
    String getColumnId(int index);
    String getColumnName(int index);
    int getColumnCount();
    int getRowCount();
    Object getValue(int row, String propertyName);
    Object getValueAt(int row, int column);
    void filter(FilterByCriteria filter);
    void sort(ComparatorByCriteria comparator);
}