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

import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
public class Config {

    /**
     * Part of the old package name to be replaced
     */
    private String oldPackage;
    /**
     * The new package name to generate to
     */
    private String newPackage;
    /**
     * The source package to generate from
     */
    private String sourcePackage;
    /**
     * The directory to output the generated source
     */
    private String outputDirectory;
    /**
     * Log object used by maven
     */
    private Log log;
    /**
     * True if interfaces should be generated for all overlay objects
     */
    private boolean generateInterface;

    /**
     * @return the oldPackage
     */
    public String getOldPackage() {
        return oldPackage;
    }

    /**
     * @param oldPackage the oldPackage to set
     */
    public void setOldPackage(String oldPackage) {
        this.oldPackage = oldPackage;
    }

    /**
     * @return the newPackage
     */
    public String getNewPackage() {
        return newPackage;
    }

    /**
     * @param newPackage the newPackage to set
     */
    public void setNewPackage(String newPackage) {
        this.newPackage = newPackage;
    }

    /**
     * @return the sourcePackage
     */
    public String getSourcePackage() {
        return sourcePackage;
    }

    /**
     * @param sourcePackage the sourcePackage to set
     */
    public void setSourcePackage(String sourcePackage) {
        this.sourcePackage = sourcePackage;
    }

    /**
     * @return the outputDirectory
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * @param outputDirectory the outputDirectory to set
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @return the generateInterface
     */
    public boolean isGenerateInterface() {
        return generateInterface;
    }

    /**
     * @param generateInterface the generateInterface to set
     */
    public void setGenerateInterface(boolean generateInterface) {
        this.generateInterface = generateInterface;
    }
}
