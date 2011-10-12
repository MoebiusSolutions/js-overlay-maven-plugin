/**
 * Copyright (C) 2011 Moebius Solutions, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moesol;

import java.io.File;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
public class ClassInfo {
    private String pckageName;
    private String className;
    private String origClassName;
    private final Config config;

    public ClassInfo(Config config){
        this.config = config;
    }
    
    public void setPackageName(String packageName) {
        this.pckageName = packageName.replace("/", ".");
        int lastChar = this.pckageName.length() - 1;
        if(this.pckageName.charAt(lastChar) == '.'){
            this.pckageName = this.pckageName.substring(0, lastChar);
        }
    }

    public void setClassName(String clsName){
        origClassName = clsName;
        className = clsName.replace("-", "_").replace("$", "_");
    }
    /**
     * Gets the package name by replacing config.oldPackage with config.newPackage if set.
     * @param packageName
     * @return
     */
    public String getNewPackageName() {
        String pak = pckageName;
        if (config.oldPackage != null && config.newPackage != null) {
            pak = pak.replaceAll(config.oldPackage, config.newPackage);
        }
        return pak;
    }

    public File getOutputDirectory() {
        return new File(config.outputDirectory, getNewPackageName().replace(".", "/"));
    }

    public File getOutputFile() {
        return new File(getOutputDirectory(), className + "Jso.java");
    }

    public Class getOriginalClass() throws ClassNotFoundException {
        return Class.forName(getOriginalPackage() + "." + origClassName);
    }

    private String getOriginalPackage() {
        return pckageName;
    }

    public String getClassName() {
        return className;
    }
    
}
