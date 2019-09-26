/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code is a property tester to determine:
 * - Whether the Observer is loaded or not
 * - Whether a capsule / capsule part is observed or not
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.infra.tools.util.PlatformHelper;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;

public class ObserverPropertyTester extends PropertyTester {

	/* The Marker id */
	private static final String MARKER = "ca.queensu.cs.observer.capsule.marker";

	/* Is capsule observed property name */
	private static final String IS_CAPSULE_OBSERVED = "isCapsuleObserved";
	
	/* Is not capsule observed property name */
	private static final String IS_NOT_CAPSULE_OBSERVED = "isNotCapsuleObserved";
	
	public ObserverPropertyTester() {
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		Object element = receiver;
		if (receiver instanceof List && !((List) receiver).isEmpty()) {
			element = ((List) receiver).get(0);
		}
		
		Element eobj = PlatformHelper.getAdapter(element, Element.class);
		if (eobj == null || !(eobj instanceof Element))
			return false;
		
		switch (property) {
		case IS_CAPSULE_OBSERVED:
			return isCapsuleObserved((Element)eobj) == asBoolean(expectedValue, true);
		case IS_NOT_CAPSULE_OBSERVED:
			return isNotCapsuleObserved((Element)eobj) == asBoolean(expectedValue, true);
		default:
			return false;
		}
		
	}
	
	/**
	 * Convenience method returning the IResource corresponding to a Resource
	 *
	 * @param resource
	 *            The Resource from which the corresponding IResource has to be retrieved
	 * @return the IResource corresponding to the Resource
	 */
	public static IResource getIResource(Resource resource) {
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
	
	private boolean hasObserveMarker(Element element) {
		IResource iresource = getIResource(element.eResource());
		try {
			IMarker[] markers = iresource.findMarkers(MARKER, false, IResource.DEPTH_ZERO);
			return markers.length == 1;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isCapsuleObserved(Element element) {
		assert element == null  || !(element instanceof Class) : "Element should be an instance of org.eclipse.uml2.uml.Class";
		return !hasObserveMarker(element);
		
	}
	
	private boolean isNotCapsuleObserved(Element element) {
		assert element == null  || !(element instanceof Class) : "Element should be an instance of org.eclipse.uml2.uml.Class";
		return hasObserveMarker(element);
	}
	
	boolean asBoolean(Object value, boolean defaultValue) {
		return (value instanceof Boolean)
				? (Boolean) value
				: (value == null)
						? defaultValue
						: Boolean.valueOf(String.valueOf(value));
	}

}
