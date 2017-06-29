/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code is an handler to observe a capsule:
 * - It applies the observable stereotype
 * - It creates the Observer port automatically
 * FIXME: adding the Observer port leads to some graphical issues.
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.emf.facet.util.ui.internal.exported.handler.HandlerUtils;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.tools.util.PlatformHelper;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Stereotype;

public class ObserveCapsuleHandler extends ObserveHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object element = HandlerUtils.getSelection();
		
		Element eobj = PlatformHelper.getAdapter(element, Element.class);
		
		if (eobj == null || !(eobj instanceof Class))
			return null;
		
		Class capsule = (Class)eobj;
		try {
			TransactionalEditingDomain editingDomain = getEditingDomain(capsule);
			applyObservableCapsuleStereotype(capsule, editingDomain);
			createObserverPort(capsule, editingDomain);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private void applyObservableCapsuleStereotype(Class capsule, TransactionalEditingDomain editingDomain) {
		Stereotype stereotype = capsule.getApplicableStereotype("Observation::ObservableCapsule");
		applyObservableStereotype(capsule, stereotype, editingDomain);
	}
	
	private void createObserverPort(Class capsule, TransactionalEditingDomain editingDomain) {
		Collaboration observerProtocol = getObserverProtocol(capsule.getModel());
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			
			@Override
			protected void doExecute() {
				Port port = capsule.createOwnedPort("observation", observerProtocol);
				Stereotype RTPort = port.getApplicableStereotype("UMLRealTime::RTPort");
				port.applyStereotype(RTPort);
				port.setIsBehavior(true);
				port.setIsConjugated(false);
				port.setIsService(false);
				port.setValue(RTPort, "isWired", "false");
				port.setValue(RTPort, "isPublish", "false");
				port.setValue(RTPort, "isNotification", "true");
				port.setType(observerProtocol);
			}
		});
	}

}
