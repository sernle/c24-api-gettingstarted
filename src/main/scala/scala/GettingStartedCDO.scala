package scala;

import biz.c24.io.api.C24
import biz.c24.io.api.C24Scala._
import biz.c24.io.api.data.ValidationManager
import biz.c24.io.gettingstarted.customer.CustomersFile
import biz.c24.io.gettingstarted.transform.GenerateContactListTransform
import java.io.File



/**
 * A sample of using the C24-iO API from Scala.
 * It builds upon the equivalent Java GettingStartedCDO so please review
 * that code first
 */
object GettingStartedCDO {

  
    def main(args: Array[String]) {
      
        // All of the standard C24 API is obviously usable in Scala, e.g.:
          
        var customerFile = C24.parse(classOf[CustomersFile]) as C24.Format.XML from new File("src/main/resources/Customers.xml")
        
        C24.validate(customerFile)
        
        for(customer <- customerFile.getCustomer()) {
            println(customer.getCustomerNumber());
        }
        
        
        C24.write(customerFile) as C24.Format.JSON to System.out
          

        // The Scala extensions however provide some enhancements over what is possible in Java.
          
        // The first is that parsing, validation, transformation and marshaling can be fluently chained together:
      
        (
            C24.parse(classOf[CustomersFile]) as C24.Format.XML from new File("src/main/resources/Customers.xml")
            ensureValid()
            transformUsing new GenerateContactListTransform()
            write() as C24.Format.JSON to System.out
        )
        
        // The result of each line above (with the exception of the write line) is a CDO, so you can chain as 
        // much or as little processing together as you need.
        
        customerFile = (
                        C24.parse(classOf[CustomersFile]) as C24.Format.XML from new File("src/main/resources/Customers.xml")
                        ensureValid()
                       )

        
        // The second enhancement is that pre-configured components can be chained in a monadic way to 
        // create a proceessing pipeline
               
        var parser = C24.parse(classOf[CustomersFile]) as C24.Format.XML
        var validate = new ValidationManager
        var transform = new GenerateContactListTransform
        var writer = C24.write() as C24.Format.JSON
      
        new File("src/main/resources/Customers.xml") -> parser -> validate -> transform -> writer -> System.out
    }
}