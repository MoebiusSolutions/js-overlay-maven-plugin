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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import org.easymock.EasyMock;
import java.io.File;
import com.moesol.test.TestObject;
import com.moesol.test.TestObject2;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
        assertEquals("java.lang.String", gen.getType(stringMethod));
        Method intMethod = to.getClass().getMethod("getInteger");
        assertEquals("int", gen.getType(intMethod));
        Method xmlDataMethod = to.getClass().getMethod("getXmlDate");
        assertEquals("date", gen.getType(xmlDataMethod));
        Method testBool = to.getClass().getMethod("isBool");
        assertEquals("boolean", gen.getType(testBool));
        Method testObject2Method = to.getClass().getMethod("getTestObject2");
        assertEquals("com.moesol.test.TestObject2Jso", gen.getType(testObject2Method));
        config.oldPackage = "com.moesol";
        config.newPackage = "com.newname";
        assertEquals("com.newname.test.TestObject2Jso", gen.getType(testObject2Method));
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
    public void testLowerFirstChar() {
        assertEquals("bob", gen.getPropertyName("Bob"));
        assertEquals("BOb", gen.getPropertyName("BOb"));
        assertEquals("BOB", gen.getPropertyName("BOB"));
        assertEquals("bob", gen.getPropertyName("bob"));
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
        assertEquals(3, list.size());
        ClassInfo ci = list.get(0);
        assertEquals("com.moesol.test", ci.getNewPackageName());
        assertEquals("TestObject", ci.getClassName());
        assertEquals(new File("target/gen-jso/com/moesol/test/TestObjectJso.java"), ci.getOutputFile());
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
        config.sourcePackage = "org.apache.commons.io.output";
        File outputDir = new File("target/gen-jar/");
        FileUtils.deleteDirectory(outputDir);
        config.outputDirectory = "target/gen-jar";
        List<ClassInfo> list = gen.processJar(new File("target/gen-jar"));
        assertEquals(11, list.size());
        
        outputDir.mkdirs();
        list = gen.processJar(new File("target/gen-jar"));
        assertEquals(0, list.size());
        
        outputDir.setLastModified(0);
        list = gen.processJar(new File("target/gen-jar"));
        assertEquals(11, list.size());
    }

    private Log createMockLog() {
        return EasyMock.createNiceMock(Log.class);
    }
}
