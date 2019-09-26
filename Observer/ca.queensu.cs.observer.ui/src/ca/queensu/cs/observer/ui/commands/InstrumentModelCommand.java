package ca.queensu.cs.observer.ui.commands;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.EolModule;
//import org.eclipse.epsilon.eol.IEolExecutableModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dt.ExtensionPointToolNativeTypeDelegate;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.types.EolNativeType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.uml2.uml.Model;
import org.osgi.framework.Bundle;

import ca.queensu.cs.observer.ui.Activator;

public class InstrumentModelCommand {// extends RecordingCommand {

	private Resource resourceToInstrument;
	private org.eclipse.emf.common.util.URI resourceToInstrumentUri;
	
	private TransactionalEditingDomain domain;
	
	protected IEolModule module;//IEolExecutableModule module;

	public InstrumentModelCommand(TransactionalEditingDomain domain) {
	//	super(domain);
		this.domain = domain;
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

//	@Override
	protected void doExecute() {
		
		// Relative link to the main EOL file
		String eolScript = "EOLScripts/UMLRTObserverInstrumentation.eol";
		
		// Create a console
		MessageConsole myConsole = findConsole("Observer Console");
	    MessageConsoleStream out = myConsole.newMessageStream();
	    
	    // Create an Epsilon module
	    module = createModule();
	    module.getContext().getNativeTypeDelegates().add(new ExtensionPointToolNativeTypeDelegate());
	    // Parse the EOL script
	    try {
	    	module.parse(getFileURI(eolScript));
	    if (module.getParseProblems().size() > 0) {
	      System.err.println("Parse errors occured...");
	      for (ParseProblem problem : module.getParseProblems()) {
	        System.err.println(problem.toString());
	      }
	      return;
	    }

		for (IModel model : getModels()) {
		  module.getContext().getModelRepository().addModel(model);
		}
	    
		Object object = execute(module);
	    System.out.println(((org.eclipse.uml2.uml.Model)object).getPackagedElements());
	//	this.resourceToInstrument.save(null);
		
		GenerateCodeCommand generateCommand = new GenerateCodeCommand(domain);
		generateCommand.setInstrumentedModel((Model) object);
		generateCommand.doExecute();
		
//		domain.getCommandStack().execute(generateCommand);
		
	    module.getContext().getModelRepository().dispose();
	    

	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IEolModule createModule() {
		return new EolModule();
	}
	  
	public List<IModel> getModels() throws Exception {
		List<IModel> models = new ArrayList<IModel>();
		String namespaceURI = 	"http://www.eclipse.org/uml2/5.0.0/UML," +
								"http://www.eclipse.org/uml2/5.0.0/Types," +
								"http://www.eclipse.org/uml2/5.0.0/UML/Profile/Standard," +
								"http://www.eclipse.org/papyrus/umlrt/cppproperties," +
								"http://www.eclipse.org/papyrus/umlrt," +
								"http://www.eclipse.org/emf/2002/Ecore," +
								"http://www.eclipse.org/papyrus/umlrt/statemachine," +
								"http://www.eclipse.org/papyrus/umlrt/systemelements," +
								"http://www.eclipse.org/papyrus/infra/gmfdiag/css"
				;
		models.add(createEmfModelByURI("UMLRTModel", "UMLRTModel", this.resourceToInstrumentUri, 
				namespaceURI, true, true));
//		models.add(createEmfModelByURI("UMLRTModel", "DebuggingAgent", org.eclipse.emf.common.util.URI.createPlatformResourceURI("/CommGateWay/model.uml", true), 
//				namespaceURI, true, true));
//		models.add(createEmfModelByURI("UMLPrimitiveTypes", "UMLPrimitiveTypes", org.eclipse.emf.common.util.URI.createPlatformPluginURI("/org.eclipse.uml2.uml.resources/libraries/UMLPrimitiveTypes.library.uml", true), 
//				namespaceURI, true, true));
		//models.add(createEmfModelByURI("DebuggingAgent", "DebuggingAgent", uri.toString(), 
		//		namespaceURI, true, true));
		return models;
	}
	
	protected Object execute(IEolModule module)  throws EolRuntimeException {

		// Retrieve the currently selected Ecore resource
		Resource res = getResource();
				
		// Convert it into an IFile
		IFile file = convertToFile(res);
		
		// Retrieve the observer marker. Create it if it does not exist
		IMarker marker = getMarker(file);
		
		// Get the configurations
		IConfigurationElement[] communications = Activator.getDefault().getCommunicationConfig();
		IConfigurationElement[] serializations = Activator.getDefault().getSerializationConfig();
		
		// Retrieve the first configurations' names
		String communicationName = communications[0].getAttribute("name");
		String serializationName = serializations[0].getAttribute("name");
		
		// Get the stored communication/serialization value, or use the default one (first on the list)
		String communicationValue = marker.getAttribute("communication", communicationName);
		String serializationValue = marker.getAttribute("serialization", serializationName);
		
		IConfigurationElement communicationConfiguration = null;
		IConfigurationElement serializationConfiguration = null;
		
		// Retrieve the corresponding communication / serialization
		for (IConfigurationElement communication : communications) {
			String name = communication.getAttribute("name");
			if (name.equals(communicationName)) {
				communicationConfiguration = communication;
				break;
			}
		}
		for (IConfigurationElement serialization : serializations) {
			String name = serialization.getAttribute("name");
			if (name.equals(serializationName)) {
				serializationConfiguration = serialization;
				break;
			}
		}
		
		
		
		// Retrieve the path to the header and include files
		String communicationPluginName = ((RegistryContributor)communicationConfiguration.getContributor()).getActualName();
		Bundle plugin = Platform.getBundle (communicationPluginName);
		String pluginLocation = plugin.getLocation().toString().substring(15); 
		String communication_include_file = pluginLocation + communicationConfiguration.getAttribute("cpp_include_file");
		String communication_src_file = pluginLocation + communicationConfiguration.getAttribute("cpp_source_file");

		String serializationPluginName = ((RegistryContributor)serializationConfiguration.getContributor()).getActualName();
		plugin = Platform.getBundle (serializationPluginName);
		pluginLocation = plugin.getLocation().toString().substring(15); 
		String serialization_include_file = pluginLocation + serializationConfiguration.getAttribute("cpp_include_file");
		String serialization_src_file = pluginLocation + serializationConfiguration.getAttribute("cpp_source_file");
		

		module.getContext().getFrameStack().putGlobal(
			new Variable("method_src", communication_src_file, EolNativeType.Instance),
			new Variable("method_include", communication_include_file, EolNativeType.Instance),
			new Variable("serializer_include", serialization_include_file, EolNativeType.Instance),
			new Variable("serializer_src", serialization_src_file, EolNativeType.Instance), 
			new Variable("observerPath", "platform:/plugin/ca.queensu.cs.observer/libraries/observer.uml", EolNativeType.Instance)
		);
		
		return module.execute();
	    
	}
	  
	  // loading EMF model
	protected EmfModel createEmfModelByURI(String name, String aliases, 
			  org.eclipse.emf.common.util.URI uri, String metamodel, boolean readOnLoad, boolean storeOnDisposal) 
	          throws EolModelLoadingException, URISyntaxException {
		EmfModel emfModel = new EmfModel();
	    StringProperties properties = new StringProperties();
	    properties.put(EmfModel.PROPERTY_NAME, name);
	    properties.put(EmfModel.PROPERTY_ALIASES, aliases);
	    properties.put(EmfModel.PROPERTY_METAMODEL_URI, metamodel);
	    properties.put(EmfModel.PROPERTY_MODEL_URI, uri);
	  	properties.put(EmfModel.PROPERTY_READONLOAD, readOnLoad + "");
	  	properties.put(EmfModel.PROPERTY_STOREONDISPOSAL, storeOnDisposal + "");
	  	
	    emfModel.load(properties, (IRelativePathResolver) null);
	    return emfModel;
	}
	
	
	public void setResourceToInstrument(org.eclipse.emf.common.util.URI resourceUri) {
		this.resourceToInstrumentUri = resourceUri;
	}
	
	  
  private MessageConsole findConsole(String name) {
      ConsolePlugin plugin = ConsolePlugin.getDefault();
      IConsoleManager conMan = plugin.getConsoleManager();
      IConsole[] existing = conMan.getConsoles();
      for (int i = 0; i < existing.length; i++)
         if (name.equals(existing[i].getName()))
            return (MessageConsole) existing[i];
      //no console found, so create a new one
      MessageConsole myConsole = new MessageConsole(name, null);
      conMan.addConsoles(new IConsole[]{myConsole});
      return myConsole;
   }
  
  
  protected java.net.URI getFileURI(String fileName) throws URISyntaxException {

	java.net.URI uri= URIUtil.fromString("file:"+fileName);
	
	if (uri.isAbsolute())
		return uri;
	
//	uri = TransformUMLRTModel.class.getClassLoader().getResource("../" + fileName).toURI();
	uri = this.getClass().getClassLoader().getResource("../" + fileName).toURI();
	return uri;
  }

}