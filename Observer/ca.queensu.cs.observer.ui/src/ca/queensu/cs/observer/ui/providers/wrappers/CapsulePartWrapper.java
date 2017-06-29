/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Wrapper used for instances of capsule parts in the Observer
 * explorer
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.providers.wrappers;

import org.eclipse.uml2.uml.Property;

public class CapsulePartWrapper {
	
	private Property property;
	private int index;
	
	public CapsulePartWrapper(Property property, int index) {
		this.property = property;
		this.index = index;
	}
	
	public String getName() {
		return property.getName() + ":" + index;
	}
	
	public Property getProperty() {
		return property;
	}

}
