

Vector DB Insert

- What should i start with abstraction or an endpoint?
 
Abstraction makes people to understand more , which should often come last.

we will go for actual project 


Every entry to be inserted to be converted to a Document object. 

```java

new Document("Vaccum CL")
```

VectorStore has a method named add which will help you to insert documents within the database. Only thing is you cannot insert single entry , rather you have to insert multiple entries at once. 


