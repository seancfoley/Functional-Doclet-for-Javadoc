package tools.doclets.formats.html;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLConnection;

import javax.xml.bind.DatatypeConverter;

import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.CustomAllClassesFrameWriter;
import com.sun.tools.doclets.formats.html.CustomHelpWriter;
import com.sun.tools.doclets.formats.html.CustomPackageWriter;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.formats.html.PackageFrameWriter;
import com.sun.tools.doclets.formats.html.PackageIndexFrameWriter;
import com.sun.tools.doclets.formats.html.PackageTreeWriter;
import com.sun.tools.doclets.internal.toolkit.AbstractDoclet;
import com.sun.tools.doclets.internal.toolkit.builders.AbstractBuilder;
import com.sun.tools.doclets.internal.toolkit.builders.CustomPackageSummaryBuilder;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.doclets.internal.toolkit.util.IndexBuilder;
import com.sun.tools.doclets.internal.toolkit.util.Util;

public class FunctionalDoclet extends HtmlDoclet {

	
	static InputStream toMarkable(InputStream stream) {
		if(stream.markSupported()) {
			return stream;
		}
		return new BufferedInputStream(stream);
	}
	
	static byte[] toBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buffer[] = new byte[1024];
		int num;
		while((num = stream.read(buffer,  0,  buffer.length)) != -1) {
			outputStream.write(buffer,  0,  num);
		}
		return outputStream.toByteArray();
	}
	
	static String toHexData(byte bytes[]) {
		return DatatypeConverter.printBase64Binary(bytes);
	}
	
	static String getImageSrc(String path) throws IOException {
		try (InputStream stream = FunctionalDoclet.class.getResourceAsStream(path)) {
			InputStream markableStream = toMarkable(stream);
			String mimeType = URLConnection.guessContentTypeFromStream(markableStream);
			String imageData = toHexData(toBytes(markableStream));
			return "data:" + mimeType + ";base64," + imageData;
		}
	}
	
	static String getImage(String src)  {
		return "<img src=\"" + src + "\" style=\"vertical-align: text-top;\" >";
	}
	
	static String getImageStyle(String imgString)  {
		String cssString = "url(" + imgString + ")";
		String style = "background: " + cssString + " no-repeat; width: 16px; display: inline-block;";//16 pixels
		return "<div style=\"" + style + "\">&nbsp;</div>";
	}
	
	public static String RIGHT_ARROW_SRC, DOWN_ARROW_SRC, BLANK_ARROW_SRC;
	public static String CLASS_IMG, INTERFACE_IMG, ENUM_IMG, ANNOTATION_IMG, BLANK_ARROW_IMG;
	public static String CLASS_DIV, INTERFACE_DIV, ENUM_DIV, ANNOTATION_DIV;
	
	static {
		try {
			RIGHT_ARROW_SRC = getImageSrc("/tools/doclets/formats/html/right_arrow_icon.png");
			DOWN_ARROW_SRC = getImageSrc("/tools/doclets/formats/html/down_arrow_icon.png");
			BLANK_ARROW_SRC = getImageSrc("/tools/doclets/formats/html/blank_arrow_icon.png");
			BLANK_ARROW_IMG = getImage(BLANK_ARROW_SRC);
			String src = getImageSrc("/tools/doclets/formats/html/class_obj.png");
			CLASS_IMG = getImage(src);
			CLASS_DIV = getImageStyle(src);
			src = getImageSrc("/tools/doclets/formats/html/int_obj.png");
			INTERFACE_IMG = getImage(src);
			INTERFACE_DIV = getImageStyle(src);
			src = getImageSrc("/tools/doclets/formats/html/enum_obj.png");
			ENUM_IMG = getImage(src);
			ENUM_DIV = getImageStyle(src);
			src = getImageSrc("/tools/doclets/formats/html/annotation_obj.png");
			ANNOTATION_IMG = getImage(src);
			ANNOTATION_DIV = getImageStyle(src);
		} catch(IOException e) {
			throw new Error(e);
		}		
	}
	
	/*
	 
 */
	public static String EXPAND_COLLAPSE_SCRIPTS = 
		"<script type=text/javascript>" + 
		"function addWindowOnload(func) {\n" +
		" var currentOnload = window.onload;\n" +
		" if (!currentOnload || typeof currentOnload != 'function') {\n" +
		"  window.onload = func;\n" +
		" } else window.onload = function() {\n" +
		"  currentOnload();\n" +
		"  func();\n" +
		" }\n" +
		"}\n" +
		"function switchOutImg(imageId, matchSrc, matchTitle, newSrc, newTitle) {\n" +
		" var img = document.getElementById(imageId);\n" +
		" if(img && img.title == matchTitle) { img.src = newSrc; img.title = newTitle; }\n" + 
		"}\n" +
		"function showContentCheckImage(contentDivId, show, imageId, matchSrc, matchTitle, newSrc, newTitle) {\n" +
		" var div = document.getElementById(contentDivId);\n" + //the div enclosing the content to be shown or hidden
		" div.style.display = show ? 'block' : 'none';\n" +
		" if(imageId) switchOutImg(imageId, matchSrc, matchTitle, newSrc, newTitle);\n" +
		"}\n" +
		"function showContent(contentDivId, show, imageId, showSrc, hideSrc, showTitle, hideTitle) {\n" +
		" var div = document.getElementById(contentDivId);\n" + //the div enclosing the content to be shown or hidden
		" div.style.display = show ? 'block' : 'none';\n" +
		" if(imageId) {\n" +
		" 	var img = document.getElementById(imageId);\n" +  //the toggle image
		" 	img.src = show ? showSrc : hideSrc;\n" +
		" 	img.title = show ? showTitle : hideTitle;\n" + 
		" }\n" +
		"}\n" +
		"function toggleContent(contentDivId, imageId, hideSrc, showSrc, hideTitle, showTitle) {\n" +
		" var div = document.getElementById(contentDivId);\n" + //the div enclosing the content to be shown or hidden
		" var show = (div.style.display == 'none');\n" +
		" showContent(contentDivId, show, imageId, hideSrc, showSrc, hideTitle, showTitle);\n" +
		" return show;\n" +
		"}"  + 
		"</script>";
	
	//static boolean OLD_STYLE = true;
	static boolean OLD_STYLE;
	
	public static boolean start(RootDoc root) {
		HtmlDoclet doclet = new FunctionalDoclet();
		return doclet.start(doclet, root);
	}
	
	 @Override
	public boolean start(AbstractDoclet doclet, RootDoc root) {
		    doclet.configuration = configuration();
	        configuration.root = root;
	        try {
	        	//there is a isValidDoclet(doclet) test in AbstractDoclet, to skip that we jump straight to the private method startGeneration
				Method method = AbstractDoclet.class.getDeclaredMethod("startGeneration", RootDoc.class);
				method.setAccessible(true);
				method.invoke(this, root);
	        } catch (Exception exc) {
	            exc.printStackTrace();
	            return false;
	        }
	        return true;
	    }
	
	
	 @Override
	protected void generateOtherFiles(RootDoc root, ClassTree classtree) throws Exception {
		 super.generateOtherFiles(root,  classtree);
		 //this will overwrite the same files generated in super.generateOtherFiles
		 CustomAllClassesFrameWriter.generate(configuration, new IndexBuilder(configuration, configuration.nodeprecated, true));
		 CustomHelpWriter.generateCustom(configuration);
	 }
	 
	@Override
	protected void generatePackageFiles(ClassTree classtree) throws Exception {
		PackageDoc[] packages = configuration.packages;
        if (packages.length > 1) {
            PackageIndexFrameWriter.generate(configuration);
        }
        PackageDoc prev = null, next;
        for (int i = 0; i < packages.length; i++) {
            // if -nodeprecated option is set and the package is marked as
            // deprecated, do not generate the package-summary.html, package-frame.html
            // and package-tree.html pages for that package.
            if (!(configuration.nodeprecated && Util.isDeprecated(packages[i]))) {
            	if(OLD_STYLE) {
            		PackageFrameWriter.generate(configuration, packages[i]);
            	} else {
            		CustomPackageFrameWriter.generate(configuration, packages[i]);
            	}
                next = (i + 1 < packages.length &&
                        packages[i + 1].name().length() > 0) ? packages[i + 1] : null;
                //If the next package is unnamed package, skip 2 ahead if possible
                next = (i + 2 < packages.length && next == null) ? packages[i + 2] : next;
                if(OLD_STYLE) {
                	AbstractBuilder packageSummaryBuilder =
                        configuration.getBuilderFactory().getPackageSummaryBuilder(
                        packages[i], prev, next);
                	packageSummaryBuilder.build();
                } else {
                	CustomPackageSummaryBuilder builder = new CustomPackageSummaryBuilder(configuration, packages[i], 
                			new CustomPackageWriter(configuration, packages[i], prev, next));
                	builder.build();
                }
                
                if (configuration.createtree) {
                    PackageTreeWriter.generate(configuration,
                            packages[i], prev, next,
                            configuration.nodeprecated);
                }
                prev = packages[i];
            }
        }
    }
	
}
