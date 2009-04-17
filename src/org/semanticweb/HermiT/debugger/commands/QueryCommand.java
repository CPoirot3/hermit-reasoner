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

public class QueryCommand extends AbstractCommand {

    public QueryCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "query";
    }
    public String[] getDescription() {
        return new String[] {
            "","prints whether there is a clash",
            "+C|$DG|? nodeID|?","prints facts for C - concept, DG - decr. graph, ? any unary predicate",
            "==|!=|-R|? nodeID|? nodeID|?","prints facts for R-role, ? any binary predicate"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: query");
        writer.println("Prints whether there is a clash or not.");
        writer.println("or");
        writer.println("usage: query unaryPredicate nodeID");
        writer.println("or");
        writer.println("usage: query binaryPredicate nodeID nodeID");
        writer.print("Prints all facts for the given unary or binary ");
        writer.print("predicate and node IDs. DLPredicate and ");
        writer.print("nodeID can be replaced with ? for unrestricted ");
        writer.println("querying. q can be used instead of query.");
    }
    public void execute(String[] args) {
        Object[] tuple=new Object[args.length-1];
        if (tuple.length==0) {
            // no further argument, so just check for a clash
            if (m_debugger.getTableau().getExtensionManager().containsClash())
                m_debugger.getOutput().println("Tableau currently contains a clash.");
            else
                m_debugger.getOutput().println("Tableau currently does not contain a clash.");
        }
        else {
            // further query arguments
            if ("?".equals(args[1]))
                tuple[0]=null;
            else {
                tuple[0]=getDLPredicate(args[1]);
                if (tuple[0]==null) {
                    m_debugger.getOutput().println("Invalid predicate '"+args[1]+"'.");
                    return;
                }
            }
            for (int index=1;index<tuple.length;index++) {
                String nodeIDString=args[index+1];
                if ("?".equals(nodeIDString))
                    // no particular nodeID given
                    tuple[index]=null;
                else {
                    int nodeID;
                    try {
                        nodeID=Integer.parseInt(nodeIDString);
                    }
                    catch (NumberFormatException e) {
                        m_debugger.getOutput().println("Invalid node ID.");
                        return;
                    }
                    tuple[index]=m_debugger.getTableau().getNode(nodeID);
                    if (tuple[index]==null) {
                        m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                        return;
                    }
                }
            }
            boolean[] boundPositions=new boolean[tuple.length];
            for (int index=0;index<tuple.length;index++)
                if (tuple[index]!=null)
                    boundPositions[index]=true;
            ExtensionTable extensionTable=m_debugger.getTableau().getExtensionManager().getExtensionTable(tuple.length);
            ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(boundPositions,ExtensionTable.View.TOTAL);
            System.arraycopy(tuple,0,retrieval.getBindingsBuffer(),0,tuple.length);
            retrieval.open();
            Set<Object[]> facts=new TreeSet<Object[]>(Printing.FactComparator.INSTANCE);
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                facts.add(tupleBuffer.clone());
                retrieval.next();
            }
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("===========================================");
            StringBuffer queryName=new StringBuffer("Query:");
            writer.print("Query:");
            for (int index=1;index<args.length;index++) {
                writer.print(' ');
                writer.print(args[index]);
                queryName.append(' ');
                queryName.append(args[index]);
            }
            writer.println();
            writer.println("===========================================");
            for (Object[] fact : facts) {
                writer.print(' ');
                printFact(fact,writer);
                writer.println();
            }
            writer.println("===========================================");
            writer.flush();
            showTextInWindow(buffer.toString(),queryName.toString());
            selectConsoleWindow();
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
