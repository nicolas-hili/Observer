/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: this code creates a preference page for configuring the Observer
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.preferences;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ca.queensu.cs.observer.ui.Activator;
import ca.queensu.cs.observer.ui.preferences.fieldeditors.CheckboxGroupFieldEditor;

public class ObserverRootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private String[][] serializationMethods;
	private Composite warningComposite;

	/**
	 * @wbp.parser.constructor
	 */
	public ObserverRootPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public ObserverRootPreferencePage(int style) {
		super(style);
		// TODO Auto-generated constructor stub
	}

	public ObserverRootPreferencePage(String title, int style) {
		super(title, style);
		// TODO Auto-generated constructor stub
	}

	public ObserverRootPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Observer preference page");
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		CheckboxGroupFieldEditor source = (CheckboxGroupFieldEditor)event.getSource();
		String name = source.getPreferenceName();
		
		for (Control child : warningComposite.getChildren())
			child.dispose();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor("ca.queensu.cs.observer.serializations");
		
		if (name.equals("SERIALIZATION")) {
			String[] values = ((String)event.getNewValue()).split(",");
			for (String value : values) {
				if (value == null)
					continue;
				
				for (int i = 0; i < config.length; i++) {
					String serializationName = config[i].getAttribute("name");
					if (serializationName.equals(value)) {
						String restriction = config[i].getAttribute("restriction");
						if (restriction != null && !restriction.isEmpty()) {
							
							IWorkbench workbench = PlatformUI.getWorkbench();
							ISharedImages images = workbench.getSharedImages();
							Image image = images.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
							ImageData data = image.getImageData().scaledTo(16, 16);
						
							Image warning = new Image(null, data);
									
							GridData gd = new GridData(GridData.FILL_HORIZONTAL);
							gd.heightHint = 30;
							

							CLabel label = new CLabel(warningComposite, SWT.WRAP);
							label.setImage(warning);
							label.setText(value + ": " + restriction);
							label.setLayoutData(gd);
							
							warningComposite.pack(true);
							label.pack(true);
							
						}
					}
				}
			}
		}
	}

	

	private String[][] getCommunicationMethods() {
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor("ca.queensu.cs.observer.communications");
		
		if (config == null)
			return new String[0][];
		
		String[][] serializations = new String[config.length][];
		
		for (int i = 0; i < config.length; i++) {
			
			IConfigurationElement e = config[i];
			 
			final String o = e.getAttribute("label");
			
			String[] val = new String[] {
				e.getAttribute("label"),
				e.getAttribute("name")
			};
			
			serializations[i] = val;
		}
		
		return serializations;
	}

	@Override
	public void dispose() {
		
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control fieldEditorParent = super.createContents(parent);
		
		warningComposite = new Composite((Composite)fieldEditorParent, SWT.WRAP);
		warningComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 1;
        
        warningComposite.setLayout(layout);
		
		
		return fieldEditorParent;
	}
	
	@Override
	protected void createFieldEditors() {
		
		CheckboxGroupFieldEditor serialization= new CheckboxGroupFieldEditor(
				"SERIALIZATION", "&Serialization methods (at least one has to be chosen)", 2,
				Activator.getDefault().getSerializationMethods(),
	          getFieldEditorParent(), true);
		
		CheckboxGroupFieldEditor method= new CheckboxGroupFieldEditor(
				"METHOD", "&Communication methods (at least one has to be chosen)", 2,
				getCommunicationMethods(),
	          getFieldEditorParent(), true);
		
		addField(serialization);
		addField(method);
		
		
	}

}
