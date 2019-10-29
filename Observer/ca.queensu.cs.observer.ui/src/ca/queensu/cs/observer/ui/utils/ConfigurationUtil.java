package ca.queensu.cs.observer.ui.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Model;

import ca.queensu.cs.observer.ui.Activator;

public class ConfigurationUtil {

	static ConfigurationUtil instance = null;
	
	private ConfigurationUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static ConfigurationUtil getInstance() {
		if(instance == null)
			instance = new ConfigurationUtil();
		
		return instance;
	}
	
	/**
	 * Return configuration element based on the extension point
	 * @param extensionPoint - the extension point
	 * @return Array of IConfigurationElement
	 */
	private IConfigurationElement[] getConfigurations(String extensionPoint) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(extensionPoint);
		
		if (config == null)
			return new IConfigurationElement[0];
		
		return config;
	}
	
	/**
	 * Return the serialization formats registered through the extension point
	 * @return Array of IConfigurationElement
	 */
	public IConfigurationElement[] getSerializationFormats() {
		return this.getConfigurations("ca.queensu.cs.observer.serializations");
	}
	
	/**
	 * Return the communication methods registered through the extension point
	 * @return Array of IConfigurationElement
	 */
	public IConfigurationElement[] getCommunicationMethods() {
		return this.getConfigurations("ca.queensu.cs.observer.communications");
	}
	
	/**
	 * Get the selected resource
	 * @return
	 */
	private Resource getResource() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null) {
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        if (firstElement instanceof IAdaptable) {
	        	Model model = (Model)((IAdaptable)firstElement).getAdapter(Model.class);
	        	Resource res = model.eResource();
	        	return res;
	        }
	    }
	    return null;
	}
	
	private IFile convertToFile(Resource res) {
		return ResourcesPlugin.getWorkspace()
							  .getRoot()
							  .getFile(new Path(res.getURI().toPlatformString(true)));
	}
	
	private IMarker getMarker(IResource res) {
		IMarker[] markers = null;
		int depth = IResource.DEPTH_ZERO;
		try {
			markers = res.findMarkers("ca.queensu.cs.observer.marker", true, depth);
			if (markers.length == 0) {
				return res.createMarker("ca.queensu.cs.observer.marker");
			}
			else {
				return markers[0];
			}
		} catch (CoreException e) {
	      // something went wrong
	   }
		return null;
	}
	
	private IConfigurationElement getConfiguration(String extensionPoint, String name) {
		// Retrieve the currently selected Ecore resource
		Resource res = getResource();
		
		// Convert it into an IFile
		IFile file = convertToFile(res);
		
		// Retrieve the observer marker. Create it if it does not exist
		IMarker marker = getMarker(file);
		
		// Get the serialization configurations
		IConfigurationElement[] configurations = this.getConfigurations(extensionPoint);
		
		// Retrieve the first communication
		String configurationName = configurations[0].getAttribute("name");
		
		// Get the stored communication value, or use the default one (first on the list
		String configurationValue = marker.getAttribute(name, configurationName);
		
		if (configurationValue.equals(configurationName))
			return configurations[0];
		
		for (int i = 0; i < configurations.length; i++) {
			IConfigurationElement configuration = configurations[i];
			if (configuration.getAttribute("name").equals(configurationValue)) {
				return configuration;
			}
		}
		return configurations[0];
		
	}
	
	public IConfigurationElement getSerializationFormat() {
		return getConfiguration("ca.queensu.cs.observer.serializations", "serialization");
	}
	
	public IConfigurationElement getCommunicationMethod() {
		return getConfiguration("ca.queensu.cs.observer.communications", "communication");
	}
	
	public String getSerializationFormatName() {
		return getSerializationFormat().getAttribute("name");
	}
	
	public String getCommunicationMethodName() {
		return getCommunicationMethod().getAttribute("name");
	}
	
}
