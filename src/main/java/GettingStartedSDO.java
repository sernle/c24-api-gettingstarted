import java.io.File;
import java.io.IOException;

import utils.SdoUtils;
import biz.c24.io.api.C24;
import static biz.c24.io.api.C24.Format.*;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.presentation.SdoSink;
import biz.c24.io.api.presentation.SdoSink.SdoContext;
import biz.c24.io.api.presentation.SdoSource;
import biz.c24.io.api.presentation.sdo.SdoContextTextualWriter;
import biz.c24.io.gettingstarted.customer.sdo.*;

/**
 * A very simple getting-started guide for working with C24 SDOs.
 * Please ensure you are familiar with the corresponding CDO guide before proceeding.
 *
 */
public class GettingStartedSDO {
    
    public static void main(String[] args) throws IOException, ValidationException {
        
        // In most cases, we start out the same way as for CDOs
        // First we parse our message into the corresponding CDO:
        
        File file = new File("/Customers.xml");
        
        biz.c24.io.gettingstarted.customer.CustomersFile cdoFile = C24.parse(biz.c24.io.gettingstarted.customer.CustomersFile.class).from(file);
        
        // It is especially important to ensure that the CDO is valid before creating an SDO
        // SDOs are less tolerant to invalid messages than CDOs
        
        C24.validate(cdoFile);
        
        // Now we create an SDO version of the CDO:
        
        CustomersFile sdoFile = C24.toSdo(cdoFile);
        
        // We could have parsed to an SDO directly by using the SDO class in the call to C24.parse above
        // however then we couldn't have performed validation so this is best suited for cases where we know our input is valid.
        // CustomersFile sdoFile = C24.parse(CustomersFile.class).from(file);
        
        // We can interrogate the parsed data in exactly the same way as
        // you would with any POJO or CDO, for example:
        
        for(Customer customer : sdoFile.getCustomer()) {
            System.out.println(customer.getCustomerNumber());
        }
        
        // And we can still access nested objects, exactly the same as for a CDO:
        
        Address address = sdoFile.getCustomer()[0].getAddress();
        for(String line : address.getAddressLine()) {
            System.out.println(line);
        }
        
        // If you only need a single element from an array, it's more efficient to use the 
        // getter than takes an ordinal. We could rewrite the line above as:
        address = sdoFile.getCustomer(0).getAddress();
        
        // The call above doesn't generate any objects, however if you look at sdoFile in a debugger
        // you'll see that it doesn't contain any Customer objects; neither do the Customer objects
        // contain an Address object. So where did they come from?
        //
        // Behind the scenes we maintain template objects for each type. They're stored ThreadLocal so
        // there's at most 1 per-thread per-type. When you ask for the Customer object above, we grab the
        // template object, point it to our underlying byte array and tell it where in the array its data starts.
        // That process continues down the call chain.
        //
        // This is perfect for data grids, particularly running queries over huge numbers of SDOs but it does mean
        // you need to be careful if you're using SDOs in your own code. For example:
        
        Address address1 = sdoFile.getCustomer(1).getAddress();
        Address address2 = sdoFile.getCustomer(2).getAddress();
        address1.getAddressLine(0); // ERROR
        
        // Why is this an error? Because the 3 lines above run in the same thread, both calls to getAddress above return
        // the same template object, so address1 and address2 are both references to the same object. By the time that
        // we call getAddressLine(0) on address1, the Address object is now pointing at Customer2's address data and not
        // Customer1's.
        // Arrays are the exception to this; because we're returning multiple objects, we have to create new objects.
        
        // So it still has the same POJO API, but it's much smaller. The CDO is likely
        // several times the size of the original file; the SDO is several times smaller:
        
        System.out.println("Original file size: " + SdoUtils.sizeOf(file));
        System.out.println("SDO compacted size: " + SdoUtils.sizeOf(sdoFile));
        
        // There's lots of clever stuff based on your model to make this as compact as possible
        // In general the more prescriptive your model is, the better the compaction will be
        //
        // There is a feature however which allows you to get great compaction without constraining
        // the data in your file. In the Studio, if you have an element or attribute which frequently
        // (but not always) is populated with one of a set of values, enter them as the Common Values
        // for that data component. These values will then compact down extremely well in the SDO.
        //
        // In our sample model, London is a frequently used value in addresses. You'll note that the
        // string doesn't appear in the SDO even though it's in our source file - instead we've 
        // written a much shorter reference to its location in the common values for that field
        
        SdoUtils.visualise(sdoFile, System.out);
        
        // When you're debugging SDOs, it's sometimes helpful to understand what is stored where.
        // There's another method on the SdoSink that will help here:
        
        SdoContext context = new SdoSink().writeToSdoWithContext(cdoFile);
        
        // This object contains both the SDO and also contextual information to describe it
        
        SdoContextTextualWriter.writeTo(System.out, context);
        
        // Like CDOs, you can write them out in multiple formats:
        C24.write(sdoFile).as(JSON).to(System.out);
        
                
        // SDOs are read-only however. If you want to modify them you need to go back to the corresponding CDO:       
        cdoFile = C24.toCdo(sdoFile);
        
        // Now all the standard CDO features are available, such as modification, XPath queries, writing etc.
       
        
    }

}
