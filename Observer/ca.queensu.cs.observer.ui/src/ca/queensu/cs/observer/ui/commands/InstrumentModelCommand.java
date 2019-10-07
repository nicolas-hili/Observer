package ca.queensu.cs.observer.ui.commands;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.FileUtil;
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
import org.eclipse.papyrusrt.umlrt.core.utils.CapsuleUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import ca.queensu.cs.observer.ui.Activator;
import ca.queensu.cs.observer.ui.console.ObserverConsole;

public class InstrumentModelCommand {// extends RecordingCommand {

	/* The Marker id */
	private static final String MARKER = "ca.queensu.cs.observer.capsule.marker";

	private Resource resourceToInstrument;
	private org.eclipse.emf.common.util.URI resourceToInstrumentUri;
	
	private TransactionalEditingDomain domain;
	
	protected IEolModule module;//IEolExecutableModule module;
	private Model model;

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
		
		
		ObserverConsole console = ObserverConsole.getInstance();
		console.write("Creating Epsilon module...");
		
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
		console.write("Done.\n");
		console.write("Instrumenting... ");
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
	
	/**
	 * Convenience method returning the IResource corresponding to a Resource
	 *
	 * @param resource
	 *            The Resource from which the corresponding IResource has to be retrieved
	 * @return the IResource corresponding to the Resource
	 */
	public static IResource getIResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		String uriPath = resource.getURI().toPlatformString(true);
		if (uriPath == null) {
			return null;
		}
		IResource iresource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uriPath));
		if (iresource != null) {
			if (iresource.exists()) {
				return iresource;
			}
		}
		return null;
	}
	
	private boolean hasObserveMarker(Element element) {
		IResource iresource = getIResource(element.eResource());
		try {
			IMarker[] markers = iresource.findMarkers(MARKER, false, IResource.DEPTH_ZERO);
			return markers.length == 1;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
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
		
		
		
		String communication_include_file = "",
				communication_source_file = "",
				serialization_include_file = "",
				serialization_source_file = "";
		
		// Retrieve the path to the header and include files
		//ObserverConsole console = ObserverConsole.getInstance();
		
		try {
			
			String communicationPluginName = ((RegistryContributor)communicationConfiguration.getContributor()).getActualName();
			Bundle plugin = Platform.getBundle (communicationPluginName);

			communication_include_file = this.getFileURL(communicationConfiguration.getAttribute("cpp_include_file"), plugin).toString();
			communication_source_file = this.getFileURL(communicationConfiguration.getAttribute("cpp_source_file"), plugin).toString();
	
			String serializationPluginName = ((RegistryContributor)serializationConfiguration.getContributor()).getActualName();
			plugin = Platform.getBundle (serializationPluginName);
			serialization_include_file = this.getFileURI(serializationConfiguration.getAttribute("cpp_include_file"), plugin).toString();
			serialization_source_file = this.getFileURI(serializationConfiguration.getAttribute("cpp_source_file"), plugin).toString();

		} catch (InvalidRegistryObjectException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*		String pluginLocation = plugin.getLocation().toString().substring(15); 
		console.write("\n");
		console.write(pluginLocation);
		String communication_include_file = pluginLocation +"/"+ communicationConfiguration.getAttribute("cpp_include_file");
		console.write("\n");
		console.write(communication_include_file);
		String communication_src_file = pluginLocation + "/" + communicationConfiguration.getAttribute("cpp_source_file");

		String serializationPluginName = ((RegistryContributor)serializationConfiguration.getContributor()).getActualName();
		plugin = Platform.getBundle (serializationPluginName);
		pluginLocation = plugin.getLocation().toString().substring(15); 
		String serialization_include_file = pluginLocation + "/" + serializationConfiguration.getAttribute("cpp_include_file");
		String serialization_src_file = pluginLocation + "/" + serializationConfiguration.getAttribute("cpp_source_file");
	*/	
		
		// Get all unobserved capsules
		String pes = model.getPackagedElements()
								  .stream()
								  .filter(pe -> pe instanceof Classifier && CapsuleUtils.isCapsule((Classifier)pe))
								  .filter(pe -> hasObserveMarker(pe))
								  .map(pe -> pe.getName())
								  .collect(Collectors.joining(","));

		module.getContext().getFrameStack().putGlobal(
			new Variable("method_src", communication_source_file, EolNativeType.Instance),
			new Variable("method_include", communication_include_file, EolNativeType.Instance),
			new Variable("serializer_include", serialization_include_file, EolNativeType.Instance),
			new Variable("serializer_src", serialization_source_file, EolNativeType.Instance), 
			new Variable("unobserved_capsules", pes, EolNativeType.Instance),
			new Variable("observerPath", "platform:/plugin/ca.queensu.cs.observer/libraries/observer.uml", EolNativeType.Instance)
		);
		
		
//		module.getContext().setOutputStream(console.getStream());
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
	
	protected java.net.URI getFileURI(String fileName) throws URISyntaxException {
		Bundle bundle = Activator.getDefault().getBundle();
		return getFileURI(fileName, bundle);
	}
	
	protected java.net.URL getFileURL(String fileName) throws URISyntaxException {
		Bundle bundle = Activator.getDefault().getBundle();
		return getFileURL(fileName, bundle);
	}
	
	protected String removeProtocol(String url) {
		return url.substring(url.indexOf("/"));
	}
	
	protected java.net.URL getFileURL(String fileName, Bundle bundle) throws URISyntaxException {
		ObserverConsole console = ObserverConsole.getInstance();
//		Path path = new Path(fileName);
//		URL url = bundle.getResource(fileName);
		String path = bundle.getLocation() + "/" + fileName;
		if (path.startsWith("reference:")) {
    		path = path.substring("reference:".length());
    	}
		URL url = null;
		
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		console.write("\n---");
		console.write("\nBundle: " + bundle.getSymbolicName());
		console.write("\nFile to retrieve: " + fileName);
    	console.write("\nURL: " + path);
    	console.write("\n---");
    	
		return url;
	}
  
	protected java.net.URI getFileURI(String fileName, Bundle bundle) throws URISyntaxException {
		ObserverConsole console = ObserverConsole.getInstance();
		Path path = new Path(fileName);
		URL fileURL = FileLocator.find(bundle, path, null);
		   
		
 //   	String url = bundle.getLocation();
 //   	url += "/"+fileName;
		try {
			fileURL = FileLocator.resolve(fileURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = fileURL.toString();
//		String url = bundle.getResource(fileName).toURI().toString();
    	console.write("\n---");
    	console.write("\nURL: " + url);
//    	console.write("\nprunned URL: " + url.substring(15));
    	
  /*  	System.out.println(url.substring(15));
    	String content = FileUtil.getFileContents(new File(url.substring(15)));
    	System.out.println(content); */
    	
    	
	//	java.net.URI res = bundle.getResource(fileName).toURI();
	//	URI uri = new URI(url.substring(10));
    //	url = url.substring(0, url.indexOf("/") - 1);
    	
    	URI uri = new URI(url);
    	
    	console.write("\nFormatted URL: " + url);
    	//console.write("\nPrunned URL: " + url.substring(10));
		console.write("\nBundle: " + bundle.getSymbolicName());
		console.write("\nFile to retrieve: " + fileName);
		console.write("\nState: " + bundle.getState());
		
//		console.write("\nURI: " + res );
		console.write("\n---");
		return uri;
//		return res;
	  }

  public void setModel(Model model) {
	this.model = model;
  }

}
