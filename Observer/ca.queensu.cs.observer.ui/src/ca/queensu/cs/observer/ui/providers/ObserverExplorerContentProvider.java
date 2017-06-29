/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Content provider for the Observer Explorer. Currently not used
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.providers;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.papyrusrt.umlrt.core.utils.CapsulePartUtils;
import org.eclipse.papyrusrt.umlrt.core.utils.CapsuleUtils;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;

import ca.queensu.cs.observer.ui.providers.wrappers.CapsulePartWrapper;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;

public class ObserverExplorerContentProvider implements ITreeContentProvider {

	private Class[] capsules;
	
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Model) {
			if (capsules == null) {
				initializeParents((Model)parentElement);
				return capsules;
			} else if (parentElement instanceof Class) {
				return new Object[0];
			}
		}
		else if (parentElement instanceof Class) {
			
			ArrayList<Property> capsuleParts = new ArrayList<Property>();
			
			for (Class capsule: capsules) {
				for (Property attribute : ((Class) capsule).getAllAttributes()) {
					if (CapsulePartUtils.isCapsulePart(attribute)
							&& parentElement.equals(attribute.getType())) {
						capsuleParts.add(attribute);
					}
				}
			}
			return capsuleParts.toArray(new Property[capsuleParts.size()]);
		}
		else if (parentElement instanceof Property) {
			int replication = ((Property) parentElement).getUpper();
			if (replication > 1) {
				CapsulePartWrapper wrappers[] = new CapsulePartWrapper[replication];
				for (int i = 0; i < replication; i++) {
					wrappers[i] = new CapsulePartWrapper((Property) parentElement, i);
				}
				return wrappers;
			}
			return new Object[0];
		}
		return new Object[0];
	}

	private void initializeParents(Model parentElement) {
		ArrayList<Class> capsules = new ArrayList<Class>();
		EList<PackageableElement> pes = parentElement.getPackagedElements();
		for (PackageableElement pe : pes) {
			if (pe instanceof Class && CapsuleUtils.isCapsule((Class)pe)) {
				capsules.add((Class)pe);
			}
		}
		
		this.capsules = capsules.toArray(new Class[capsules.size()]);
		
	}
	
	@Override
	public void dispose() {
		this.capsules = null;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		ITreeContentProvider.super.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof Element) {
			if (((Element) element).getOwner() != null) {
				return ((Element) element).getOwner();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof Model || element instanceof Class || element instanceof Property);
	}

}
