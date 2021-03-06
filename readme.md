# Introduction

JSON to Java entity & dto mapping, supporting extensive restructuring requiring annotations only.
Spring uses Jackson to map JSON to Java objects; unfortunately, other than renaming fields, it lacks much power.
This is an alternative that doesn't require writing `unpackNested(Map<String, Object>)` methods the way Jackson requires; it instead relies on simple `@JsonAnnotation("{path}/{name}#{flag1,...flagn}?{condition}")` annotations.

For example, when you need to map a JSON object such as 
```
{
  "x": {
    "y": {
      "z": "xyz"
    }
  }
}
```
to an entity such as 
```
class Ent {
    String z;
}
```
it is as simple as 
```
@JsonAnnotation("x/y/")
```

# Renaming Fields

`@JsonAnnotation("{name}")`

### Input

```json
{
  "firstName": "Manuel",
  "lastName": "Reuter"
}
```

### Entity

```java
package example.renamed;

import mapper.JsonAnnotation;

public class RenamedEntity {
    String firstName;

    @JsonAnnotation("lastName")
    String familyName;
}
```

### Output

```text
RenamedEntity
  firstName : Manuel
  familyName: Reuter

```

# Omitting Field Name

ending with a / `@JsonAnnotation("{path}/")`

### Input

```json
{
  "tips": {
    "tip1": "usually, JsonAnnotation paths are strucutred as `path/name`",
    "tip2": "but if u ommit the name, (e.g. `path/`), JsonAnnotation will automatically append the field name"
  },
  "tip3": "careful not to confuse that with the `name` pattern, where the trailing `/` is omited"
}
```

### Entity

```java
package example.ommited;

import mapper.JsonAnnotation;

public class OmittedEntity {
    @JsonAnnotation("tips/tip1")
    String tip1;

    @JsonAnnotation("tips/")
    String tip2;

    @JsonAnnotation("tip3")
    String tip3;
}
```

### Output

```text
OmittedEntity
  tip1      : usually, JsonAnnotation paths are strucutred as `path/name`
  tip2      : but if u ommit the name, (e.g. `path/`), JsonAnnotation will automatically append the field name
  tip3      : careful not to confuse that with the `name` pattern, where the trailing `/` is omited

```

# Ignoring Parts of Structure

blank annotations `@JsonAnnotation("")`

### Input

```json
{
  "ignoredField": "The ignoredField field has a blank annotation, so it will be ignored",
  "key": "The tuple class has a blank annotation,",
  "value": "so it's fields will look for `key` and `value` instead of `tuple/key` and `tuple/value`"
}
```

### Entity

```java
package example.ignored;

import mapper.JsonAnnotation;

public class IgnoredEntity {

    @JsonAnnotation("")
    String ignoredField;

    @JsonAnnotation("")
    Tuple tuple;
    
    static class Tuple {
       String key;
       String value;
    }
}
```

### Output

```text
IgnoredEntity
  ignoredField: null
  tuple     
    key       : The tuple class has a blank annotation,
    value     : so it's fields will look for `key` and `value` instead of `tuple/key` and `tuple/value`

```

# Restructuring Object



### Input

```json
{
  "nestMe": "i was originally unnested, but am afraid jsonAnnotation will hide me deep inside",
  "unestMe": {
    "value": "i was originally nested, but am confident jsonAnnotation will let me out",
    "middle": {
      "middleValue": "put me in the middle"
    }
  }
}
```

### Entity

```java
package example.restructured;

import mapper.JsonAnnotation;

public class RestructuredEntity {
    @JsonAnnotation("unestMe/value")
    String outer;

    @JsonAnnotation("unestMe/middle")
    Nester middle;

    static class Nester {
        @JsonAnnotation("middleValue")
        String value;
    }

    @JsonAnnotation("")
    NesterOuter nesterOuter;

    static class NesterOuter {
        @JsonAnnotation("")
        NesterInner nesterInner;
    }

    static class NesterInner {
        @JsonAnnotation("nestMe")
        String value;
    }
}
```

### Output

```text
RestructuredEntity
  outer     : i was originally nested, but am confident jsonAnnotation will let me out
  middle    
    value     : put me in the middle
  nesterOuter
    nesterInner
      value     : i was originally unnested, but am afraid jsonAnnotation will hide me deep inside

```

# Traversing Paths



### Input

```json
{
  "zero": 0,
  "one": 1,
  "parent": {
    "two": 2,
    "three": 3,
    "root": {
      "four": 4,
      "five": 5,
      "child": {
        "five": 5,
        "six": 6
      }
    }
  }
}
```

### Entity

```java
package example.traversal;

import mapper.JsonAnnotation;

public class TraversalEntity {

    @JsonAnnotation("parent/root")
    Numbers numbers;

    static class Numbers {
        @JsonAnnotation("~/")
        String zero;
        @JsonAnnotation("^/^/")
        String one;
        @JsonAnnotation("~/parent/")
        String two;
        @JsonAnnotation("^/")
        String three;
        String four;
        @JsonAnnotation("child/five")
        String fivee;
        @JsonAnnotation("child/")
        String six;
    }
}
```

### Output

```text
TraversalEntity
  numbers   
    zero      : 0
    one       : 1
    two       : 2
    three     : 3
    four      : 4
    fivee     : 5
    six       : 6

```

# Lists



### Input

```json
{
  "list": [
    1,
    2,
    3,
    4
  ],
  "listObjs": [
    {
      "value": 1
    },
    {
      "value": 2
    },
    {
      "value": 3
    },
    {
      "value": 4
    }
  ]
}
```

### Entity

```java
package example.list;

import mapper.JsonAnnotation;

import java.util.ArrayList;

public class ListEntity {
    ArrayList<Integer> list;

    @JsonAnnotation("list.0")
    ArrayList<Integer> listWithAnnotation;

    ArrayList<Value> listObjs;
    
    @JsonAnnotation("listObjs.0")
    ArrayList<Value> listObjsAnnotated;

    static class Value {
        int value;
    }
}
```

### Output

```text
ListEntity
  list      : (size 4)
    1         
    2         
    3         
    4         
  listWithAnnotation: (size 4)
    1         
    2         
    3         
    4         
  listObjs  : (size 4)
    0         
      value     : 1
    1         
      value     : 2
    2         
      value     : 3
    3         
      value     : 4
  listObjsAnnotated: (size 4)
    0         
      value     : 1
    1         
      value     : 2
    2         
      value     : 3
    3         
      value     : 4

```

# Extended Lists



### Input

```json
{
  "parent": {
    "billData": [
      {
        "value": 30,
        "currency": "usd"
      },
      {
        "value": 15,
        "currency": "usd"
      }
    ]
  },
  "billPaidDates": [
    "01/01/1991",
    "05/05/1995"
  ]
}
```

### Entity

```java
package example.listExtended;

import mapper.JsonAnnotation;

import java.util.ArrayList;

public class ListExtendedEntity {
    @JsonAnnotation("parent/billData.0")
    ArrayList<Bill> bills;

    static class Bill {
        @JsonAnnotation("value")
        String amount;
        String currency;
        @JsonAnnotation("~/billPaidDates.0")
        String date;
    }
}
```

### Output

```text
ListExtendedEntity
  bills     : (size 2)
    0         
      amount    : 30
      currency  : usd
      date      : 01/01/1991
    1         
      amount    : 15
      currency  : usd
      date      : 05/05/1995

```

# Nested List



### Input

```json
{
  "nestedList": [
    [
      11,
      12,
      13
    ],
    [
      21,
      22,
      23
    ]
  ]
}
```

### Entity

```java
package example.nestedlist;

import mapper.JsonAnnotation;

import java.util.ArrayList;

public class NestedListEntity {
    ArrayList<ArrayList<Integer>> nestedList;

    @JsonAnnotation("nestedList.0")
    ArrayList<ArrayList<Integer>> nestedListRename;

    @JsonAnnotation("nestedList.0.2")
    ArrayList<NestedList> nestedListExpanded;

    static class NestedList {
        @JsonAnnotation("")
        ArrayList<Integer> innerList;
    }
}
```

### Output

```text
NestedListEntity
  nestedList: (size 2)
    0         : (size 3)
      11        
      12        
      13        
    1         : (size 3)
      21        
      22        
      23        
  nestedListRename: (size 2)
    0         : (size 3)
      11        
      12        
      13        
    1         : (size 3)
      21        
      22        
      23        
  nestedListExpanded: (size 2)
    0         
      innerList : (size 3)
        11        
        12        
        13        
    1         
      innerList : (size 3)
        21        
        22        
        23        

```

# Typed Fields



### Input

```json
{
  "number": 10,
  "numericString": "135",
  "string": "string",
  "boolean": true,
  "booleanString": "false"
}
```

### Entity

```java
package example.typed;

import mapper.JsonAnnotation;

public class TypedEntity {
    @JsonAnnotation("number")
    int integer;

    @JsonAnnotation("number")
    Integer integerWrapped;

    @JsonAnnotation("numericString")
    int integerFromString;
    
    @JsonAnnotation("number")
    byte bite;
    
    String string;
    
    @JsonAnnotation("number")
    String stringFromInteger;

    @JsonAnnotation("boolean")
    String stringFromBoolean;

    @JsonAnnotation("boolean")
    Boolean booleen;

    @JsonAnnotation("booleanString")
    Boolean booleanFromString;
}
```

### Output

```text
TypedEntity
  integer   : 10
  integerWrapped: 10
  integerFromString: 135
  bite      : 10
  string    : string
  stringFromInteger: 10
  stringFromBoolean: true
  booleen   : true
  booleanFromString: false

```

# Conditional Paths

`@JsonAnnotation("{path}/{name}#{flag1,...flagn}?{condition}")`

### Input

```json
{
  "flags": "flags are indicated by the `#` symbol and delimited by the `,` symbol; e.g., `path/name#flag1,flag2,flag3`",
  "flagsInheriting": "flags r inherited. For example, an outer path `path1#flag1` with a nested path `path2#flag2` with a nested field `name#flag3` will result in the path `path1/path2/name#flag1,flag2,flag3",
  "conditions": "conditions are indicated by the `?` symbol. If a path condition is not found in the path flags, then the path is ignored; e.g. `path/name?flag1` will be ignored; however, `path/name#flag1?flag1` will be active.",
  "amountDue": {
    "value": "300",
    "currency": "dollar"
  },
  "lastAmountPaid": "400",
  "lastPaymentCurrency": "euro"
}
```

### Entity

```java
package example.conditional;

import mapper.JsonAnnotation;

public class ConditionalEntity {
    @JsonAnnotation("flags#flag1")
    String flags;

    @JsonAnnotation("conditions#flag2?flag2")
    String conditions;

    @JsonAnnotation("#flag3")
    InheritedFlagExample inheritedFlagExample;

    static class InheritedFlagExample {
        @JsonAnnotation("flagsInheriting?flag3")
        String flagsInheriting;
    }

    @JsonAnnotation("amountDue#next")
    Payment nextPayment;
    
    @JsonAnnotation("#last")
    Payment lastPayment;
    
    static class Payment {
        @JsonAnnotation("value?next")
        @JsonAnnotation("lastAmountPaid?last")
        String value;
        @JsonAnnotation("currency?next")
        @JsonAnnotation("lastPaymentCurrency?last")
        String currency;
    }
}
```

### Output

```text
ConditionalEntity
  flags     : flags are indicated by the `#` symbol and delimited by the `,` symbol; e.g., `path/name#flag1,flag2,flag3`
  conditions: conditions are indicated by the `?` symbol. If a path condition is not found in the path flags, then the path is ignored; e.g. `path/name?flag1` will be ignored; however, `path/name#flag1?flag1` will be active.
  inheritedFlagExample
    flagsInheriting: flags r inherited. For example, an outer path `path1#flag1` with a nested path `path2#flag2` with a nested field `name#flag3` will result in the path `path1/path2/name#flag1,flag2,flag3
  nextPayment
    value     : 300
    currency  : dollar
  lastPayment
    value     : 400
    currency  : euro

```

