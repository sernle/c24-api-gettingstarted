import java.io.File;
import java.io.IOException;

import utils.PreonUtils;
import biz.c24.io.api.C24;
import static biz.c24.io.api.C24.Format.*;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.data.preon.util.PreonContext;
import biz.c24.io.api.data.preon.util.PreonContextTextualWriter;
import biz.c24.io.gettingstarted.customer.preon.*;

/**
 * A very simple getting-started guide for working with C24 Preons.
 * Please ensure you are familiar with the corresponding CDO guide before proceeding.
 * 
 * SDOs are a licensed feature of the C24-iO Enterprise Edition. 
 *
 */
public class GettingStartedPreon {
    
    public static void main(String[] args) throws IOException, ValidationException {
        
        // In most cases, we start out the same way as for CDOs
        // First we parse our message into the corresponding CDO:
        
        File file = new File("src/main/resources/Customers.xml");
        
        biz.c24.io.gettingstarted.customer.CustomersFile cdoFile = C24.parse(biz.c24.io.gettingstarted.customer.CustomersFile.class).from(file);
        
        // It is especially important to ensure that the CDO is valid before creating a Preon
        // Preons are less tolerant to invalid messages than CDOs
        
        C24.validate(cdoFile);
        
        // Now we create an SDO version of the CDO:
        
        CustomersFile preonFile = cdoFile.toPreon();
        
        // We could have parsed to a Preon directly by using the Preon class in the call to C24.parse above
        // however then we couldn't have performed validation so this is best suited for cases where we know our input is valid.
        preonFile = C24.parse(CustomersFile.class).from(file);
        
        // We can interrogate the parsed data in exactly the same way as
        // you would with any POJO or CDO, for example:
        
        for(Customer customer : preonFile.getCustomer()) {
            System.out.println(customer.getCustomerNumber());
        }
        
        // And we can still access nested objects, exactly the same as for a CDO:
        
        Address address = preonFile.getCustomer(0).getAddress();
        for(String line : address.getAddressLine()) {
            System.out.println(line);
        }
        
        // If you only need a single element from an array, it's more efficient to use the 
        // getter than takes an ordinal. We could rewrite the line above as:
        address = preonFile.getCustomer(0).getAddress();
        
        // As opposed to constructing an array/Collection and potentially constructing objects for all the elements in it
        // with the ordinal-based getCustomer(int ordinal) we just extract the one we want.
        
        // So our Preon still has the same POJO API, but it's much smaller. The CDO is likely
        // several times the size of the original file; the Preon is several times smaller:
        
        System.out.println("Original file size:   " + PreonUtils.sizeOf(file));
        System.out.println("Preon compacted size: " + PreonUtils.sizeOf(preonFile));
        
        // There's lots of clever stuff based on your model to make this as compact as possible
        // In general the more prescriptive your model is, the better the compaction will be
        //
        // There is a feature however which allows you to get great compaction without constraining
        // the data in your file. In the Studio, if you have an element or attribute which frequently
        // (but not always) is populated with one of a set of values, enter them as the Common Values
        // for that data component. These values will then compact down extremely well in the Preon.
        //
        // In our sample model, London is a frequently used value in addresses. You'll note that the
        // string doesn't appear in the Preon even though it's in our source file - instead we've 
        // written a much shorter reference to its location in the common values for that field
        
        PreonUtils.visualise(preonFile, System.out);
        
        // When you're debugging Preons, it's sometimes helpful to understand what is stored where.
        // There's another method on the CDO that will help here:
        
        PreonContext context = cdoFile.toPreonWithContext();
        
        // This object contains both the Preon and also contextual information to describe it
        
        PreonContextTextualWriter.writeTo(System.out, context);
        
        // Like CDOs, you can write them out in multiple formats:
        C24.write(preonFile).as(JSON).to(System.out);
        
                
        // SDOs are read-only however. If you want to modify them you need to go back to the corresponding CDO:       
        cdoFile = C24.toCdo(preonFile);
        
        // Now all the standard CDO features are available, such as modification, XPath queries, writing etc.
       
        
    }

}
