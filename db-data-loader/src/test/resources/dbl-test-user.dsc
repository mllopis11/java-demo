#type;tableName
T;LDR_USER_STG

#type;colName;startPos;length;datatype;required;expression
C;id;1;10;N;true;
C;firstname;11;20;C;true;
C;lastname;31;20;C;true;
C;gender;51;1;C;true;
C;birthdate;52;10;C;true;TO_DATE(%value%, 'YYYY-MM-DD')
C;email;62;30;C;false;
C;is_enabled;93;5;B;true;
