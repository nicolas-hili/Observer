/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Abstract observer handler
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResource;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Stereotype;

public abstract class ObserveHandler extends AbstractHandler {
	
	protected ModelSet getModelSet(Element element) throws ServiceException {
		return ServiceUtilsForResource.getInstance().getModelSet(element.eResource());
	}
	
	protected TransactionalEditingDomain getEditingDomain(Element element) throws ServiceException {
		return ServiceUtilsForResource.getInstance().getTransactionalEditingDomain(element.eResource());
	}
	
	protected void applyObservableStereotype(Element element, Stereotype stereotype, TransactionalEditingDomain editingDomain) {
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
		
			@Override
			protected void doExecute() {
				element.applyStereotype(stereotype);
			}
		});
	}
	
	protected void unapplyObservableStereotype(Element element, Stereotype stereotype, TransactionalEditingDomain editingDomain) {
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
		
			@Override
			protected void doExecute() {
			element.unapplyStereotype(stereotype);
			}
		});
	}
	
	protected Collaboration getObserverProtocol(Model model) {
		
		Package observerPackage = getObserverPackage(model);
		if (observerPackage == null)
			return null;
		
		Package observerProtocolPackage = (Package)observerPackage.getPackagedElement("Observation");
		
		for (PackageableElement pe: observerProtocolPackage.getPackagedElements()) {
			if (pe.getName().equalsIgnoreCase("Observation") && pe instanceof Collaboration) {
				return (Collaboration)pe;
			}
		}
		
		return null;
	}
	
	protected Package getObserverPackage(Model model) {
		PackageableElement pe = model.getPackagedElement("Observation");
		if (pe != null && pe instanceof org.eclipse.uml2.uml.Package)
			return (Package)pe;
		return null;
	}

}
