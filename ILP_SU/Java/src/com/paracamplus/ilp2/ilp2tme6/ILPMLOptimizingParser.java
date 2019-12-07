package com.paracamplus.ilp2.ilp2tme6;

import antlr4.*;
import com.paracamplus.ilp1.compiler.normalizer.*;
import com.paracamplus.ilp1.parser.*;
import com.paracamplus.ilp2.interfaces.*;
import com.paracamplus.ilp2.parser.ilpml.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class ILPMLOptimizingParser extends ILPMLParser {
    public ILPMLOptimizingParser(IASTfactory factory) {
        super(factory);
    }

    @Override
    public IASTprogram getProgram() throws ParseException {
        try {
            ANTLRInputStream in = new ANTLRInputStream(input.getText());
            // flux de caractères -> analyseur lexical
            ILPMLgrammar2Lexer lexer = new ILPMLgrammar2Lexer(in);
            // analyseur lexical -> flux de tokens
            CommonTokenStream tokens =	new CommonTokenStream(lexer);
            // flux tokens -> analyseur syntaxique
            ILPMLgrammar2Parser parser = new ILPMLgrammar2Parser(tokens);
            // démarage de l'analyse syntaxique
            ILPMLgrammar2Parser.ProgContext tree = parser.prog();
            // parcours de l'arbre syntaxique et appels du Listener
            ParseTreeWalker walker = new ParseTreeWalker();
            ILPMLListener extractor = new ILPMLListener((IASTfactory)factory);
            walker.walk(extractor, tree);
            CopyTransform copyTransform =new RenameTransform((IASTfactory)factory);
            return copyTransform.visit(tree.node, NormalizationEnvironment.EMPTY);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}
