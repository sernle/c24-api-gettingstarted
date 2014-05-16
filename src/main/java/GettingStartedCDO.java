import java.io.File;
import java.io.IOException;

import biz.c24.io.api.C24;
import static biz.c24.io.api.C24.Format.*;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.gettingstarted.customer.Customer;
import biz.c24.io.gettingstarted.customer.CustomersFile;
import biz.c24.io.gettingstarted.customer.Address;


public class GettingStartedCDO {
    
    public static void main(String[] args) throws IOException, ValidationException {
        
        // The first thing to know is the type that you want to parse.
        // In our case it's CustomersFile.class
        // We want to read it in, from a file, where it is stored in its default format:
        
        CustomersFile file = C24.parse(CustomersFile.class).from(new File("/Customers.xml"));
        
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
        
        
        
    }

}
