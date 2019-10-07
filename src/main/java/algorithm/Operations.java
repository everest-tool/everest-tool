/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.io.SocketOutputStream;
import org.bridj.util.Pair;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import model.Automaton_;
import model.State_;
import model.IOLTS;
import model.LTS;
import model.Transition_;
import util.Constants;

/**
 * Class Operations contain complement, determinism, union, intersection
 * 
 * @author Camila
 */
public class Operations {

	/***
	 * completes the automaton received by parameter
	 * 
	 * @param Q
	 *            automaton
	 * @return acomp complet automaton
	 */
	public static Automaton_ complement(Automaton_ Q) {
		Automaton_ acomp = new Automaton_();
		// define the initial state
		acomp.setInitialState(Q.getInitialState());
		// define o alphabet
		acomp.setAlphabet(Q.getAlphabet());
		// completes the received automaton by parameter
		State_ scomp = new State_("complement");
		// add states to automato, gives "new" to not paste reference
		acomp.setStates(new ArrayList<State_>(Q.getStates()));

		// add self loop in scomp acceptance state, with all labels
		for (String a : acomp.getAlphabet()) {
			acomp.addTransition(new Transition_(scomp, a, scomp));
		}

		// add the state to be used to complete
		acomp.addState(scomp);
		acomp.addFinalStates(scomp);

		// reverses who was final state cases to be, who was was not becomes final
		// state
		for (State_ e : acomp.getStates()) {
			// checks whether the state is already in the list of end states
			if (!Q.getFinalStates().contains(e)) {
				acomp.addFinalStates(e);
			}
		}

		List<State_> result;

		// completes states that are not input / output complete
		for (State_ e : Q.getStates()) {
			for (String l : acomp.getAlphabet()) {

				// if there are no transitions, you must complete by creating a new transition
				// from state "e" to state "scomp" with the label "l"
				if (!Q.transitionExists(e.getName(), l)) {
					acomp.addTransition(new Transition_(e, l, scomp));
				} else {
					// checks whether there is a transition from state "e" with the label "l"
					result = Q.reachedStates(e.getName(), l);

					// when no deterministic, so starting from "e" with "l" can
					// reach more than one end state, so a transition for each found state must be
					// created
					for (State_ estadoFim : result) {
						acomp.addTransition(new Transition_(e, l, estadoFim));
					}
				}
			}

		}

		return acomp;
	}

	/***
	 * Converts TAU transitions into EPSILON transition
	 * 
	 * @param transitions
	 * 
	 * @return as transitions without the TAU label
	 */
	// public static List<Transition_> processTauTransition(List<Transition_>
	// transitions) {
	// List<Transition_> newTransitions = new ArrayList<Transition_>();
	// // treats transitions when TAU in LTS converts to EPSILON in automato
	// for (Transition_ t : transitions) {
	// if (t.getLabel().equals(Constants.TAU)) {
	// newTransitions.add(new Transition_(t.getIniState(), Constants.EPSILON,
	// t.getEndState()));
	// } else {
	// newTransitions.add(t);
	// }
	// }
	// return newTransitions;
	// }

	/***
	 * It transforms the nondeterministic automaton into deterministic, removing the
	 * EPSILON transitions and transitions starting from the same state with the
	 * same label
	 * 
	 * @param automaton
	 *            a ser determinizado
	 * @return automato deterministico
	 */
	public static Automaton_ convertToDeterministicAutomaton(Automaton_ automaton) {

		// verifies whether the automaton is already deterministic
		if (!automaton.isDeterministic()) {
			// create new deterministic automaton
			Automaton_ deteministic = new Automaton_();
			// the initial state of the deterministic automaton are all states reached from
			// the initial state of the
			// automaton received by parameter with the transition epsilon, the name of the
			// initial state is therefore the
			// union of all states reached separated by separator1
			String stateName = automaton.reachableStatesWithEpsilon(automaton.getInitialState()).stream()
					.map(x -> x.getName()).collect(Collectors.joining(Constants.SEPARATOR));

			// define the initial state of deterministic automaton
			deteministic.setInitialState(new State_(stateName.replace(Constants.SEPARATOR, "")));
			// null state, used when no state is reached
			State_ nullState = new State_(Constants.EMPTY + Constants.SEPARATOR);
			// list which defines the stop condition and serves to go through the states of
			// the automato
			List<String> aux = new ArrayList<String>(Arrays.asList(stateName));
			// copy of the aux list, since elements are removed from the aux list,
			// and therefore a list is needed to store the states already visited
			List<String> auxCopy = new ArrayList<String>(aux);
			// list of states reached
			List<State_> auxState = new ArrayList<State_>();
			// used to know which states make each state:
			// (state1@state2) a state composed of two synchronized states
			String[] stringState;
			List<State_> result;
			State_ state;
			List<String> finalStates = new ArrayList<String>();

			// checks whether the initial state is final state
			stringState = stateName.split(Constants.SEPARATOR);
			for (String s : stringState) {
				auxState.add(new State_(s));
			}

			if (!Collections.disjoint(automaton.getFinalStates(), auxState)) {
				finalStates.add(stateName.replaceAll(Constants.SEPARATOR, ""));
			}

			// remove EPSILON from the alphabet
			automaton.getAlphabet().remove(Constants.EPSILON);
			// while there are states to be covered
			while (aux.size() > 0) {
				// current state is the first list state
				stringState = aux.remove(0).split(Constants.SEPARATOR);
				// add the state removed from the auxiliary list to the list of automato states
				deteministic.addState(new State_(String.join("", stringState)));

				for (String alphabet : automaton.getAlphabet()) {
					// initialize aux list
					auxState = new ArrayList<State_>();
					// for each state that is composed the state. ex: (state1 @ state2) in this case
					// there are two states
					for (int i = 0; i < stringState.length; i++) {
						if (automaton.transitionExists(stringState[i], alphabet)) {
							// check if the transition exists from the stringState[i] with the word
							// "alphabet"
							result = automaton.reachedStates(stringState[i], alphabet);
							// for each state reached by the transitions found
							for (State_ estadosFim : result) {
								// add the reachable states with EPSILON from the states
								// found (result.getFetStates ())
								auxState.addAll(automaton.reachableStatesWithEpsilon(estadosFim));
							}
						}

					}

					// if there are states reached from stateString with the label "alphabet"
					if (auxState.size() > 0) {
						// remove possible duplicate states
						Set<String> set = new HashSet<>(auxState.size());
						auxState.removeIf(p -> !set.add(p.getName()));
						// orders the states reached so that there is no possibility of considering, for
						// example,
						// that the state "ab" is different from "ba"
						auxState.sort(Comparator.comparing(State_::getName));
						// the name of the state reached is the union of all states reached separated by
						// the separator
						stateName = auxState.stream().map(x -> x.getName())
								.collect(Collectors.joining(Constants.SEPARATOR));

						// verifies if the state reached is a final state, if one of the states
						// reached is final state in the original automaton received by parameter
						if (!Collections.disjoint(automaton.getFinalStates(), auxState)
								&& !finalStates.contains(stateName.replaceAll(Constants.SEPARATOR, ""))) {
							finalStates.add(stateName.replaceAll(Constants.SEPARATOR, ""));
						}
						// } else {
						// // if no state is reached from stateString with the label "alphabet"
						// // then consider that starting from stateString with the label "alphabet"
						// // arrives in the "nullState" state
						// stateName = nullState.getName();
						// }
						// creates the state reached
						state = new State_(stateName);
						// add this state to automaton
						deteministic.addState(new State_(state.getName().replaceAll(Constants.SEPARATOR, "")));
						// adds the new transition from the "StateString" with the "alphabet" label to
						// the "state"
						deteministic.addTransition(new Transition_(new State_(String.join("", stringState)), alphabet,
								new State_(state.getName().replaceAll(Constants.SEPARATOR, ""))));

						// if the reached state is not in the list auxCopy should add it to explore
						// later
						if (!auxCopy.contains(state.getName())) {
							aux.add(stateName);
							auxCopy.add(stateName);
						}
					}
				}
			}

			// adds the final states to the deterministic automaton
			for (String nomeEstadoFinal : finalStates) {
				deteministic.addFinalStates(new State_(nomeEstadoFinal));
			}

			return deteministic;
		}

		// when the automaton received by parameter is already deterministic, it returns
		// itself
		return automaton;
	}

	/***
	 * receives as parameter two automata and returns the automaton resulting from
	 * the intersection between them.
	 * 
	 * @param Q
	 *            automaton
	 * @param S
	 *            automaton
	 * @return automaton result of the intersection between the automata Q and S
	 */
	public static Automaton_ intersection(Automaton_ Q, Automaton_ S, Integer nFinalState) {
		// intersection automato
		Automaton_ Ar = new Automaton_();
		// add alphabet to automaton
		Ar.setAlphabet(Q.getAlphabet());
		// creates and defines the initial state of the automaton as the synchronization
		// of the initial states of S and Q
		String nameSyncState = S.getInitialState().getName() + Constants.SEPARATOR + Q.getInitialState().getName();
		// remove the name of the state
		State_ r0 = new State_(nameSyncState.replaceAll(Constants.SEPARATOR, ""));
		// used for split
		r0.setInfo(nameSyncState);
		// set initial state
		Ar.setInitialState(r0);
		// add initial state to list of state
		Ar.addState(r0);

		// list of synchronized states that will be visited and removed while the
		// algorithm runs
		List<State_> synchronizedStates = new ArrayList<State_>();
		// add state r0 to list of states
		synchronizedStates.add(r0);
		// variables for synchronization of the states of S and Q
		String s = null, q = null;
		State_ removed = null, synchronized_ = null;
		String[] part;
		List<State_> sTransitions, qTransitions;

		// while there are states to be synchronized
		endIntersection: while (synchronizedStates.size() > 0) {
			// emoves the first element from the list of states to be synchronized
			removed = synchronizedStates.remove(0);

			// as the state name is (s, q), splits the string to find the 's' and 'q'
			part = removed.getInfo().split(Constants.SEPARATOR);
			s = part[0];
			q = part[1];

			// check for transitions in S and Q that synchronize
			for (String l : Ar.getAlphabet()) {

				// synchronizes in Q and S
				if (S.transitionExists(s, l) && Q.transitionExists(q, l)) {
					// if there exists in S transitions starting from state "s" with the label "l"
					sTransitions = S.reachedStates(s, l);
					// if there exists in Q transitions from state "q" with the label "l"
					qTransitions = Q.reachedStates(q, l);

					// synchronized states in S, there may be more than one in case of no
					// determinism
					for (State_ endStateS : sTransitions) {
						// synchronized states in Q, there may be more than one in case of
						// non-determinism
						for (State_ endStateQ : qTransitions) {
							// creates a new state that matches what was synced
							nameSyncState = endStateS.getName() + Constants.SEPARATOR + endStateQ.getName();
							// the state name is without the separator
							synchronized_ = new State_(nameSyncState.replaceAll(Constants.SEPARATOR, ""));
							// description of the state with the separator
							synchronized_.setInfo(nameSyncState);
							if (!Ar.getStates().contains(synchronized_)) {
								synchronizedStates.add(new State_(synchronized_));
							}

							// adds the created state to the automaton
							synchronized_.setName(synchronized_.getName().replaceAll(Constants.SEPARATOR, ""));
							Ar.addState(synchronized_);

							// adds the transition that corresponds to synchronization
							Ar.addTransition(new Transition_(removed, l, synchronized_));

							// add final state
							if (S.getFinalStates().contains(new State_(endStateS.getName()))
									&& Q.getFinalStates().contains(new State_(endStateQ.getName()))) {
								Ar.addFinalStates(synchronized_);

								if (nFinalState != null && Ar.getFinalStates().size() == nFinalState
										+ (nFinalState < 15 ? (nFinalState * 2) : (nFinalState / 4))) {
									// System.err.println("break");
									break endIntersection;
								}
							}

						}
					}
				}
			}
		}

		// part = null;
		// s = "";
		// q = "";
		// List<State_> finalStateQ = new ArrayList<State_>(Q.getFinalStates());
		// List<State_> finalStateS = new ArrayList<State_>(S.getFinalStates());
		//
		// // synchronized states to check which end states
		// for (State_ state : Ar.getStates()) {
		// // from the getInfo it is possible to separate the synchronized states
		// part = state.getInfo().split(Constants.SEPARATOR);
		// s = part[0];
		// q = part[1];
		// // verifies whether the synchronized states were final states in Q and S
		// if (finalStateS.contains(new State_(s)) && finalStateQ.contains(new
		// State_(q))) {
		// Ar.addFinalStates(state);
		// }
		// }
		return Ar;
	}

	/***
	 * receives as parameter two automata and returns the automaton resulting from
	 * the union between them.
	 * 
	 * @param Q
	 *            automaton
	 * @param S
	 *            automaton
	 * @return automaton result of the union between the automata Q and S
	 */
	public static Automaton_ union(Automaton_ Q, Automaton_ S) {
		// automato union
		Automaton_ Ay = new Automaton_();

		// the transitions of the union automaton, is the union of the transitions of Q
		// and S
		Set<Transition_> transicoes = new HashSet<Transition_>();
		transicoes.addAll(Q.getTransitions());
		transicoes.addAll(S.getTransitions());
		Ay.setTransitions(new ArrayList<Transition_>(transicoes));

		// the states of the union automata, is the union of the states of Q and S
		Set<State_> estados = new HashSet<State_>();
		estados.addAll(Q.getStates());
		estados.addAll(S.getStates());
		Ay.setStates(new ArrayList<State_>(estados));

		// the final states of the union automaton, is the union of the final states of
		// Q and S
		Set<State_> estadosFinais = new HashSet<State_>();
		estadosFinais.addAll(Q.getFinalStates());
		estadosFinais.addAll(S.getFinalStates());
		Ay.setFinalStates(new ArrayList<State_>(estadosFinais));

		// the alphabet of the union automata, is the union of the alphabet of Q and S
		Set<String> alfabeto = new HashSet<String>(Q.getAlphabet());
		alfabeto.addAll(S.getAlphabet());
		Ay.setAlphabet(new ArrayList<String>(alfabeto));

		// to union the automaton is created a new state that will be the initial state
		State_ estado = new State_("init");
		// add the inital state to list of states
		Ay.addState(estado);
		// definir as initial state
		Ay.setInitialState(estado);
		// add the transitions from the initial state with the label EPSILON to the
		// initial state of S and of Q
		Ay.addTransition(new Transition_(estado, Constants.EPSILON, S.getInitialState()));
		Ay.addTransition(new Transition_(estado, Constants.EPSILON, Q.getInitialState()));

		return convertToDeterministicAutomaton(Ay);
	}

	/***
	 * Using the brics library converts the regex into Automaton
	 * 
	 * @param regex
	 * @param tag
	 *            string that will serve to name the states, to distinguish the
	 *            states of D and F because the library creates states named "1",
	 *            "2", "3" and therefore there may be conflict going forward
	 * @return automaton which accepts the regex received by parameter
	 */
	public static Automaton_ regexToAutomaton(String regex, String tag, List<String> alphabet) {
		// order alphabet descending by length string and then alphabetically
		Collections.sort(alphabet, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.length() != o2.length()) {
					return o2.length() - o1.length(); // overflow impossible since lengths are non-negative
				}
				return o1.compareTo(o2);
			}
		});

		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < alphabet.size(); i++) {
			map.put(Character.toString(Constants.ALPHABET[i]), alphabet.get(i));
			regex = regex.replace(alphabet.get(i), Character.toString(Constants.ALPHABET[i]));
		}

		// regex to automaton
		RegExp regExp = new RegExp(regex, RegExp.ALL);
		Automaton automaton = regExp.toAutomaton();

		Automaton_ automatonBrics = automatonBricsInAutomaton_(automaton, tag);
		List<Transition_> newTransition = new ArrayList<>();
		for (Transition_ t : automatonBrics.getTransitions()) {
			newTransition.add(new Transition_(t.getIniState(), map.get(t.getLabel()), t.getEndState()));
		}
		List<String> newAlphabet = new ArrayList<>();
		for (String a : automatonBrics.getAlphabet()) {
			newAlphabet.add(map.get(a));
		}
		automatonBrics.setAlphabet(newAlphabet);
		automatonBrics.setTransitions(newTransition);

		return automatonBrics;
	}

	// // state coverage, dijkstra {ab, n�o pega ax}
	// public static List<String> getWordsFromAutomaton(Automaton_ a, boolean ioco,
	// int nTestCases) {
	//
	// String tagSeparator = " -> ";
	// List<String> words = new ArrayList<>();
	//
	// // Map<String, Boolean> open_state = new HashMap<>();//state, open or not
	// Map<String, String> parent_state = new HashMap<>();// state-parent
	// Map<String, Integer> cost_state = new HashMap<>();// state-cost
	// Map<String, String> label = new HashMap<>();
	// Map<String, Integer> cost_state_rm = new HashMap<>();// state-cost
	//
	// // initialize cust of ini state as 0
	// cost_state.put(a.getInitialState().getName(), 0);
	// // open_state.add(a.getInitialState().getName());
	//
	// // initialize cost of state as infinity
	// for (State_ s : a.getStates()) {
	// if (!s.equals(a.getInitialState())) {
	// cost_state.put(s.getName(), Integer.MAX_VALUE);
	// }
	//
	// }
	//
	// cost_state_rm = cost_state.entrySet().stream()
	// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	//
	// int cont = 0;
	// // dijkstra
	// List<Transition_> transitions;
	// while (cost_state_rm.size() != 0) {
	// // verify if all final states was explorated, ****** se der bug tira isso
	// cont = 0;
	// for (State_ s : a.getFinalStates()) {
	// if (cost_state_rm.containsKey(s.getName())) {
	// cont++;
	// }
	// }
	// if (cont == 0) {
	// break;
	// }
	//
	// // lowest cost state
	// Entry<String, Integer> min = Collections.min(cost_state_rm.entrySet(),
	// Comparator.comparing(Entry::getValue));
	// // close state
	// // open_state.remove(min.getKey());
	// cost_state_rm.remove(min.getKey());
	//
	// // adjacent transitions of min
	// transitions = a.transitionsByIniState(new State_(min.getKey()));
	// for (Transition_ t : transitions) {
	// // +1 because the cost of all transitions is 1
	// if (min.getValue() + 1 < cost_state.get(t.getEndState().getName())) {
	// // update cust
	// cost_state_rm.put(t.getEndState().getName(), min.getValue() + 1);
	// cost_state.put(t.getEndState().getName(), min.getValue() + 1);
	// parent_state.put(t.getEndState().getName(), min.getKey());
	// label.put(t.getEndState().getName(), t.getLabel());
	// }
	// }
	// }
	//
	// // get words
	// String current = "";
	// String word = "";
	// String[] word_parts;
	//
	// for (State_ s : a.getFinalStates()) {
	//
	// current = s.getName();
	// if (current.equals(a.getInitialState().getName())) {
	// word = " ";
	// } else {
	// word = "";
	// }
	//
	// while (!current.equals(a.getInitialState().getName())) {
	// word += label.get(current) + tagSeparator;
	//
	// current = parent_state.get(current);
	//
	// if (current == null) {
	// break;
	// }
	//
	// }
	//
	// // invert word, to initi by initState
	// word_parts = word.split(tagSeparator);
	// word = "";
	// for (int i = word_parts.length - 1; i >= 0; i--) {
	// word += word_parts[i] + tagSeparator;
	// }
	//
	// // remove last tag
	// if (word.lastIndexOf(tagSeparator) == word.length() - tagSeparator.length())
	// {
	// word = word.substring(0, word.lastIndexOf(tagSeparator));
	// }
	//
	// if (ioco) { // remove last output if ioco
	// // System.err.println("word:"+word);
	// if (word.lastIndexOf(" -> ") != -1) {
	// word = word.substring(0, word.lastIndexOf(" -> "));
	// }
	// }
	//
	// if (!words.contains(word)) {
	// // System.out.println("word:"+word);
	// words.add(word);
	// }
	//
	// if (words.size() == nTestCases + (nTestCases < 15 ? (nTestCases * 2) :
	// (nTestCases / 4))) {// Constants.MAX_TEST_CASES
	// break;
	// }
	// }
	//
	// return words;
	// }

	// **************************************************************************************
	// transition coverage, dijkstra, menores caminhos {ab e bx}
	public static List<String> getWordsFromAutomaton(Automaton_ a, boolean ioco, int nTestCases) {

		String tagSeparator = " -> ";
		List<String> words = new ArrayList<>();

		// Map<String, Boolean> open_state = new HashMap<>();//state, open or not
		Map<String, List<String>> parent_state = new HashMap<>();// state-parent
		Map<String, Integer> cost_state = new HashMap<>();// state-cost
		Map<String, List<String>> label = new HashMap<>();
		Map<String, Integer> cost_state_rm = new HashMap<>();// state-cost

		List<String> labels;
		List<String> parents;

		// initialize cust of ini state as 0
		cost_state.put(a.getInitialState().getName(), 0);
		// open_state.add(a.getInitialState().getName());

		// initialize cost of state as infinity
		for (State_ s : a.getStates()) {
			if (!s.equals(a.getInitialState())) {
				cost_state.put(s.getName(), Integer.MAX_VALUE);
			}

		}

		cost_state_rm = cost_state.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		int cont = 0;
		// dijkstra
		List<Transition_> transitions;
		while (cost_state_rm.size() != 0) {
			// verify if all final states was explorated, ****** se der bug tira isso
			cont = 0;
			for (State_ s : a.getFinalStates()) {
				if (cost_state_rm.containsKey(s.getName())) {
					cont++;
				}
			}
			if (cont == 0) {
				break;
			}

			// lowest cost state
			Entry<String, Integer> min = Collections.min(cost_state_rm.entrySet(),
					Comparator.comparing(Entry::getValue));
			// close state
			// open_state.remove(min.getKey());
			cost_state_rm.remove(min.getKey());

			// adjacent transitions of min
			transitions = a.transitionsByIniState(new State_(min.getKey()));
			for (Transition_ t : transitions) {
				// +1 because the cost of all transitions is 1
				if (min.getValue() + 1 <= cost_state.get(t.getEndState().getName())) {
					// update cust
					cost_state_rm.put(t.getEndState().getName(), min.getValue() + 1);
					cost_state.put(t.getEndState().getName(), min.getValue() + 1);

					if (min.getValue() + 1 == cost_state.get(t.getEndState().getName())) {
						if (label.get(t.getEndState().getName()) != null) {
							labels = new ArrayList<String>(label.get(t.getEndState().getName()));
							labels.add(t.getLabel());
							parents = new ArrayList<String>(parent_state.get(t.getEndState().getName()));
							parents.add(min.getKey());
						} else {
							labels = Arrays.asList(new String[] { t.getLabel() });
							parents = Arrays.asList(new String[] { min.getKey() });
						}

					} else {
						labels = Arrays.asList(new String[] { t.getLabel() });
						parents = Arrays.asList(new String[] { min.getKey() });
					}

					parent_state.put(t.getEndState().getName(), parents);
					label.put(t.getEndState().getName(), labels);
				}
			}
		}

		// get words
		String current = "";

		// String word = "";
		String[] word_parts;
		MultiValueMap wordsMap = new MultiValueMap();
		MultiValueMap wordsMap_aux = new MultiValueMap();

		for (State_ s : a.getFinalStates()) {// final states
			current = s.getName();
			wordsMap = new MultiValueMap();
			wordsMap.put(current, "");
			wordsMap_aux = new MultiValueMap();

			end: do {

				for (Object key : wordsMap.keySet()) {
					if (key.equals(a.getInitialState().getName())) {

						break end;
					}

					current = Objects.toString(key);

					
					
					for (Object v : (Collection<String>) wordsMap.get(key)) {
						for (int i = 0; i < parent_state.get(current).size(); i++) {
							wordsMap_aux.put(parent_state.get(current).get(i),
									v + label.get(current).get(i) + tagSeparator);

						}

					}

				}

				wordsMap.remove(current);
				wordsMap.putAll(wordsMap_aux);
			} while (wordsMap.size() != 0);

			for (String word : (Collection<String>) wordsMap.get(a.getInitialState().getName())) {
				// invert word, to initi by initState
				word_parts = word.split(tagSeparator);
				word = "";
				for (int j = word_parts.length - 1; j >= 0; j--) {
					word += word_parts[j] + tagSeparator;
				}

				// remove last tag
				if (word.lastIndexOf(tagSeparator) == word.length() - tagSeparator.length()) {
					word = word.substring(0, word.lastIndexOf(tagSeparator));
				}

				if (ioco) { // remove last output if ioco
					// System.err.println("word:"+word);
					if (word.lastIndexOf(" -> ") != -1) {
						word = word.substring(0, word.lastIndexOf(" -> "));
					}
				}

				if (!words.contains(word)) {
					words.add(word);
				}

				if (words.size() == nTestCases + (nTestCases < 15 ? (nTestCases * 2) : (nTestCases / 4))) {// Constants.MAX_TEST_CASES
					break;
				}
			}

		}

		return words;

	}

	// **********************************************************************

	// //transition coverage
	// static volatile State_ current;
	//
	// /***
	// * Receive an automaton and return the words reached from each automato's
	// final
	// * state
	// *
	// * @param a
	// * @param ioco
	// * @return all words that reach the final states
	// */
	// public static List<String> getWordsFromAutomaton(Automaton_ a, boolean ioco,
	// int nTestCases) {// boolean
	// // transitionCoverSpec
	//
	// String word = "";
	// String tagWord = " , ";
	// String tagLetter = " -> ";
	// List<State_> stateToVisit = new ArrayList<State_>();
	// stateToVisit.add(a.getInitialState());
	// a.getStates().forEach(p -> p.setInfo(null));
	// a.getStates().forEach(p -> p.setVisited(false));
	// a.getStates().forEach(p -> p.setId(-1));
	// a.getTransitions().forEach(p -> p.getEndState().setId(-1));
	//
	// State_ endState, iniState;
	// String[] aux;
	// List<String> words = new ArrayList<String>();
	// List<String> news = new ArrayList<String>();
	// int id = 0, current_id = 0;
	//
	// while (!stateToVisit.isEmpty()) {
	// current_id = -1;
	// current = stateToVisit.remove(0);
	// current = a.getStates().stream().filter(x ->
	// x.equals(current)).findFirst().orElse(null);
	// // System.out.println(current);
	// if (!current.isVisited()) {
	// current.setVisited(true);
	// List<Transition_> transicoes = a.transitionsByIniState(current);
	//
	// // //ini: order transitions (endState)
	// if (current.getId() == -1) {
	// current.setId(id);
	// current_id = id;
	// id++;
	// }
	//
	// for (Transition_ t : transicoes) {
	// endState = t.getEndState();
	// if (endState.getId() == -1) {
	// endState.setId(id);
	// id++;
	// }
	// if (current_id != -1 && endState == current) {
	// endState.setId(current_id);
	// }
	// }
	//
	// Comparator<Transition_> compareById = (Transition_ o1, Transition_ o2) ->
	// Integer
	// .compare(o1.getEndState().getId(), o2.getEndState().getId());
	// Collections.sort(transicoes, compareById);
	// // //end: order transitions
	//
	// for (Transition_ t : transicoes) {
	// news = new ArrayList<String>();
	// iniState = a.getStates().stream().filter(x ->
	// x.equals(t.getIniState())).findFirst().orElse(null);
	// endState = a.getStates().stream().filter(x ->
	// x.equals(t.getEndState())).findFirst().orElse(null);
	//
	// word = "";
	//
	// if (iniState.getInfo() != null) {
	// aux = iniState.getInfo().split(tagWord);
	// if (aux.length > 0) {
	// for (int i = 0; i < aux.length; i++) {
	// word = aux[i] + tagLetter + t.getLabel() + tagWord;
	// news.add(word);
	// }
	//
	// if (iniState.equals(a.getInitialState())) {
	// news.add(t.getLabel() + tagWord);
	// }
	// } else {
	// word = iniState.getInfo() + tagLetter + t.getLabel() + tagWord;
	// news.add(word);
	// }
	//
	// } else {
	// word = t.getLabel() + tagWord;
	// news.add(word);
	// }
	//
	// for (String p : news) {
	// word = (endState.getInfo() != null ? (endState.getInfo()) : "") + p;
	// endState.setInfo(word);
	// }
	//
	// if (!endState.isVisited()) {
	// stateToVisit.add(endState);
	// }
	//
	// }
	// }
	// }
	//
	// word = "";
	// end: for (State_ e : a.getStates()) {
	// if (a.getFinalStates().contains(e)) {
	// if (e.getInfo() != null) {
	// aux = e.getInfo().split(tagWord);
	// // if (transitionCoverSpec) {
	// for (int i = 0; i < aux.length; i++) {
	// word = aux[i];
	// if (ioco) { // remove last output if ioco
	// // System.err.println("word:"+word);
	// if (word.lastIndexOf(" -> ") != -1) {
	// word = word.substring(0, word.lastIndexOf(" -> "));
	// }
	// }
	// words.add(word);
	// if (words.size() == nTestCases + (nTestCases < 15 ? (nTestCases * 2) :
	// (nTestCases / 4))) {// Constants.MAX_TEST_CASES
	// break end;
	// }
	// }
	// // } else {
	// // // catch smaller word
	// // Arrays.sort(aux, new java.util.Comparator<String>() {
	// // @Override
	// // public int compare(String s1, String s2) {
	// // // TODO: Argument validation (nullity, length)
	// // return s1.length() - s2.length();// comparision
	// // }
	// // });
	// //
	// // word = aux[0];
	// // words.add(word);
	// // }
	//
	// }
	// }
	// }
	//
	// return words;
	// }

	/***
	 * Checks the conformance of the automaton and when it does not conform returns
	 * a word that shows the nonconformity
	 * 
	 * @param a
	 *            automaton
	 * @return whether it conforms or not and the word that proves the
	 *         non-conformity
	 */
	public static String veredict(Automaton_ a) {
		if (a.getFinalStates().size() > 0) {
			return Constants.MSG_NOT_CONFORM;
		} else {
			return Constants.MSG_CONFORM;
		}
	}

	// return the state paths get a test case
	public static List<List<State_>> statePath(IOLTS S, String tc) {
		List<List<State_>> state_path = new ArrayList<>();
		state_path.add(Arrays.asList(S.getInitialState()));
		List<State_> aux = new ArrayList<>();
		List<List<State_>> state_path_aux = new ArrayList<>();

		List<Transition_> transitions_s = new ArrayList<>();
		// words of test case
		endWalkPath: for (String word : tc.split(" -> ")) {
			aux = new ArrayList<>();
			state_path_aux = new ArrayList<>();

			// for each state reached by the word
			for (List<State_> states : state_path) {
				// get transitions reached
				transitions_s = states.get(states.size() - 1).getTransitions().stream()
						.filter(x -> x.getLabel().equals(word)).collect(Collectors.toList());

				// if none transition reached, in case of conformance based on language, break
				if (transitions_s.size() == 0) {
					aux = new ArrayList<>(states);
					aux.add(new State_("[" + Constants.NO_TRANSITION + states.get(states.size() - 1).getName()
							+ " with label " + word + " ]"));
					state_path_aux.add(aux);
					state_path = state_path_aux;
					break endWalkPath;
				} else {// get all states reached by word
					for (Transition_ t : transitions_s) {
						aux = new ArrayList<>(states);
						aux.add(t.getEndState());
						state_path_aux.add(aux);
					}
				}

			}
			state_path = state_path_aux;

		}

		// return list of state paths
		return state_path;
	}

	public static String path(IOLTS S, IOLTS I, boolean ioco, List<String> testSuite, int nTestCases) {
		int contTestCase = 0;
		String path_s = "";
		String path_i = "";
		List<String> out_s;
		List<String> out_i;
		String path = "";
		List<String> out_s_aux;
		List<String> out_i_aux;


		// tc, state path and outputs
		// for each test case
		for (String tc : testSuite) {
			out_s = new ArrayList<>();
			out_i = new ArrayList<>();
			path_s = "";
			path_i = "";

			// get state paths from test case (specification)
			for (List<State_> s : statePath(S, tc)) {
				path_s += "\n\t path:" + StringUtils.join(s, " -> ");
				// if ioco show outputs from last state
				if (ioco) {
					out_s_aux = S.outputsOfState(s.get(s.size() - 1));
					path_s += "\n\t output: " + out_s_aux + "\n";
					out_s.addAll(out_s_aux);
				}
			}
			// get state paths from test case (implementation)
			for (List<State_> i : statePath(I, tc)) {
				path_i += "\n\t path:" + StringUtils.join(i, " -> ");
				// if ioco show outputs from last state
				if (ioco) {
					out_i_aux = I.outputsOfState(i.get(i.size() - 1));
					path_i += "\n\t output: " + out_i_aux + "\n";
					out_i.addAll(out_i_aux);
				}
			}

			if (!ioco) {// if not ioco dont show output
				contTestCase++;
				path += "Test case: \t" + tc + ""; // + "\n"
				path += "\n\nModel: \n" + path_s;
				path += "\n\nImplementation: \n " + path_i;
				path += "\n\n################################################################## \n";
			} else {
				out_s = new ArrayList<>(new HashSet<>(out_s));
				out_i = new ArrayList<>(new HashSet<>(out_i));
				// verify output of implementation is specified by specification
				if (!out_s.containsAll(out_i) && !(ioco && out_s.size() == 0)) {
					contTestCase++;
					path += "Test case: \t" + tc + ""; // + "\n"
					path += "\n\nModel outputs: " + out_s + " \n" + path_s;
					path += "\n\nImplementation outputs: " + out_i + " \n " + path_i;
					path += "\n\n################################################################## \n";

					if (contTestCase >= nTestCases) {// Constants.MAX_TEST_CASES_REAL //*ok
						break;
					}
				}

			}

		}

		return path;
	}

	/***
	 * Path on conformance based on language
	 * 
	 * @param S
	 *            specification model
	 * @param I
	 *            implementation model
	 * @param fault
	 *            model containing words that detect implementation failure
	 * @return the paths covered by the test cases by implementation and
	 *         specification
	 */
	public static String path(LTS S, LTS I, Automaton_ faultModel, boolean ioco, boolean transitionCoverSpec,
			int nTestCases) {
		IOLTS iolts_s = new IOLTS(S);
		IOLTS iolts_i = new IOLTS(I);

		iolts_s.setInputs(iolts_s.getAlphabet());
		iolts_s.setOutputs(new ArrayList<String>());
		iolts_i.setInputs(iolts_i.getAlphabet());
		iolts_i.setOutputs(new ArrayList<String>());

		List<String> testCases;

		testCases = getWordsFromAutomaton(faultModel, ioco, nTestCases);// transitionCoverSpec

		return path(iolts_s, iolts_i, ioco, testCases, nTestCases);
	}

	/***
	 * 
	 * @param S
	 *            specification model
	 * @param I
	 *            implementation model
	 * @param faultModel
	 *            model containing words that detect implementation failure
	 * @return the paths covered by the test cases by implementation and
	 *         specification
	 */
	public static String path(IOLTS S, IOLTS I, Automaton_ faultModel, boolean ioco, boolean transitionCoverSpec,
			int nTestCases) {
		int contTestCase = 0;
		List<String> testCases;
		String path = "";
		
		// INI: inserts transitions into states to improve the processing of the
				// getWordsFromAutomaton
				// verify if model has changed in the interface
				if (S.getInitialState().getTransitions().size() == 0) {
					if (S.getStates().stream().findAny().orElse(null).getTransitions().size() == 0) {
						for (Transition_ t : S.getTransitions()) {
							S.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null)
									.addTransition(t);
							t.setIniState(
									S.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null));
							t.setEndState(
									S.getStates().stream().filter(x -> x.equals(t.getEndState())).findFirst().orElse(null));
						}
					}
					S.setInitialState(
							S.getStates().stream().filter(x -> x.equals(S.getInitialState())).findFirst().orElse(null));
				}
				// verify if model has changed in the interface
				if (I.getInitialState().getTransitions().size() == 0) {
					if (I.getStates().stream().findAny().orElse(null).getTransitions().size() == 0) {
						for (Transition_ t : I.getTransitions()) {
							I.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null)
									.addTransition(t);
							t.setIniState(
									I.getStates().stream().filter(x -> x.equals(t.getIniState())).findFirst().orElse(null));
							t.setEndState(
									I.getStates().stream().filter(x -> x.equals(t.getEndState())).findFirst().orElse(null));
						}
					}
					I.setInitialState(
							I.getStates().stream().filter(x -> x.equals(I.getInitialState())).findFirst().orElse(null));
				}
				// END: inserts transitions into states to improve the processing of the
				// getWordsFromAutomaton
		
		if (ioco) {
			testCases = getWordsFromAutomaton(faultModel, ioco, nTestCases);
			
//			List<State_> states = new ArrayList<>();
//			for (String tc : testCases) {
//				for (List<State_> state : statePath(I, tc)) {
//					states.add(state.get(state.size() - 1));
//				}
//			}			
//			Automaton_ a = I.ioltsToAutomaton();					
//			a.setFinalStates(new ArrayList<>(new HashSet<>(states)));		
//			testCases = getWordsFromAutomaton(a, false, nTestCases);

			State_ currentState_s;
			State_ currentState_i;

			List<String> implOut, specOut;

			// verify if initial states results in non ioco, with test case ''
			currentState_s = S.getInitialState();
			currentState_i = I.getInitialState();
			implOut = I.outputsOfState(currentState_i);
			specOut = S.outputsOfState(currentState_s);

			// verify if initial state is not conform
			if (!specOut.containsAll(implOut) && !(ioco && specOut.size() == 0)) {// (ioco && specOut.size() == 0) ->
																					// when the test case can not be run
																					// completely
				contTestCase++;
				path = "Test case: \t" + ""; // + "\n"
				path += "\n\nModel outputs: " + specOut + " \n\n\t path:" + currentState_s.getName() + "\n\t output: "
						+ S.outputsOfState(currentState_s);
				path += "\n\nImplementation outputs: " + implOut + " \n\n\t path: " + currentState_i.getName()
						+ "\n\t output: " + I.outputsOfState(currentState_i);
				path += "\n\n################################################################## \n";

			}

		} else {
			testCases = getWordsFromAutomaton(faultModel, ioco, nTestCases);// transitionCoverSpec
		}

		return path + path(S, I, ioco, testCases, nTestCases);
		// return path;
	}

	/***
	 * Converts the Automaton object generated by the brics library in the Automato
	 * object of this project
	 * 
	 * @param a
	 *            brics library automaton
	 * @param tag
	 *            string that will serve to name the states, to distinguish the
	 *            states of D and F because the library creates states named "1",
	 *            "2", "3" and therefore there may be conflict going forward
	 * @return Automaton built according to parameter Automaton
	 */
	public static Automaton_ automatonBricsInAutomaton_(Automaton a, String tag) {
		// automaton of return
		Automaton_ automaton = new Automaton_();

		// states of brics Automaton
		Set<State> states = a.getStates();

		for (State state : states) {
			// ini state
			State_ iniState = getBricsState(state, tag);
			// add state to automaton_
			automaton.addState(iniState);
			// get all transitions from state
			Set<Transition> transitions = state.getTransitions();
			for (Transition t : transitions) {
				// add transition to automaton_
				for (Transition_ transition : getBricsTransition(iniState, t, tag)) {
					automaton.addTransition(transition);
				}
			}
		}

		// final states
		for (State finalState : a.getAcceptStates()) {
			automaton.addFinalStates(getBricsState(finalState, tag));
		}

		// define the initial state
		automaton.setInitialState(getBricsState(a.getInitialState(), tag));

		return automaton;

	}

	/***
	 * Retrieves Automaton transitions based on the iniState, the brics library does
	 * not allow direct access to the transition label so the value passed through
	 * the toString
	 * 
	 * @param iniState
	 * @param transition
	 * @param tag
	 *            used to name the state
	 * @return transitions list of iniState
	 */
	public static List<Transition_> getBricsTransition(State_ iniState, Transition transition, String tag) {
		String string = transition.toString();
		String[] parts = string.split("->");
		String[] transicoes = parts[0].split("-");
		List<Transition_> listaTransicoes = new ArrayList<Transition_>();
		for (String t : transicoes) {
			listaTransicoes.add(new Transition_(iniState, t.replaceAll("\\s", ""),
					new State_(tag + parts[1].replaceAll("\\s+", ""))));
		}

		return listaTransicoes;
	}

	public static State_ getStateByName(IOLTS S, String stateName) {
		return S.getStates().stream().filter(x -> x.getName().equals(stateName)).findFirst().orElse(new State_());
	}

	/***
	 * Retrieves the state given the parameter State, because the brics library does
	 * not allow to directly access the label of the state, then used here the value
	 * passed by the toString
	 * 
	 * @param state
	 * @param tag
	 *            used to name the state
	 * @return state
	 */
	public static State_ getBricsState(State state, String tag) {
		String stringState = state.toString();
		int idxInitial = stringState.indexOf("state") + "state".length();
		int idxFinal = stringState.indexOf('[');
		stringState = stringState.substring(idxInitial, idxFinal).replaceAll("\\s+", "");
		return new State_(tag + stringState);
	}

}
