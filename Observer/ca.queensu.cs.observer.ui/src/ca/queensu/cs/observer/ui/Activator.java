package ca.queensu.cs.observer.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.queensu.cs.observer.ui.preferences.ObserverNode;
import ca.queensu.cs.observer.ui.preferences.ObserverPreferencePage;


public class Activator extends AbstractUIPlugin {
	
	
	public static final String PAPYRUSRT_PREFERENCE_PAGE_ID = "org.eclipse.papyrusrt.umlrt.common.ui.papyrusrt";
	public static final String OBSERVER_PREFERENCE_PAGE_ID = "ca.queensu.cs.observer.ui.preferences";
	public static final String SERIALIZATION_PREFERENCE_PAGE_ID = "ca.queensu.cs.observer.ui.preferences.serialization";
	public static final String COMMUNICATION_PREFERENCE_PAGE_ID = "ca.queensu.cs.observer.ui.preferences.communication";
	// The plug-in ID
	public static final String PLUGIN_ID = "ca.queensu.cs.observer.ui"; //$NON-NLS-1$
	

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		PreferenceManager pmngr= PlatformUI.getWorkbench().getPreferenceManager();
		
		IPreferenceNode root = pmngr.find(PAPYRUSRT_PREFERENCE_PAGE_ID
				+ "/" + OBSERVER_PREFERENCE_PAGE_ID
				+ "/" + SERIALIZATION_PREFERENCE_PAGE_ID);
		createPreferencePageSubNodes(root);
	}
	
	public void createPreferencePageSubNodes(IPreferenceNode root) {
		
		IConfigurationElement[] configs = this.getSerializationConfig();
		for (IConfigurationElement config: configs) {
			
			String name = config.getAttribute("name");
			String label = config.getAttribute("label");
			String description = config.getAttribute("description");
			if (config.getAttribute("preference_page") == null)
				continue;
			
			try {
				ObserverPreferencePage o = (ObserverPreferencePage)config.createExecutableExtension("preference_page");
				o.setPrefix("ser_");
				o.setTitle(label);
				o.setDescription(description);
				o.setPreferenceStore(Activator.getDefault().getPreferenceStore());
				o.setConfig(config);
				if (o instanceof PreferencePage) {
					// Node creation
					IPreferenceNode node = new ObserverNode(name, o, config, "ser_");
					root.add(node);	
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public String[][] getSerializationMethods() {
		
		IConfigurationElement[] configs = this.getSerializationConfig();
		
		String[][] serializations = new String[configs.length][];
		
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement e = configs[i];
			 
			
			String[] val = new String[] {
				e.getAttribute("label"),
				e.getAttribute("name")
			};
			serializations[i] = val;
		}
		return serializations;
	}
	
	public IConfigurationElement[] getSerializationConfig() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor("ca.queensu.cs.observer.serializations");
		
		
		if (config == null)
			return new IConfigurationElement[0];

		return config;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
