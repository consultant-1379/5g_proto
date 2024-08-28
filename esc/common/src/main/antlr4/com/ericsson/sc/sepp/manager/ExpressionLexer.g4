lexer grammar ExpressionLexer;

// Building blocks or otherwise called tokens, to be recognized by the parser	
// -------------------------
DOT			:	'.' 	;
PAR_OPEN 	: 	'('		;
PAR_CLOSE	:	')' 	;
BR_OPEN		:	'[' 	;
BR_CLOSE	:	']' 	;

// Operators
// -------------------------
AND			: 'and'	    ;
OR			: 'or' 	    ;
NOT			: 'not' 	;
EXISTS		: 'exists'  ;
IS_EMPTY	: 'isempty' ;
EQ			: '=='		;
CASE_INSENSITIVE_EQ: '=';
IS_IN_SUBNET: 'isinsubnet';
IS_VALID_JSON: 'isvalidjson';
// ImpPh:3 ISABSURI	: 'isAbsoluteUri' 	;

// Operands
// -------------------------
	
/*
 * BUG DND-31872
 * The identifier of var. cannot be any of the above tokens.
 * The characters of a sequence are mapped to the first token that matches.
 * e.g. For input vat.empty, var. is mapped to the VAR token and empty is mapped 
 * to the empty token. It is not mapped to the STRING or VARIABLE token because
 * EMPTY token is declared first. In order to overcome this, we create a sub-lexer, using antlr modes.
 * The mode can change after a particular token is recognized. When the mode changes, the lexer will
 * only consider the tokens of that mode.
 */
VAR 		: 'var.'  			-> pushMode(VAR_IDENTIFIER_MODE	)			;
REQ 		: 'req.'  						;
RESP		: 'resp.'						;
NFDATA      : 'nfdata'                      ;
// ImpPh:4 TABLE		: 'table'			;

// Operand fields
// -------------------------
HEADER		    : 'header'	         ;
METHOD		    : 'method'	         ;
PATH		    : 'path'	         ;
BODY		    : 'body'	         ;
NF              : 'nf'               ;
SERVICE         : 'service'          ;
SCPDOMAININFO   : 'scpdomaininfo'    ;
IPENDPOINT      : 'ipendpoint'       ;
API_NAME        : 'apiname'          ;

//NF Data Fields
NFINSTANCEID            : 'nf-instance-id'			;
NFTYPE                	: 'nf-type'					;
NFSTATUS				: 'nf-status'				;
LOCALITY              	: 'locality'				;
NFSETID					: 'nf-set-id'				;
SERVEDNFSETID			: 'served-nf-set-id'		;
SCPDOMAIN              	: 'scp-domain'				;
CAPACITY              	: 'capacity'				;
PRIORITY              	: 'priority'				;
STATUS					: 'status'					;
SETID					: 'set-id'					;
NFSERVICEID             : 'nf-service-id'			;
DOMAIN              	: 'domain'				    ;
FQDN                  	: 'fqdn'					;
IPV4_ADDRESS          	: 'ipv4-address'			;
IPV6_ADDRESS          	: 'ipv6-address'			;
PORT					: 'port'					;
SCHEME					: 'scheme'					;
TRANSPORT				: 'transport'				;


// Literals 
// -------------------------
BOOLEAN : 'true' | 'false' 													;
// https://www.ietf.org/rfc/rfc3986.txt
STRING: '\'' (Unreserved | GenDelims | SubDelims | LetterOrDigit )* '\''	;
// ImpPh:3 INTEGER : NoZeroDigit (Digit)* ;
NUMBER         : '-'? ('0' | NoZeroDigit (Digit)*) ('.' Digit +)? (Exponent [+\-]? ('0' | NoZeroDigit (Digit)*))?       ;
// -------------------------
fragment Unreserved
	: '-' | '.' | '_' | '~'
	| '%' | ' '
;
fragment GenDelims
	: ':' | '/' | '?' | '#' | '[' | ']' | '@'
;
fragment SubDelims
	: '!' | '$' | '&' | '\\\'' | '(' | ')'
	| '*' | '+' | '^' | ',' | ';'  | '='
;

fragment LetterOrDigit:
	[a-zA-Z0-9];
fragment Digit
	: [0-9] ;
fragment NoZeroDigit
	: [1-9] ;
fragment Letter
    : [a-zA-Z] ;
fragment CapitalLetter
	: [A-Z] ;
fragment SmallLetter 
	: [a-z] ;
fragment Exponent
	: [Ee] ;
	
WS : [ \t\r\n]+ -> channel(HIDDEN); // skip spaces, tabs, newlines
UNKNOWN_CHAR 	: . 			;

mode VAR_IDENTIFIER_MODE;

VARIABLE_IDENTIFIER_EXCEPTIONS
	: BOOLEAN | AND | OR | NOT | EXISTS | IS_EMPTY | EQ | IS_IN_SUBNET | IS_VALID_JSON
;
VARIABLE_IDENTIFIER       : Letter+ (Letter | Digit | '_')*   ->  popMode    ;


