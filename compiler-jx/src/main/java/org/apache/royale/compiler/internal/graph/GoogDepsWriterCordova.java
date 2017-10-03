/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.royale.compiler.internal.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.swc.ISWC;

public class GoogDepsWriterCordova extends GoogDepsWriter {

    public GoogDepsWriterCordova(File outputFolder, String mainClassName, JSGoogConfiguration config, List<ISWC> swcs)
	{
		super(outputFolder, mainClassName, config, swcs);
	}
	
    private final String FLEXJS_CORDOVA_PLUGIN = "@royalecordovaplugin";
    
    public ArrayList<String> cordovaPlugins = new ArrayList<String>();

    @Override
	protected void otherScanning(String s)
	{	
    	int c = s.indexOf(FLEXJS_CORDOVA_PLUGIN);
    	if (c > -1)
    	{
    		cordovaPlugins.add(s.substring(c + FLEXJS_CORDOVA_PLUGIN.length()).trim());
    	}
	}
}
