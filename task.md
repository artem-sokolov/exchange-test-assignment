
# Task description  
  

Create a program for simplified order matching. The exchange has four tradeable securities ("A", "B", "C" и "D"). Base currency is USD ("$").  
     

The input consists of two files. The first file `clients.txt` contains the list of clients and their initial balances (securities and USD). The second file `orders.txt` contains the stream of incoming orders in chronological order.   
  

The program should output a file called `result.txt` looking similar to the `clients.txt` but containing the final state of client balances after processing all orders.  
     

## Files structure  
  

All files are plain text, where each line represents a single record. Record fields are separated by a "\\t" symbol. Clients and securities names are strings consisting of letters and digits. All numbers are integers.  
     

### File `clients.txt`  
  

Each line consists of the following fields:  

* Client name  
* USD balance  
* "A" security balance   
* "B" security balance   
* "C" security balance   
* "D" security balance   
     

Here's an example:  
     

```  
C1  1000    10  5   15  0  
C2  2000    3   35  40  10  
```  
     

### File `orders.txt`  
     

The format is as follows:  

* Client name  
* Deal side: "s" for selling and "b" for buying  
* Security name  
* Price  
* Amount  
  

Example:  
     

```  
C1  b   A   7   12  
C2  s   A   8   10  
```  
     

## Notes  
     

1. The orders match when price and amount match exactly.  
1. For the trade to happen counterparties have to be different clients.  
1. For simplicity, one may assume clients balances may be negative.  
     

## Expected results  
     

* File `result.txt` with the final state of clients balances after processing all orders.  
* Source code (github project)
