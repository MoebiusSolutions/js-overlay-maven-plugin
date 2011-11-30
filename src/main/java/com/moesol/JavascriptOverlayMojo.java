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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 * @phase generate-sources
 * @goal overlay
 * @requiresDependencyResolution compile
 */
public class JavascriptOverlayMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        Config config = new Config();
        config.newPackage = toPackage;
        config.oldPackage = fromPackage;
        config.sourcePackage = sourcePackage;
        config.outputDirectory = outputDirectory;
        config.log = getLog();
        config.generateInterface = generateInterface;
        JavaScriptOverlayGenerator gen = new JavaScriptOverlayGenerator(config);
        try {
            gen.generate();
            // add new generated source to build class path
            project.addCompileSourceRoot(outputDirectory);
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoExecutionException("Error", ex);
        }
    }
    /**
     * The original package to be renamed.
     * Requires the toPackage to be set.
     *
     * @parameter expression="${overlay.fromPackage}"
     */
    private String fromPackage;
    /**
     * The new package name for the javascript overlay objects.
     * This is optional
     *
     * @parameter expression="${overlay.toPackage}"
     */
    private String toPackage;
    /**
     * The source package to search for POJOs.
     *
     * @parameter expression="${overlay.sourcePackage}"
     * @required
     */
    private String sourcePackage;
    /**
     * The directory to write the java files to.
     *
     * @parameter expression="${overlay.outputDirectory}" default-value="${basedir}/target/generated-sources/js-overlay"
     */
    private String outputDirectory;
    /**
     * The Maven project instance for the executing project.
     *
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;
    /**
     * Generate interfaces for each Jso object.
     *
     * @parameter expression="${overlay.generateInterface}" default-value="true"
     */
    private boolean generateInterface;

}
