# Functional-Doclet-for-Javadoc
A doclet with more emphasis on organizing by function rather than organizing by categories of reference types

[Here is an example](https://seancfoley.github.io/IPAddress/IPAddress/apidocs/) for the [Java library IPAddress] (https://github.com/seancfoley/IPAddress)

## Standard Javadoc
Standard Javadoc organizes types according to the categories: interfaces, classes, enums, exceptions and errors.  From the point of view of a user attempting to understand your code, this is not an optimal presentation.  Firstly, here is no indication regarding which types may be most important and should be examined first.  While package names are sometimes indicative, withing a given package there is no indication.  Additionally, the division between interfaces, classes, enums, exceptions and errors is somewhat arbitrary.  For instance, the difference between an interface and an abstract class can be small and meaningless at times.  Similarly, an enum can often have little difference from a non-enum class since an enum can have any number of fields and methods, and sometimes non-enum classes are used for lists and ordering. 

Grouping types in this manner is not functionally meaningful, which is what generally interests the reader of javadoc.  

## Functional Doclet
This doclet modifies the standard doclet in the following manners:
* Classes/interfaces/enums of primary interest are denoted as "core", by using an annotation in the code, and shown separately within the javadoc
* The remaining types are divided into the categories of those things not "thrown" (classes, interfaces, enums) and those things that are "thrown" (exceptions and errors)
* Nested types are as nested within their parent types inside the javadoc, just like in the code.
* Icons for interfaces, classes, and enums

![sample](https://github.com/seancfoley/Functional-Doclet-for-Javadoc/blob/master/Functional%20Doclet%20for%20Javadoc/sample.png)

Organizing the types in this manner provides a more functional visual perspective of the code.

The functionally primary types of interest are clearly identified and separated in the javadoc, throwable types are separated from the remaining, and nested classes are associated with their enclosing types.

## Usage

 Use the custom.core annotation to identify your core types:
`/**
   @custom.core
  */`

When running the javadoc tool, add `-tag custom.core:a:Core` to your command line.  Use tools.doclets.formats.html.FunctionalDoclet as the doclet (options `-doclet` and `-docletpath`).  Use stylesheet_custom.css as the stylesheet (option `-stylesheetfile`).

## Dependencies

This doclet requires a Java 8 SDK, it is built on top of the Java 8 Javadoc code.

