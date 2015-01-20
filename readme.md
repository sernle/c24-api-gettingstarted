# Parsing, Validating, Transforming and Marshaling with C24-iO


Now that you've generated your models and Java classes from the iO Studio it's time to use them in your own projects. C24-iO provides a number of different APIs to cater for a variety of use cases. The fluent API presented here is the most intuitive of them and will be appropriate for most uses however please contact C24 if you'd like details on the other APIs.

All the code and samples used in this section can be downloaded from https://github.com/C24-Technologies/c24-api-gettingstarted

This document also introduces some of the new features included in the new v4.7 release of C24-iO.

## Getting Started

The Runtime APIs are included in the c24-io-api jar. You can either copy it (and its dependencies) from your iO Studio installation or use a dependency manager - for Maven the relevant sections are:

    <repositories>
        <repository>
            <id>c24-nexus</id>
            <name>C24 Nexus</name>
            <url>http://repo.c24io.net/nexus/content/groups/public</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>biz.c24.io</groupId>
            <artifactId>c24-io-api</artifactId>
            <version>Insert C24 Runtime Version Here</version>
        </dependency>
	</dependencies>
	
Your code needs to then import the core C24 class:

    import biz.c24.io.api.C24;
    import static biz.c24.io.api.C24.Format.*;
   
The second import is optional but makes your code slightly cleaner where you're explicitly setting input or output formats (more on this later).

You also need to import your model classes. For the code sample in the referenced project we need:

    import biz.c24.io.gettingstarted.customer.CustomersFile;
    
### Parsing

Parsing is started with the `C24.parse(type)` method, for example:

    C24.parse(CustomersFile.class)...
    
At a minimum we need to tell the parser where to read the message from. iO supports parsing directly from Strings, Files, InputStreams and Readers:

    CustomersFile file = C24.parse(CustomersFile.class).from(new File("/customers.xml"));
    
If you use the File variant, if iO cannot file the file in the filesystem using the path specified, it will also use the same path to search the classpath.

The result of the parse call is a fully bound Java object tree which corresponds to your model definition. Our CustomersFile contains multiple customers, each of which has an identifying customer number:

    for(Customer customer : file.getCustomer()) {
       System.out.println(customer.getCustomerNumber());
    }
        
In this case the default format for our model was XML but we can override that at parsing time if, for example, we've actually received the file in JSON format:

    C24.parse(CustomersFile.class).as(JSON).from(...)
    
We can use JSON directly as we statically imported the C24 Format class above. In general, if you've defined your model correctly, you won't need to change the source format. Overrides are provided for JSON, XML, TEXT, BINARY & TAG_VALUE.

In the same way that we can override the default format, we can do the same for the encoding:

    C24.parse(CustomersFile.class).using("UTF-8").from(...)
    
Any character set supported by your JVM (see `java.nio.charset.Charset.availableCharsets()`) can be used.

#### Going Further

It is possible to intercept the message before iO parses it by supplying an instance of PreProcessor to the parser. For example, to remove duplicate line endings and to convert all line endings to \r\n we could:

    public static PreProcessor LineEndingCleaner = new PreProcessor() {
    
        public String preprocess(String message) {

            message = message.replaceAll("[\\r\\n]+", "\r\n"); // Clean up line endings
            return message;
        }    
    };
    
    ...
    
    MyType obj = C24.parse(MyType.class).preprocess(new LineEndingCleaner()).from(...);
    
An instance of `biz.c24.io.api.presentation.ParseListener` can also be used during parsing to intercept the parsing process:

    ParseListener listener = ...;
    
    C24.parse(MyType.class).with(listener).from(...);
    
Please see the ParseListener Javadoc and the 'Process as Batch' documentation for further details on the ParseListener.

__Support for the `with(listener)` grammar requires iO 4.6.10 or above. For earlier versions, please set the ParseListener on the Source directly and use the `from(Source)` method.__
    
Pre-configured Sources can also be used directly with the `from(...)` method if you wish to set advanced properties on them. Alternatively you can expose your configured Source as a new Format:

    public static Format TolerantFIX = new Format() {

    @Override
    public Source getSource() {

        FIXSource fixSource = new FIXSource();

        fixSource.setEndOfDataRequired(true);
        fixSource.setAllowFieldsOutOfOrder(true);
        fixSource.setAllowNoData(true);
        fixSource.setIgnoreRepeatingGroupOrder(true);
        fixSource.setIgnoreUnknownFields(true);

        return fixSource;
    }
    
    ...
    
    MyType obj = C24.parse(MyType.class).as(TolerantFIX).from(...);
    
__Support for user-defined Formats requires iO 4.7 or above.__    
    
### Validation

The quickest way to determine if an object is valid is to use the fail-fast:

    try {
        C24.validate(file);
    } catch(ValidationException vEx) {
        System.out.println("Message was invalid. Field " + vEx.getFieldName() + 
                           ": " + vEx.getReason() + " (" + vEx.getObject().toString() + ")");
    }
    
If you want to get all failures in the message from a single validation pass, instead use:

    ValidationEvent[] failures = C24.validateFully(file);
    if(failures != null && failures.length > 0) {
        System.out.println("Message was invalid due to:");
        for(ValidationEvent failure : failures) {
            
            System.out.println(failure.getLocation() + ": " + failure.getMessage() + 
                               " (" + failure.getObject().toString() + ")");
        }
    }
    
### Transformation

The simplest transformation API works with 1:1 transformations:

    GenerateContactListTransform xform = new GenerateContactListTransform();
        
    ContactDetailsFile cdFile = xform.transform(file);

The result of the transformation is also a CDO and hence can be validated, interogated, transformed and marshaled in the same was as any other CDO.

For n:m transformations there is an alternative transform method which takes and returns arrays of objects.

### Marshaling

Marshaling requires at a minimum the object to be marshaled and details on where to write the result to:

    C24.write(file).to(System.out);
    
The `to(...)` method accepts Writers, OutputStreams and Files. Alternatively the `toStr()` method writes directly to a String. 

Just as with the `C24.parse(...)` method, you can control the output format and encoding by using the `as(...)` and `using(...)` methods:

    String json = C24.write(file).as(JSON).using("UTF-8").toStr();
    
__The toStr() method requires iO 4.6.10 or above. In earlier releases you can use to(StringWriter) to marshal your object to a String.__ 

#### Going Further

__The MarshalListener interface is available in iO 4.6.10 and above__

Certain Sinks implement the StreamingSink interface which allows a MarshalListener to intercept the marshaling process for types which have the 'Process As Batch' property set to true.

MarshalListeners can be injected into the marshaling process using the `with(MarshalListener)` grammar:

    MarshalListener listener = ...;
    
    C24.write(file).with(listener).to(System.out);
    
If you attempt to use a MarshalListener on a Sink which doesn't support them, an `UnsupportedOperationException` will be thrown from the `to(...)` method.

MarshalListeners can mutate, remove or add objects to the marshaled output. The MarshalListener below duplicates in the output every CDO it is invoked on:

    MarshalListener listener = new MarshalListener() {
        
        @Override
        public boolean marshal(ComplexDataObject value, StreamingSink sink) throws Exception {
            
            // Write out value
            sink.marshal(value);
            // ... and write it out again
            sink.marshal(value);
            
            // Tell the Sink that we've marshaled value so it doesn't need to.
            return true;
         
        }
    };


## SDOs

Parsing directly to SDOs works in exactly the same way as parsing CDOs[^1]. All of the syntax shown in Parsing above works identically for SDOs - it's simply a case of passing the SDO class in to the `parse(...)` call:

    import biz.c24.io.gettingstarted.customer.sdo.CustomersFile;

    CustomersFile file = C24.parse(CustomersFile.class).from(new File("/customers.xml"));

[^1]: This is true for most types, however there are bespoke methods for custom binary protocols such as those used by the telco standards.

Marshaing SDOs is also the same as for CDOs and again the full syntax shown above is valid:

    String json = C24.write(file).as(JSON).using("UTF-8").toStr();
    
__The toStr() method requires iO 4.6.10 or above. In earlier releases you can use to(StringWriter) to marshal your object to a String__ 
    
Validation and transformation are not supported directly by SDOs so if you wish to use these in your process flow you should parse the CDO first and convert to an SDO when you need a compact, read-only version of your message.

Converting between CDOs and SDOs is simple:

    biz.c24.io.gettingstarted.customer.CustomersFile cdoFile = ...;
    
    biz.c24.io.gettingstarted.customer.sdo.CustomersFile sdoFile = C24.toSdo(cdoFile);
    
    cdoFile = C24.toCdo(sdoFile);

## Scala
__The C24-iO Scala Library is available with iO v4.7.0 and above.__ 

To build the Scala examples in the reference project using Maven, add -Pscala to the build line.

An additional jar (c24-io-api-scala) further enhances the C24 API to provide a more idiomatic Scala-based interface. In this case your Maven project requires the following dependency:

    <dependency>
        <groupId>biz.c24.io</groupId>
        <artifactId>c24-io-api-scala</artifactId>
        <version>${c24.io.api.version}</version>
    </dependency>

And the relevant classes are included with:

    import biz.c24.io.api.C24Scala._
    
The fluent API is enhanced to allow parsing, validation, transformation and marshaling to be naturally chained together:

    (
        C24.parse(classOf[CustomersFile]) as XML from new File("/Customers.xml") 
        ensureValid()
        transformUsing new GenerateContactListTransform()
        write() as JSON to System.out
    )

The use of implicit conversion means that at the end of each stage (line) above, the result is a CDO (unless you're parsing SDOs - see later) so you can use as much or as little of the API as you need.

Alternatively there is a monadic API for building a process out of predefined components:

      var parser = C24.parse(classOf[CustomersFile]) as XML
      var validate = new ValidationManager
      var transform = new GenerateContactListTransform
      var writer = C24.write() as JSON
      
      new File("/Customers.xml") -> parser -> validate -> transform -> writer -> System.out

As with CDOs, the `parse(...)` and `write(...)` methods both work with SDOs (and in the case of the former return an SDO). Validation and transformation are not supported on SDOs and, although the API could transparently convert between CDO & SDO, these features are only supported directly via the CDO-based API so users can ensure they're creating the most efficient processing pipeline.

## Java 8 Deploy Option
_The Deploy Java 8 code option is available with io v4.7.0 and above._

To build the samples with the java 8 option, first ensure your selected Java version is 1.8 then execute the following maven profile:

    mvn clean install -Pjava8

To include it in your maven project add the following dependency:

    <dependency>
        <groupId>biz.c24.io</groupId>
        <artifactId>c24-io-api-java8</artifactId>
        <version>${c24.io.api.version}</version>
    </dependency>

To generate code with the Java 8 features you can select the JDK_8 option in the project profile.
Alternatively if you use the C24 maven deploy task set the following property:

    <javaVersion>JDK_8</javaVersion>


Deploying C24 code with the Java 8 option selected will result in the following behaviours:

1. ISO8601 Date and Time types will be mapped to java.time Date and Time types
2. Elements that have cardinality of more than 1 will be mapped to type safe collections
3. Properties that are backed by a C24 Enumeration will have a corresponding getter and setter that works with a generated java enum

See src/main/java8/WorkingWithJava8Extensions.java class for a full working example
