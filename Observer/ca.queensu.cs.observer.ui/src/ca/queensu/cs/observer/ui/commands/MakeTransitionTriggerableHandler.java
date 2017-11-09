/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code is an handler to allow the observer to trigger 
 * a specific transition.
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.emf.facet.util.ui.internal.exported.handler.HandlerUtils;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.tools.util.PlatformHelper;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLFactory;

public class MakeTransitionTriggerableHandler extends ObserveHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Object element = HandlerUtils.getSelection();
		
		Element eobj = PlatformHelper.getAdapter(element, Element.class);
		
		if (eobj == null || !(eobj instanceof Transition))
			return null;
		
		Transition transition = (Transition)eobj;
		try {
			TransactionalEditingDomain editingDomain = getEditingDomain(transition);
			makeTransitionTriggerable(transition, editingDomain);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

	private void makeTransitionTriggerable(Transition transition, TransactionalEditingDomain editingDomain) {
		
		Class capsule = ((Class)transition.getOwner().getOwner().getOwner());
		Collaboration protocol = getObserverProtocol(transition.getModel());
		EList<Trigger> triggers = transition.getTriggers();
		Trigger initialTrigger = triggers.get(0);
		Port port = initialTrigger.getPorts().get(0);
		Port observerPort = capsule.getOwnedPort("observation", protocol);
		
		CallEvent event = (CallEvent)initialTrigger.getEvent();
		String messageName = event.getOperation().getName();
		String portName = port.getName();
		String capsuleName = ((NamedElement)transition.getOwner().getOwner().getOwner()).getName();
		String proposedName = capsuleName + "_" + portName + "_" + messageName;
		
		Package observerPackage = protocol.getPackage();
		

		Interface observerInterface = protocol.getAllImplementedInterfaces().get(0);
		
		if (observerInterface == null)
			return;
		
		CallEvent existingCallEvent = null;
		Operation existingOperation = null;
		
		for (Operation operation: observerInterface.getOwnedOperations()) {
			if (operation.getName().equals(proposedName)) {
				existingOperation = operation;
				break;
			}
		}
		
		for (PackageableElement pe: observerPackage.getPackagedElements()) {
			if (pe instanceof CallEvent && proposedName.equals(pe.getName())) {
				existingCallEvent = (CallEvent)pe;
				break;
			}
		}
		
		final CallEvent callEvent = existingCallEvent;
		final Operation op = existingOperation;
		
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			
			@Override
			protected void doExecute() {
				
				if (callEvent != null && op != null) {
					Trigger mimicTrigger = UMLFactory.eINSTANCE.createTrigger();
					mimicTrigger.getPorts().add(observerPort);
					mimicTrigger.setEvent(callEvent);
					triggers.add(mimicTrigger);
				}
				else {
					Operation mimicOperation = UMLFactory.eINSTANCE.createOperation();
					mimicOperation.setName(proposedName);
					
					EAnnotation observerAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
					observerAnnotation.setSource("observer");
					observerAnnotation.getReferences().add(((CallEvent)initialTrigger.getEvent()).getOperation());
					
					observerAnnotation.getDetails().put("name", messageName);
					observerAnnotation.getDetails().put("capsule", capsuleName);
					mimicOperation.getEAnnotations().add(observerAnnotation);
					
					
					CallEvent mimicEvent = UMLFactory.eINSTANCE.createCallEvent();
					mimicEvent.setName(proposedName);
					mimicEvent.setOperation(mimicOperation);
					
					observerPackage.getPackagedElements().add(mimicEvent);
					observerInterface.getOwnedOperations().add(mimicOperation);
					
					Trigger mimicTrigger = UMLFactory.eINSTANCE.createTrigger();
					mimicTrigger.getPorts().add(observerPort);
					mimicTrigger.setEvent(mimicEvent);
					triggers.add(mimicTrigger);
				}
			}
		});
	}


}
