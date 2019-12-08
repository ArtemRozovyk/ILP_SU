package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.compiler.*;
import com.paracamplus.ilp1.interfaces.*;
import com.paracamplus.ilp1.interpreter.interfaces.*;
import com.paracamplus.ilp2.interfaces.IASTprogram;
import com.paracamplus.ilp2.interfaces.*;
import com.paracamplus.ilp2.interfaces.IASTfactory;

import java.util.*;

public class CallAnalysis extends CopyTransform<String> {

    Map<String,Set<String>> graph;


    public CallAnalysis(IASTfactory factory) {
        super(factory);
        graph=new HashMap<>();
    }



    @Override
    public IASTexpression visit(IASTinvocation iast, String invocator) throws CompilationException {

        String appel =((IASTvariable)iast.getFunction()).getName();

        if(graph.containsKey(invocator))
        {
            graph.get(invocator).add(appel);
        }else {
            Set<String> singleSet=new HashSet<>();
            singleSet.add(appel);
            graph.put(invocator,singleSet);
        }
       iast.getFunction().accept(this, invocator);
       for(IASTexpression argument: iast.getArguments()){
           argument.accept(this, invocator);
       }

        return iast;
    }

    boolean depthCall(String appel, Set<String > alreadyVisited){
        if(alreadyVisited.contains(appel)){
            return true;
        }else{
            alreadyVisited.add(appel);
            if(!graph.containsKey(appel))return false;
            for(String appel2 : graph.get(appel)){
                if(depthCall(appel2,alreadyVisited)){
                    return true;
                }
            }
            return false;
        }
    }


    boolean isRecursive(IASTvariable f) throws CompilationException {
        String callerName=f.getName();
        Set<String> alreadyVisited = new HashSet<>();
        alreadyVisited.add(callerName);

        if(!graph.containsKey(callerName))return false;
        for(String appel : graph.get(callerName)){
            if(depthCall(appel,alreadyVisited)){
                return true;
            }
        }
        return false;
    }



    public IASTprogram visit(IASTprogram program, String s) throws CompilationException {

        for(IASTfunctionDefinition funDef: program.getFunctionDefinitions()){
            funDef.getBody().accept(this,funDef.getName());
        }
       return program;
    }
}
