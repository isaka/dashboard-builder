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
package org.jboss.dashboard.toolkit.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigValueWriter extends ConfigWriter {

    public ConfigValueWriter(String factoryPath) {
        super(factoryPath, factoryPath + "/etc/extras/comments");
    }

    protected ConfigValueWriter(String factoryPath, String commentsPath) {
        super(factoryPath, commentsPath);
    }

    public void writeProperty(String factoryPropertyPath, String value) throws IOException {
        String factoryComponent;
        String propName;

        int propertyStart = factoryPropertyPath.lastIndexOf(PROPERTY_SEPARATOR);
        factoryComponent = factoryPropertyPath.substring(0, propertyStart);
        propName = factoryPropertyPath.substring(propertyStart + 1, factoryPropertyPath.length());

        Properties props = new Properties();
        props.setProperty(propName, value);
        writePropertiesToComponent(factoryComponent, props);
    }

    public String readProperty(String factoryPropertyPath) throws IOException {
        String factoryComponent;
        String propName;

        int propertyStart = factoryPropertyPath.lastIndexOf(PROPERTY_SEPARATOR);
        factoryComponent = factoryPropertyPath.substring(0, propertyStart);
        propName = factoryPropertyPath.substring(propertyStart + 1, factoryPropertyPath.length());

        File propertiesFileToRead = getPropertiesFilePath(factoryComponent);
        Properties existingProperties = new Properties();
        if (propertiesFileToRead.exists()) {
            FileInputStream fis = new FileInputStream(propertiesFileToRead);
            existingProperties.load(fis);
            fis.close();
            return existingProperties.getProperty(propName);
        }
        return null;
    }
}