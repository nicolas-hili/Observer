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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResource;
import org.eclipse.uml2.uml.Element;

public abstract class ObserveHandler extends AbstractHandler {
	

	/* The Marker id */
	protected static final String MARKER = "ca.queensu.cs.observer.capsule.marker";
	
	
	protected ModelSet getModelSet(Element element) throws ServiceException {
		return ServiceUtilsForResource.getInstance().getModelSet(element.eResource());
	}
	
	protected TransactionalEditingDomain getEditingDomain(Element element) throws ServiceException {
		return ServiceUtilsForResource.getInstance().getTransactionalEditingDomain(element.eResource());
	}
	
	/**
	 * Convenience method returning the IResource corresponding to a Resource
	 *
	 * @param resource
	 *            The Resource from which the corresponding IResource has to be retrieved
	 * @return the IResource corresponding to the Resource
	 */
	protected IResource getIResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		String uriPath = resource.getURI().toPlatformString(true);
		if (uriPath == null) {
			return null;
		}
		IResource iresource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uriPath));
		if (iresource != null) {
			if (iresource.exists()) {
				return iresource;
			}
		}
		return null;
	}
	

}
