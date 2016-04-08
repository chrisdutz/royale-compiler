package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;

public class ForLoopEmitter extends JSSubEmitter implements
        ISubEmitter<IForLoopNode>
{
    public ForLoopEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);

        startMapping(node);
        writeToken(ASEmitterTokens.FOR);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        IContainerNode cnode = node.getConditionalsContainerNode();
        final IASNode node0 = cnode.getChild(0);
        if (node0.getNodeID() == ASTNodeID.Op_InID)
        {
            //for(in)
            getWalker().walk(cnode.getChild(0));
        }
        else //for(;;)
        {
            emitForStatements(cnode);
        }

        startMapping(node, cnode);
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!EmitterUtils.isImplicit(xnode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        getWalker().walk(node.getStatementContentsNode());
    }

    protected void emitForStatements(IContainerNode node)
    {
        final IASNode node0 = node.getChild(0);
        final IASNode node1 = node.getChild(1);
        final IASNode node2 = node.getChild(2);

        int column = node.getColumn();
        // initializer
        if (node0 != null)
        {
            getWalker().walk(node0);

            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                column += node0.getAbsoluteEnd() - node0.getAbsoluteStart();
            }
            startMapping(node, node.getLine(), column);
            write(ASEmitterTokens.SEMICOLON);
            column++;
            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                write(ASEmitterTokens.SPACE);
                column++;
            }
            endMapping(node);
        }
        // condition or target
        if (node1 != null)
        {
            getWalker().walk(node1);
            
            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                column += node1.getAbsoluteEnd() - node1.getAbsoluteStart();
            }
            startMapping(node, node.getLine(), column);
            write(ASEmitterTokens.SEMICOLON);
            if (node2.getNodeID() != ASTNodeID.NilID)
                write(ASEmitterTokens.SPACE);
            endMapping(node);
        }
        // iterator
        if (node2 != null)
        {
            getWalker().walk(node2);
        }
    }
}
