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
    public String oldPackage;
    /**
     * The new package name to generate to
     */
    public String newPackage;
    /**
     * The source package to generate from
     */
    public String sourcePackage;
    /**
     * The directory to output the generated source
     */
    public String outputDirectory;
    /**
     * Log object used by maven
     */
    public Log log;
    /**
     * True if interfaces should be generated for all overlay objects
     */
    public boolean generateInterface;
}
