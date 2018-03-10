/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: preference page for the observer communication media/
 * serialisation methods. Automatically populated
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ca.queensu.cs.observer.ui.Activator;
import ca.queensu.cs.observer.ui.preferences.fieldeditors.MultiLineTextFieldEditor;
import ca.queensu.cs.observer.ui.preferences.fieldeditors.ObserverListEditor;
import ca.queensu.cs.observer.ui.preferences.fieldeditors.ObserverListEditorBasicMonitor;



public class ObserverPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	IConfigurationElement config;
	
	MultiLineTextFieldEditor visual;
	
	Map<String, String> map = new HashMap<String, String>();
	
	private List<ObserverListEditor> listEditors = new ArrayList<ObserverListEditor>();

	private String prefix = "";
	
	public IConfigurationElement getConfig() {
		return config;
	}
	
	public void setConfig(IConfigurationElement config) {
		this.config = config;
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	} 
	
	@Override
	protected void createFieldEditors() {

		String configName = config.getAttribute("name");
		
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
		IConfigurationElement[] attributeConfigs = config.getChildren("attribute");
		for (IConfigurationElement attributeConfig : attributeConfigs) {
		
			String name = attributeConfig.getAttribute("name");
			String label = attributeConfig.getAttribute("label");
			String type = attributeConfig.getAttribute("type");
			boolean isRequired = Boolean.parseBoolean(attributeConfig.getAttribute("is_required"));
			
			map.put(name, getPreferenceStore().getString(prefix + configName + "_" + name));
			
			createField(prefix + configName + "_" + name, type, label, isRequired);
			
		}
		
		visual = new MultiLineTextFieldEditor("config",
				"&generated config&:&", getFieldEditorParent());
		visual.setPreferenceStore(null);
		
		addField(visual);
		rebuildVisual();
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		FieldEditor editor = (FieldEditor)event.getSource();
		String[] fragments = editor.getPreferenceName().split("[_]");
		String name = fragments[fragments.length-1];
		map.put(name, String.valueOf(event.getNewValue()));
		
		if (name.equals("separator")) {
			for (int i = 0; i < listEditors.size(); i++) {
				ObserverListEditor listEditor = listEditors.get(i);
				listEditor.setSeparator(map.get("separator"));
			}
		}
		rebuildVisual();
		super.propertyChange(event);
	}
	
	void rebuildVisual() {
		
		StringBuffer buf = new StringBuffer();
		
		String configName = config.getAttribute("name");
		buf.append("# Used by " + configName + "\n");
		buf.append("["+configName+"]\n");
		
		for (Map.Entry<String, String> entry : map.entrySet()) {
			buf.append("\t" + entry.getKey() + "=" + entry.getValue() +"\n");
		}
		
		
		visual.setStringValue(buf.toString());
		
	}
	
	
	
	private void createField(String name, String type, String label, boolean isRequired) {
		switch (type) {
		case "String":
			createFieldForString(name, label, isRequired);
			break;
		case "Char":
			createFieldForChar(name, label, isRequired);
			break;
		case "Integer":
			createFieldForInteger(name, label, isRequired);
			break;
		case "Boolean":
			createFieldForBoolean(name, label, isRequired);
			break;
		case "List":
			createFieldForList(name, label, isRequired);
		}
		
	}
	
	private void createFieldForString(String name, String label, boolean isRequired) {
		StringFieldEditor editor = new StringFieldEditor(name, "&"+label+"&:&",
                getFieldEditorParent());
		editor.setEmptyStringAllowed(!isRequired);
		addField(editor);
	}
	
	private void createFieldForList(String name, String label, boolean isRequired) {
		
		ObserverListEditor editor = new ObserverListEditor(name, "&"+label+"&:&",
				new ObserverListEditorBasicMonitor(),
				getFieldEditorParent());
		
		editor.setSeparator(map.get("separator"));
		editor.setEmptyEntryAllowed(!isRequired);
		
		addField(editor);
		listEditors.add(editor);
		
	}

	private void createFieldForChar(String name, String label, boolean isRequired) {
		StringFieldEditor editor = new StringFieldEditor(name, "&"+label+":",
                getFieldEditorParent());
		editor.setEmptyStringAllowed(!isRequired);
		editor.setTextLimit(1);
		addField(editor);
	}

	private void createFieldForInteger(String name, String label, boolean isRequired) {
		
		IntegerFieldEditor editor = new IntegerFieldEditor(name, "&"+label+":",
                getFieldEditorParent());
		editor.setEmptyStringAllowed(!isRequired);
		String value = this.getPreferenceStore().getString(name);
		editor.setStringValue(value);
		addField(editor);
	}
	
	private void createFieldForBoolean(String name, String label, boolean isRequired) {
		
		BooleanFieldEditor editor = new BooleanFieldEditor(name, "&"+label+":",
                getFieldEditorParent());
		addField(editor);
		
		Boolean value = this.getPreferenceStore().getBoolean(name);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	public void setPrefix(String prefix) {
		this.prefix  = prefix;
		
	}
}
