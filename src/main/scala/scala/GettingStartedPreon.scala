package scala;

import biz.c24.io.api.C24
import biz.c24.io.api.C24Scala._
import biz.c24.io.api.data.ValidationManager
import scala.collection.JavaConversions._

import biz.c24.io.gettingstarted.customer.preon.CustomersFile

import java.io.File


/**
 * This example builds on the Scala GettingStartedCDO and the
 * Java GettingStartedPreon examples; please review them first.
 * 
 * Preons are a licensed feature of the C24-iO Enterprise Edition.
 */
object GettingStartedPreon {

   def main(args: Array[String]) {
     
       // Parsing & marshaling are identical to that with CDOs in Scala except that you pass in the Preon version of the class
 
       var customerFile = C24.parse(classOf[CustomersFile]) as C24.Format.XML from new File("src/main/resources/Customers.xml")
        
       for(customer <- customerFile.getCustomer()) {
           println(customer.getCustomerNumber());
       }
         
       C24.write(customerFile) as C24.Format.JSON to System.out
       
       
       // The same is true of the monadic API
       
      var parser = C24.parse(classOf[CustomersFile]) as C24.Format.XML
      var writer = C24.write() as C24.Format.JSON
      
      new File("src/main/resources/Customers.xml") -> parser -> writer -> System.out
       
       
       // As Preons can't be directly validated and transformed (or modified) you might want to parse the CDO first and 
       // convert it to a Preon when you're ready
       
       var cdo = C24.parse(classOf[biz.c24.io.gettingstarted.customer.CustomersFile]) as C24.Format.XML from new File("src/main/resources/Customers.xml")
       // validate, transform, edit, ...
       customerFile = cdo.toPreon()
       
       // Of course, you can go back to a CDO at any time
       
       cdo = C24.toCdo(customerFile)
    }
}
