package com.sun.tools.doclets.formats.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.formats.html.markup.HtmlStyle;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.Table;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.TableEntry;
import com.sun.tools.doclets.internal.toolkit.util.Util;

import tools.doclets.formats.html.FunctionalDoclet;

public class CustomPackageWriter extends PackageWriterImpl {
	int rowCount;
	
	public CustomPackageWriter(ConfigurationImpl configuration,
            PackageDoc packageDoc, PackageDoc prev, PackageDoc next)
            throws IOException {
		super(configuration, packageDoc, prev, next);
	}
	
	
	public void addClassesSummaryTable(Table dataTable, Content summaryContentTree) {
		if(dataTable.entries.size() > 0) {
			//System.out.println();
			//System.out.println("building " + dataTable);
			//System.out.println();
			rowCount = 0;
			List<TableEntry> entries = dataTable.entries;
			Content caption = getTableCaption(new RawHtml(dataTable.label));
	        Content table = HtmlTree.TABLE(HtmlStyle.typeSummary, 0, 3, 0, dataTable.tableSummary, caption);
	        table.addContent(getSummaryTableHeader(dataTable.tableHeader, "col"));
	        Content tbody = new HtmlTree(HtmlTag.TBODY);
	        for (int i = 0; i < entries.size(); i++) {
	        	TableEntry entry = entries.get(i);
	        	ClassDoc classDoc = entry.classDoc;
	            if (!Util.isCoreClass(classDoc) ||
	                !configuration.isGeneratedDoc(classDoc)) {
	                continue;
	            }
	            LinkInfoImpl link = new LinkInfoImpl(configuration, LinkInfoImpl.Kind.PACKAGE, classDoc);
	            link.label(entry.relativeName);
	            Content classContent = getLink(link);
	            
	            Content icon = getImageIcon(classDoc);
	            Content entryContent = HtmlTree.DIV(icon);
	            entryContent.addContent(classContent);
	            
	            
	            Content tdClass = HtmlTree.TD(HtmlStyle.colFirst, entryContent);
	            HtmlTree tr = HtmlTree.TR(tdClass);
	            if (++rowCount % 2 == 0)
	                tr.addStyle(HtmlStyle.altColor);
	            else
	                tr.addStyle(HtmlStyle.rowColor);
	            HtmlTree tdClassDescription = new HtmlTree(HtmlTag.TD);
	            tdClassDescription.addStyle(HtmlStyle.colLast);
	            if (Util.isDeprecated(classDoc)) {
	                tdClassDescription.addContent(deprecatedLabel);
	                if (classDoc.tags("deprecated").length > 0) {
	                    addSummaryDeprecatedComment(classDoc, classDoc.tags("deprecated")[0], tdClassDescription);
	                }
	            }
	            else {
	                addSummaryComment(classDoc, tdClassDescription);
	            }
	            tr.addContent(tdClassDescription);
	            tbody.addContent(tr);
	            addSubTable(entry.nestedInterfaces, tbody, 1);
	            addSubTable(entry.nestedClasses, tbody, 1);
	            addSubTable(entry.nestedEnums, tbody, 1);
	            addSubTable(entry.nestedExceptions, tbody, 1);
	            addSubTable(entry.nestedErrors, tbody, 1);
	            
	        }
	        table.addContent(tbody);
	        Content li = HtmlTree.LI(HtmlStyle.blockList, table);
	        summaryContentTree.addContent(li);
	        
		}
	}
	
   public static Content getStyleIcon(ClassDoc classDoc) {
    	String style;
    	if(classDoc.isAnnotationType()) {
    		style = FunctionalDoclet.ANNOTATION_DIV;
    	} else if(classDoc.isEnum()) {
    		style = FunctionalDoclet.ENUM_DIV;
    	} else if(classDoc.isInterface()) {
    		style = FunctionalDoclet.INTERFACE_DIV;
    	} else {
    		style = FunctionalDoclet.CLASS_DIV;
    	}
    	return new RawHtml(style);
    }
   
   public static Content getImageIcon(ClassDoc classDoc) {
   	String img;
   	if(classDoc.isAnnotationType()) {
   		img = FunctionalDoclet.ANNOTATION_IMG;
   	} else if(classDoc.isEnum()) {
   		img = FunctionalDoclet.ENUM_IMG;
   	} else if(classDoc.isInterface()) {
   		img = FunctionalDoclet.INTERFACE_IMG;
   	} else {
   		img = FunctionalDoclet.CLASS_IMG;
   	}
   	return new RawHtml(img);
   }
    
	void addSubTable(List<TableEntry> entries, Content tbody, int indentCount) {
		if(entries.size() > 0) {
	        for (int i = 0; i < entries.size(); i++) {
	        	TableEntry entry = entries.get(i);
	        	ClassDoc classDoc = entry.classDoc;
	            if (!Util.isCoreClass(classDoc) ||
	                !configuration.isGeneratedDoc(classDoc)) {
	                continue;
	            }
	            LinkInfoImpl link = new LinkInfoImpl(configuration, LinkInfoImpl.Kind.PACKAGE, classDoc);
	            link.label(entry.relativeName);
	            Content classContent = getLink(link);
	             Content icon = getImageIcon(classDoc);
	            Content entryContent = HtmlTree.DIV(icon);
	            entryContent.addContent(classContent);
	            for(int j = 0; j < indentCount; j++) {
	            	entryContent = HtmlTree.UL(HtmlStyle.inheritance, HtmlTree.LI(null, HtmlTree.UL(HtmlStyle.inheritance, HtmlTree.LI(entryContent))));
	            }
	            
	            Content tdClass = HtmlTree.TD(HtmlStyle.colFirst, entryContent);
	            HtmlTree tr = HtmlTree.TR(tdClass);
	            if (++rowCount % 2 == 0)
	                tr.addStyle(HtmlStyle.altColor);
	            else
	                tr.addStyle(HtmlStyle.rowColor);
	            HtmlTree tdClassDescription = new HtmlTree(HtmlTag.TD);
	            tdClassDescription.addStyle(HtmlStyle.colLast);
	            if (Util.isDeprecated(classDoc)) {
	                tdClassDescription.addContent(deprecatedLabel);
	                if (classDoc.tags("deprecated").length > 0) {
	                    addSummaryDeprecatedComment(classDoc, classDoc.tags("deprecated")[0], tdClassDescription);
	                }
	            }
	            else {
	                addSummaryComment(classDoc, tdClassDescription);
	            }
	            tr.addContent(tdClassDescription);
	            tbody.addContent(tr);
	            addSubTable(entry.nestedInterfaces, tbody, indentCount + 1);
	            addSubTable(entry.nestedClasses, tbody, indentCount + 1);
	            addSubTable(entry.nestedEnums, tbody, indentCount + 1);
	            addSubTable(entry.nestedExceptions, tbody, indentCount + 1);
	            addSubTable(entry.nestedErrors, tbody, indentCount + 1);
	        } 
		}
	}
	
	 @Override
	public void addClassesSummary(ClassDoc[] classes, String label,
            String tableSummary, String[] tableHeader, Content summaryContentTree) {
        if(classes.length > 0) {
            Arrays.sort(classes);
            Content caption = getTableCaption(new RawHtml(label));
            Content table = HtmlTree.TABLE(HtmlStyle.typeSummary, 0, 3, 0,
                    tableSummary, caption);
            table.addContent(getSummaryTableHeader(tableHeader, "col"));
            Content tbody = new HtmlTree(HtmlTag.TBODY);
            for (int i = 0; i < classes.length; i++) {
                if (!Util.isCoreClass(classes[i]) ||
                    !configuration.isGeneratedDoc(classes[i])) {
                    continue;
                }
                Content classContent = getLink(new LinkInfoImpl(
                        configuration, LinkInfoImpl.Kind.PACKAGE, classes[i]));
                Content icon = getImageIcon(classes[i]);
	            Content entryContent = HtmlTree.DIV(icon);
	            entryContent.addContent(classContent);
	            Content tdClass = HtmlTree.TD(HtmlStyle.colFirst, entryContent);
                HtmlTree tr = HtmlTree.TR(tdClass);
                if (i%2 == 0)
                    tr.addStyle(HtmlStyle.altColor);
                else
                    tr.addStyle(HtmlStyle.rowColor);
                HtmlTree tdClassDescription = new HtmlTree(HtmlTag.TD);
                tdClassDescription.addStyle(HtmlStyle.colLast);
                if (Util.isDeprecated(classes[i])) {
                    tdClassDescription.addContent(deprecatedLabel);
                    if (classes[i].tags("deprecated").length > 0) {
                        addSummaryDeprecatedComment(classes[i],
                            classes[i].tags("deprecated")[0], tdClassDescription);
                    }
                }
                else
                    addSummaryComment(classes[i], tdClassDescription);
                tr.addContent(tdClassDescription);
                tbody.addContent(tr);
            }
            table.addContent(tbody);
            Content li = HtmlTree.LI(HtmlStyle.blockList, table);
            summaryContentTree.addContent(li);
        }
    }
}


