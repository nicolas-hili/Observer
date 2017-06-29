/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: extension of FlatModel2Cpp for the Observer capsule.
 * TODO: adding support for hierarchical state machines
 * TODO: adding support for junction points
 * TODO: adding support for entry / exit actions
 * 
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/


package ca.queensu.cs.observer.umlrt.codegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.papyrusrt.codegen.cpp.CppCodePattern;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.ActionDeclarationGenerator;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.FlatModel2Cpp;
import org.eclipse.papyrusrt.codegen.lang.cpp.element.MemberFunction;
import org.eclipse.papyrusrt.codegen.lang.cpp.stmt.UserCode;
import org.eclipse.papyrusrt.xtumlrt.common.ActionCode;
import org.eclipse.papyrusrt.xtumlrt.common.Capsule;
import org.eclipse.papyrusrt.xtumlrt.common.Port;
import org.eclipse.papyrusrt.xtumlrt.common.Protocol;
import org.eclipse.papyrusrt.xtumlrt.common.ProtocolBehaviourFeature;
import org.eclipse.papyrusrt.xtumlrt.common.ProtocolBehaviourFeatureKind;
import org.eclipse.papyrusrt.xtumlrt.common.Signal;
import org.eclipse.papyrusrt.xtumlrt.statemach.StateMachine;
import org.eclipse.papyrusrt.xtumlrt.statemach.Transition;
import org.eclipse.papyrusrt.xtumlrt.trans.from.uml.UML2xtumlrtSMTranslator;
import org.eclipse.uml2.uml.Operation;

public class ObserverCapsuleFlatModel2Cpp extends FlatModel2Cpp {

	@SuppressWarnings("unused")
	private StateMachine stateMachine;
	
	private Capsule capsuleContext;
	private CppCodePattern cpp;
	private String observerPortName;
	private UML2xtumlrtSMTranslator trans;
	
	/**
	 * Objects used for the generation
	 */
	private final ActionDeclarationGenerator actionDeclarationGenerator = new ActionDeclarationGenerator();

	private Protocol protocol;

	
	public ObserverCapsuleFlatModel2Cpp(StateMachine stateMachine, Capsule capsuleContext, CppCodePattern cpp) {
		super();
		this.capsuleContext = capsuleContext;
		this.stateMachine = stateMachine;
		this.cpp = cpp;
		
		this.observerPortName = "observation";
		
        EList<Port> ports = this.capsuleContext.getPorts();
        for (int i = 0; i < ports.size(); i++) {
        	Port port = ports.get(i);
        	if (port.getType().getName().equalsIgnoreCase("Observation") && port.isConjugate()) {
        		this.observerPortName = port.getName();
        		this.protocol = port.getType();
        		break;
        	}
        }
	}
	
	@Override
	protected void _generateActionFunc(ActionCode a, Transition t) {
		super._generateActionFunc(a, t);
	}
	
	@Override
	protected MemberFunction generateActionChainFunc(Transition t) {

		MemberFunction func = super.generateActionChainFunc(t);
		String name = t.getName();
		if (name.equals("command received")) {
			generateReadFunc(t, func);		
		} else if (name.equals("event received")) {
			generateWriteFunc(t, func);
		} else if (name.equals("register capsules")) {
			generateRegisterFunc(t, func);
		}
		return func;
	}
	
	protected MemberFunction generateReadFunc(Transition t, MemberFunction func) {
		
		ArrayList<Signal> signals = new ArrayList<Signal>();
		
		for (ProtocolBehaviourFeature signal: protocol.getProtocolBehaviourFeatures()) {
			
			if (signal.getKind().equals(ProtocolBehaviourFeatureKind.IN)) {				
				signals.add((Signal)signal);
			}
		}
		

		Map<String, List<String>> capsules = new HashMap<String, List<String>>();
		Map<String, String> mapping = new HashMap<String, String>();
		
		for (Signal signal: signals) {
			
			Operation operation = (Operation) this.cpp.getTranslator().getSource(signal);
			EAnnotation observerEAnnotation = operation.getEAnnotation("observer");
			
			if (observerEAnnotation == null)
				continue;
			
			EMap<String, String> map = observerEAnnotation.getDetails();
			
			String name = map.get("name");
			String capsuleName = map.get("capsule");
			
			if (name == null || capsuleName == null)
				continue;
			
			mapping.put(name, signal.getName());
			
			if (capsules.get(capsuleName) == null) {
				capsules.put(capsuleName, new ArrayList<String>());
			}
			
			capsules.get(capsuleName).add(name);
			
		}
		
		// Initialization
		func.add(new UserCode("/* Initialization: */\n"
				+ "\n\n"
				+ "std::string data = CLIUtils::trim(this->method->read());"
				+ "\n"
				+ "std::stringstream ss;"
				+ "\n\n"
				+ "if (data == \"\") return;"
				+ "\n"
				+ "std::vector<std::string> cmd = CLIUtils::tokenizeCommand(data);"
				+ "\n"
				+ "if (cmd.size() == 0) return;"
				+ "\n"
		));
		
		// List capsules
		func.add(new UserCode("/* List all observable capsules: */\n"
				+ "if (cmd.size() > 1 && cmd[0] == \"list\" && cmd[1] == \"capsules\") {"
				+ "\n\t\t"
				+ "ss << \"List of capsules\\n\";"
				+ "\n\t"
				+ "std::map<std::string, size_t>::iterator iter;"
				+ "\n\t"
				+ "for(iter = capsules.begin(); iter != capsules.end(); iter++) {"
				+ "\n\t\t"
				+ "ss << \" - \" << iter->first << \" [\"<< capsuleTypes[iter->first]<< \"]\\n\";"
				+ "\n\t"
				+ "}"
				+ "\n\t"
				+ "this->method->sendData(ss.str());"
				+ "\n"
				+ "}"
				+ "\n"
		));
		
		// Show capsule
		func.add(new UserCode("/* Show a specific capsule: */\n"
				+ "if (cmd.size() > 2 && cmd[0] == \"show\" && cmd[1] == \"capsule\") {"
				+ "\n\t"
				+ "std::string capsuleType = capsuleTypes[cmd[2]];"
				+ "\n\t"
				+ "if (capsuleType == \"\") {"
				+ "\n\t"
				+ "printf(\"error: %s\\n\", capsuleType.c_str());"
				+ "\n\t"
				+ "return;"
				+ "\n\t"
				+ "}"
				+ "\n\t"
	            + "ss << \"Capsule: \" << cmd[2];"
	            + "\n\t"
	            + "ss  << \" type: \" << capsuleTypes[cmd[2]] << \"\\n\";"
	            + "\n\t"
	            + "ss  << \"List of triggers:\\n\";"
	    ));
		for (String capsuleName: capsules.keySet()) {
			func.add(new UserCode (""
					+ "if (capsuleType == \""+capsuleName+"\") {"
				));
			for (String name: capsules.get(capsuleName)) {
				func.add(new UserCode (""
			            + "ss << \" - "+name+"\\n\";"
			            + "\n\t"
					));
			}
			func.add(new UserCode ("}"));
		}
		func.add(new UserCode (""
				+ "this->method->sendData(ss.str());"
				+ "\n\t"
				+ "}"
				+ "\n"
				));

		
		// Trigger capsule signal
		func.add(new UserCode("/* Trigger a capsule signal: */\n"
				+ "if (cmd.size() > 4 && cmd[0] == \"send\") {"
				+ "\n\t"
				+ "std::string capsule = cmd[4];"
				+ "\n\t"
				+ "std::string capsuleType = capsuleTypes[capsule];"
				+ "\n\t"
				+ "std::string trigger = cmd[1];"
		));
		
		for (String capsuleName: capsules.keySet()) {
			func.add(new UserCode (""
					+ "if (capsuleType == \""+capsuleName+"\") {"
				));
			for (String name: capsules.get(capsuleName)) {
				func.add(new UserCode (""
			            + "if (trigger == \""+name+"\") {"
			            + "\n\t"
			            +  "size_t index = capsules[capsule];"
			            + "\n\t"
			            + "observation."+mapping.get(name)+"().sendAt(index);"
			            + "\n"
			            + "}"
					));
			}
			func.add(new UserCode ("}"));
		}
		
		func.add(new UserCode("}"));
		
				
		return func;
	}
	
	protected MemberFunction generateWriteFunc(Transition t, MemberFunction func) {
		return func;
	}
	
	protected MemberFunction generateRegisterFunc(Transition t, MemberFunction func) {
		return func;
	}
	
}