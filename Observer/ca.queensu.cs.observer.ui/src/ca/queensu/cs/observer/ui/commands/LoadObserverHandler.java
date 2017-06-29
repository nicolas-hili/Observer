/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code is an handler to initialize the Observer capsule
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.commands;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.uml.tools.model.UmlUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;

import ca.queensu.cs.observer.Activator;


public class LoadObserverHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		ModelSet modelSet = null;
		final ResourceSet resourceSet = new ResourceSetImpl();
		
		if (!(editor instanceof PapyrusMultiDiagramEditor)) {
			return null;
		}
		
		ServicesRegistry services = ((PapyrusMultiDiagramEditor)editor).getServicesRegistry();
		try {
			modelSet = services.getService(ModelSet.class);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(modelSet);
		
		// Get the observation profile
		URI uri = URI.createPlatformPluginURI("ca.queensu.cs.observer.profile/model/observation.profile.uml", true);
		Resource resource = modelSet.getResource(uri, true);
		Profile profile = (Profile)resource.getContents().get(0);
		
		
		try {
			Resource observerUmlResource = null;
			String path = "/libraries/observer.uml";
			observerUmlResource = loadTemplateResource(path, resourceSet);
			EcoreUtil.resolveAll(observerUmlResource);
			
			
			// copy all elements
			EcoreUtil.Copier copier = new EcoreUtil.Copier();
			Collection<EObject> umlObjects = copier.copyAll(observerUmlResource.getContents());
			copier.copyReferences();

			
			// set copied elements in goods resources
			final EList<EObject> contents = UmlUtils.getUmlResource(modelSet).getContents();

			final Model model = (Model)UmlUtils.getUmlResource(modelSet).getContents().get(0);

			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
				
				@Override
				protected void doExecute() {
					

					for (EObject umlObject: umlObjects) {
						if (umlObject instanceof Package)
							model.getPackagedElements().add((PackageableElement) umlObject);
						else
							contents.add(umlObject);
					}
					
					// Apply the profile
					model.applyProfile(profile);
				}
			});
			
			
		}
		finally {
			EMFHelper.unload(resourceSet);
		}
		
		
		return null;
	}
	
	/**
	 * Load template resource.
	 *
	 * @param path
	 *        the path
	 * @return the resource
	 */
	private Resource loadTemplateResource(String path, ResourceSet resourceSet) {
		java.net.URL templateURL = Platform.getBundle(Activator.PLUGIN_ID).getResource(path);
		if(templateURL != null) {
			String fullUri = templateURL.getPath();
			URI uri = URI.createPlatformPluginURI(Activator.PLUGIN_ID + fullUri, true);
			Resource resource = resourceSet.getResource(uri, true);
			if(resource.isLoaded()) {
				return resource;
			}
		}
		return null;
	}

}
