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
import org.eclipse.papyrus.infra.tools.util.PlatformHelper;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

public class ObserverPropertyTester extends PropertyTester {

	/* Is capsule observed property name */
	private static final String IS_CAPSULE_OBSERVED = "isCapsuleObserved";
	
	/* Is not capsule observed property name */
	private static final String IS_NOT_CAPSULE_OBSERVED = "isNotCapsuleObserved";
	
	/* Is capsule part observed property name */
	private static final String IS_CAPSULE_PART_OBSERVED = "isCapsulePartObserved";
	
	/* Is not capsule part observed property name */
	private static final String IS_NOT_CAPSULE_PART_OBSERVED = "isNotCapsulePartObserved";
	
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
		case IS_CAPSULE_PART_OBSERVED:
			return isCapsulePartObserved((Element)eobj) == asBoolean(expectedValue, true);
		case IS_NOT_CAPSULE_PART_OBSERVED:
			return isNotCapsulePartObserved((Element)eobj) == asBoolean(expectedValue, true);
		default:
			return false;
		}
		
	}

	private boolean isCapsuleObserved(Element element) {
		assert element == null  || !(element instanceof Class) : "Element should be an instance of org.eclipse.uml2.uml.Class";
		
		Stereotype stereotype = element.getApplicableStereotype("Observation::ObservableCapsule");
		
		return stereotype != null && element.isStereotypeApplied(stereotype);
		
	}
	
	private boolean isNotCapsuleObserved(Element element) {
		assert element == null  || !(element instanceof Class) : "Element should be an instance of org.eclipse.uml2.uml.Class";
		
		Stereotype stereotype = element.getApplicableStereotype("Observation::ObservableCapsule");
		
		return stereotype != null && !element.isStereotypeApplied(stereotype);
	}

	private boolean isCapsulePartObserved(Element element) {
		assert element == null  || !(element instanceof Property) : "Element should be an instance of org.eclipse.uml2.uml.Property";
		
		Stereotype stereotype = element.getApplicableStereotype("Observation::ObservableCapsulePart");
		
		return stereotype != null && element.isStereotypeApplied(stereotype);
		
	}
	
	private boolean isNotCapsulePartObserved(Element element) {
		assert element == null  || !(element instanceof Property) : "Element should be an instance of org.eclipse.uml2.uml.Property";
		
		Stereotype stereotype = element.getApplicableStereotype("Observation::ObservableCapsulePart");
		
		return stereotype != null && !element.isStereotypeApplied(stereotype);
	}
	
	boolean asBoolean(Object value, boolean defaultValue) {
		return (value instanceof Boolean)
				? (Boolean) value
				: (value == null)
						? defaultValue
						: Boolean.valueOf(String.valueOf(value));
	}

}
