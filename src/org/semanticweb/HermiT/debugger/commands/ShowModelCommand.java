package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Printing;

public class ShowModelCommand extends AbstractCommand {

    public ShowModelCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showModel";
    }
    public String[] getDescription() {
        return new String[] {
            "","prints all assertions",
            "predicate","prints all assertions for the given predicate"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showModel");
        writer.println("or");
        writer.println("usage: showModel DLPredicate");
        writer.println("If no predicate is given, prints the whole model, otherwise prints all assertions containing the predicate. ");
    }
    public void execute(String[] args) {
        Set<Object[]> facts=new TreeSet<Object[]>(Printing.FactComparator.INSTANCE);
        String title;
        if (args.length<2) {
            for (ExtensionTable extensionTable : m_debugger.getTableau().getExtensionManager().getExtensionTables()) {
                ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.TOTAL);
                loadFacts(facts,retrieval);
            }
            title="Current model";
        }
        else {
            DLPredicate dlPredicate=getDLPredicate(args[1]);
            if (dlPredicate!=null) {
                ExtensionTable extensionTable=m_debugger.getTableau().getExtensionManager().getExtensionTable(dlPredicate.getArity()+1);
                boolean[] bindings=new boolean[extensionTable.getArity()];
                bindings[0]=true;
                ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(bindings,ExtensionTable.View.TOTAL);
                retrieval.getBindingsBuffer()[0]=dlPredicate;
                loadFacts(facts,retrieval);
                title="Assertions containing the predicate '"+m_debugger.getPrefixes().abbreviateURI(dlPredicate.toString())+"'.";
            }
            else {
                int nodeID;
                try {
                    nodeID=Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e) {
                    m_debugger.getOutput().println("Invalid ID of the node.");
                    return;
                }
                Node node=m_debugger.getTableau().getNode(nodeID);
                if (node==null) {
                    m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                    return;
                }
                for (ExtensionTable extensionTable : m_debugger.getTableau().getExtensionManager().getExtensionTables())
                    for (int position=0;position<extensionTable.getArity();position++) {
                        boolean[] bindings=new boolean[extensionTable.getArity()];
                        bindings[position]=true;
                        ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(bindings,ExtensionTable.View.TOTAL);
                        retrieval.getBindingsBuffer()[position]=node;
                        loadFacts(facts,retrieval);
                    }
                title="Assertions containing node '"+node.getNodeID()+"'.";
            }
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        Object lastPredicate=null;
        for (Object[] fact : facts) {
            if (lastPredicate!=fact[0]) {
                lastPredicate=fact[0];
                writer.println();
            }
            writer.print(' ');
            printFact(fact,writer);
            writer.println();
        }
        writer.flush();
        showTextInWindow(buffer.toString(),title);
        selectConsoleWindow();
    }
    protected void loadFacts(Set<Object[]> facts,ExtensionTable.Retrieval retrieval) {
        retrieval.open();
        while (!retrieval.afterLast()) {
            facts.add(retrieval.getTupleBuffer().clone());
            retrieval.next();
        }
    }
    protected void printFact(Object[] fact,PrintWriter writer) {
        Object dlPredicate=fact[0];
        if (dlPredicate instanceof Concept)
            writer.print(((Concept)dlPredicate).toString(m_debugger.getPrefixes()));
        else if (dlPredicate instanceof DLPredicate)
            writer.print(((DLPredicate)dlPredicate).toString(m_debugger.getPrefixes()));
        else
            throw new IllegalStateException("Internal error: invalid predicate.");
        writer.print('[');
        for (int position=1;position<fact.length;position++) {
            if (position!=1)
                writer.print(',');
            writer.print(((Node)fact[position]).getNodeID());
        }
        writer.print(']');
    }
}
