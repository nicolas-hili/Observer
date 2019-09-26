package ca.queensu.cs.observer.ui.commands;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Model;

import ca.queensu.cs.observer.ui.Activator;

public class SelectCommunicationMethodContributionItem extends ContributionItem {

	public SelectCommunicationMethodContributionItem() {
		// TODO Auto-generated constructor stub
	}

	public SelectCommunicationMethodContributionItem(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
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
	
	@Override
	public void fill(Menu menu, int index) {
		
		// Retrieve the currently selected Ecore resource
		Resource res = getResource();
		
		// Convert it into an IFile
		IFile file = convertToFile(res);
		
		// Retrieve the observer marker. Create it if it does not exist
		IMarker marker = getMarker(file);
		
		// Get the communication configurations
		IConfigurationElement[] communications = Activator.getDefault().getCommunicationConfig();
		
		// Retrieve the first communication
		String communicationName = communications[0].getAttribute("name");
		
		// Get the stored communication value, or use the default one (first on the list
		String communicationValue = marker.getAttribute("communication", communicationName);	
		
		for (IConfigurationElement communication : communications) {
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO, index);
	        menuItem.setText(communication.getAttribute("label"));
	        menuItem.setData("name", communication.getAttribute("name"));
	        menuItem.setData("configuration", communication);
	        
	        if (communication.getAttribute("name").equals(communicationValue)) {
	        	menuItem.setSelection(true);
	        }
	        
	        menuItem.addSelectionListener(new SelectionAdapter() {
	        	public void widgetSelected(SelectionEvent e) {  
	        		MenuItem item =  (MenuItem)e.getSource();
	        		try {
						marker.setAttribute("communication", item.getData("name"));
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	        	}
	        }); 
		}
	}
		
	@Override
	public boolean isDynamic() {
		return true;
	}	

}
