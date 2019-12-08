package com.paracamplus.ilp2.ilp2tme6;

import com.paracamplus.ilp1.compiler.*;
import com.paracamplus.ilp1.compiler.interfaces.*;
import com.paracamplus.ilp1.compiler.optimizer.*;
import com.paracamplus.ilp1.compiler.test.*;
import com.paracamplus.ilp1.parser.xml.*;
import com.paracamplus.ilp2.ast.*;
import com.paracamplus.ilp2.compiler.*;
import com.paracamplus.ilp2.compiler.Compiler;
import com.paracamplus.ilp2.interfaces.*;
import com.paracamplus.ilp2.parser.ilpml.*;
import com.paracamplus.ilp2.parser.xml.XMLParser;
import org.junit.runners.*;

import java.io.*;
import java.lang.*;
import java.util.*;

public class CompilerTest extends com.paracamplus.ilp2.compiler.test.CompilerTest {


    protected static String[] samplesDirName = { "SamplesILP2", "SamplesILP1" };
    protected static String pattern = "ur?[0-78]\\d*-[123456](gfv)?";
    protected static String scriptCommand = "C/compileThenRun.sh +gc";
    protected static String XMLgrammarFile = "XMLGrammars/grammar2.rng";

    public CompilerTest(final File file) {
        super(file);
    }

    @Override
    public void configureRunner(CompilerRunner run) throws CompilationException {
        // configuration du parseur
        IASTfactory factory = new ASTfactory();
        IXMLParser xMLParser = new XMLParser(factory);
        xMLParser.setGrammar(new File(XMLgrammarFile));
        run.setXMLParser(xMLParser);
        run.setILPMLParser(new ILPMLOptimizingParser(factory));

        // configuration du compilateur
        IOperatorEnvironment ioe = new OperatorEnvironment();
        OperatorStuff.fillUnaryOperators(ioe);
        OperatorStuff.fillBinaryOperators(ioe);
        IGlobalVariableEnvironment gve = new GlobalVariableEnvironment();
        GlobalVariableStuff.fillGlobalVariables(gve);
        com.paracamplus.ilp2.compiler.Compiler compiler = new Compiler(ioe, gve);
        compiler.setOptimizer(new IdentityOptimizer());
        run.setCompiler(compiler);

        // configuration du script de compilation et exécution
        run.setRuntimeScript(scriptCommand);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<File[]> data() throws Exception {
        return CompilerRunner.getFileList(samplesDirName, pattern);
    }
}
