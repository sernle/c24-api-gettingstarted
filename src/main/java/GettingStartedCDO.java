import java.io.File;
import java.io.IOException;

import biz.c24.io.api.C24;
import static biz.c24.io.api.C24.Format.*;
import biz.c24.io.api.data.ValidationEvent;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.gettingstarted.contact.ContactDetailsFile;
import biz.c24.io.gettingstarted.customer.Customer;
import biz.c24.io.gettingstarted.customer.CustomersFile;
import biz.c24.io.gettingstarted.customer.Address;
import biz.c24.io.gettingstarted.transform.GenerateContactListTransform;


public class GettingStartedCDO {
    
    public static void main(String[] args) throws IOException, ValidationException {
        
        // The first thing to know is the type that you want to parse.
        // In our case it's CustomersFile.class
        // We want to read it in, from a file, where it is stored in its default format:
        
        CustomersFile file = C24.parse(CustomersFile.class).from(new File("/Customers.xml"));
        
        // We could have overriden the default format by using the as(Format) method or
        // default encoding by calling the using("Character set") method
        
        // Before we go any further, let's check that the file was valid
        C24.validate(file);
        
        // Now we can interrogate the parsed data in exactly the same way as
        // you would with any POJO, for example:
        
        for(Customer customer : file.getCustomer()) {
            System.out.println(customer.getCustomerNumber());
        }
        
        // Nested objects are also type safe:
        Address address = file.getCustomer()[0].getAddress();
        for(String line : address.getAddressLine()) {
            System.out.println(line);
        }
        
        // CDOs are read-write so we can modify, add and delete values
        
        file.getCustomer()[0].setEmailAddress("invalid@c24.biz");
        
        
        // We can also write back to a new Customer file.
        // By default it will write in the same format that we read it in, however we can 
        // use alternative representations too:
        
        C24.write(file).as(JSON).to(System.out);
        
        
        // What would have happened above if our file was invalid?
        // The model we're using requires certain fields to start with a capital letter, so let's introduce some violations:
        file.getCustomer()[0].setCustomerAcronym("oTwist");
        file.getCustomer()[1].setCountryOfResidence("us");
        
        try {
            C24.validate(file);
        } catch(ValidationException vEx) {
            System.out.println("Message was invalid. Field " + vEx.getFieldName() + ": " + vEx.getReason() + " (" + vEx.getObject().toString() + ")");
        }
        
        // We introduced 2 violations but the error only contained one - what happened?
        // The validation above is fail-fast - it will throw an exception as soon as invalid data is found.
        // If we want to fully validate the message we can use the following form:
        
        ValidationEvent[] failures = C24.validateFully(file);
        if(failures != null && failures.length > 0) {
            System.out.println("Message was invalid due to:");
            for(ValidationEvent failure : failures) {
                
                System.out.println(failure.getLocation() + ": " + failure.getMessage() + " (" + failure.getObject().toString() + ")");
            }
        }
        
        // The simplest way to use your Transforms is:
        GenerateContactListTransform xform = new GenerateContactListTransform();
        
        ContactDetailsFile cdFile = xform.transform(file);
        
        // The result of the transformation is also a CDO, so all the validation, transformation and marshaling code we've seen so far 
        // works with it too:
        
        C24.write(cdFile).as(XML).to(System.out);
        
        // The transformation approach above only works for 1:1 transforms. For n:m transforms we need to use a more advanced form:
        
        cdFile = (ContactDetailsFile) xform.transform(new Object[][]{{file}})[0][0];
 
        // The input and output arrays allow you to pass (and receive) more CDOs
        
    }

}
