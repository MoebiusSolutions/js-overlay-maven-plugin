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

import com.moesol.test.TestObject2;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author summersb
 */
public class JavaScriptOverlayGeneratorTest {

    JavaScriptOverlayGenerator gen;
    Config config;

    @Before
    public void setup() {
        config = new Config();
        config.setLog(createMockLog());
        config.setSourcePackage("com.moesol.test");
        gen = new JavaScriptOverlayGenerator(config);
    }

    @Test
    public void testGetPackage() throws Exception {
        ClassInfo ci = new ClassInfo(config);
        ci.setPackageName("com.test");
        assertEquals("com.test", ci.getNewPackageName());
        ci.setPackageName("com/test/");
        assertEquals("com.test", ci.getNewPackageName());
        ci.setPackageName("com.test.");
        assertEquals("com.test", ci.getNewPackageName());
        config.setOldPackage("com.test");
        config.setNewPackage("org.stuff");
        ci.setPackageName("com.test");
        assertEquals("org.stuff", ci.getNewPackageName());
        ci.setPackageName("com/test/");
        assertEquals("org.stuff", ci.getNewPackageName());
        ci.setPackageName("com.test.");
        assertEquals("org.stuff", ci.getNewPackageName());
        config.setNewPackage(null);
        ci.setPackageName("com.test");
        assertEquals("com.test", ci.getNewPackageName());
        ci.setPackageName("com/test/");
        assertEquals("com.test", ci.getNewPackageName());
        ci.setPackageName("com.test.");
        assertEquals("com.test", ci.getNewPackageName());
    }

    @Test
    public void testGetMethods() throws IntrospectionException {
        Class<? extends TestObject2> aClass = new TestObject2().getClass();
        PropertyDescriptor[] methods = gen.getMethods(aClass);
        assertEquals(4, methods.length);
    }

    @Test
    public void testProcessDirectory() throws Exception {
        config.setOutputDirectory("target/gen-jso");
        gen.setLoader(Thread.currentThread().getContextClassLoader());
        List<ClassInfo> list = gen.processDirectory("com/moesol/test/", new File("target/gen-jso"));
        assertEquals(5, list.size());
        HashMap<String, ClassInfo> names = new HashMap<String, ClassInfo>();
        for (ClassInfo ci : list) {
            names.put(ci.getClassName(), ci);
        }
        assertTrue(names.containsKey("TestObject"));
        assertEquals("com.moesol.test", names.get("TestObject").getNewPackageName());
        assertEquals(new File("target/gen-jso/com/moesol/test/TestObjectJso.java"), names.get("TestObject").getOutputFile());
    }

    @Test
    public void testWriteJso() throws Exception {
        List<ClassInfo> cis = new ArrayList<ClassInfo>();
        ClassInfo ci = new ClassInfo(config);
        ci.setClassName("TestObject");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        ClassInfo ci2 = new ClassInfo(config);
        ci2.setClassName("TestObject2");
        ci2.setPackageName("com.moesol.test");
        cis.add(ci2);
        config.setOutputDirectory("target/test-write");
        if (ci.getOutputDirectory().exists()) {
            FileUtils.deleteDirectory(ci.getOutputDirectory());
        }
        assertTrue("Unable to create output directory", ci.getOutputDirectory().mkdirs());
        gen.writeJso(cis);
        assertTrue("Generated file is missing", ci.getOutputFile().exists());
        assertTrue("Generated file is missing", ci2.getOutputFile().exists());
    }

    @Test
    public void testProcessJar() throws Exception {
        gen.setLoader(Thread.currentThread().getContextClassLoader());
        config.setSourcePackage("org.easymock");
        File outputDir = new File("target/gen-jar/");
        FileUtils.deleteDirectory(outputDir);
        config.setOutputDirectory("target/gen-jar");
        List<ClassInfo> list = gen.processJar(new File("target/gen-jar"));
        assertEquals(83, list.size());

        outputDir.mkdirs();
        list = gen.processJar(new File("target/gen-jar"));
        assertEquals(0, list.size());

        outputDir.setLastModified(0);
        list = gen.processJar(new File("target/gen-jar"));
        assertEquals(83, list.size());
    }

    @Test
    public void testEnum() throws Exception {
        List<ClassInfo> cis = new ArrayList<ClassInfo>();
        ClassInfo ci = new ClassInfo(config);
        ci.setClassName("TestObject");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        ClassInfo ci2 = new ClassInfo(config);
        ci2.setClassName("Color");
        ci2.setPackageName("com.moesol.test");
        cis.add(ci2);
        ClassInfo ci3 = new ClassInfo(config);
        ci3.setClassName("TestObject$InnerEnum");
        ci3.setPackageName("com.moesol.test");
        cis.add(ci3);
        config.setOutputDirectory("target/test-enum");
        if (ci.getOutputDirectory().exists()) {
            FileUtils.deleteDirectory(ci.getOutputDirectory());
        }
        assertTrue("Unable to create output directory", ci.getOutputDirectory().mkdirs());
        gen.writeJso(cis);
        assertTrue("Generated file is missing", ci.getOutputFile().exists());
        assertTrue("Generated file is missing", ci2.getOutputFile().exists());
        assertTrue("Generated file is missing", ci3.getOutputFile().exists());
    }

    @Test
    public void testCompileGenSourceWithInterface() throws Exception {
        config.setOldPackage("com.moesol.test");
        config.setNewPackage("com.moesol.test.newpackage");
        compile(true, "target/test-compile-interface",
            "Color.java"
            ,"ListHelper.java"
            ,"ITestObject.java"
            ,"TestObjectJso.java"
            ,"TestObject_InnerEnum.java"
            ,"ITestObject2.java"
            ,"TestObject2Jso.java");
        assertEquals(14, new File("target/test-compile-interface/com/moesol/test/newpackage").list().length);
    }

    @Test
    public void testCompileGenSource() throws Exception {
        config.setOldPackage("com.moesol.test");
        config.setNewPackage("com.moesol.test");
        compile(false, "target/test-compile",
            "Color.java", 
            "ListHelper.java",
            "TestObjectJso.java", 
            "TestObject_InnerEnum.java", 
            "TestObject2Jso.java");
        assertEquals(10, new File("target/test-compile/com/moesol/test").list().length);
    }

    @Test
    public void testArray() throws Exception{
        testCompileGenSourceWithInterface();
        File f = new File("target/test-compile-interface");
        URL u = f.toURI().toURL();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            m.setAccessible(true);
            m.invoke(ucl, new Object[]{u});
        }
        Class[] clses = new Class[]{Class.forName("com.moesol.test.newpackage.TestObjectJso"), Class.forName("com.moesol.test.newpackage.ITestObject")};
        for (Class cls : clses) {
            Method m = cls.getDeclaredMethod("getStringList");
            assertNotNull(m);
            Class[] params = new Class[]{String[].class};//new Class[]{Class.forName("com.moesol.test.newpackage.ListHelper")};
            m = cls.getDeclaredMethod("setStringList", params);
            assertNotNull(m);
            m = cls.getDeclaredMethod("getStringArray");
            assertNotNull(m);
            assertEquals(String[].class.getName(), m.getReturnType().getName());
            m = cls.getDeclaredMethod("setStringArray", params);
            assertNotNull(m);
            m = cls.getDeclaredMethod("getColorArray");
            assertNotNull(m);
            params = new Class[]{Class.forName("[Lcom.moesol.test.newpackage.Color;")};
            m = cls.getDeclaredMethod("setColorArray", params);
            assertNotNull(m);
            m = cls.getDeclaredMethod("getColorList");
            assertNotNull(m);
            params = new Class[]{Class.forName("com.moesol.test.newpackage.ListHelper")};
            m = cls.getDeclaredMethod("setColorList", params);
            assertNotNull(m);
            m = cls.getDeclaredMethod("setList", params);
            assertNotNull(m);
            m = cls.getDeclaredMethod("getList");
            assertNotNull(m);
            assertEquals("com.moesol.test.newpackage.ListHelper", m.getReturnType().getName());
        }
    }

    private Log createMockLog() {
        return EasyMock.createNiceMock(Log.class);
    }

    private void compile(boolean compile, String outputPath, String... classList) throws IOException, ClassNotFoundException, IntrospectionException {
        List<ClassInfo> cis = new ArrayList<ClassInfo>();
        ClassInfo ci = new ClassInfo(config);
        ci.setClassName("TestObject");
        ci.setPackageName(config.getOldPackage());
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("Color");
        ci.setPackageName(config.getOldPackage());
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("TestObject$InnerEnum");
        ci.setPackageName(config.getOldPackage());
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("TestObject2");
        ci.setPackageName(config.getOldPackage());
        cis.add(ci);
        config.setGenerateInterface(compile);
        config.setOutputDirectory(outputPath);
        if (ci.getOutputDirectory().exists()) {
            FileUtils.deleteDirectory(ci.getOutputDirectory());
        }
        assertTrue("Unable to create output directory", ci.getOutputDirectory().mkdirs());
        gen.writeJso(cis);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String cp = System.getProperty("java.class.path");
        String[] args = new String[classList.length+2];
        args[0] = "-cp";
        args[1] = cp;
        int index = 2;
        for (String string : classList) {
            args[index++] = outputPath + "/" + config.getNewPackage().replace(".", "/") + "/" + string;
        }
        int status = compiler.run(null, null, null, args);
        assertEquals(0, status);
    }
}
