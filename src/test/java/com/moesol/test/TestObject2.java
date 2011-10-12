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

import org.junit.Ignore;

/**
 *
 * @author summersb
 * @author <a href="http://www.moesol.com/">Moebius Solutions, Inc.</a>
 */
@Ignore
public class TestObject2 {

    private String name;
    private boolean bool;
    private Boolean bool2;
    
    public String shouldNotAppear(){
        return null;
    }

    public String getGetterOnly(){
        return null;
    }
    
    public boolean isGetterOnly(){
        return false;
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
     * @return the bool2
     */
    public Boolean getBool2() {
        return bool2;
    }

    /**
     * @param bool2 the bool2 to set
     */
    public void setBool2(Boolean bool2) {
        this.bool2 = bool2;
    }
}
