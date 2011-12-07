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
    private Config config;

    /**
     * Constructor
     * @param config 
     */
    public ClassInfo(Config config){
        this.config = config;
    }
    
    /**
     * Sets the package name for this type
     * @param packageName 
     */
    public void setPackageName(String packageName) {
        this.setPckageName(packageName.replace("/", "."));
        int lastChar = this.getPckageName().length() - 1;
        if(this.getPckageName().charAt(lastChar) == '.'){
            this.setPckageName(this.getPckageName().substring(0, lastChar));
        }
    }

    /**
     * Sets the classname for this type
     * @param clsName 
     */
    public void setClassName(String clsName){
        setOrigClassName(clsName);
        className = clsName.replace("-", "_").replace("$", "_");
    }
    /**
     * Gets the package name by replacing config.oldPackage with config.newPackage if set.
     * @param packageName
     * @return
     */
    public String getNewPackageName() {
        String pak = getPckageName();
        if (getConfig().getOldPackage() != null && getConfig().getNewPackage() != null) {
            pak = pak.replaceAll(getConfig().getOldPackage(), getConfig().getNewPackage());
        }
        return pak;
    }

    /**
     * Returns the directory to write this class to.
     * @return 
     */
    public File getOutputDirectory() {
        return new File(getConfig().getOutputDirectory(), getNewPackageName().replace(".", "/"));
    }

    /**
     * Returns a {@link File} for the class to be generated
     * @return
     * @throws ClassNotFoundException 
     */
    public File getOutputFile() throws ClassNotFoundException {
        if(getOriginalClass().isEnum()){
            return new File(getOutputDirectory(), getClassName() + ".java");
        }
        return new File(getOutputDirectory(), getClassName() + "Jso.java");
    }

    /**
     * The source class being generated
     * @return
     * @throws ClassNotFoundException 
     */
    public Class getOriginalClass() throws ClassNotFoundException {
        return Class.forName(getOriginalPackage() + "." + getOrigClassName());
    }

    /**
     * The original package
     * @return 
     */
    private String getOriginalPackage() {
        return getPckageName();
    }

    /**
     * The class name
     * @return 
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the pckageName
     */
    public String getPckageName() {
        return pckageName;
    }

    /**
     * @param pckageName the pckageName to set
     */
    public void setPckageName(String pckageName) {
        this.pckageName = pckageName;
    }

    /**
     * @return the origClassName
     */
    public String getOrigClassName() {
        return origClassName;
    }

    /**
     * @param origClassName the origClassName to set
     */
    public void setOrigClassName(String origClassName) {
        this.origClassName = origClassName;
    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return config;
    }
    
}
