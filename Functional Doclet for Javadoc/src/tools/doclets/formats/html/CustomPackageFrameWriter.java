package tools.doclets.formats.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.CustomAllClassesFrameWriter;
import com.sun.tools.doclets.formats.html.CustomAllClassesFrameWriter.ListData;
import com.sun.tools.doclets.formats.html.LinkInfoImpl;
import com.sun.tools.doclets.formats.html.PackageFrameWriter;
import com.sun.tools.doclets.formats.html.markup.HtmlConstants;
import com.sun.tools.doclets.formats.html.markup.HtmlStyle;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.formats.html.markup.StringContent;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.Table;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder.TableEntry;
import com.sun.tools.doclets.internal.toolkit.util.DocPaths;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import com.sun.tools.doclets.internal.toolkit.util.Util;

public class CustomPackageFrameWriter extends PackageFrameWriter {
	
    private PackageDoc packageDoc;

    public CustomPackageFrameWriter(ConfigurationImpl configuration,
                              PackageDoc packageDoc)
                              throws IOException {
    	super(configuration, packageDoc);
        this.packageDoc = packageDoc;
    }

 
    public static void generate(ConfigurationImpl configuration, PackageDoc packageDoc) {
    	CustomPackageFrameWriter packgen;
        try {
            packgen = new CustomPackageFrameWriter(configuration, packageDoc);
            String pkgName = Util.getPackageName(packageDoc);
            Content body = packgen.getBody(false, packgen.getWindowTitle(pkgName));
            Content pkgNameContent = new StringContent(pkgName);
            Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, HtmlStyle.bar,
                    packgen.getTargetPackageLink(packageDoc, "classFrame", pkgNameContent));
            body.addContent(heading);
            packgen.addClassListing(body); 
            packgen.printHtmlDocument(
                    configuration.metakeywords.getMetaKeywords(packageDoc), false, body);
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                    "doclet.exception_encountered",
                    exc.toString(), DocPaths.PACKAGE_FRAME.getPath());
            throw new DocletAbortException(exc);
        }
    }

    public static boolean isAppCore(Doc doc) {
        if (doc instanceof ProgramElementDoc) {
            if (((ProgramElementDoc)doc).containingPackage().tags("custom.core").length > 0)
                return true;
        }
        return doc.tags("custom.core").length > 0;
    }

    public static boolean isNested(Doc doc) {
    	return doc instanceof ClassDoc && doc.name().contains(".");
    }
    
    @Override
	protected void addClassListing(Content contentTree) {
    	ClassDoc[] everything =  packageDoc.isIncluded() ? packageDoc.allClasses(true) : configuration.classDocCatalog.allClasses(Util.getPackageName(packageDoc));
    	everything = Util.filterOutPrivateClasses(everything, configuration.javafx);
    	Arrays.sort(everything);
    	 ArrayList<ClassDoc> core = new ArrayList<ClassDoc>();
	   	 ArrayList<ClassDoc> throwables = new ArrayList<ClassDoc>();
	   	 ArrayList<ClassDoc> nonCore = new ArrayList<ClassDoc>();
	   	ArrayList<ClassDoc> annotations = new ArrayList<ClassDoc>();
	   	 for (Doc doc : everything) {
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
         addClassKindListing(table, contentTree);
         
         table = CustomPackageSummaryBuilder.buildTableStructure(nonCore.toArray(new ClassDoc[nonCore.size()]), 
        		 "Types",
        		 "Type Names and Descriptions",
        		 new String[] {"Type Name", "Description"});
         addClassKindListing(table, contentTree);
         table = CustomPackageSummaryBuilder.buildTableStructure(throwables.toArray(new ClassDoc[throwables.size()]), 
        		 "Throwables",
        		 "Throwable Names and Descriptions",
        		 new String[] {"Type Name", "Description"});
         addClassKindListing(table, contentTree);
         table = CustomPackageSummaryBuilder.buildTableStructure(annotations.toArray(new ClassDoc[annotations.size()]), 
          		 "Annotations",
          		 "Annotation Type Names and Descriptions",
          		 new String[] {"Type Name", "Description"});
    }
    
    int id;
    
    protected void addClassKindListing(Table table, Content contentTree) {
    	if(table.entries.size() > 0) {
    		if(id == 0) {
    			contentTree.addContent(new RawHtml(FunctionalDoclet.EXPAND_COLLAPSE_SCRIPTS));
			}
    		String idPrefix ="id" + ++id;
    		ListData listData = new ListData(idPrefix, configuration) {
    			@Override
    			public Content getLink(TableEntry entry) {
    				return CustomPackageFrameWriter.this.getLink(new LinkInfoImpl(configuration, LinkInfoImpl.Kind.PACKAGE_FRAME, entry.classDoc).label(entry.relativeName).target("classFrame"));
    			}
    		};
    		HtmlTree ul = new HtmlTree(HtmlTag.UL);
            ul.setTitle(new RawHtml(table.label));
    		CustomAllClassesFrameWriter.addContents(table.entries, listData, ul, 0);
    		Content heading = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING, true, new RawHtml(table.label));
    		Content div = HtmlTree.DIV(HtmlStyle.indexContainer, heading);
    		Content toggleAllContent = listData.getToggleAllContent();
    		if(toggleAllContent != null) {
    			div.addContent(toggleAllContent);
    		}
    		div.addContent(ul);
            contentTree.addContent(div);
    	}
    }
}
