/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: extension of FlatModel2Cpp for the Observable capsule.
 * TODO: adding support for hierarchical state machines
 * TODO: adding support for junction points
 * TODO: adding support for entry / exit actions
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.umlrt.codegenerator;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.papyrusrt.codegen.cpp.CppCodePattern;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.ActionDeclarationGenerator;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.FlatModel2Cpp;
import org.eclipse.papyrusrt.codegen.lang.cpp.element.MemberFunction;
import org.eclipse.papyrusrt.codegen.lang.cpp.stmt.UserCode;
import org.eclipse.papyrusrt.xtumlrt.common.AbstractAction;
import org.eclipse.papyrusrt.xtumlrt.common.Capsule;
import org.eclipse.papyrusrt.xtumlrt.common.Port;
import org.eclipse.papyrusrt.xtumlrt.statemach.StateMachine;
import org.eclipse.papyrusrt.xtumlrt.statemach.Transition;
import org.eclipse.papyrusrt.xtumlrt.statemach.Vertex;
import org.eclipse.papyrusrt.xtumlrt.trans.from.uml.UML2xtumlrtModelTranslator;
import org.eclipse.papyrusrt.xtumlrt.trans.from.uml.UML2xtumlrtSMTranslator;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.Class;

public class ObservableCapsuleFlatModel2Cpp extends FlatModel2Cpp {

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

	
	public ObservableCapsuleFlatModel2Cpp(StateMachine stateMachine, Capsule capsuleContext, CppCodePattern cpp) {
		super();
		this.capsuleContext = capsuleContext;
		this.stateMachine = stateMachine;
		this.cpp = cpp;
		
		this.observerPortName = "observation";
		
        EList<Port> ports = this.capsuleContext.getPorts();
        for (int i = 0; i < ports.size(); i++) {
        	Port port = ports.get(i);
        	if (port.getType().getName().equalsIgnoreCase("Observation") && !port.isConjugate()) {
        		this.observerPortName = port.getName();
        		break;
        	}
        }
        
	}
	
	@Override
	protected void generateActionFunc(final AbstractAction a, final Transition t) {
/*		System.out.println("----");
		System.out.println("capsule:" + this.capsuleContext + " \t transition: " + t.getName());
		System.out.println(a);
		System.out.println("----");
		System.out.println("\n"); */
		super.generateActionFunc(a, t);
	}
	
	@Override
	protected MemberFunction generateActionChainFunc(Transition t) {

		MemberFunction func = super.generateActionChainFunc(t);

		trans = ((UML2xtumlrtModelTranslator)this.cpp.getTranslator()).getStateMachineTranslator();
		
		ArrayList<String> definitions = new ArrayList<String>();
		ArrayList<String> undefinitions = new ArrayList<String>();
		ArrayList<String> params = new ArrayList<String>();
		int i =0;
		org.eclipse.uml2.uml.Transition initT = (org.eclipse.uml2.uml.Transition)trans.getSource(t);
		if (initT.getTriggers().size() > 0) {
			CallEvent event = (CallEvent) initT.getTriggers().get(0).getEvent();
			EList<Parameter> parameters = event.getOperation().getOwnedParameters();
			for (Parameter parameter : parameters) {
				Type type = parameter.getType();
				if (type instanceof PrimitiveType) {
					String primitiveType = "int";
					params.add(parameter.getName());
					switch (type.getName()) {
					case "Boolean":
						primitiveType = "bool";
						break;
					case "Integer":
						primitiveType = "int";
						break;
					case "String":
						primitiveType = "char";
						break;
						default:
						System.err.println("problem with type: " + type.getName());
					}
					definitions.add("#define " + parameter.getName() + " ( *(" + primitiveType + " *)msg->getParam( "+i+" ) )");
					undefinitions.add("#undef " + parameter.getName());
				}
				else if (type instanceof Class) {
					Class complexParameter = (Class) type;
					definitions.add("#define " + parameter.getName() + " ( *(const " + type.getName() + " *)msg->getParam( "+i+" ) )");
					undefinitions.add("#undef " + parameter.getName());
					for (Property attribute : complexParameter.getOwnedAttributes()) {
						if (attribute.getType() instanceof PrimitiveType) {
						params.add(parameter.getName()+"."+attribute.getName());
						}
					}
				}
				i++;
			}
		}
		
		Vertex sourceState = t.getSourceVertex();
		Vertex targetState = t.getTargetVertex();
		
		for (String definition: definitions)
			func.add(new UserCode (definition));
		func.add(new UserCode(
				"std::stringstream ss;"
			+	"\n"
			+	"ss << this->slot->containerClass->name;"
			+	"\n"
			+	"ss << \".\";"
			+	"\n"
			+	"ss << this->getName();"
			+	"\n"
			+	"ss << \":\";"
			+	"\n"
			+	"ss << this->getIndex();"
			+	"\n"
		));
		
		
		func.add(new UserCode(
				"EventObj eobj1;\n" +
				"Event e1;\n" +
				"e1.setSourceName(\""+sourceState.getName()+"\");\n" +
				"e1.setCapsuleInstance(ss.str().c_str());\n" +
				"e1.setTimestamp();\n" +
				"e1.setEventSource(Event::State);\n" +
				"e1.setEventKind(Event::StateExitEnd);\n" +
				"e1.setParam(\"capsuleName\", \""+this.capsuleContext.getName()+"\");\n"
		));
		
		for(String param : params) {
			func.add(new UserCode("e1.setParam(\""+param+"\", "+param+");\n"));
		}
		
		func.add(new UserCode(
				"eobj1.event = e1;\n\n"		
		));
		
		func.add(new UserCode(
				this.observerPortName + ".event(eobj1).send();\n\n"			
		));
		
		func.add(new UserCode(
				"EventObj eobj2;\n" +
				"Event e2;\n" +
				"e2.setSourceName(\""+targetState.getName()+"\");\n" +
				"e2.setCapsuleInstance(ss.str().c_str());\n" +
				"e2.setTimestamp();\n" +
				"e2.setEventSource(Event::State);\n" +
				"e2.setEventKind(Event::StateEntryStart);\n" +
				"e2.setParam(\"capsuleName\", \""+this.capsuleContext.getName()+"\");\n"			
		));
		
		for(String param : params) {
			func.add(new UserCode("e2.setParam(\""+param+"\", "+param+");\n"));
		}
		
		func.add(new UserCode(
				"eobj2.event = e2;\n\n"		
		));
		
		func.add(new UserCode(
				this.observerPortName + ".event(eobj2).send();\n\n"			
		));
		for (String undefinition: undefinitions)
			func.add(new UserCode (undefinition));
		
		return func;
	}
	
}