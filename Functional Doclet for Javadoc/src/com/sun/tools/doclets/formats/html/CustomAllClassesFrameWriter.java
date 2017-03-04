package com.sun.tools.doclets.formats.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.sun.tools.doclets.formats.html.markup.HtmlConstants;
import com.sun.tools.doclets.formats.html.markup.HtmlStyle;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.Table;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.TableEntry;
import com.sun.tools.doclets.internal.toolkit.util.DocPath;
import com.sun.tools.doclets.internal.toolkit.util.DocPaths;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import com.sun.tools.doclets.internal.toolkit.util.IndexBuilder;
import com.sun.tools.doclets.internal.toolkit.util.Util;

import tools.doclets.formats.html.FunctionalDoclet;
import tools.doclets.formats.html.CustomPackageFrameWriter;

public class CustomAllClassesFrameWriter extends AllClassesFrameWriter {
	public CustomAllClassesFrameWriter(ConfigurationImpl configuration,
            DocPath filename, IndexBuilder indexbuilder)
         throws IOException {
		super(configuration, filename, indexbuilder);
	}

	public static void generate(ConfigurationImpl configuration, IndexBuilder indexbuilder) {
		try {
			CustomAllClassesFrameWriter allclassgen = new CustomAllClassesFrameWriter(configuration,  DocPaths.ALLCLASSES_FRAME, indexbuilder);
			allclassgen.buildAllClassesFile(true);
			allclassgen.close();
			allclassgen = new CustomAllClassesFrameWriter(configuration, DocPaths.ALLCLASSES_NOFRAME, indexbuilder);
			allclassgen.buildAllClassesFile(false);
			allclassgen.close();
		} catch (IOException exc) {
			throw new DocletAbortException(exc);
		}
	}
	
    @Override
	protected void buildAllClassesFile(boolean wantFrames) throws IOException {
        String label = configuration.getText("doclet.All_Classes");
        Content body = getBody(false, getWindowTitle(label));
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, HtmlStyle.bar, allclassesLabel);
        body.addContent(heading);
        addAllClasses(body, wantFrames);
        printHtmlDocument(null, false, body);
    }
	
    
	@Override
	protected void addAllClasses(Content content, boolean wantFrames) {
		ArrayList<Doc> allClasses = new ArrayList<Doc>();
		//Note this gets the list of classes per letter
        for (int i = 0; i < indexbuilder.elements().length; i++) {
            Character unicode = (Character)((indexbuilder.elements())[i]);
            List<Doc> letterList = indexbuilder.getMemberList(unicode);
            allClasses.addAll(letterList);
        }
        Collections.sort(allClasses);
        ArrayList<ClassDoc> core = new ArrayList<ClassDoc>();
	   	 ArrayList<ClassDoc> throwables = new ArrayList<ClassDoc>();
	   	 ArrayList<ClassDoc> nonCore = new ArrayList<ClassDoc>();
	   	ArrayList<ClassDoc> annotations = new ArrayList<ClassDoc>();
	   	 for (Doc doc : allClasses) {
	   		 ClassDoc c = (ClassDoc) doc;
	       	 if(c.isAnnotationType()) {
	       		annotations.add(c);
	       	 }else if(CustomPackageFrameWriter.isAppCore(c)) {
        		 core.add(c);
        	 } else if (c.isException() || c.isError()) {
            	 throwables.add(c);
             } else {
            	 nonCore.add(c);
             }
        }
	   	 
	   	 
	   	Table table = CustomPackageSummaryBuilder.buildTableStructure(core.toArray(new ClassDoc[core.size()]), 
       		 "Core Types",
       		 "Core Type Names and Descriptions",
       		 new String[] {"Type Name", "Description"});
	   	addList(table, wantFrames, content);
	   	table = CustomPackageSummaryBuilder.buildTableStructure(nonCore.toArray(new ClassDoc[nonCore.size()]), 
       		 "Interfaces, Classes, and Enums",
       		 "Interface, Class, and Enum Names and Descriptions",
       		 new String[] {"Type Name", "Description"});
	   	addList(table, wantFrames, content);
	   	table = CustomPackageSummaryBuilder.buildTableStructure(throwables.toArray(new ClassDoc[throwables.size()]), 
       		 "Throwables",
       		 "Throwable Names and Descriptions",
       		 new String[] {"Type Name", "Description"});
	   	addList(table, wantFrames, content);
	   	table = CustomPackageSummaryBuilder.buildTableStructure(annotations.toArray(new ClassDoc[annotations.size()]), 
          		 "Annotations",
          		 "Annotation Type Names and Descriptions",
          		 new String[] {"Type Name", "Description"});
	   	addList(table, wantFrames, content);
    }

	protected void addList(Table table, final boolean wantFrames, Content content) {
		if(table.entries.size() > 0) {
			if(id == 0) {
				content.addContent(new RawHtml(FunctionalDoclet.EXPAND_COLLAPSE_SCRIPTS));
			}
			String idPrefix ="id" + ++id;
			ListData listData = new ListData(idPrefix, configuration) {
				@Override
				public Content getLink(TableEntry entry) {
					ClassDoc cd = entry.classDoc;
					 Content linkContent;
					if (wantFrames) {
		                linkContent = CustomAllClassesFrameWriter.this.getLink(new LinkInfoImpl(configuration, LinkInfoImpl.Kind.ALL_CLASSES_FRAME, cd).label(entry.relativeName).target("classFrame"));
		            } else {
		                linkContent = CustomAllClassesFrameWriter.this.getLink(new LinkInfoImpl(configuration, LinkInfoImpl.Kind.DEFAULT, cd).label(entry.relativeName));
		            }
					return linkContent;
				}
			};
			
			HtmlTree ul = new HtmlTree(HtmlTag.UL);
	        ul.setTitle(new RawHtml(table.label));
			addContents(table.entries, listData, ul, 0);
	        Content heading = HtmlTree.HEADING(HtmlConstants.PACKAGE_HEADING, true, new RawHtml(table.label));
			Content div = HtmlTree.DIV(HtmlStyle.indexContainer, heading);
			Content toggleAllContent = listData.getToggleAllContent();
			if(toggleAllContent != null) {
				div.addContent(toggleAllContent);
			}
			div.addContent(ul);
	        content.addContent(div);
		}
	}
	
	 int id;
	
	public static abstract class ListData {
		String idPrefix;
		int rowIndex;
		String toggleAllString = "";
		String toggleAllVar;
		ConfigurationImpl configuration;
		
		public ListData(String idPrefix, ConfigurationImpl configuration) {
			this.idPrefix = idPrefix;
			toggleAllVar = idPrefix + "toggle";
			this.configuration = configuration;
		}
		
		public Content getToggleAllContent() {
			if(toggleAllString.length() > 0) {
				String toggleStr = toggleAllVar + " = !" + toggleAllVar + "; ";
				String funcName = idPrefix + "toggleAll";
				RawHtml rawHtml = new RawHtml(
						"<script>var " + toggleAllVar + " = false;\n" +
							"function " + funcName + "() {\n" +
							toggleAllString +
							"}\n" +
						"</script>" + 
						"<a href=# onclick=\"" + toggleStr + funcName + "(); this.innerHTML=" + toggleAllVar + " ? 'Hide Nested' : 'Show Nested'; return false;\">Show Nested</a>" +
						"<script>addWindowOnload(" + funcName + ");</script><br><br>"
						);
				return rawHtml;
			}
			return null;
		}
		
		public abstract Content getLink(TableEntry entry);
		
	}
	
	public static List<Integer> addContents(List<TableEntry> entries, ListData listData, Content content, int indentCount) {
		 List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < entries.size(); i++) {
       	TableEntry entry = entries.get(i);
       	ClassDoc cd = entry.classDoc;
           if (!Util.isCoreClass(cd)  || !listData.configuration.isGeneratedDoc(cd)) {
               continue;
           }
           Content linkContent = listData.getLink(entry);
           Content icon = CustomPackageWriter.getImageIcon(cd);
           Content entryContent = HtmlTree.SPAN(HtmlTree.EMPTY);
           HtmlTree liPrefix = HtmlTree.SPAN(HtmlTree.EMPTY);
           entryContent.addContent(liPrefix);
           entryContent.addContent(icon);
           entryContent.addContent(linkContent);
           if(indentCount > 0) {
        	   //for some reason, the first li does not indent at all, only the ones after that, so we need this extra one
        	   entryContent = HtmlTree.UL(HtmlStyle.inheritance, HtmlTree.LI(entryContent));
	           for(int j = 0; j < indentCount; j++) {
	        	   entryContent = HtmlTree.UL(HtmlStyle.inheritance, HtmlTree.LI(entryContent));
	           }
           }
           int idIndex = listData.rowIndex++;
           result.add(idIndex);
           HtmlTree li = HtmlTree.LI(entryContent);
           content.addContent(li);
           int nestedStartIndex = listData.rowIndex;
           List<Integer> nestedRows = new ArrayList<Integer>();
           nestedRows.addAll(addContents(entry.nestedInterfaces, listData, content, indentCount + 1));
           nestedRows.addAll(addContents(entry.nestedClasses, listData, content, indentCount + 1));
           nestedRows.addAll(addContents(entry.nestedEnums, listData, content, indentCount + 1));
           nestedRows.addAll(addContents(entry.nestedExceptions, listData, content, indentCount + 1));
           nestedRows.addAll(addContents(entry.nestedErrors, listData, content, indentCount + 1));
           int nestedEndIndex = listData.rowIndex;
           String liIdPrefix = listData.idPrefix + "li";
           li.addAttr(HtmlAttr.ID, liIdPrefix + idIndex);
           if(nestedRows.size() > 0) {
        	   String expandString = "", collapseString = "";
        	   String imgId = listData.idPrefix + "img" + idIndex;
        	   String showContentVar = "show" + idIndex;
        	   int directNestedIndex = nestedRows.remove(0);
        	   //what the javascript does: it for a given li entry, it checks the entry below to see if it is expanded or collapsed.  
        	   //If collapsed, it will expand all, it expanded, it will collapse all.  It has also switched the image of this first entry.
        	   //When expanding, it will show only the directly nested.
        	   //When collapsing, it will collapse all the nested: the directly nested, the nested of those, the nested of those, and so on.
        	   //The collapsing will also switch the images.
        	   String checkFirst = "var " + showContentVar + " = toggleContent('" + liIdPrefix + directNestedIndex + "', " + 
		    		   "'" + imgId + "', '" + FunctionalDoclet.DOWN_ARROW_SRC + "', '" + FunctionalDoclet.RIGHT_ARROW_SRC + "', 'collapse', 'expand');";
		       listData.toggleAllString += " showContent('" + liIdPrefix + directNestedIndex + "', " + listData.toggleAllVar + ", " + 
		    		   "'" + imgId + "', '" + FunctionalDoclet.DOWN_ARROW_SRC + "', '" + FunctionalDoclet.RIGHT_ARROW_SRC + "', 'collapse', 'expand');"; 
		       for(int nextDirectNestedIndex : nestedRows) {
		    	   expandString += " showContent('" + liIdPrefix + nextDirectNestedIndex + "', true);";
	       		   listData.toggleAllString += " showContent('" + liIdPrefix + nextDirectNestedIndex + "', " + listData.toggleAllVar + ");";
	       	   }
		       for(int nextNestedIndex = nestedStartIndex; nextNestedIndex < nestedEndIndex; nextNestedIndex++) {
		    	   collapseString += " showContentCheckImage('" + liIdPrefix + nextNestedIndex + "', false, '" + listData.idPrefix + "img" + nextNestedIndex + "', '" + FunctionalDoclet.DOWN_ARROW_SRC + "', 'collapse', '" + FunctionalDoclet.RIGHT_ARROW_SRC + "', 'expand');";
		       }
		       String onClickString = checkFirst + "\n if(" + showContentVar + ") {\n" + expandString + "\n} else {\n" + collapseString + "\n}";
		         RawHtml rawHtml = new RawHtml("<a onclick=\"" + onClickString + "\"><img title=\"collapse\" id=\"" + imgId + "\" src=\"" + FunctionalDoclet.DOWN_ARROW_SRC + "\" style=\"vertical-align: text-top;\" ></a>");
		        liPrefix.addContent(rawHtml);
	         } else {
	           	liPrefix.addContent(new RawHtml(FunctionalDoclet.BLANK_ARROW_IMG));
	         }
	       }
		   return result;
	   }
}
