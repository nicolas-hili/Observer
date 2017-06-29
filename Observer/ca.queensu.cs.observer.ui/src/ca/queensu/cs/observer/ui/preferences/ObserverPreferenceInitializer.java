/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ca.queensu.cs.observer.ui.Activator;

public class ObserverPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		IConfigurationElement[] configs = Activator.getDefault().getSerializationConfig();
		for (IConfigurationElement config: configs) {
			
			String serializationName = config.getAttribute("name");
			
			IConfigurationElement[] attributeConfigs = config.getChildren("attribute");
			for (IConfigurationElement attributeConfig : attributeConfigs) {
				String name = attributeConfig.getAttribute("name");
				String defaultValue = attributeConfig.getAttribute("default_value");
				if (!defaultValue.isEmpty()) {
					store.setDefault("ser_" + serializationName + "_" + name,
							defaultValue);
				}
			}
		}
	}

}
