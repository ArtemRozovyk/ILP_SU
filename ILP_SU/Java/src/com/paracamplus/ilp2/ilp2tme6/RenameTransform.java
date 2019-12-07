package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.compiler.*;
import com.paracamplus.ilp1.compiler.normalizer.*;
import com.paracamplus.ilp1.interfaces.*;
import com.paracamplus.ilp2.interfaces.IASTfactory;
import com.paracamplus.ilp2.interfaces.IASTprogram;
import com.paracamplus.ilp2.interfaces.*;

public class RenameTransform extends CopyTransform<INormalizationEnvironment> {
    static int counter;
    public RenameTransform(IASTfactory factory) {
        super(factory);
    }
    @Override
    public IASTexpression visit(IASTassignment iast, INormalizationEnvironment iNormalizationEnvironment) throws CompilationException {
        IASTexpression expression = iast.getExpression();
        IASTexpression newexpression = expression.accept(this, iNormalizationEnvironment);
        return (factory).newAssignment(iast.getVariable(), newexpression);    }
    @Override
    public IASTexpression visit(IASTblock block, INormalizationEnvironment iNormalizationEnvironment) throws CompilationException {
        IASTblock.IASTbinding[] bindings = block.getBindings();
        IASTblock.IASTbinding[] newbindings = new IASTblock.IASTbinding[bindings.length];
        INormalizationEnvironment newEnv = iNormalizationEnvironment;
        for ( int i=0 ; i<bindings.length ; i++ ) {
            IASTblock.IASTbinding binding = bindings[i];
            IASTexpression expr = binding.getInitialisation();
            IASTexpression newexpr = expr.accept(this, iNormalizationEnvironment);
            IASTvariable variable = binding.getVariable();
            ///
            IASTvariable newvariable = factory.newVariable(variable.getName()+""+counter++);
            newEnv = newEnv.extend(variable,newvariable);
            ///
            newbindings[i] = factory.newBinding(newvariable, newexpr);
        }
        IASTexpression newbody = block.getBody().accept(this, newEnv);
        return factory.newBlock(newbindings, newbody);
    }

    @Override
    public IASTexpression visit(IASTvariable iast, INormalizationEnvironment iNormalizationEnvironment) throws CompilationException {
        try {
            return iNormalizationEnvironment.renaming(iast);
        } catch (NoSuchLocalVariableException exc) {
            return iast;
        }
    }


    public IASTprogram visit(IASTprogram program, INormalizationEnvironment data) throws CompilationException {
        counter=1;
        INormalizationEnvironment newEnv = data;
        //visit def fun
        IASTfunctionDefinition[] funDefs = program.getFunctionDefinitions();
        int i=0;
        for(IASTfunctionDefinition fd: funDefs){
            IASTvariable[] vars= fd.getVariables();
            IASTvariable[] newvars=new IASTvariable[fd.getVariables().length];
            int j=0;
            for(IASTvariable v :vars){
                        //
                        IASTvariable vb= factory.newVariable(v.getName()+""+counter++);
                        newEnv=newEnv.extend(v,vb);
                        newvars[j++]=vb;
                        //
            }
            IASTexpression newbody=fd.getBody().accept(this,newEnv);
            funDefs[i++]=factory.newFunctionDefinition(
                    factory.newVariable(fd.getFunctionVariable().getName()),newvars,newbody);
        }

        IASTexpression body = program.getBody();
        IASTexpression newbody = body.accept(this, data);
        return factory.newProgram(funDefs, newbody);
    }

    }
