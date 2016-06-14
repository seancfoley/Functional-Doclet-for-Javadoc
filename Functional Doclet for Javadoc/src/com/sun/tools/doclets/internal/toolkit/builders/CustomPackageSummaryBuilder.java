package com.sun.tools.doclets.internal.toolkit.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.formats.html.CustomPackageWriter;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.Util;

import tools.doclets.formats.html.CustomPackageFrameWriter;

public class CustomPackageSummaryBuilder {
	final Configuration configuration;

   private final PackageDoc packageDoc;

    private final CustomPackageWriter packageWriter;

    public CustomPackageSummaryBuilder(
            Configuration configuration,
            PackageDoc pkg,
            CustomPackageWriter packageWriter) {
    	this.configuration = configuration;
        this.packageDoc = pkg;
        this.packageWriter = packageWriter;
    }

    /**
     * Build the package summary.
     */
    public void build() throws IOException {
    	try {
    		buildPackageDoc(null);
    	} catch(Exception e) {
    		e.printStackTrace();
 
    	}
    }

    /**
     * Build the package documentation.
     *
     * @param node the XML element that specifies which components to document
     * @param contentTree the content tree to which the documentation will be added
     */
    public void buildPackageDoc(XMLNode node) throws Exception {
        Content contentTree = packageWriter.getPackageHeader(Util.getPackageName(packageDoc));
        buildContent(null, contentTree);
        packageWriter.addPackageFooter(contentTree);
        packageWriter.printDocument(contentTree);
        packageWriter.close();
        Util.copyDocFiles(configuration, packageDoc);
    }

    /**
     * Build the content for the package doc.
     *
     * @param node the XML element that specifies which components to document
     * @param contentTree the content tree to which the package contents
     *                    will be added
     */
    public void buildContent(XMLNode node, Content contentTree) {
        Content packageContentTree = packageWriter.getContentHeader();
        buildSummary(null, packageContentTree);
        buildPackageDescription(null, packageContentTree);
        buildPackageTags(null, packageContentTree);
        contentTree.addContent(packageContentTree);
    }

    /**
     * Build the package summary.
     *
     * @param node the XML element that specifies which components to document
     * @param packageContentTree the package content tree to which the summaries will
     *                           be added
     */
    public void buildSummary(XMLNode node, Content packageContentTree) {
    	Content summaryContentTree = packageWriter.getSummaryHeader();
        buildAllClassesSummary(summaryContentTree);
        buildAnnotationTypeSummary(null,summaryContentTree);
        packageContentTree.addContent(summaryContentTree);
    }
    
    public static class TableEntry {
    	public String relativeName;
		 public final ClassDoc classDoc;
		 public final List<TableEntry> nestedInterfaces = new ArrayList<TableEntry>();
		 public final List<TableEntry> nestedClasses = new ArrayList<TableEntry>();
		 public final List<TableEntry> nestedEnums = new ArrayList<TableEntry>();
		 public final List<TableEntry> nestedExceptions = new ArrayList<TableEntry>();
		 public final List<TableEntry> nestedErrors = new ArrayList<TableEntry>();
		 
		 TableEntry(ClassDoc classDoc) {
			 this.classDoc = classDoc;
			 relativeName = classDoc.name();
		 }
		 
		 void setRelativeName(String relativeName) {
			 this.relativeName = relativeName;
		 }
		 
		 void add(TableEntry entry) {
			 nestedClasses.add(entry);
		 }
		 
		 public String toString(String indent) {
	  		  StringBuilder builder = new StringBuilder();
	  		  builder.append(indent).append(classDoc.name()).append(System.lineSeparator());
	  		  indent += '\t';
	  		 // System.out.println("nested count " + (nestedInterfaces.size() + nestedClasses.size() + nestedEnums.size() + nestedExceptions.size() + nestedErrors.size()));
	  		  for(TableEntry entry : nestedInterfaces) {
	  			  builder.append(entry.toString(indent));
	  		  }
	  		  for(TableEntry entry : nestedClasses) {
	  			  builder.append(entry.toString(indent));
	  		  }
	  		  for(TableEntry entry : nestedEnums) {
	  			  builder.append(entry.toString(indent));
	  		  }
	  		  for(TableEntry entry : nestedExceptions) {
	  			  builder.append(entry.toString(indent));
	  		  }
	  		  for(TableEntry entry : nestedErrors) {
	  			  builder.append(entry.toString(indent));
	  		  }
	  		  return builder.toString();
	  	  }
	 }
    
    public static class Table {
  	  public final String label, tableSummary, tableHeader[];
  	public final List<TableEntry> entries;
  	
  	  Table(String label, String tableSummary, String tableHeader[], List<TableEntry> entries) {
  		  this.label = label;
  		  this.tableHeader = tableHeader;
  		  this.tableSummary = tableSummary;
  		  this.entries = entries;
  	  }
  	  
  	  @Override
	public String toString() {
  		  StringBuilder builder = new StringBuilder();
  		  for(TableEntry entry : entries) {
  			  builder.append(entry.toString(""));
  		  }
  		  return builder.toString();
  	  }
    }
	 
	 
    
    void buildAllClassesSummary(Content packageContentTree) {
    	ClassDoc[] everything =  packageDoc.isIncluded() ? packageDoc.allClasses(true) : configuration.classDocCatalog.allClasses(Util.getPackageName(packageDoc));
    	everything = Util.filterOutPrivateClasses(everything, configuration.javafx);
        Arrays.sort(everything);
    	//we have four groups.  Annotations are not handled here.  Exceptions and errors are grouped.  The rest is grouped into core and not core.
    	 ArrayList<ClassDoc> core = new ArrayList<ClassDoc>();
    	 ArrayList<ClassDoc> throwables = new ArrayList<ClassDoc>();
    	 ArrayList<ClassDoc> nonCore = new ArrayList<ClassDoc>();
    	 for (ClassDoc c : everything) {
        	 if(!c.isAnnotationType()) {
	        	 if(CustomPackageFrameWriter.isAppCore(c)) {
	        		 core.add(c);
	        	 } else if (c.isException() || c.isError()) {
	            	 throwables.add(c);
	             } else {
	            	 nonCore.add(c);
	             }
        	 }
         }
         Table table = buildTableStructure(core.toArray(new ClassDoc[core.size()]), 
        		 "Core Types",
        		 "Core Type Names and Descriptions",
        		 new String[] {"Type Name", "Description"});
         packageWriter.addClassesSummaryTable(table, packageContentTree);
         table = buildTableStructure(nonCore.toArray(new ClassDoc[nonCore.size()]), 
        		 "Classes, Interfaces, and Enums",
        		 "Class, Interface, and Enum Names and Descriptions",
        		 new String[] {"Type Name", "Description"});
         packageWriter.addClassesSummaryTable(table, packageContentTree);
         table = buildTableStructure(throwables.toArray(new ClassDoc[throwables.size()]), 
        		 "Exceptions and Errors",
        		 "Exception and Error Names and Descriptions",
        		 new String[] {"Type Name", "Description"});
         packageWriter.addClassesSummaryTable(table, packageContentTree);
    }
    
    public static Table buildTableStructure(ClassDoc classes[], String label, String tableSummary, String tableHeader[]) {
    	ArrayList<ClassDoc> allList = new ArrayList<ClassDoc>(classes.length);
    	allList.addAll(Arrays.asList(classes));
    	//Collections.sort(allList);
    	 HashMap<String, TableEntry> mapping = new HashMap<String, TableEntry>();
         List<TableEntry> roots = new ArrayList<TableEntry>(allList.size());
         top:
	    for(int i = 0; i < allList.size(); i++) {
	    	  ClassDoc classDoc = allList.get(i);
	    	  String name = classDoc.name();
	    	  String containerName = name;
	    	  TableEntry entry = new TableEntry(classDoc);
	    	  mapping.put(name, entry);
	    	  int nestedIndex = name.lastIndexOf('.');
	    	  while(nestedIndex > 0) {
		    	  containerName = containerName.substring(0, nestedIndex);
		    	  if(nestedIndex >= 0) {
		    		  TableEntry container = mapping.get(containerName);
		    		  if(container != null) {
		    			  String relativeName = name.substring(nestedIndex + 1);
		    			  entry.setRelativeName(relativeName);
		    			  container.add(entry);
		    			  
		    			  continue top;
		    		  }
		    		  nestedIndex = containerName.lastIndexOf('.');
		    		 continue;
		    	  }
		    	break;
	    	  }
	    	  
	    	  roots.add(entry);
	      }
         Table table = new Table(label, tableSummary, tableHeader, roots);
         return table;
    }

    /**
     * Build the summary for the annotation type in this package.
     *
     * @param node the XML element that specifies which components to document
     * @param summaryContentTree the summary tree to which the annotation type
     *                           summary will be added
     */
    public void buildAnnotationTypeSummary(XMLNode node, Content summaryContentTree) {
        String annotationtypeTableSummary =
                configuration.getText("doclet.Member_Table_Summary",
                configuration.getText("doclet.Annotation_Types_Summary"),
                configuration.getText("doclet.annotationtypes"));
        String[] annotationtypeTableHeader = new String[] {
            configuration.getText("doclet.AnnotationType"),
            configuration.getText("doclet.Description")
        };
        ClassDoc[] annotationTypes =
                packageDoc.isIncluded()
                        ? packageDoc.annotationTypes()
                        : configuration.classDocCatalog.annotationTypes(
                                Util.getPackageName(packageDoc));
        annotationTypes = Util.filterOutPrivateClasses(annotationTypes, configuration.javafx);
        if (annotationTypes.length > 0) {
        	packageWriter.addClassesSummary(
                    annotationTypes,
                    configuration.getText("doclet.Annotation_Types_Summary"),
                    annotationtypeTableSummary, annotationtypeTableHeader,
                    summaryContentTree);
        }
    }

    /**
     * Build the description of the summary.
     *
     * @param node the XML element that specifies which components to document
     * @param packageContentTree the tree to which the package description will
     *                           be added
     */
    public void buildPackageDescription(XMLNode node, Content packageContentTree) {
        if (configuration.nocomment) {
            return;
        }
        packageWriter.addPackageDescription(packageContentTree);
    }

    /**
     * Build the tags of the summary.
     *
     * @param node the XML element that specifies which components to document
     * @param packageContentTree the tree to which the package tags will be added
     */
    public void buildPackageTags(XMLNode node, Content packageContentTree) {
        if (configuration.nocomment) {
            return;
        }
        packageWriter.addPackageTags(packageContentTree);
    }
}

