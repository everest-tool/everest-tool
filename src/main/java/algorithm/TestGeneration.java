package algorithm;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bridj.util.Pair;

import model.Automaton_;
import model.Graph;
import model.IOLTS;
import model.LTS;
import model.State_;
import model.Transition_;
import parser.ImportAutFile;
import util.Constants;
import view.ConformanceView;

public class TestGeneration {

	public static void main(String[] args) {
		String path = "C:\\Users\\camil\\Documents\\aut-separados\\iolts-spec.aut";
		try {
			IOLTS iolts = ImportAutFile.autToIOLTS(path, false, null, null);

			List<State_> endStates = new ArrayList<>();
			List<Transition_> endTransitions = new ArrayList<>();
			iolts.addQuiescentTransitions();
			// System.out.println(iolts);

			Automaton_ multgraph = multiGraphD(iolts, 1);

			// System.out.println(multgraph);//201 transições

			// *implementar
			// get word from multgraph

			// System.out.println(new Date());
			// List<String> words = Graph.getWords(multgraph);
			// System.out.println(new Date());
			//// words = new ArrayList<>(new HashSet<>(words));
			//
			// // System.out.println(words.size());
			//
			// for (String w : words) {
			// System.out.println(w);
			// }

			// // to decrease the performance of statePath
			// if (multgraph.getInitialState().getTransitions().size() == 0) {
			// if
			// (multgraph.getStates().stream().findAny().orElse(null).getTransitions().size()
			// == 0) {
			// for (Transition_ t : multgraph.getTransitions()) {
			// multgraph.getStates().stream().filter(x ->
			// x.equals(t.getIniState())).findFirst().orElse(null)
			// .addTransition(t);
			// t.setIniState(multgraph.getStates().stream().filter(x ->
			// x.equals(t.getIniState())).findFirst()
			// .orElse(null));
			// t.setEndState(multgraph.getStates().stream().filter(x ->
			// x.equals(t.getEndState())).findFirst()
			// .orElse(null));
			// }
			// }
			// multgraph.setInitialState(multgraph.getStates().stream()
			// .filter(x ->
			// x.equals(multgraph.getInitialState())).findFirst().orElse(null));
			// }
			//
			// List<String> testCases = new ArrayList<>();
			// // set words to reach final state, because of the modification of the final
			// // states
			// for (String w : words) {
			// for (List<State_> states_ : Operations.statePath(multgraph, w)) {
			//
			// // endState = states_.get(states_.size()-1);//-1
			// for (Transition_ t : endTransitions.stream()
			// .filter(x -> x.getIniState().equals(states_.get(states_.size() - 1)))
			// .collect(Collectors.toList())) {
			// testCases.add(w + " -> " + t.getLabel());
			// }
			// }
			// }
			//
			// // System.out.println(testCases);
			//
			// for (String tc : testCases) {
			// System.out.println(testPurpose(multgraph, tc, iolts.getOutputs(),
			// iolts.getInputs()));// "a -> x -> b ->
			// // a -> a -> b
			// // -> b -> x"
			// }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static IOLTS testPurpose(Automaton_ multgraph, String testCase, List<String> li, List<String> lu) {
		li = new ArrayList<String>(new HashSet(new ArrayList<String>(li)));
		lu = new ArrayList<String>(new HashSet(new ArrayList<String>(lu)));
		IOLTS tp = new IOLTS();
		li = new ArrayList<>(new HashSet<>(li));
		lu = new ArrayList<>(new HashSet<>(lu));
		tp.setInputs(li);
		tp.setOutputs(lu);

		State_ ini = new State_(multgraph.getInitialState().getName());
		tp.setInitialState(ini);

		State_ pass = new State_("pass");
		tp.addState(ini);
		tp.addState(pass);

		State_ current = ini;

		for (String w : testCase.split(" -> ")) {
			for (State_ s : multgraph.reachedStates(current.getName(), w)) {
				tp.addState(s);
				tp.addTransition(new Transition_(current, w, s));
				current = new State_(s.getName());
			}
		}

		boolean existsLu;
		for (State_ s : tp.getStates().stream().filter(x -> !x.getName().equals("pass") && !x.getName().equals("fail"))
				.collect(Collectors.toList())) {
			existsLu = false;

			for (String l : li) {
				if (!tp.transitionExists(s.getName(), l)) {
					tp.addTransition(new Transition_(s, l, pass));
				}
			}

			for (String l : lu) {
				if (tp.transitionExists(s.getName(), l)) {
					existsLu = true;
				}
			}

			if (existsLu == false) {
				tp.addTransition(new Transition_(s, lu.get(0), pass));
			}
		}

		State_ fail = new State_("fail");
		for (String l : li) {
			tp.addTransition(new Transition_(pass, l, pass));
			tp.addTransition(new Transition_(fail, l, fail));
		}

		// invert inp/out to generate tp
		List<String> inp = tp.getInputs();
		tp.setInputs(tp.getOutputs());
		tp.setOutputs(inp);

		return tp;
	}

	/***
	 * 
	 * @param S
	 *            the specification model
	 * @param m
	 *            max states of specification
	 * @return
	 */
	public static Automaton_ multiGraphD(IOLTS S, int m) {
		int level = 0;
		IOLTS D = new IOLTS();
		State_ iniState = new State_(S.getInitialState() + Constants.COMMA + level);
		D.setInitialState(iniState);
		D.addState(iniState);
		State_ current;

		List<State_> toVisit = new ArrayList<>();
		toVisit.add(S.getInitialState());

		List<State_> toVisit_aux = new ArrayList<>(toVisit);

		int totalLevels = S.getStates().size() * m + 1;

		List<String> L = new ArrayList<>(S.getInputs());
		L.addAll(S.getOutputs());

		D.setAlphabet(L);

		List<Pair<String, String>> nextLevel = new ArrayList<>();
		State_ d;

		// construct first level
		while (toVisit.size() > 0) {
			current = toVisit.remove(0);// remove from the beginning of queue
			for (Transition_ t : S.transitionsByIniState(current)) {
				if (!toVisit_aux.contains(t.getEndState())) {
					toVisit.add(t.getEndState());
					toVisit_aux.add(t.getEndState());
				}

				if (current.equals(t.getEndState()) || nextLevel.contains(new Pair(current, t.getEndState()))) {
					d = new State_(t.getEndState() + Constants.COMMA + "1");
				} else {
					for (Transition_ t2 : S.transitionsByIniState(t.getEndState())) {
						if (t2.getEndState().equals(current)) {
							nextLevel.add(new Pair(t.getEndState(), current));
						}
					}
					d = new State_(t.getEndState() + Constants.COMMA + "0");
				}

				D.addState(d);
				D.addTransition(new Transition_(new State_(current + Constants.COMMA + "0"), t.getLabel(), d));
			}
		}

		int leveld, leveld2 = 0;
		String named, named2;
		State_ ini, end;
		List<Transition_> transition = new ArrayList<>();
		List<State_> states = new ArrayList<>();

		// construct rest of levels
		for (Transition_ t : D.getTransitions()) {
			named = t.getIniState().getName().split(Constants.COMMA)[0];
			named2 = t.getEndState().getName().split(Constants.COMMA)[0];
			leveld = Integer.parseInt(t.getIniState().getName().split(Constants.COMMA)[1]);
			leveld2 = Integer.parseInt(t.getEndState().getName().split(Constants.COMMA)[1]);

			while (leveld2 + 1 < totalLevels) {
				leveld++;
				leveld2++;

				ini = new State_(named + Constants.COMMA + leveld);
				end = new State_(named2 + Constants.COMMA + leveld2);
				transition.add(new Transition_(ini, t.getLabel(), end));
				states.add(ini);
				states.add(end);
			}
		}
		D.getTransitions().addAll(transition);
		D.getStates().addAll(new ArrayList<>(new HashSet<>(states)));

		State_ fail = new State_("fail");
		D.addState(fail);
		for (String l : S.getOutputs()) {
			for (State_ s : D.getStates()) {
				if (D.reachedStates(s.getName(), l).size() == 0) {
					D.addTransition(new Transition_(s, l, fail));
				}
			}
		}

		Automaton_ a = D.ltsToAutomaton();
		a.setFinalStates(Arrays.asList(new State_[] { fail }));

		return a;
	}

	public static void runTestWithTP(String tpFolder, boolean oneIut, String pathImplementation, String iutFolder,
			String pathCsv) {
		File tpFolderF = new File(tpFolder);
		File[] listOfTpFiles = tpFolderF.listFiles();

		File iutFolderF;
		File[] listOfIutFiles;
		IOLTS tp;

		Automaton_ tpAutomaton;
		List<String> wordsTp;

		List<List<String>> toSave = new ArrayList<>();

		try {
			// each tp
			for (File fileTp : listOfTpFiles) {
				if (ConformanceView.isAutFile(fileTp)) {
					tp = ImportAutFile.autToIOLTS(tpFolder + "//" + fileTp.getName(), false, new ArrayList<>(), new ArrayList<>());
					tpAutomaton = tp.ioltsToAutomaton();
					tpAutomaton.addFinalStates(new State_("fail"));
					wordsTp = Graph.getWords(tpAutomaton);

					for (String word : wordsTp) {

						// one iut
						if (oneIut) {
							toSave = runIutTp(pathImplementation, word, tpFolder, fileTp);

							saveOnCSVFile(toSave, pathCsv);
						} else {
							// iut in batch
							if (!oneIut) {
								iutFolderF = new File(iutFolder);
								listOfIutFiles = iutFolderF.listFiles();

								// for each iut
								for (File fileIut : listOfTpFiles) {
									if (ConformanceView.isAutFile(fileIut)) {
										toSave = runIutTp(iutFolder + "//" + fileIut.getName(), word, tpFolder, fileTp);

									}
								}

							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<List<String>> runIutTp(String pathImplementation, String word, String tpFolder, File fileTp) {
		List<List<String>> toSave = new ArrayList<>();
		try {
			IOLTS iut;
			iut = ImportAutFile.autToIOLTS(pathImplementation, false, null, null);
			iut.addQuiescentTransitions();

			List<String> partialResult = new ArrayList<>();
			List<List<State_>> statesPath;
			//System.out.println(word);
			Operations.addTransitionToStates(iut);
			statesPath = Operations.statePath(iut, word);
			for (List<State_> statePath : statesPath) {
				partialResult = new ArrayList<>();
				partialResult.add(pathImplementation);
				partialResult.add(tpFolder + "//" + fileTp.getName());
				partialResult.add(word);
				partialResult.add(statePath.toString());

				if (statePath.get(statePath.size() - 1).getName().contains(Constants.NO_TRANSITION)) {
					// inconclusive
					partialResult.add(Constants.RUN_VERDICT_INCONCLUSIVE);

				} else {
					// not conform
					partialResult.add(Constants.RUN_VERDICT_NON_CONFORM);

				}

				toSave.add(partialResult);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toSave;
	}

	public static void saveOnCSVFile(List<List<String>> toSave, String pathCsv) {

		try {
			pathCsv += "\\run-result.csv";
			String delimiterCSV = ",";

			ArrayList<String> headerCSV = new ArrayList<String>();
			headerCSV.add("iut file");
			headerCSV.add("tp file");
			headerCSV.add("test case");
			headerCSV.add("state path");
			headerCSV.add("verdict");

			FileWriter csvWriter;

			File file = new File(pathCsv);
			if (!file.exists()) {
				file.createNewFile();
			}

			if (new File(pathCsv).length() == 0) {
				csvWriter = new FileWriter(pathCsv);

				for (String header : headerCSV) {
					csvWriter.append(header);
					csvWriter.append(delimiterCSV);
				}
				csvWriter.append("\n");
			} else {
				csvWriter = new FileWriter(pathCsv, true);
			}

			for (List<String> row : toSave) {
				csvWriter.append(String.join(delimiterCSV, row));
			}

			csvWriter.append("\n");
			csvWriter.flush();
			csvWriter.close();

		} catch (Exception e)

		{
			e.printStackTrace();
		}
	}
}
