/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Label provider for the Observer Explorer. Currently not used
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.providers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.papyrusrt.umlrt.tooling.ui.labelprovider.UMLRTLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;

import ca.queensu.cs.observer.ui.providers.wrappers.CapsulePartWrapper;

public class ObserverExplorerLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof Model) {
			return ((Model) element).getName();
		} else if (element instanceof Class) {
			return ((Class) element).getName();
		} else if (element instanceof Property) {
			Property prop = (Property) element;
			
			StringBuffer buf = new StringBuffer();
			//buf.append(" \u27E3> ");
			buf.append(prop.getName());
			buf.append(": ");
			buf.append(prop.getType().getName());
			buf.append(" (in "+ ((Class)prop.getOwner()).getName()+")");
			return buf.toString();
		} else if (element instanceof CapsulePartWrapper) {
			return ((CapsulePartWrapper) element).getName();
		}
		return null;
	}
	
	@Override
	public String getDescription(Object element) {
		String text = getText(element);
        return "This is a description of " + text;
	}

	@Override
	public Image getImage(Object element) {
		UMLRTLabelProvider labelProvider = new UMLRTLabelProvider();
		if (element instanceof NamedElement) {
			NamedElement elem = (NamedElement) element;
			Image image = labelProvider.getImage(element);
			return image;
		}
		else if (element instanceof CapsulePartWrapper) {
			Image image = labelProvider.getImage(((CapsulePartWrapper) element).getProperty());
			return image;
		}
		return null;
	}

}
