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
import java.io.*;
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
    private ClassInfo topPackage;

    /**
     * Constructor.
     *
     * @param config
     */
    public JavaScriptOverlayGenerator(Config config) {
        this.config = config;
        ReturnType.setConfig(config);
    }

    /**
     * Main method to generate GWT Javascript Overlay types
     *
     * @throws ClassNotFoundException when sourcePackage does not exist
     * @throws IOException
     */
    public void generate() throws ClassNotFoundException, IOException, IntrospectionException {
        generate(new File(config.getOutputDirectory()));
    }

    /**
     * Determine if source is a directory or jar file then call correct methods
     *
     * @param targetDir
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void generate(File targetDir) throws ClassNotFoundException, IOException, IntrospectionException {
        loader = Thread.currentThread().getContextClassLoader();

        String packageName = config.getSourcePackage().replace(".", "/") + "/";
        URL packageURL = loader.getResource(packageName);
        String protocol = packageURL.getProtocol();
        List<ClassInfo> list;
        if ("jar".equals(protocol)) {
            list = processJar(targetDir);
        } else {
            list = processDirectory(packageName, targetDir);
        }
        writeJso(list);
    }

    /**
     * Read a jar file and add all matching classfiles to the List
     *
     * @param targetDir
     * @return List<ClassInfo> containing all matching classes from the jar
     * @throws IOException
     * @throws ClassNotFoundException
     */
    List<ClassInfo> processJar(File targetDir) throws IOException, ClassNotFoundException {
        config.getLog().debug("Procesing jar file");
        ArrayList<ClassInfo> list = new ArrayList<ClassInfo>();
        String searchPath = config.getSourcePackage().replace(".", "/") + "/";
        URL packageURL = loader.getResource(searchPath);
        String jarFileName;
        JarFile jf;
        Enumeration<JarEntry> jarEntries;
        String entryName;

        // build jar file name
        jarFileName = packageURL.getFile();
        jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
        jarFileName = jarFileName.replaceAll("%20", " ");
        long jarTime = new File(jarFileName).lastModified();
        File genDirectory = new File(config.getOutputDirectory());
        long genTime = genDirectory.lastModified();
        if (genTime >= jarTime) {
            config.getLog().info("Generated source up to date, skipping");
            return list;
        }
        config.getLog().info("Writing Javascript Overlay files");
        jf = new JarFile(jarFileName);
        config.getLog().info("Opening jar " + jarFileName);
        jarEntries = jf.entries();
        while (jarEntries.hasMoreElements()) {
            entryName = jarEntries.nextElement().getName();
            config.getLog().debug("Entry " + entryName);
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
     *
     * @param packageName
     * @param targetDir
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    List<ClassInfo> processDirectory(String packageName, File targetDir) throws ClassNotFoundException, IOException {
        config.getLog().debug("processing directory " + packageName);
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
     * Take a List of ClassInfo objects and create the directory for each one, then call writeJso for each ClassInfo
     *
     * @param ci
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void writeJso(List<ClassInfo> ci) throws IOException, ClassNotFoundException, IntrospectionException {
        topPackage = getTopPackage(ci);
        for (ClassInfo classInfo : ci) {
            config.getLog().debug("Creating directory " + classInfo.getOutputDirectory().getAbsolutePath());
            File packageDir = classInfo.getOutputDirectory();
            packageDir.mkdirs();
            writeJso(classInfo);
        }
        if(topPackage != null){
            writeArrayHelper(topPackage);
        }
    }

    /**
     * Write java class file to disk
     *
     * @param classInfo
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws IntrospectionException
     */
    void writeJso(ClassInfo classInfo) throws ClassNotFoundException, IOException, IntrospectionException {
        if (classInfo.getClassName().equals("package_info")) {
            config.getLog().info("Skipping class package_info");
            return;
        }
        Class<?> cls = classInfo.getOriginalClass();
        if (cls.isEnum()) {
            writeJavaEnum(classInfo);
            return;
        }
        PropertyDescriptor[] methods = getMethods(cls);
        generateInferfaces(classInfo, methods);
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(classInfo.getOutputFile()));
        PrintStream ps = new PrintStream(fos);
        ps.printf("package %s;%n", classInfo.getNewPackageName());
        ps.printf("public class %sJso extends com.google.gwt.core.client.JavaScriptObject %s{%n", classInfo.getClassName(), config.isGenerateInterface() ? "implements I" + classInfo.getClassName() : "");
        ps.printf("  protected %sJso(){}%n", classInfo.getClassName());
        for (PropertyDescriptor propDescriptor : methods) {
            writeReadFunction(propDescriptor.getReadMethod(), ps);
            writeWriteFunction(propDescriptor.getWriteMethod(), ps);
        }
        generateEvalMethods(classInfo, cls, ps);
        String genInterface = config.isGenerateInterface() ? "  @Override" : "";
        ps.printf("%s%n  public final native java.lang.String _getClassName()/*-{return '%s';}-*/;%n", genInterface, classInfo.getNewPackageName() + "." + classInfo.getClassName() + "Jso");
        // create getJson function
        ps.printf("%s%n  public final String _getJsonString(){%n    return new com.google.gwt.json.client.JSONObject(this).toString();%n  }%n", genInterface);

        ps.printf("}%n");
        ps.close();
        fos.close();
    }

    /**
     * Write getter methods
     *
     * @param method
     * @param ps
     */
    void writeReadFunction(Method method, PrintStream ps) {
        if (method == null) {
            return;
        }
        ReturnType returnType = ReturnType.getType(method);
        String methodName = method.getName();
        String lowerMethodName = returnType.getPropertyName(methodName);
        String override = config.isGenerateInterface() ? "@Override" : "";
        if (returnType.isEnum()) {
            if (returnType.isArray()) {
                readEnumList(ps, returnType, methodName, lowerMethodName);
            } else {
                ps.printf("  private final native java.lang.String _%s()/*-{return this[\"%s\"];}-*/;%n", methodName, lowerMethodName);
                ps.printf("  %s%n  public final %s %s(){%n    return %s.valueOf(_%s());    %n  }%n", override, returnType.getQualifiedReturnType(topPackage), methodName, returnType.getName(), methodName);
            }
        } else if (returnType.isDate()) {
            ps.printf("  %s%n  public final native java.lang.String %s()/*-{return new String(this[\"%s\"]);}-*/;%n", override, methodName, lowerMethodName);
        } else if (returnType.isList()) {
            if (returnType.isParameterTypeEnum()) {
                readEnumList(ps, returnType, methodName, lowerMethodName);
            } else {
                ps.printf("  private final native com.google.gwt.core.client.JsArray<com.google.gwt.core.client.JavaScriptObject> _%s()/*-{return this[\"%s\"];}-*/;%n", methodName, lowerMethodName);
                ps.printf("  %s%n  public final %s %s(){%n", override, returnType.getQualifiedReturnType(topPackage), methodName);
                ps.printf("    return new %s(_%s());%n    }%n", returnType.getQualifiedReturnType(topPackage), methodName);
            }
        } else {
            ps.printf("  %s%n  public final native %s %s()/*-{return this[\"%s\"];}-*/;%n", override, returnType.getQualifiedReturnImplType(topPackage), methodName, lowerMethodName);
        }
    }

    /**
     * Write the method for getting enums.
     *
     * @param ps
     * @param returnType
     * @param methodName
     * @param lowerMethodName
     */
    private void readEnumList(PrintStream ps, ReturnType returnType, String methodName, String lowerMethodName) {
        if (config.isGenerateInterface()) {
            ps.printf("  @Override%n");
        }
        if (returnType.isArray()) {
            ps.printf("  public final %s %s(){%n"
                + "    java.lang.String[] data = _%s();%n"
                + "    %s[] retData = new %s[data.length];%n"
                + "    for(int i=0; i<data.length; i++){%n"
                + "      retData[i] = %s.valueOf(data[i]);%n"
                + "    }%n"
                + "    return retData;%n  }%n",
                returnType.getQualifiedReturnType(topPackage),
                methodName,
                methodName,
                returnType.getParameterType(),
                returnType.getParameterType(),
                returnType.getParameterType());
            ps.printf("  public final native java.lang.String[] _%s()/*-{return this[\"%s\"];}-*/;%n", methodName, lowerMethodName);
        } else {
//            TODO fix this here, can not use instance variables
            ps.printf("  public final %s %s(){%n", returnType.getQualifiedReturnType(topPackage), methodName);
            ps.printf("    return new %s(_%s());%n",returnType.getQualifiedReturnType(topPackage), methodName);
            ps.printf("  }%n");
            ps.printf("  public final native com.google.gwt.core.client.JsArray _%s()/*-{return this[\"%s\"];}-*/;%n", methodName, lowerMethodName);
//            ps.printf("    java.lang.String[] data = _%s();%n", lowerMethodName);
//            ps.printf("    %s retData = new %s((com.google.gwt.core.client.JsArray<com.google.gwt.core.client.JavaScriptObject>)com.google.gwt.core.client.JsArray.createArray());%n", lowerMethodName, methodName);
//            ps.printf("    for(int i=0; i<data.length; i++){%n"
//                + "      retData.add(%s.valueOf(data[i]));%n"
//                + "    }%n"
//                + "    return retData;%n  }%n",
//                returnType.getQualifiedReturnType(topPackage),
//                returnType.getQualifiedReturnType(topPackage),
//                returnType.getParameterType());
        }
    }

    /**
     * Write the methods for setting enums.
     *
     * @param ps
     * @param type
     * @param methodName
     * @param lowerMethodName
     */
    private void writeEnumList(PrintStream ps, ReturnType type, String methodName, String lowerMethodName) {
        ps.printf("  public final native void _%s(java.lang.String[] value )/*-{this[\"%s\"] = value;}-*/;%n", methodName, lowerMethodName);
        if (config.isGenerateInterface()) {
            ps.printf("  @Override%n");
        }
        if (type.isArray()) {
            ps.printf("  public final void %s(%s[] value){%n"
                + "    java.lang.String[] data = new String[value.length];%n"
                + "    for(int i=0; i<data.length; i++){%n"
                + "      data[i] = value[i].name();%n"
                + "    }%n"
                + "    _%s(data);%n  }%n", methodName, type.getParameterType(), methodName);
        } else {
            ps.printf("  public final void %s(%s value){%n"
                + "    java.lang.String[] data = new String[value.length()];%n"
                + "    for(int i=0; i<data.length; i++){%n"
                + "      data[i] = value.get(i).name();%n"
                + "    }%n"
                + "    _%s(data);%n  }%n", methodName, type.getQualifiedReturnType(topPackage), methodName);
        }
    }

    /**
     * Write the setter method for all settable properties.
     *
     * @param method
     * @param ps
     */
    void writeWriteFunction(Method method, PrintStream ps) {
        if (method == null) {
            return;
        }
        ReturnType paramType = ReturnType.getType(method);
        String methodName = method.getName();
        String lowerMethodName = paramType.getPropertyName(methodName);
        String override = config.isGenerateInterface() ? "@Override" : "";
        if (paramType.isEnum() && paramType.isArray()) {
            writeEnumList(ps, paramType, methodName, lowerMethodName);
        } else if (paramType.isEnum()) {
            ps.printf("  %s%n  public final void %s(%s value){%n    _%s(value.name());    %n  }%n", override, methodName, paramType.getName(), methodName);
            ps.printf("  public final native void _%s(java.lang.String value)/*-{this[\"%s\"] = value;}-*/;%n", methodName, lowerMethodName);
        } else if(paramType.isList()){
            if(paramType.isParameterTypeEnum()){
                writeEnumList(ps, paramType, methodName, lowerMethodName);
            }else{
                ps.printf("  %s%n  public final native void %s(%s value)/*-{this[\"%s\"] = value;}-*/;%n", override, methodName, paramType.getQualifiedReturnType(topPackage), lowerMethodName);
            }
        }else {
            ps.printf("  %s%n  public final native void %s(%s value)/*-{this[\"%s\"] = value;}-*/;%n", override, methodName, paramType.getQualifiedReturnType(topPackage), lowerMethodName);
        }
    }

    /**
     * Gets the declared methods in a class and returns an array of methods for all bean properties (get/set)
     *
     * If only getters exist they will be ignored
     *
     * @param cls
     * @return
     */
    PropertyDescriptor[] getMethods(Class cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls, Object.class);
        PropertyDescriptor[] methodDescriptors = beanInfo.getPropertyDescriptors();

        return methodDescriptors;
    }

    void setLoader(ClassLoader classLoader) {
        this.loader = classLoader;
    }

    /**
     * Generate a pure java enum implementation
     *
     * @param classInfo
     */
    private void writeJavaEnum(ClassInfo classInfo) throws ClassNotFoundException, IOException {
        Class<?> cls = classInfo.getOriginalClass();
        BufferedOutputStream fos = null;
        PrintStream ps = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(classInfo.getOutputFile()));
            ps = new PrintStream(fos);
            ps.printf("package %s;%n", classInfo.getNewPackageName());
            ps.printf("public enum %s {%n", classInfo.getClassName());
            Object[] enumConstants = cls.getEnumConstants();
            for (int i = 0; i < enumConstants.length; i++) {
                Object object = enumConstants[i];
                ps.print(object);
                if (i < enumConstants.length - 1) {
                    ps.print(",");
                }
            }
            ps.printf(";%n}%n");
        } finally {
            ps.close();
            fos.close();
        }
    }

    /**
     * Write the interface class
     *
     * @param methods
     * @param ps
     */
    private void generateInterface(PropertyDescriptor[] methods, PrintStream ps) {
        ps.printf("  java.lang.String _getJsonString();%n");
        ps.printf("  java.lang.String _getClassName();%n");
        for (PropertyDescriptor propertyDescriptor : methods) {
            Method method = propertyDescriptor.getReadMethod();
            if (method != null) {
                ReturnType returnType = ReturnType.getType(method);
                ps.printf("  %s %s();%n", returnType.getQualifiedReturnType(topPackage), method.getName());
            }
            method = propertyDescriptor.getWriteMethod();
            if (method != null) {
                ReturnType paramType = ReturnType.getType(method);
                ps.printf("  void %s(%s value);%n", method.getName(), paramType.getQualifiedReturnType(topPackage));
            }
        }
    }

    /**
     * Create a an ArrayHelper class that implements part of the java.util.List api and is backed by JsArray in the
     * client space. The class only gets generated if one of the objects uses a List.
     *
     * @param list
     */
    private void writeArrayHelper(ClassInfo packageInfo) throws ClassNotFoundException, IOException {
        System.out.println("Building " + packageInfo.getOutputDirectory() + "/ListHelper.java");
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(packageInfo.getOutputDirectory(), "ListHelper.java")));
        PrintStream ps = new PrintStream(fos);
        ps.printf("package %s;%n", packageInfo.getNewPackageName());
        ps.printf("import com.google.gwt.core.client.JavaScriptObject;%n");
        ps.printf("import com.google.gwt.core.client.JsArray;%n%n");
        ps.printf("public class ListHelper<T>{%n");
        ps.printf("  private JsArray<JavaScriptObject> array = null;%n");
        ps.printf("  public ListHelper(JsArray<JavaScriptObject> array){%n    this.array = array;%n  }%n%n");
        ps.printf("  public ListHelper(JavaScriptObject[] value){%n    array = (JsArray)JsArray.createArray();%n    for(JavaScriptObject o: value){%n      array.push(o);%n    }%n  }%n%n");
        ps.printf("  public T get(int index){%n    return (T)array.get(index);%n  }%n%n");
        ps.printf("  public void add(T obj){%n    array.push((JavaScriptObject)obj);%n  }%n%n");
        ps.printf("  public void set(int index, T obj){%n    array.set(index, (JavaScriptObject)obj);%n  }%n%n");
        ps.printf("  public int length(){%n    return array.length();%n    }%n%n");
        ps.printf("}");
        ps.close();
        fos.close();
    }
    
    /**
     * Gets the top level class that is being generated.
     *
     * @param classInfoList
     * @return
     */
    private ClassInfo getTopPackage(List<ClassInfo> classInfoList) {
        ClassInfo classInfo = null;
        for (ClassInfo ci : classInfoList) {
            if (classInfo == null) {
                classInfo = ci;
            } else {
                if (classInfo.getNewPackageName().contains(ci.getNewPackageName())) {
                    classInfo = ci;
                }
            }
        }
        return classInfo;
    }

    /**
     * Generates the interface classes if generate interfaces is true
     *
     * @param classInfo
     * @param methods
     * @throws IOException
     */
    private void generateInferfaces(ClassInfo classInfo, PropertyDescriptor[] methods) throws IOException {
        if (config.isGenerateInterface()) {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(classInfo.getOutputDirectory(), "I" + classInfo.getClassName() + ".java")));
            PrintStream ps = new PrintStream(fos);
            ps.printf("package %s;%n", classInfo.getNewPackageName());
            ps.printf("public interface I%s{%n", classInfo.getClassName());
            generateInterface(methods, ps);
            ps.printf("}%n");
            ps.close();
            fos.close();
        }
    }

    /**
     * Generates the safe javascript eval static functions
     *
     * @param classInfo
     * @param cls
     * @param ps
     */
    private void generateEvalMethods(ClassInfo classInfo, Class<?> cls, PrintStream ps) {
        XmlRootElement rootAnn = cls.getAnnotation(XmlRootElement.class);
        if (rootAnn != null) {
            // as per http://tools.ietf.org/html/rfc4627
            ps.printf("  public static native %sJso eval%s(String jsonText) /*-{"
                + "return !(/[^,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]/.test( "
                + "jsonText.replace(/\"(\\\\.|[^\"\\\\])*\"/g, ''))) "
                + " && "
                + "eval('(' + jsonText + ')');}-*/;%n", classInfo.getClassName(), classInfo.getClassName());
            ps.printf("  public static native com.google.gwt.core.client.JsArray<%sJso> eval%sArray(String jsonText) /*-{"
                + "return !(/[^,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]/.test( "
                + "jsonText.replace(/\"(\\\\.|[^\"\\\\])*\"/g, ''))) "
                + " && "
                + "eval('(' + jsonText + ')');}-*/;%n", classInfo.getClassName(), classInfo.getClassName());
        }

    }
}
