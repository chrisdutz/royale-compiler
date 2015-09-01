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

package org.apache.flex.compiler.internal.codegen.js.jx;

import java.util.ArrayList;

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.PackageScope;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.NativeUtils;

public class PackageHeaderEmitter extends JSSubEmitter implements
        ISubEmitter<IPackageDefinition>
{

    public PackageHeaderEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = EmitterUtils.findType(containedScope
                .getAllLocalDefinitions());
        if (type == null)
            return;

        writeNewline("/**");
        writeNewline(" * " + type.getQualifiedName());
        writeNewline(" *");
        writeNewline(" * @fileoverview");
        writeNewline(" *");
        writeNewline(" * @suppress {checkTypes}");
        writeNewline(" */");
        writeNewline();

        /* goog.provide('x');\n\n */
        write(JSGoogEmitterTokens.GOOG_PROVIDE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(getEmitter().formatQualifiedName(type.getQualifiedName()));
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
    }

    public void emitContents(IPackageDefinition definition)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();

        ArrayList<String> writtenRequires = new ArrayList<String>();

        ITypeDefinition type = EmitterUtils.findType(containedScope
                .getAllLocalDefinitions());
        if (type == null)
            return;

        ITypeNode typeNode = type.getNode();
        if (typeNode instanceof ClassNode)
        {
            ClassNode classNode = (ClassNode) typeNode;
            ASDocComment asDoc = (ASDocComment) classNode.getASDocComment();
            if (asDoc != null)
            {
                String asDocString = asDoc.commentNoEnd();
                String ignoreToken = JSFlexJSEmitterTokens.IGNORE_IMPORT
                        .getToken();
                int ignoreIndex = asDocString.indexOf(ignoreToken);
                while (ignoreIndex != -1)
                {
                    String ignorable = asDocString.substring(ignoreIndex
                            + ignoreToken.length());
                    int endIndex = ignorable.indexOf("\n");
                    ignorable = ignorable.substring(0, endIndex);
                    ignorable = ignorable.trim();
                    // pretend we've already written the goog.requires for this
                    writtenRequires.add(ignorable);
                    ignoreIndex = asDocString.indexOf(ignoreToken,
                            ignoreIndex + ignoreToken.length());
                }
            }
        }

        //        if (project == null)
        //            project = getWalker().getProject();

        FlexJSProject flexProject = (FlexJSProject) getProject();
        ASProjectScope projectScope = (ASProjectScope) flexProject.getScope();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(type);
        ArrayList<String> requiresList = flexProject.getRequires(cu);
        ArrayList<String> interfacesList = flexProject.getInterfaces(cu);

        String cname = type.getQualifiedName();
        writtenRequires.add(cname); // make sure we don't add ourselves

        boolean emitsRequires = false;
        if (requiresList != null)
        {
            for (String imp : requiresList)
            {
                if (imp.contains(JSGoogEmitterTokens.AS3.getToken()))
                    continue;

                if (imp.equals(cname))
                    continue;

                if (NativeUtils.isNative(imp))
                    continue;

                if (writtenRequires.indexOf(imp) == -1)
                {

                    /* goog.require('x');\n */
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(fjs.formatQualifiedName(imp));
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);

                    writtenRequires.add(imp);

                    emitsRequires = true;
                }
            }
        }

        boolean emitsInterfaces = false;
        if (interfacesList != null)
        {
            for (String imp : interfacesList)
            {
                if (writtenRequires.indexOf(imp) == -1)
                {
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(fjs.formatQualifiedName(imp));
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);

                    emitsInterfaces = true;
                }
            }
        }

        // erikdebruin: Add missing language feature support, with e.g. 'is' and 
        //              'as' operators. We don't need to worry about requiring
        //              this in every project: ADVANCED_OPTIMISATIONS will NOT
        //              include any of the code if it is not used in the project.
        boolean isMainCU = flexProject.mainCU != null
                && cu.getName().equals(flexProject.mainCU.getName());
        if (isMainCU)
        {
            ICompilerProject project = this.getProject();
            if (project instanceof FlexJSProject)
            {
            	if (((FlexJSProject)project).needLanguage)
            	{
		            write(JSGoogEmitterTokens.GOOG_REQUIRE);
		            write(ASEmitterTokens.PAREN_OPEN);
		            write(ASEmitterTokens.SINGLE_QUOTE);
		            write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
		            write(ASEmitterTokens.SINGLE_QUOTE);
		            write(ASEmitterTokens.PAREN_CLOSE);
		            writeNewline(ASEmitterTokens.SEMICOLON);
            	}
            }
        }

        if (emitsRequires || emitsInterfaces || isMainCU)
        {
            writeNewline();
        }

        writeNewline();
        writeNewline();
    }

}
