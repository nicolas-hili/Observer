package ca.queensu.cs.observer.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.ui.handlers.HandlerUtil;

public class GenerateObservableCodeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// Get the current selection
		EObject eobj = getCurrentSelection(event);
		
		// Retrieve its resource and transactional editing domain
		Resource resource = getCurrentResource(eobj);
		TransactionalEditingDomain editingDomain = getEditingDomain(eobj);
		
		// Return null if something went wrong at this time 
		if (eobj == null || resource == null || editingDomain == null)
			return null;
		
		// Retrieve the file corresponding to the EMF Resource
		String path = resource.getURI().toPlatformString(true);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(new Path(path));
		
		// Prepare the name of the copied resource
		String name = new Path(path).removeFileExtension().lastSegment() + "_observed";	
		
		// Copy the file
		IFile copiedFile = root.getFile(new Path(path).removeFileExtension().removeLastSegments(1).append(name).addFileExtension("uml"));
		try {
			if (copiedFile.exists()) {
				copiedFile.delete(true, null);
			}
			copiedFile.create(file.getContents(), true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Retrieve the URI of the copied file
		URI uri = resource.getURI().trimFileExtension().trimSegments(1).appendSegment(name).appendFileExtension("uml");
		
		// Instrument the model
		InstrumentModelCommand instrumentCommand = new InstrumentModelCommand(editingDomain);
		instrumentCommand.setResourceToInstrument(uri);
		instrumentCommand.doExecute();
		return null;
	}
	
	private EObject getCurrentSelection(ExecutionEvent event) {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			if (!selection.isEmpty()) {
				EObject eobj = EMFHelper.getEObject(selection.getFirstElement());
				return eobj;
			}
		}
		return null;
	}
	
	private Resource getCurrentResource(EObject eobj) {
		return (eobj != null) ? eobj.eResource() : null;
	}
	
	private TransactionalEditingDomain getEditingDomain(EObject eobj) {
		return TransactionUtil.getEditingDomain(eobj);
	}

}
