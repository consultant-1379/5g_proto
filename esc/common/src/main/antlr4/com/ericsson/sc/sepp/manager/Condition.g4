parser grammar Condition;

options {
	tokenVocab = ExpressionLexer;
}

root 
	: expression EOF
;


expression
	: PAR_OPEN expr=expression PAR_CLOSE		         		#ParentheticExpr 
	| left=expression op=EQ right=expression					#BinaryExpr 
	| left=expression op=CASE_INSENSITIVE_EQ right=expression	#BinaryExpr
	| left=expression op=IS_IN_SUBNET right=expression          #BinaryExpr
	| arg=expression op=(IS_EMPTY | EXISTS | IS_VALID_JSON)		#UnaryExpr
	| op=NOT arg=expression										#UnaryExpr
	| left=expression op=AND right=expression 					#BinaryExpr
	| left=expression op=OR right=expression 					#BinaryExpr
	| data_r													#Variable
	| literal_r													#Literal
;

literal_r
	:	BOOLEAN													#literalBool
	| 	STRING 													#literalStr
	// ImpPh:3 | 	INTEGER 									#literalInt
	| 	NUMBER 													#literalJsonNumber
;

data_r
	: req | resp | var 
	// ImpPh:4|  table
;

req
	: REQ property=HEADER key=key_val
	| REQ property=PATH
	| REQ property=METHOD
	| REQ property=BODY
	| REQ property=API_NAME
;

resp
	: RESP property=HEADER key=key_val
	| RESP property=BODY
;

var
	: VAR name=identifier
;
identifier
	: VARIABLE_IDENTIFIER
;
//table
//	: TABLE DOT name=VARIABLE (key=key_val | indx=index)
//;

key_val
	: BR_OPEN key=STRING BR_CLOSE
;

//index
//	: BR_OPEN indx=INTEGER BR_CLOSE
//;
