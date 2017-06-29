/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code creates an Observer Node in the preference page
 * depending on the preference page the user enters
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import ca.queensu.cs.observer.ui.Activator;

public class ObserverNode extends PreferenceNode {

	IConfigurationElement config;
	String prefix;
	
	public ObserverNode(String id, IPreferencePage preferencePage, IConfigurationElement config, String prefix) {
		super(id, preferencePage);
		this.config = config;
		this.prefix = prefix;
	}


	@Override
	public IPreferencePage getPage() {
		IPreferencePage page = super.getPage();
		if (page == null) {
			try {
				page = (PreferencePage)config.createExecutableExtension("preference_page");
				((ObserverPreferencePage)page).setPrefix(prefix);
				page.setTitle(config.getAttribute("label"));
				page.setDescription(config.getAttribute("description"));
				((PreferencePage)page).setPreferenceStore(Activator.getDefault().getPreferenceStore());
				((ObserverPreferencePage)page).setConfig(config);
				this.setPage(page);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return page;
		
	}
	
	@Override
		public String getId() {
			String id = super.getId();
			if (id == null || id.isEmpty()) {
				System.out.println("problem");
			}
			return id;
		}

	@Override
	public String getLabelText() {
		String labelText = super.getLabelText();
		if (labelText == null || labelText.isEmpty()) {
			this.getPage(); // trigger the creation of the page, so the label is not empty
			return super.getLabelText();
		}
		return labelText;
	}
	

}
