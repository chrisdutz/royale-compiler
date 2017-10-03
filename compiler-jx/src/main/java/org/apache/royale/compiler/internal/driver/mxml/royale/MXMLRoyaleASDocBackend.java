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

package org.apache.royale.compiler.internal.driver.mxml.royale;

import java.io.FilterWriter;
import java.util.List;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.IJSWriter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleASDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLBlockWalker;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLWriter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleASDocEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleBlockWalker;
import org.apache.royale.compiler.internal.driver.js.goog.ASDocConfiguration;
import org.apache.royale.compiler.internal.driver.mxml.ASDocASSourceFileHandler;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.targets.RoyaleSWCTarget;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.royale.compiler.internal.visitor.mxml.MXMLNodeSwitch;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IBlockVisitor;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link MXMLBlockWalker} is used to traverse the {@link IMXMLFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class MXMLRoyaleASDocBackend extends MXMLRoyaleSWCBackend
{

    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(ASDocConfiguration.class);
    }

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter out)
    {
        return new MXMLRoyaleASDocEmitter(out);
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(RoyaleProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
    {
        MXMLBlockWalker walker = new MXMLRoyaleBlockWalker(errors, project,
                mxmlEmitter, asEmitter, asBlockWalker);

        ASNodeSwitch asStrategy = new ASNodeSwitch(
                (IBlockVisitor) asBlockWalker);
        walker.setASStrategy(asStrategy);

        MXMLNodeSwitch mxmlStrategy = new MXMLNodeSwitch(walker);
        walker.setMXMLStrategy(mxmlStrategy);

        return walker;
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSGoogDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public IJSEmitter createEmitter(FilterWriter out)
    {
        IJSEmitter emitter = new JSRoyaleASDocEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }
    
    @Override
    public IJSWriter createMXMLWriter(RoyaleProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return new MXMLWriter(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public JSTarget createTarget(RoyaleProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor)
    {
        return new RoyaleSWCTarget(project, settings, monitor);
    }
    
    @Override
    public ISourceFileHandler getSourceFileHandlerInstance()
    {
        return ASDocASSourceFileHandler.INSTANCE;
    }

    @Override
    public String getOutputExtension()
    {
        return "json";
    }

}
