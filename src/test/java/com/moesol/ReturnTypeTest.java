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

import com.moesol.test.TestObject;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author summersb
 */
public class ReturnTypeTest {
    
    @Test
    public void testLowerFirstChar() {
        ReturnType returnType = new ReturnType();
        assertEquals("bob", returnType.getPropertyName("Bob"));
        assertEquals("BOb", returnType.getPropertyName("BOb"));
        assertEquals("BOB", returnType.getPropertyName("BOB"));
        assertEquals("bob", returnType.getPropertyName("bob"));
    }
    
    @Test
    public void testGetType() throws Exception {
        Config config = new Config();
        ReturnType.setConfig(config);
        TestObject to = new TestObject();
        Method stringMethod = to.getClass().getMethod("getString");
        assertEquals("java.lang.String", ReturnType.getType(stringMethod).getName());
        Method intMethod = to.getClass().getMethod("getInteger");
        assertEquals("int", ReturnType.getType(intMethod).getName());
        Method xmlDataMethod = to.getClass().getMethod("getXmlDate");
        assertTrue(ReturnType.getType(xmlDataMethod).isDate());
        assertEquals("date", ReturnType.getType(xmlDataMethod).getName());
        Method testBool = to.getClass().getMethod("isBool");
        assertEquals("boolean", ReturnType.getType(testBool).getName());
        Method testObject2Method = to.getClass().getMethod("getTestObject2");
        assertEquals("com.moesol.test.TestObject2Jso", ReturnType.getType(testObject2Method).getName());
        Method testObjectListGet = to.getClass().getMethod("getList");
        assertTrue(ReturnType.getType(testObjectListGet).isList());
        assertFalse(ReturnType.getType(testObjectListGet).isDate());
        assertTrue(ReturnType.getType(testObjectListGet).isList());
        assertEquals("com.moesol.test.TestObject2Jso", ReturnType.getType(testObjectListGet).getParameterType());
        Method testObjectListSet = to.getClass().getMethod("setList", List.class);
        assertTrue(ReturnType.getType(testObjectListSet).isList());
        assertEquals("com.moesol.test.TestObject2Jso", ReturnType.getType(testObjectListSet).getParameterType());
        Method testIntArray = to.getClass().getMethod("getIntArray");
        ReturnType type = ReturnType.getType(testIntArray);
        assertFalse(type.isList());
        assertTrue(type.isArray());
        assertEquals("int", type.getParameterType());
        Method testObjArray = to.getClass().getMethod("getObjArray");
        type = ReturnType.getType(testObjArray);
        assertTrue(type.isArray());
        assertFalse(type.isList());
        assertEquals("com.moesol.test.TestObject2Jso", type.getParameterType());
        Method testStringList = to.getClass().getMethod("getStringList");
        type = ReturnType.getType(testStringList);
        assertTrue(type.isArray());
        assertFalse(type.isList());
        assertEquals("java.lang.String", type.getParameterType());
        Method testStringArray = to.getClass().getMethod("getStringArray");
        type = ReturnType.getType(testStringArray);
        assertTrue(type.isArray());
        assertFalse(type.isList());
        assertEquals("java.lang.String", type.getParameterType());
        config.setOldPackage("com.moesol");
        config.setNewPackage("com.newname");
        assertEquals("com.newname.test.TestObject2Jso", ReturnType.getType(testObject2Method).getName());
        assertEquals("com.newname.test.TestObject2Jso", ReturnType.getType(testObjectListSet).getParameterType());
        assertEquals("com.newname.test.TestObject2Jso", ReturnType.getType(testObjArray).getParameterType());
    }

}
