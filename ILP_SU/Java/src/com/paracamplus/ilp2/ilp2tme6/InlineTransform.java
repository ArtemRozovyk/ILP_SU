package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.compiler.*;
import com.paracamplus.ilp1.compiler.normalizer.*;
import com.paracamplus.ilp1.interfaces.*;
import com.paracamplus.ilp1.interpreter.*;
import com.paracamplus.ilp1.interpreter.interfaces.*;
import com.paracamplus.ilp2.interfaces.*;
import com.paracamplus.ilp2.interfaces.IASTfactory;
import com.paracamplus.ilp2.interfaces.IASTprogram;

import java.util.*;

public class InlineTransform extends CopyTransform<IGlobalVariableEnvironment> {

    private RenameTransform renameTransform;
    private CallAnalysis callAnalysis;

    public InlineTransform(IASTfactory factory) {
        super(factory);
        callAnalysis=new CallAnalysis(factory);
        renameTransform=new RenameTransform(factory);
    }



    @Override
    public IASTexpression visit(IASTinvocation iast, IGlobalVariableEnvironment gve) throws CompilationException {

        IASTvariable funVariable = (IASTvariable) iast.getFunction().accept(this, gve);
        if(callAnalysis.isRecursive(funVariable)){
            return iast;
        }


        Function function=null;
        Invocable invocable= (Invocable) gve.getGlobalVariableValue(funVariable.getName());
        //not in the map -> it must be primitive
        if (invocable==null){
            return iast;
        }
        if ( invocable instanceof Function) {
           function=((Function)invocable);
        }else{
            throw new CompilationException("You are calling something that is not a function");
        }


        IASTexpression[] arguments = iast.getArguments();
        IASTblock.IASTbinding[] argToBindings= new IASTblock.IASTbinding[arguments.length];
        int i=0;
        for(IASTvariable funarg : function.getVariables()){
            argToBindings[i]=factory.newBinding(funarg,arguments[i]);
            i++;
        }
        return factory.newBlock(argToBindings,function.getBody());

    }

    public IASTprogram visit(IASTprogram program, IGlobalVariableEnvironment gve) throws CompilationException {
        callAnalysis.visit(program,null);
        renameTransform.visit(program, NormalizationEnvironment.EMPTY);


        for ( IASTfunctionDefinition function : program.getFunctionDefinitions() ) {
            Invocable invocable = new Function(function.getVariables(),
                    function.getBody(),
                    new EmptyLexicalEnvironment());
            gve.addGlobalVariableValue(function.getFunctionVariable().getName(),invocable);
        }

        program.getBody().accept(this,gve);
        return program;
    }

}
