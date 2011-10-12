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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
public class JavaScriptOverlayGenerator {

    private ClassLoader loader;
    private final Config config;

    public JavaScriptOverlayGenerator(Config config) {
        this.config = config;
    }

    /**
     * Main method to generate GWT Javascript Overlay types
     * @throws ClassNotFoundException when sourcePackage does not exist
     * @throws IOException 
     */
    public void generate() throws ClassNotFoundException, IOException, IntrospectionException {
        generate(new File(config.outputDirectory));
    }

    /**
     * Determine if source is a directory or jar file then call correct methods
     * @param targetDir
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    private void generate(File targetDir) throws ClassNotFoundException, IOException, IntrospectionException {
        loader = Thread.currentThread().getContextClassLoader();

        String packageName = config.sourcePackage.replace(".", File.separator) + File.separator;
        URL packageURL = loader.getResource(packageName);
        String protocol = packageURL.getProtocol();
        List<ClassInfo> list = null;
        if ("jar".equals(protocol)) {
            list = processJar(targetDir);
        } else {
            list = processDirectory(packageName, targetDir);
        }
        writeJso(list);
    }

    /**
     * Read a jar file and add all matching classfiles to the List
     * @param targetDir
     * @return List<ClassInfo> containing all matching classes from the jar
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    List<ClassInfo> processJar(File targetDir) throws IOException, ClassNotFoundException {
        config.log.debug("Procesing jar file");
        ArrayList<ClassInfo> list = new ArrayList<ClassInfo>();
        String searchPath = config.sourcePackage.replace(".", "/") + File.separator;
        URL packageURL = loader.getResource(searchPath);
        String jarFileName;
        JarFile jf;
        Enumeration<JarEntry> jarEntries;
        String entryName;

        // build jar file name
        jarFileName = packageURL.getFile();
        jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
        long jarTime = new File(jarFileName).lastModified();
        File genDirectory = new File(config.outputDirectory);
        long genTime = genDirectory.lastModified();
        if (genTime >= jarTime) {
            config.log.info("Generated source up to date, skipping");
            return list;
        }
        config.log.info("Writing Javascript Overlay files");
        jf = new JarFile(jarFileName);
        config.log.info("Opening jar " + jarFileName);
        jarEntries = jf.entries();
        while (jarEntries.hasMoreElements()) {
            entryName = jarEntries.nextElement().getName();
            config.log.debug("Entry " + entryName);
            if (entryName.startsWith(searchPath) && entryName.endsWith(".class")) {
                String path = entryName.substring(0, entryName.length() - 6);
                String className = path.substring(path.lastIndexOf('/') + 1);
                String packageName = path.substring(0, path.lastIndexOf('/')).replace("/", ".");
                ClassInfo ci = new ClassInfo(config);
                ci.setPackageName(packageName);
                ci.setClassName(className);
                list.add(ci);
            }
        }
        return list;
    }

    /**
     * Recursively search a directory for all matching class files
     * @param packageName
     * @param targetDir
     * @return
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    List<ClassInfo> processDirectory(String packageName, File targetDir) throws ClassNotFoundException, IOException {
        config.log.debug("processing directory " + packageName);
        ArrayList<ClassInfo> list = new ArrayList<ClassInfo>();
        URL packageURL = loader.getResource(packageName);
        File folder = new File(packageURL.getFile());
        File[] contenuti = folder.listFiles();
        String entryName;
        for (File actual : contenuti) {
            if (actual.isDirectory()) {
                list.addAll(processDirectory(packageName + actual.getName() + "/", targetDir));
            } else {
                entryName = actual.getName();
                if (entryName.endsWith("class")) {
                    entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                    ClassInfo ci = new ClassInfo(config);
                    ci.setPackageName(packageName);
                    ci.setClassName(entryName);
                    list.add(ci);
                }
            }
        }
        return list;
    }

    /**
     * Take a List of ClassInfo objects and create the directory for each one, then call
     * writeJso for each ClassInfo
     * @param ci
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    void writeJso(List<ClassInfo> ci) throws IOException, ClassNotFoundException, IntrospectionException {
        for (ClassInfo classInfo : ci) {
            config.log.debug("Creating directory " + classInfo.getOutputDirectory().getAbsolutePath());
            File packageDir = classInfo.getOutputDirectory();
            packageDir.mkdirs();
            writeJso(classInfo);
        }
    }

    void writeJso(ClassInfo classInfo) throws ClassNotFoundException, IOException, IntrospectionException {
        if (classInfo.getClassName().equals("package_info")) {
            config.log.info("Skipping class package_info");
            return;
        }
        Class<?> cls = classInfo.getOriginalClass();
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(classInfo.getOutputFile()));
        PrintStream ps = new PrintStream(fos);
        ps.printf("package %s;%n", classInfo.getNewPackageName());
        ps.printf("public class %sJso extends com.google.gwt.core.client.JavaScriptObject {%n", classInfo.getClassName());
        ps.printf("  protected %sJso(){}%n", classInfo.getClassName());
        PropertyDescriptor[] methods = getMethods(cls);
        for (PropertyDescriptor propDescriptor : methods) {
            writeReadFunction(propDescriptor.getReadMethod(), ps);
            writeWriteFunction(propDescriptor.getWriteMethod(), ps);
        }
        XmlRootElement rootAnn = cls.getAnnotation(XmlRootElement.class);
        if (rootAnn != null) {
            ps.printf("  public static native %sJso eval%s(String jsonText) /*-{return eval('(' + jsonText + ')');}-*/;%n", classInfo.getClassName(), classInfo.getClassName());
            ps.printf("  public static native com.google.gwt.core.client.JsArray<%sJso> eval%sArray(String jsonText) /*-{return eval('(' + jsonText + ')');}-*/;%n", classInfo.getClassName(), classInfo.getClassName());
        }
        ps.printf("}%n");
        ps.close();
        fos.close();
    }

    void writeReadFunction(Method method, PrintStream ps) {
        if (method == null) {
            return;
        }
        String returnType = getType(method);
        String methodName = method.getName();
        String lowerMethodName = getPropertyName(methodName);
        if (lowerMethodName.equals("default")) {
            config.log.warn("Skipping property with name of Default");
            return;
        }
        if ("date".equals(returnType)) {
            ps.printf("  public final native java.lang.String %s()/*-{return new String(this.%s);}-*/;%n", methodName, lowerMethodName);
        } else {
            ps.printf("  public final native %s %s()/*-{return this.%s;}-*/;%n", returnType, methodName, lowerMethodName);
        }
    }

    void writeWriteFunction(Method method, PrintStream ps) {
        if (method == null) {
            return;
        }
        String paramType = getType(method);
        String methodName = method.getName();
        String lowerMethodName = getPropertyName(methodName);
        if (lowerMethodName.equals("default")) {
            config.log.warn("Skipping property with name of Default");
            return;
        }
        if ("date".equals(paramType)) {
            ps.printf("  public final native void %s(java.lang.String value)/*-{this.%s = value;}-*/;%n", methodName, paramType, lowerMethodName);
        } else {
            ps.printf("  public final native void %s(%s value)/*-{this.%s = value;}-*/;%n", methodName, paramType, lowerMethodName);
        }
    }

    /**
     * Gets the declared methods in a class and returns an array of methods
     * for all bean properties (get/set)
     * 
     * If only getters exist they will be ignored
     * @param cls
     * @return 
     */
    PropertyDescriptor[] getMethods(Class cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls, Object.class);
        PropertyDescriptor[] methodDescriptors = beanInfo.getPropertyDescriptors();
        return methodDescriptors;
    }

    /**
     * Returns a javascript friendly type for the java type.
     * Converts an XMLGregorianCalendar type to an interal date
     * which is converted to a String wrapper around the long of the data.
     * Appends Jso to all non primitive and java.xxxx types
     * @param method
     * @return 
     */
    String getType(Method method) {
        Class<?> type = method.getReturnType();
        if ("void".equals(type.getName())) {
            type = method.getParameterTypes()[0];
        }
        if (type.getName().equals("javax.xml.datatype.XMLGregorianCalendar")) {
            return "date";
        }
        String name = type.getName();
        int lastDot = name.lastIndexOf(".");
        if (lastDot == -1) {
            return name;
        }
        ClassInfo ci = new ClassInfo(config);
        ci.setPackageName(name);
        name = ci.getNewPackageName();
        if (name.startsWith("java") == false) {
            name = name.replace("$", "_") + "Jso";
        }
        return name;
    }

    /**
     * Changes the first character of the method name to lower case.
     * @param name
     * @return 
     */
    String getPropertyName(String name) {
        if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }
        name = Introspector.decapitalize(name);
        return name;
    }

    void setLoader(ClassLoader classLoader) {
        this.loader = classLoader;
    }
}
