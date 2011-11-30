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

/**
 *
 * @author summersb
 */
public class ReturnType {

    /**
     * Name of the return type
     */
    public String name;
    
    /**
     * True if this type is a Date
     */
    public boolean isDate;
    /**
     * True if this type is a Java List
     */
    public boolean isList;
    /**
     * True if this type is a primitive array
     */
    public boolean isArray;
    /**
     * True if this type is an Enum
     */
    public boolean isEnum;
    /**
     * True if this type is a Parameter type, such as generics
     */
    public String parameterType;
    /**
     * True if the parameter type is an Enum
     */
    public boolean parameterTypeIsEnum;
    
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

}
