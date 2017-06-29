/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code is an handler to un-observe a capsule.
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
import org.eclipse.papyrusrt.umlrt.core.utils.RTPortUtils;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

public class UnobserveCapsuleHandler extends ObserveHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object element = HandlerUtils.getSelection();
		
		Element eobj = PlatformHelper.getAdapter(element, Element.class);
		
		if (eobj == null || !(eobj instanceof Class))
			return null;
		
		Class capsule = (Class)eobj;
		try {
			TransactionalEditingDomain editingDomain = getEditingDomain(capsule);
			unapplyObservableCapsuleStereotype(capsule, editingDomain);
			removeObserverPort(capsule, editingDomain);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private void removeObserverPort(Class capsule, TransactionalEditingDomain editingDomain) {
		Port sapPort = getObserverPort(capsule);
		if (sapPort == null)
			return;
		
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			
			@Override
			protected void doExecute() {
				sapPort.destroy();
			}
		});
	}

	private void unapplyObservableCapsuleStereotype(Class capsule, TransactionalEditingDomain editingDomain) {
		Stereotype stereotype = capsule.getAppliedStereotype("Observation::ObservableCapsule");
		unapplyObservableStereotype(capsule, stereotype, editingDomain);
	}

	private Port getObserverPort(Class capsule) {
		
		Collaboration observerProtocol = getObserverProtocol(capsule.getModel());
		
		for (Property property : capsule.getAllAttributes()) {

			if (!RTPortUtils.isRTPort(property))
				continue;
			
			Port port = (Port)property;
			
			if (observerProtocol.equals(port.getType())) {
				return port;
			}
		}
		return null;
	}

}
