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
package com.moesol.test;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Ignore;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
@Ignore
@XmlRootElement
public class TestObject {
    private String string;
    private int integer;
    private Integer intObject;
    private XMLGregorianCalendar xmlDate;
    private TestObject2 testObject2;
    private boolean bool;
    private List<TestObject2> list;
    private int[] intArray;
    private TestObject2[] objArray;
    private List<String> stringList;
    private String[] stringArray;

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @param string the string to set
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * @return the integer
     */
    public int getInteger() {
        return integer;
    }

    /**
     * @param integer the integer to set
     */
    public void setInteger(int integer) {
        this.integer = integer;
    }

    /**
     * @return the intObject
     */
    public Integer getIntObject() {
        return intObject;
    }

    /**
     * @param intObject the intObject to set
     */
    public void setIntObject(Integer intObject) {
        this.intObject = intObject;
    }

    /**
     * @return the xmlDate
     */
    public XMLGregorianCalendar getXmlDate() {
        return xmlDate;
    }

    /**
     * @param xmlDate the xmlDate to set
     */
    public void setXmlDate(XMLGregorianCalendar xmlDate) {
        this.xmlDate = xmlDate;
    }

    /**
     * @return the testObject2
     */
    public TestObject2 getTestObject2() {
        return testObject2;
    }

    /**
     * @param testObject2 the testObject2 to set
     */
    public void setTestObject2(TestObject2 testObject2) {
        this.testObject2 = testObject2;
    }

    /**
     * @return the bool
     */
    public boolean isBool() {
        return bool;
    }

    /**
     * @param bool the bool to set
     */
    public void setBool(boolean bool) {
        this.bool = bool;
    }

    /**
     * @return the list
     */
    public List<TestObject2> getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<TestObject2> list) {
        this.setList(list);
    }

    /**
     * @return the intArray
     */
    public int[] getIntArray() {
        return intArray;
    }

    /**
     * @param intArray the intArray to set
     */
    public void setIntArray(int[] intArray) {
        this.setIntArray(intArray);
    }

    /**
     * @return the objArray
     */
    public TestObject2[] getObjArray() {
        return objArray;
    }

    /**
     * @param objArray the objArray to set
     */
    public void setObjArray(TestObject2[] objArray) {
        this.objArray = objArray;
    }

    /**
     * @return the stringList
     */
    public List<String> getStringList() {
        return stringList;
    }

    /**
     * @param stringList the stringList to set
     */
    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    /**
     * @return the stringArray
     */
    public String[] getStringArray() {
        return stringArray;
    }

    /**
     * @param stringArray the stringArray to set
     */
    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
}
