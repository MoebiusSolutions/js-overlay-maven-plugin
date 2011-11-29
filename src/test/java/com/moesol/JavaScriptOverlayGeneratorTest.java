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

import java.util.HashMap;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import org.easymock.EasyMock;
import java.io.File;
import com.moesol.test.TestObject;
import com.moesol.test.TestObject2;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
public class JavaScriptOverlayGeneratorTest {

    JavaScriptOverlayGenerator gen;
    Config config;

    @Before
    public void setup() {
        config = new Config();
        config.log = createMockLog();
        gen = new JavaScriptOverlayGenerator(config);
    }

//    @Test
//    public void testGenerate() throws Exception {
//        gen.setOutputDirectory("target/gen-output");
//        gen.setPkg("com.moesol.test");
//        gen.generate();
//    }
    @Test
    public void testGetType() throws Exception {
        TestObject to = new TestObject();
        Method stringMethod = to.getClass().getMethod("getString");
        assertEquals("java.lang.String", gen.getType(stringMethod).name);
        Method intMethod = to.getClass().getMethod("getInteger");
        assertEquals("int", gen.getType(intMethod).name);
        Method xmlDataMethod = to.getClass().getMethod("getXmlDate");
        assertTrue(gen.getType(xmlDataMethod).isDate);
        assertEquals("date", gen.getType(xmlDataMethod).name);
        Method testBool = to.getClass().getMethod("isBool");
        assertEquals("boolean", gen.getType(testBool).name);
        Method testObject2Method = to.getClass().getMethod("getTestObject2");
        assertEquals("com.moesol.test.TestObject2Jso", gen.getType(testObject2Method).name);
        Method testObjectListGet = to.getClass().getMethod("getList");
        assertTrue(gen.getType(testObjectListGet).isList);
        assertFalse(gen.getType(testObjectListGet).isDate);
        assertTrue(gen.getType(testObjectListGet).isList);
        assertEquals("com.moesol.test.TestObject2Jso", gen.getType(testObjectListGet).parameterType);
        Method testObjectListSet = to.getClass().getMethod("setList", List.class);
        assertTrue(gen.getType(testObjectListSet).isList);
        assertEquals("com.moesol.test.TestObject2Jso", gen.getType(testObjectListSet).parameterType);
        Method testIntArray = to.getClass().getMethod("getIntArray");
        ReturnType type = gen.getType(testIntArray);
        assertFalse(type.isList);
        assertTrue(type.isArray);
        assertEquals("int", type.parameterType);
        Method testObjArray = to.getClass().getMethod("getObjArray");
        type = gen.getType(testObjArray);
        assertTrue(type.isArray);
        assertFalse(type.isList);
        assertEquals("com.moesol.test.TestObject2Jso", type.parameterType);
        Method testStringList = to.getClass().getMethod("getStringList");
        type = gen.getType(testStringList);
        assertTrue(type.isArray);
        assertFalse(type.isList);
        assertEquals("java.lang.String", type.parameterType);
        Method testStringArray = to.getClass().getMethod("getStringArray");
        type = gen.getType(testStringArray);
        assertTrue(type.isArray);
        assertFalse(type.isList);
        assertEquals("java.lang.String", type.parameterType);
        config.oldPackage = "com.moesol";
        config.newPackage = "com.newname";
        assertEquals("com.newname.test.TestObject2Jso", gen.getType(testObject2Method).name);
        assertEquals("com.newname.test.TestObject2Jso", gen.getType(testObjectListSet).parameterType);
        assertEquals("com.newname.test.TestObject2Jso", gen.getType(testObjArray).parameterType);
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
        config.oldPackage = "com.test";
        config.newPackage = "org.stuff";
        ci.setPackageName("com.test");
        assertEquals("org.stuff", ci.getNewPackageName());
        ci.setPackageName("com/test/");
        assertEquals("org.stuff", ci.getNewPackageName());
        ci.setPackageName("com.test.");
        assertEquals("org.stuff", ci.getNewPackageName());
        config.newPackage = null;
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
        config.outputDirectory = "target/gen-jso";
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
        config.outputDirectory = "target/test-write";
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
        config.sourcePackage = "org.easymock";
        File outputDir = new File("target/gen-jar/");
        FileUtils.deleteDirectory(outputDir);
        config.outputDirectory = "target/gen-jar";
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
        config.outputDirectory = "target/test-enum";
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
    public void testCompileGenSource() throws Exception {
        List<ClassInfo> cis = new ArrayList<ClassInfo>();
        ClassInfo ci = new ClassInfo(config);
        ci.setClassName("TestObject");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("Color");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("TestObject$InnerEnum");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        ci = new ClassInfo(config);
        ci.setClassName("TestObject2");
        ci.setPackageName("com.moesol.test");
        cis.add(ci);
        config.outputDirectory = "target/test-compile";
        if (ci.getOutputDirectory().exists()) {
            FileUtils.deleteDirectory(ci.getOutputDirectory());
        }
        assertTrue("Unable to create output directory", ci.getOutputDirectory().mkdirs());
        gen.writeJso(cis);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String cp = System.getProperty("java.class.path");
        String[] args = new String[]{
            "-cp", cp, 
            "target/test-compile/com/moesol/test/Color.java"
            ,"target/test-compile/com/moesol/test/TestObjectJso.java"
            ,"target/test-compile/com/moesol/test/TestObject_InnerEnum.java"
            ,"target/test-compile/com/moesol/test/TestObject2Jso.java"
        };
        int status = compiler.run(null, null, null, args);
        assertEquals(0, status);
        assertEquals(8, new File("target/test-compile/com/moesol/test").list().length);
    }

    private Log createMockLog() {
        return EasyMock.createNiceMock(Log.class);
    }
}
