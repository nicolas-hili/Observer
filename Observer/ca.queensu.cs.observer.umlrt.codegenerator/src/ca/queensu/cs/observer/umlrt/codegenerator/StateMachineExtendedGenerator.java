/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: This code registers an extension to the Papyrus-RT 
 * state machine generator.
 * 
 * It checks if the Observer capsule is loaded. If so, it calls
 * ExtendedFlatModel2cpp instead of FlatModel2Cpp.
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.umlrt.codegenerator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.papyrusrt.codegen.cpp.AbstractBehaviourGenerator;
import org.eclipse.papyrusrt.codegen.cpp.AbstractElementGenerator;
import org.eclipse.papyrusrt.codegen.cpp.CppCodePattern;
import org.eclipse.papyrusrt.codegen.cpp.CppCodePattern.Output;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.FlatModel2Cpp;
import org.eclipse.papyrusrt.codegen.cpp.statemachines.flat.FlatModel2Cpp.CppGenerationTransformationContext;
import org.eclipse.papyrusrt.codegen.lang.cpp.element.CppClass;
import org.eclipse.papyrusrt.codegen.lang.cpp.element.MemberFunction;
import org.eclipse.papyrusrt.codegen.statemachines.transformations.FlatteningTransformer;
import org.eclipse.papyrusrt.codegen.statemachines.transformations.FlatteningTransformer.FlatteningTransformationContext;
import org.eclipse.papyrusrt.codegen.statemachines.transformations.TransformationResult;
import org.eclipse.papyrusrt.xtumlrt.common.Capsule;
import org.eclipse.papyrusrt.xtumlrt.common.Port;
import org.eclipse.papyrusrt.xtumlrt.statemach.StateMachine;
import org.eclipse.papyrusrt.xtumlrt.trans.from.uml.UML2xtumlrtModelTranslator;


public class StateMachineExtendedGenerator extends AbstractBehaviourGenerator<StateMachine, Capsule> {

	private final StateMachine stateMachine;
    private final Capsule capsuleContext;
    private boolean isObservable;
    private boolean isObserver;
    
    public StateMachineExtendedGenerator( CppCodePattern cpp, StateMachine stateMachine, Capsule capsuleContext ) {
    	super(cpp, stateMachine, capsuleContext);
        this.stateMachine = stateMachine;
        this.capsuleContext = capsuleContext;
        this.isObservable = false;
        this.isObserver = false;
        
        EList<Port> ports = this.capsuleContext.getPorts();
        for (int i = 0; i < ports.size(); i++) {
        	Port port = ports.get(i);
        	
        	if (port.getType().getName().equals("Observation") && !port.isConjugate()) {
        		this.isObservable = true;
        		break;
        	}
        	else if (port.getType().getName().equals("Observation") && port.isConjugate()) {
        		this.isObserver = true;
        		break;
        	}
        }
        
	}
    
    public static class Factory implements AbstractElementGenerator.Factory<StateMachine, Capsule>
    {
        @Override
        public AbstractElementGenerator create( CppCodePattern cpp, StateMachine stateMachine, Capsule capsuleContext )
        {
            return new StateMachineExtendedGenerator( cpp, stateMachine, capsuleContext );
        }
    }
    
    @Override
    public boolean generate() {
    	
    	// If the capsule is observable, call the extended generator
    	FlatModel2Cpp generator = (this.isObservable) ? 
    			new ObservableCapsuleFlatModel2Cpp(this.stateMachine, this.capsuleContext, this.cpp) :
    			(this.isObserver) ? new ObserverCapsuleFlatModel2Cpp(stateMachine, capsuleContext, cpp): new FlatModel2Cpp();
    	
    	FlatteningTransformer flattener = new FlatteningTransformer();
    	
    	UML2xtumlrtModelTranslator trans = (UML2xtumlrtModelTranslator)this.cpp.getTranslator();
        FlatteningTransformationContext ctx1 = new FlatteningTransformer.FlatteningTransformationContext(flattener, trans.getStateMachineTranslator());
 
        
        // First we flatten the state machine (inheritance and nesting)
        TransformationResult flatteningResult = flattener.transform( stateMachine, ctx1 );
        
        if (flatteningResult == null || flatteningResult.isSuccess() == false)
            return false;
        
        // Then we transform to C++
        // FlatModel2Cpp uses the discarded state list to construct the state enum
        CppGenerationTransformationContext ctx2 = new FlatModel2Cpp.CppGenerationTransformationContext(this.cpp, this.capsuleContext, flattener, flatteningResult.getDiscardedStates());
        return generator.transformInPlace( flatteningResult.getStateMachine(), ctx2 );   	        		
    }
    
    @Override
    protected Output getOutputKind() { return Output.BasicClass; }

    @Override
    protected void generateInitializeBody( CppClass cls, MemberFunction initializeFunc, StateMachine stateMachine, Capsule capsule )
    {
        // Do nothing. The initialize function is created by the FlatteningCppTransformer
    }

    @Override
    protected void generateInjectBody( CppClass cls, MemberFunction injectFunc, StateMachine stateMachine, Capsule capsule )
    {
        // Do nothing. The inject function is created by the FlatteningCppTransformer
    }

    @Override
    protected void generateAdditionalElements( CppClass cls, StateMachine behaviourElement, Capsule contextU )
    {
        // Do nothing. The supporting functions are created by the FlatteningCppTransformer
    }


}
