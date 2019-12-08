package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.interpreter.*;
import com.paracamplus.ilp1.interpreter.interfaces.*;
import com.paracamplus.ilp1.interpreter.test.*;
import com.paracamplus.ilp1.parser.xml.*;
import com.paracamplus.ilp2.ast.*;
import com.paracamplus.ilp2.interfaces.*;
import com.paracamplus.ilp2.interpreter.Interpreter;
import com.paracamplus.ilp2.parser.xml.XMLParser;
import org.junit.runners.*;

import java.io.*;
import java.util.*;

public class InterpreterTest extends com.paracamplus.ilp2.interpreter.test.InterpreterTest {
    public InterpreterTest(File file) {
        super(file);
    }
    protected static String[] samplesDirName = { "SamplesTME6" ,"SamplesILP2", "SamplesILP1"  };
    protected static String pattern = "ur?[0-78]\\d*-[1234567](gfv)?";
    protected static String XMLgrammarFile = "XMLGrammars/grammar2.rng";


    public void configureRunner(InterpreterRunner run) throws EvaluationException {
        // configuration du parseur
        IASTfactory factory = new ASTfactory();
        IXMLParser xmlparser = new XMLParser(factory);
        xmlparser.setGrammar(new File(XMLgrammarFile));
        run.setXMLParser(xmlparser);
        run.setILPMLParser(new ILPMLOptimizingParser(factory));

        // configuration de l'interprète
        StringWriter stdout = new StringWriter();
        run.setStdout(stdout);
        IGlobalVariableEnvironment gve = new GlobalVariableEnvironment();
        GlobalVariableStuff.fillGlobalVariables(gve, stdout);
        IOperatorEnvironment oe = new OperatorEnvironment();
        OperatorStuff.fillUnaryOperators(oe);
        OperatorStuff.fillBinaryOperators(oe);
        Interpreter interpreter = new Interpreter(gve, oe);
        run.setInterpreter(interpreter);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<File[]> data() throws Exception {
        return InterpreterRunner.getFileList(samplesDirName, pattern);
    }
}
