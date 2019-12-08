package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.compiler.*;
import com.paracamplus.ilp1.interfaces.*;
import com.paracamplus.ilp1.interpreter.interfaces.*;
import com.paracamplus.ilp2.interfaces.IASTfactory;
import com.paracamplus.ilp2.interfaces.IASTprogram;
import com.paracamplus.ilp2.interfaces.IASTvisitor;
import com.paracamplus.ilp2.interfaces.*;

public class CopyTransform<Data> implements IASTvisitor<IASTexpression,Data, CompilationException> {

    IASTfactory factory;
    public CopyTransform(IASTfactory factory){
        this.factory=factory;
    }


    public IASTprogram visit(IASTprogram program, Data data) throws CompilationException {

        IASTfunctionDefinition[] functions = program.getFunctionDefinitions();
        IASTfunctionDefinition[] newfunctions = program.getFunctionDefinitions();
        int i=0;
        for(IASTfunctionDefinition fd: functions){
            IASTvariable[] vars= fd.getVariables();
            IASTvariable[] newargs= fd.getVariables();
            int j=0;
            for(IASTvariable v :vars){
                newargs[j++]=factory.newVariable(v.getName());
            }
            IASTexpression newbd=fd.getBody().accept(this,data);
            newfunctions[i++]=factory.newFunctionDefinition(
                    factory.newVariable(fd.getFunctionVariable().getName()),newargs,newbd);
        }
        IASTexpression body = program.getBody();
        IASTexpression newbody = body.accept(this, data);
        return (factory).newProgram(newfunctions, newbody);
    }


    @Override
    public IASTexpression visit(IASTassignment iast, Data data) throws CompilationException {

        IASTexpression expression = iast.getExpression();
        IASTexpression newexpression = expression.accept(this, data);
        return (factory).newAssignment(iast.getVariable(), newexpression);    }

    @Override
    public IASTexpression visit(IASTloop iast, Data data) throws CompilationException {
        IASTexpression newcondition = iast.getCondition().accept(this, data);
        IASTexpression newbody = iast.getBody().accept(this, data);
        return (factory).newLoop(newcondition, newbody);    }

    @Override
    public IASTexpression visit(IASTalternative iast, Data data) throws CompilationException {
        IASTexpression c = iast.getCondition().accept(this, data);
        IASTexpression t = iast.getConsequence().accept(this, data);
        if ( iast.isTernary() ) {
            IASTexpression a = iast.getAlternant().accept(this, data);
            return factory.newAlternative(c, t, a);
        } else {
            IASTexpression whatever = factory.newBooleanConstant("false");
            return factory.newAlternative(c, t, whatever);
        }    }

    @Override
    public IASTexpression visit(IASTbinaryOperation iast, Data data) throws CompilationException {
        IASToperator operator = iast.getOperator();
        IASTexpression left = iast.getLeftOperand().accept(this, data);
        IASTexpression right = iast.getRightOperand().accept(this, data);
        return factory.newBinaryOperation(operator, left, right);
    }

    @Override
    public IASTexpression visit(IASTblock iast, Data data) throws CompilationException {


        IASTblock.IASTbinding[] bindings = iast.getBindings();
        IASTblock.IASTbinding[] newbindings =
                new IASTblock.IASTbinding[bindings.length];
        for ( int i=0 ; i<bindings.length ; i++ ) {
            IASTblock.IASTbinding binding = bindings[i];
            IASTexpression expr = binding.getInitialisation();
            IASTexpression newexpr = expr.accept(this, data);
            IASTvariable variable = binding.getVariable();
            IASTvariable newvariable = factory.newVariable(variable.getName());
            newbindings[i] = factory.newBinding(newvariable, newexpr);
        }
        IASTexpression newbody = iast.getBody().accept(this, data);
        return factory.newBlock(newbindings, newbody);
    }

    @Override
    public IASTexpression visit(IASTboolean iast, Data data) throws CompilationException {
        return factory.newBooleanConstant(iast.getValue().toString());
    }

    @Override
    public IASTexpression visit(IASTfloat iast, Data data) throws CompilationException {
        return factory.newFloatConstant(iast.getValue().toString());

    }

    @Override
    public IASTexpression visit(IASTinteger iast, Data data) throws CompilationException {
        return iast;
    }

    @Override
    public IASTexpression visit(IASTinvocation iast, Data data) throws CompilationException {
        IASTexpression funexpr = iast.getFunction().accept(this, data);
        IASTexpression[] arguments = iast.getArguments();
        IASTexpression[] args = new IASTexpression[arguments.length];
        for ( int i=0 ; i<arguments.length ; i++ ) {
            IASTexpression argument = arguments[i];
            IASTexpression arg = argument.accept(this, data);
            args[i] = arg;
        }

            return factory.newInvocation(funexpr, args);
        }

    @Override
    public IASTexpression visit(IASTsequence iast, Data data) throws CompilationException {
        IASTexpression[] expressions = iast.getExpressions();
        IASTexpression[] exprs = new IASTexpression[expressions.length];
        for ( int i=0 ; i< expressions.length ; i++ ) {
            exprs[i] = expressions[i].accept(this, data);
        }
        if ( iast.getExpressions().length == 1 ) {
            return exprs[0];
        } else {
            return factory.newSequence(exprs);
        }
    }

    @Override
    public IASTexpression visit(IASTstring iast, Data data) throws CompilationException {
        return factory.newStringConstant(iast.getValue());
    }

    @Override
    public IASTexpression visit(IASTunaryOperation iast, Data data) throws CompilationException {
        IASToperator operator = iast.getOperator();
        IASTexpression operand = iast.getOperand().accept(this, data);
        return factory.newUnaryOperation(operator, operand);
    }

    @Override
    public IASTexpression visit(IASTvariable iast, Data data) throws CompilationException {
        return factory.newVariable(iast.getName());
    }
}
