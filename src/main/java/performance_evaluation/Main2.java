package performance_evaluation;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.rmi.server.Operation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.Graph;

import algorithm.Operations;
import model.Automaton_;
import model.IOLTS;
import model.State_;
import model.Transition_;
import parser.ImportGraphmlFile;
import util.AutGenerator;

public class Main2 {

	public static void main(String[] args) throws Exception {

		String path = "C:\\Users\\camil\\Documents\\aut-modelos\\iolts-impl-r.aut";

		// IOLTS iolts = ImportAutFile.autToIOLTS(path, false, null, null);
		// iolts.addTransition(new Transition_(new State_("q0"), "a", new
		// State_("q3")));
		// iolts.getTransitions().remove(8);

		// for (Transition_ t: iolts.getTransitions()) {
		// t.setIniState(iolts.getStates().stream().filter(x->x.equals(t.getIniState())).findFirst().orElse(null));
		// t.setEndState(iolts.getStates().stream().filter(x->x.equals(t.getEndState())).findFirst().orElse(null));
		// iolts.getStates().stream().filter(x->x.equals(t.getIniState())).findFirst().orElse(null).addTransition(t);
		//
		// }
		// iolts.setInitialState(iolts.getStates().stream().filter(x ->
		// x.equals(iolts.getInitialState())).findFirst().orElse(null));
		//
		// System.out.println(Operations.statePath(iolts, "a -> b"));

		Automaton_ a = new Automaton_();
		// IOLTS iolts = new IOLTS();
		a.setInitialState(new State_("q0"));

		a.addState(new State_("q0"));
		a.addState(new State_("q1"));
		a.addState(new State_("q2"));
		a.addState(new State_("q3"));
		a.addState(new State_("q4"));
		a.addState(new State_("q5"));

		a.addTransition(new Transition_(new State_("q0"), "a", new State_("q1")));
		a.addTransition(new Transition_(new State_("q1"), "b", new State_("q2")));
		a.addTransition(new Transition_(new State_("q1"), "a", new State_("q3")));
		a.addTransition(new Transition_(new State_("q3"), "a", new State_("q4")));
		a.addTransition(new Transition_(new State_("q3"), "b", new State_("q4")));
		a.addTransition(new Transition_(new State_("q3"), "a", new State_("q5")));
		a.addTransition(new Transition_(new State_("q4"), "a", new State_("q5")));

		for (Transition_ t : a.getTransitions()) {
			t.setIniState(a.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null));
			t.setEndState(a.getStates().stream().filter(x -> x.equals(t.getEndState())).findFirst().orElse(null));
			a.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null).addTransition(t);

		}
		a.setInitialState(a.getStates().stream().filter(x -> x.equals(a.getInitialState())).findFirst().orElse(null));

		a.setFinalStates(new ArrayList<>());
		a.addFinalStates(new State_("q5"));
		a.addFinalStates(new State_("q4"));
		a.addFinalStates(new State_("q2"));

		IOLTS iolts = new IOLTS();
		// IOLTS iolts = new IOLTS();
		iolts.setInitialState(new State_("q0"));

		iolts.addState(new State_("q0"));
		iolts.addState(new State_("q1"));
		iolts.addState(new State_("q2"));
		iolts.addState(new State_("q3"));
		iolts.addState(new State_("q4"));
		iolts.addState(new State_("q5"));

		iolts.addTransition(new Transition_(new State_("q0"), "a", new State_("q1")));
		iolts.addTransition(new Transition_(new State_("q1"), "b", new State_("q2")));
		iolts.addTransition(new Transition_(new State_("q1"), "a", new State_("q3")));
		iolts.addTransition(new Transition_(new State_("q3"), "a", new State_("q4")));
		iolts.addTransition(new Transition_(new State_("q3"), "b", new State_("q4")));
		//iolts.addTransition(new Transition_(new State_("q3"), "a", new State_("q5")));
		iolts.addTransition(new Transition_(new State_("q4"), "a", new State_("q5")));
		
		for (Transition_ t : iolts.getTransitions()) {
			t.setIniState(iolts.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null));
			t.setEndState(iolts.getStates().stream().filter(x -> x.equals(t.getEndState())).findFirst().orElse(null));
			iolts.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null).addTransition(t);

		}
		iolts.setInitialState(
				iolts.getStates().stream().filter(x -> x.equals(iolts.getInitialState())).findFirst().orElse(null));

		// List<State_> states = new ArrayList<>();
		// states.add(new State_("q2"));
		// states.add(new State_("q3"));
		// a.setFinalStates(states);

		System.out.println(Operations.path(iolts, iolts, a, true, true, 10));

		System.out.println(Operations.getWordsFromAutomaton(a, false, 10));// true

		// // \iocoCheckerExamples
		// //
		// "C:\\Users\\camil\\Desktop\\modelos-jtorx\\iocoCheckerExamples\\VendingMachine\\r2.graphml"
		// //
		// "C:\\Users\\camil\\Desktop\\modelos-jtorx\\iocoCheckerExamples\\Newspaper\\spec.graphml"
		// //"C:\Users\camil\Desktop\modelos-jtorx\riverCrossing04\models"
		// IOLTS iolts = ImportGraphmlFile.graphToIOLTS(
		// "C:\\Users\\camil\\Desktop\\modelos-jtorx\\riverCrossing04\\models -
		// Copia\\s2.graphml", false, null,
		// null);
		// //System.out.println(iolts);
		// String aut = AutGenerator.ioltsToAut(iolts);
		//
		// String aut_file =
		// "C:\\Users\\camil\\Desktop\\modelos-jtorx\\_graphmlToAut\\riverCrossing\\s2_.aut";
		// BufferedWriter writer = new BufferedWriter(new FileWriter(aut_file));
		// writer.write(aut);
		// writer.close();

	}

}
