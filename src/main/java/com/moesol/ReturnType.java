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

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author summersb
 */
public class ReturnType {

    /**
     * @return the config
     */
    public static Config getConfig() {
        return gConfig;
    }

    /**
     * @param aConfig the config to set
     */
    public static void setConfig(Config aConfig) {
        gConfig = aConfig;
    }

    /**
     * Get the return type for a method, if void then get the 
     * type of the first parameter.
     * @param method
     * @return 
     */
    private static ReturnType getReturnType(Method method) {
        ReturnType theType = new ReturnType();
        theType.type = method.getReturnType();
        if ("void".equals(theType.type.getName())) {
            // its a set method, get the parameter type
            theType.type = method.getParameterTypes()[0];
        }
        return theType;
    }

    /**
     * Name of the return type
     */
    private String name;
    /**
     * True if this type is a Date
     */
    private boolean date;
    /**
     * True if this type is a Java List
     */
    private boolean list;
    /**
     * True if this type is a primitive array
     */
    private boolean array;
    /**
     * True if this type is an Enum
     */
    private boolean enm;
    /**
     * True if this type is a Parameter type, such as generics
     */
    private String parameterType;
    /**
     * True if the parameter type is an Enum
     */
    private boolean parameterTypeEnum;
    /**
     * Config object for the schema
     */
    private static Config gConfig;
    private String parameterImplType;
    private Class<?> type;

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

    /**
     * Returns a javascript friendly type for the java type.
     * Converts an XMLGregorianCalendar type to an internal date
     * which is converted to a String wrapper around the long of the data.
     * Appends Jso to all non primitive and java.xxxx types
     * @param method
     * @return 
     */
    static ReturnType getType(Method method) {
        ReturnType theType = getReturnType(method);
        if (theType.type.getName().equals("javax.xml.datatype.XMLGregorianCalendar")) {
            theType.setName("date");
            theType.setDate(true);
            return theType;
        }
        if (theType.type.isArray()) {
            theType.setParameterType(getClassNameType(theType.type.getComponentType(), getConfig().isGenerateInterface()));
            theType.setArray(true);
            if (theType.type.getComponentType().isEnum()) {
                theType.setParameterTypeEnum(true);
                theType.setEnum(true);
            }
            return theType;
        }
        if (getGenericTypes(method, theType)) {
            return theType;
        }
        if (theType.type.isEnum()) {
            theType.setEnum(true);
        }
        theType.setName(getClassNameType(theType.type, getConfig().isGenerateInterface()));
        return theType;
    }

    /**
     * Checks to see if the method gets or sets generic types and updates theType 
     * as required
     * @param type
     * @param method
     * @param theType
     * @return true if a generic type was found
     */
    private static boolean getGenericTypes(Method method, ReturnType theType) {
        Class<?> type = theType.type;
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> cls : interfaces) {
            if (cls.getName().equals("java.util.Collection")) {
                Type genericReturnType = method.getGenericReturnType();
                if ("void".equals(genericReturnType.toString())) {
                    genericReturnType = method.getGenericParameterTypes()[0];
                }
                ParameterizedType pt = (ParameterizedType) genericReturnType;
                Class t = (Class) pt.getActualTypeArguments()[0];
                if (pt.getClass().isEnum()) {
                    theType.setEnum(true);
                }
                theType.setParameterType(getClassNameType(t, getConfig().isGenerateInterface()));
                theType.setParameterImplType(getClassNameType(t, false));
                if (t.isEnum()) {
                    theType.setParameterTypeEnum(true);
                }
                if (theType.getParameterType().startsWith("java")) {
                    // java types do not extend JavascriptObject
                    theType.setArray(true);
                } else {
                    theType.setList(true);
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the new package and class name for a Class.
     * Appends Jso to any non java.xxxx type if not using interfaces
     * otherwise adds a I to the front of the type
     * @param type
     * @return 
     */
    private static String getClassNameType(Class<?> type, boolean genInterface) {
        String name = type.getName();
        int lastDot = name.lastIndexOf(".");
        if (lastDot == -1) {
            return name;
        }
        ClassInfo ci = new ClassInfo(getConfig());
        ci.setPackageName(name);
        name = ci.getNewPackageName();
        if (name.startsWith("java") == false) {
            name = name.replace("$", "_");
            if(genInterface && !type.isEnum()){
                lastDot = name.lastIndexOf(".");
                name = name.substring(0, lastDot+1) + "I" + name.substring(lastDot+1);
            }else if (!type.isEnum()){
                name += "Jso";
            }
        }
        return name;
    }
    
    /**
     * Get the fully qualified type of this Type.
     * This will include [] if the type is an array or ListHelper if a List
     * 
     * @return 
     */
    String getQualifiedReturnType(ClassInfo topPackage){
        if(isArray()){
            return getParameterType() + "[]";
        }
        if(isDate()){
            return "java.lang.String";
        }
        if(isList()){
            return topPackage.getNewPackageName() + ".ListHelper<" + getParameterType() + ">";
        }
        return getName();
    }
    
    /**
     * Get the fully qualified type of this Type.
     * This will include [] if the type is an array or ListHelper if a List
     * 
     * @return 
     */
    String getQualifiedReturnImplTypeAsArray(ClassInfo topPackage){
        if(isArray() || isList()){
            return  getParameterImplType() + "[]"; //getClassNameType(type, false) + "[]";
        }
        if(isDate()){
            return "java.lang.String";
        }
        return getName();
    }
    
    /**
     * Get the fully qualified type of this Type.
     * This will include [] if the type is an array or ListHelper if it is a List
     */
    String getQualifiedReturnImplType(ClassInfo topPackage){
        if(isArray()){
            return getParameterType() + "[]";
        }
        if(isDate()){
            return "java.lang.String";
        }
        if(isList()){
            return topPackage.getNewPackageName() + ".ListHelper<" + getImplName() + ">";
        }
        return getImplName();
    }
    
    /**
     * Get the fully qualified type of this Type.
     * This will include [] if the type is an array or ListHelper if it is a List
     */
    String getQualifiedReturnTypeAsArray(ClassInfo topPackage){
        if(isArray() || isList()){
            return getParameterType() + "[]";
        }
        if(isDate()){
            return "java.lang.String";
        }
        return getImplName();
    }

    /**
     * Return a cast string if this type has an interface otherwise return an empty string
     */
    String getImplCast(ClassInfo topPackage){
        if(getConfig().isGenerateInterface() && !getQualifiedReturnType(topPackage).startsWith("java.")){
            return "(" + getQualifiedReturnImplType(topPackage) + ") ";
        }
        return "";
    }
    /**
     * Get the new package and implementation class name
     * 
     * @return 
     */
    String getImplName(){
        return getClassNameType(type, false);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the date
     */
    public boolean isDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(boolean date) {
        this.date = date;
    }

    /**
     * @return the list
     */
    public boolean isList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(boolean list) {
        this.list = list;
    }

    /**
     * @return the array
     */
    public boolean isArray() {
        return array;
    }

    /**
     * @param array the array to set
     */
    public void setArray(boolean array) {
        this.array = array;
    }

    /**
     * @return the enm
     */
    public boolean isEnum() {
        return enm;
    }

    /**
     * @param enm the enm to set
     */
    public void setEnum(boolean enm) {
        this.enm = enm;
    }

    /**
     * @return the parameterType
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * @param parameterType the parameterType to set
     */
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    /**
     * @return the parameterTypeEnum
     */
    public boolean isParameterTypeEnum() {
        return parameterTypeEnum;
    }

    /**
     * @param parameterTypeEnum the parameterTypeEnum to set
     */
    public void setParameterTypeEnum(boolean parameterTypeEnum) {
        this.parameterTypeEnum = parameterTypeEnum;
    }

    /**
     * @return the parameterImplType
     */
    public String getParameterImplType() {
        return parameterImplType;
    }

    /**
     * @param parameterImplType the parameterImplType to set
     */
    public void setParameterImplType(String parameterImplType) {
        this.parameterImplType = parameterImplType;
    }
}
