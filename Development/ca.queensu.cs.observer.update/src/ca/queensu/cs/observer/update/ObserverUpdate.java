/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Description: Simple Java program used for updating the Observer after
 * extending it.
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca> - initial API and implementation
 ******************************************************************************/

package ca.queensu.cs.observer.update;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObserverUpdate {
	
	public ObserverUpdate() {
		
	}
	
	void update() {

		List<File> files = getCppFiles();

		ResourceSet rs = new ResourceSetImpl();
		UMLResourcesUtil.init(rs);
		
		
		// Get RTCppProfile
		Resource RTCppProfileResource = getRTCppProfileResource(rs);
		Profile RTCppProfile = (Profile) RTCppProfileResource.getContents().get(0);
		Stereotype artifactPropertiesStereotype = (Stereotype)RTCppProfile.getPackagedElement("ArtifactProperties");
		
		
		Resource res = getObserverResource(rs);
		Package observerPackage = (Package)res.getContents().get(0);
		Package internal = observerPackage.getNestedPackage("internal");
		
		System.out.print("Removing all contents of the internal package. ");
		internal.getPackagedElements().removeAll(internal.getPackagedElements());
		System.out.println("Done.");
		
		// trick: applying the RTCppProfile on the internal subpackage is mandatory
		// to be able to apply the stereotype on the artifact
		// Afterwards, the profile applied on internal can be manually removed
		// as the Observation package also contains the RTCppProfile profile
		if (internal.isProfileApplied(RTCppProfile))
			internal.unapplyProfile(RTCppProfile);
		
		internal.applyProfile(RTCppProfile);
		
		for (File file: files) {
			
			Boolean isSource = file.getName().endsWith("cc");
			Boolean isInclude = file.getName().endsWith("hh");
			
			if (!isSource && !isInclude)
				continue;
			
			String artifactName = file.getName().substring(0, file.getName().length()-3);
			Artifact artifact = (Artifact) internal.getPackagedElement(artifactName);
			if (artifact == null) {
				System.out.print("Creating artifact entitled: " + artifactName + ". ");
				artifact = UMLFactory.eINSTANCE.createArtifact();
				artifact.setName(artifactName);
				internal.getPackagedElements().add(artifact);
				artifact.applyStereotype(artifactPropertiesStereotype);
				System.out.println("Done.");
			}
			
			try {
				if (isSource) {
					System.out.print("Setting source of artifact entitled: " + artifactName + ". ");
					String sourceFile = String.join("\n", Files.readAllLines(file.toPath()));
					artifact.setValue(artifactPropertiesStereotype, "sourceFile", sourceFile);
					System.out.println("Done.");
				}
				else if (isInclude) {
					System.out.print("Setting include of artifact entitled: " + artifactName + ". ");
					String includeFile = String.join("\n", Files.readAllLines(file.toPath()));
					artifact.setValue(artifactPropertiesStereotype, "includeFile", includeFile);
					System.out.println("Done.");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		try {
			res.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Resource getRTCppProfileResource(ResourceSet rs) {
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		String[] fragments = location.getPath().split("/");
		int fragmentLenght = fragments.length - 1;
		String observerPath = "";
		
		for (int i = 0; i < fragmentLenght; i++) {
			observerPath += fragments[i] + "/";
		}
		observerPath += "model/";
		observerPath += "RTCppProperties.profile.uml";
		
		URI uri = URI.createFileURI(observerPath);
		
		return rs.getResource(uri, true);
	}

	List<File> getCppFiles() {
		
		List<File> files = new ArrayList<File>();
		
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		String[] fragments = location.getPath().split("/");
		int fragmentLenght = fragments.length - 2;
		String observerPath = "";
		
		for (int i = 0; i < fragmentLenght; i++) {
			observerPath += fragments[i] + "/";
		}
		observerPath += "../Observer/ca.queensu.cs.observer.cpp/";
		
		File observerSourceFolder = new File(observerPath + "src/");
		File observerIncludeFolder = new File(observerPath + "include/");
		
		if(observerSourceFolder.isDirectory() && observerIncludeFolder.isDirectory()) {
		
			for (final File srcFile : observerSourceFolder.listFiles()) {

				if (srcFile.getName().equals("Observer.cc"))
					continue;
				
				if (srcFile.getName().equals("ObserverTest.cc"))
					continue;
				
				files.add(srcFile);				
				
			}
			
			for (final File headerFile : observerIncludeFolder.listFiles()) {

				if (headerFile.getName().equals("Observer.hh"))
					continue;
				
				if (headerFile.getName().equals("ObserverTest.hh"))
					continue;
				
				files.add(headerFile);
			}
			
			return files;
		}
		else {
			System.out.println(observerPath + ": invalid path");
		}
		return Collections.emptyList();
	}
	
	Resource getObserverResource(ResourceSet rs) {
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		String[] fragments = location.getPath().split("/");
		int fragmentLenght = fragments.length - 2;
		String observerPath = "";
		
		for (int i = 0; i < fragmentLenght; i++) {
			observerPath += fragments[i] + "/";
		}
		observerPath += "../Observer/ca.queensu.cs.observer/";
		observerPath += "libraries/";
		observerPath += "observer.uml";
		
		URI uri = URI.createFileURI(observerPath);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("uml", new UMLResourceFactoryImpl());
		return rs.getResource(uri, true);
		
	}
	
	public static void main(String[] args) {
		ObserverUpdate observerUpdate = new ObserverUpdate();
		observerUpdate.update();
	}
}
