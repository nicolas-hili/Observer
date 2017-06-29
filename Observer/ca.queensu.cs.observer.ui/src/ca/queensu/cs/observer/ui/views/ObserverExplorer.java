/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Observer explorer view. Currently not used.
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 ******************************************************************************/

package ca.queensu.cs.observer.ui.views;

import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrusrt.umlrt.core.utils.RTPortUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

import ca.queensu.cs.observer.ui.providers.wrappers.CapsulePartWrapper;

public class ObserverExplorer extends CommonNavigator implements ISelectionListener, IPartListener2 {

	
	private IEditorPart papyrusEditor;
	private Object selection;
	private ServicesRegistry serviceRegistry;
	private TransactionalEditingDomain editingDomain;
	private ModelSet modelSet;
	private Collaboration observerProtocol;
	
	private Composite parent;
	
	

	public ObserverExplorer() {
	}
	
	private void applyStereotype(Element element, String stereotypeQualifiedName) {
		Stereotype stereotype = element.getApplicableStereotype(stereotypeQualifiedName);
		
		if (element.isStereotypeApplied(stereotype))
			return;
		
		if (stereotype != null) {			
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
				
				@Override
				protected void doExecute() {
					element.applyStereotype(stereotype);
				}
			});
		}
	}
	
	private void unapplyStereotype(Element element, String stereotypeQualifiedName) {
		Stereotype stereotype = element.getAppliedStereotype(stereotypeQualifiedName);
		if (stereotype != null) {
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
				
				@Override
				protected void doExecute() {
					element.unapplyStereotype(stereotype);
				}
			});
		}
	}
	
	private Collaboration getObserverProtocol(Model model) {
		
		Package observationPackage = (Package) model.getPackagedElement("Observation");
		Package observerProtocolPackage = (Package) observationPackage.getPackagedElement("Observation");
		for (PackageableElement pe: observerProtocolPackage.getPackagedElements()) {
			if (pe.getName().equalsIgnoreCase("Observation") && pe instanceof Collaboration) {
				observerProtocol = (Collaboration)pe;
				break;
			}
		}
		
		return null;
	}
	
	private Port getObserverPort(Class capsule) {
		
		assert observerProtocol != null : "the observer protocol is not found";
		
		for (Property property : capsule.getAllAttributes()) {

			if (!RTPortUtils.isRTPort(property))
				continue;
			
			Port port = (Port)property;
			
			if (port.getType().equals(observerProtocol)) {
				return port;
			}
		}
		return null;
	}
	
	private void applyStereotype(Class capsule) {
		applyStereotype(capsule, "Observation::ObservableCapsule");
		
		
		Port observerPort = this.getObserverPort(capsule);
		
		if (observerPort != null) // Observer port is already implemented
			return;
		
		
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

			@Override
			protected void doExecute() {
				Port port = capsule.createOwnedPort("observation", observerProtocol);
				Stereotype RTPort = port.getApplicableStereotype("UMLRealTime::RTPort");
				port.applyStereotype(RTPort);
				port.setIsBehavior(true);
				port.setIsConjugated(false);
				port.setIsService(false);
				port.setValue(RTPort, "isWired", "false");
				port.setValue(RTPort, "isPublish", "false");
				port.setValue(RTPort, "isPublish", "true");
				port.setType(observerProtocol);
			}
			
		});
	}
	
	private void unapplyStereotype(Class capsule) {
		unapplyStereotype(capsule, "Observation::ObservableCapsule");
		
		Port observerPort = this.getObserverPort(capsule);
		
		if (observerPort == null) // Observer port not found
			return;
				
		final Port port = observerPort;
		
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

			@Override
			protected void doExecute() {
				port.destroy();
			}
			
		});
	}
	
	private void applyStereotype(Property capsulePart) {
		applyStereotype(capsulePart, "Observation::ObservableCapsulePart");
	}
	
	private void unapplyStereotype(Property capsulePart) {
		unapplyStereotype(capsulePart, "Observation::ObservableCapsulePart");
	}
	
	private void processAndCheckParentItem(boolean isChecked, TreeItem item) {
		Object element = item.getData();
		boolean oldState = item.getChecked();
		boolean stateChangeRequired = oldState != isChecked;
		
		if (!stateChangeRequired)
			return;
		
		if (!isChecked) {
			for (TreeItem child: item.getItems()) {
				if (child.getChecked()) {
					stateChangeRequired = false;
					break;
				}
			}
		}
		
		if (!stateChangeRequired)
			return;
		

		item.setChecked(isChecked);
		if (element instanceof Class) {
			Class capsule = (Class)element;
			if (isChecked)
				applyStereotype(capsule);
			else
				unapplyStereotype(capsule);
		}
		else if (element instanceof Property) {
			Property capsulePart = (Property)element;
			if (isChecked)
				applyStereotype(capsulePart);
			else
				unapplyStereotype(capsulePart);
			
			processAndCheckParentItem(isChecked, item.getParentItem());
		}
	}
	
	private void processAndCheckSubItem(boolean isChecked, TreeItem item, boolean forceUpdate) {
			Object element = item.getData();
			boolean oldState = item.getChecked();
			
			// This condition is required to check subitems
			// (that are not checked via a user interaction)
			if (!forceUpdate && oldState != isChecked)
				item.setChecked(isChecked);
			
			// This condition determines whether the item is the item checked by the user
			// or a subitem whose state is different than its parent
			if (forceUpdate || oldState != isChecked) {
				if (element instanceof Class) {
					Class capsule = (Class)element;
					if (isChecked)
						applyStereotype(capsule);
					else
						unapplyStereotype(capsule);
				}
				else if (element instanceof Property) {
					Property capsulePart = (Property)element;
					if (isChecked)
						applyStereotype(capsulePart);
					else
						unapplyStereotype(capsulePart);
				}
				else if (element instanceof CapsulePartWrapper) {
					CapsulePartWrapper wrapper = (CapsulePartWrapper)element;
					Property capsulePart = wrapper.getProperty();
					if (isChecked)
						applyStereotype(capsulePart);
					else
						unapplyStereotype(capsulePart);
				}
			}
			
			TreeItem[] childItems = item.getItems();
			
			if (childItems.length > 0) {
				for (TreeItem childItem: childItems) {
					processAndCheckSubItem(isChecked, childItem, false);
				}
			}
	}
	
	@Override
	protected CommonViewer createCommonViewer(Composite parent) {
	  //CommonViewer treeViewer = super.createCommonViewer(parent);
	  int treeStyle = SWT.CHECK | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
	  CommonViewer treeViewer = new CommonViewer( getViewSite().getId(), parent, treeStyle );
	  treeViewer.getTree().addSelectionListener(new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.detail == SWT.CHECK) {
				TreeItem item = ((Tree)e.getSource()).getSelection()[0];
				TreeItem parent = item.getParentItem();
				boolean isChecked = item.getChecked();
				
				processAndCheckSubItem(isChecked, item, true);
				
				if (parent != null)
					processAndCheckParentItem(isChecked, parent);

			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
	});
	  return treeViewer;
	}
	
	@Override
	protected Object getInitialInput() {
		
		URIEditorInput editorInput = (URIEditorInput) ((PapyrusMultiDiagramEditor)papyrusEditor).getActiveEditor().getEditorInput();
		URI uri = editorInput.getURI().trimFragment().trimFileExtension().appendFileExtension("uml");
		Resource umlResource = this.modelSet.getResource(uri, false);
		
		Model model = (Model)umlResource.getContents().get(0);
		
		initializeObserverProtocol(model);
		return model; 		
		
	}
	
	private void initializeObserverProtocol(Model model) {
		this.observerProtocol = this.getObserverProtocol(model);
	}

	@Override
	public void createPartControl(Composite parent) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof PapyrusMultiDiagramEditor) {
			this.papyrusEditor = editor;
			ServicesRegistry serviceRegistry = (ServicesRegistry)papyrusEditor.getAdapter(ServicesRegistry.class);
			try {
				this.modelSet = serviceRegistry.getService(ModelSet.class);
				this.editingDomain = serviceRegistry.getService(TransactionalEditingDomain.class);
			} catch (ServiceException e) {
				System.out.println(this.modelSet);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		super.createPartControl(parent);
				
/*		this.parent = parent;
		
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof PapyrusMultiDiagramEditor) {
			setPapyrusEditor(editor);
		}
		else {
			setPapyrusEditor(null);
		}
		
		getSite().getPage().addSelectionListener(this);
		getSite().getPage().addPartListener(this); */
		
	}
	
	private void setPapyrusEditor(IEditorPart papyrusEditor) {
		this.papyrusEditor = papyrusEditor;
		if (this.papyrusEditor != null) {
			ServicesRegistry serviceRegistry = (ServicesRegistry)papyrusEditor.getAdapter(ServicesRegistry.class);
			try {
				this.modelSet = serviceRegistry.getService(ModelSet.class);
			} catch (ServiceException e) {
				System.out.println(this.modelSet);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.refreshView();
	}
	
	private void refreshView() {
		Control[] children = this.parent.getChildren();
		for (Control child : children) {
			child.dispose();
		}
		
		if (papyrusEditor == null)
			createNoModelAvailableLabel(parent);
		else
			createObserverExplorer(parent);
	}
	
	private void createNoModelAvailableLabel(Composite parent) {
		Label label = new Label(parent, SWT.CENTER);
		label.setText("No Model Available");
	}

	private void createObserverExplorer(Composite parent) {
		super.createPartControl(parent);
	}
	
	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		getSite().getPage().removePartListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
            return;
         IStructuredSelection ss = (IStructuredSelection) selection;
         Object o = ss.getFirstElement();
         if (o != null)
        	 this.selection = o;
       //  System.out.println(o);
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof PapyrusMultiDiagramEditor)
			this.setPapyrusEditor((IEditorPart) part);
	}

	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof PapyrusMultiDiagramEditor)
			this.setPapyrusEditor(null);
	}
	
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {}

}
