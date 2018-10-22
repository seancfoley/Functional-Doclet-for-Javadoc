# Functional-Doclet-for-Javadoc
A doclet with more emphasis on organizing by function rather than organizing by categories of reference types

[Here is an example](https://seancfoley.github.io/IPAddress/IPAddress/apidocs/) for the [Java library IPAddress](https://github.com/seancfoley/IPAddress)

## Standard Javadoc
Standard Javadoc organizes types according to the categories: interfaces, classes, enums, exceptions and errors.  From the point of view of a user attempting to understand your code, this is not an optimal presentation.  Firstly, there is no indication regarding which types may be most important and should be examined first.  While package names are sometimes indicative, withing a given package there is no indication.  Additionally, the division between interfaces, classes, enums, exceptions and errors is somewhat arbitrary.  For instance, the difference between an interface and an abstract class can be small and meaningless at times, especially in later versions of Java where interfaces can have default methods.  Similarly, an enum can often have little difference from a non-enum class since an enum can have any number of fields and methods, while sometimes non-enum classes are used for lists and ordering.  

Grouping types in this manner is not functionally meaningful, which is what generally interests the reader of javadoc.  In general, there are many cases where a developer may wish to decide on his own which types should be showcased to a user of his library.

## Functional Doclet
This doclet modifies the standard doclet in the following manners:
* Classes/interfaces/enums of primary interest are denoted as "core", by using an annotation in the code, and shown separately within the javadoc
* The remaining types are divided into the categories of those things not "thrown" (classes, interfaces, enums) and those things that are "thrown" (exceptions and errors)
* Nested types are as nested within their parent types inside the javadoc, just like in the code.
* Icons are used for interfaces, classes, and enums

![sample](https://github.com/seancfoley/Functional-Doclet-for-Javadoc/blob/master/Functional%20Doclet%20for%20Javadoc/sample.png)

Organizing the types in this manner provides a more functional visual perspective of the code.

The functionally primary types of interest are clearly identified and separated in the javadoc, throwable types are separated from the remaining, and nested classes are associated with their enclosing types.

## Usage

 Use the custom.core annotation to identify your core types:
`/**
   @custom.core
  */`

### Command line

When running the javadoc tool, add `-tag custom.core:a:Core` to your command line.  Use tools.doclets.formats.html.FunctionalDoclet as the doclet (options `-doclet` and `-docletpath`).  Use stylesheet_custom.css as the stylesheet (option `-stylesheetfile`).

### With Ant

```
<javadoc sourcepath="${src_location}" destdir="${rootjavadocdir}"
			stylesheetfile="${functional_doclet_dir}/stylesheet_custom.css">
			<doclet name="tools.doclets.formats.html.FunctionalDoclet"
				path="${functional_doclet_dir}/dist/FunctionalDoclet.jar">
				 <param name="-tag" value="custom.core:a:Core" />
			</doclet>
	</javadoc>
```

## Dependencies

This doclet requires a Java 8 SDK, it is built on top of the Java 8 Javadoc code.
It can be run anywhere, but it was built with Eclipse Mars 2, and can easily be imported into Eclipse with the existing Eclipse project files.

