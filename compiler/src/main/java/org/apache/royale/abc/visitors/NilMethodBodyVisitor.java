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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;

/**
 * An IMethodBodyVisitor that ignores its input as far as possible.
 */
public class NilMethodBodyVisitor implements IMethodBodyVisitor
{
    @Override
    public ITraitsVisitor visitTraits()
    {
        return NilVisitors.NIL_TRAITS_VISITOR;
    }

    @Override
    public void visitInstructionList(InstructionList new_list)
    {
    }

    @Override
    public void visitInstruction(int opcode, Object single_operand)
    {

    }

    public void visitInstruction(Instruction instruction)
    {

    }

    @Override
    public void visitInstruction(int opcode, Object[] operands)
    {

    }

    @Override
    public void visitInstruction(int opcode, int immediate_operand)
    {

    }

    @Override
    public void visitInstruction(int opcode)
    {

    }

    @Override
    public int visitException(Label from, Label to, Label target, Name exception_type, Name catch_var)
    {
        throw new IllegalStateException("Must implement this method and return a valid exception number.");
    }

    @Override
    public void visitEnd()
    {
    }

    @Override
    public void visit()
    {
    }

    @Override
    public void labelCurrent(Label l)
    {
    }

    @Override
    public void labelNext(Label l)
    {
    }
}
