parser grammar NfMatchConditionParser;

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
	| arg=expression op=EXISTS         						    #UnaryExpr
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
;

data_r
	: nfdata
	//| var
	// ImpPh:4|  table
;

nfdata
    : NFDATA DOT name=nfdata_fields                                              #NfDataKnown
    | NFDATA DOT property=NF DOT name=nfdata_nf_fields                           #NfDataNf
    | NFDATA DOT property=SERVICE DOT name=nfdata_service_fields                 #NfDataService
    | NFDATA DOT property=IPENDPOINT DOT name=nfdata_endpoint_fields             #NfDataIpEndpoint
    | NFDATA key=key_val                                                         #NfDataUnknown
;

var
	: VAR name=identifier
;

identifier
	: VARIABLE_IDENTIFIER
;

nfdata_fields
	: NFINSTANCEID | NFTYPE | NFSTATUS | LOCALITY | NFSETID | SCPDOMAIN
	| CAPACITY | PRIORITY | STATUS | SETID | NFSERVICEID
	| FQDN | IPV4_ADDRESS | IPV6_ADDRESS |  PORT | SCHEME | TRANSPORT
;
nfdata_nf_fields
	: NFINSTANCEID | NFTYPE | NFSTATUS | LOCALITY | NFSETID | SCPDOMAIN
;
nfdata_service_fields
	: CAPACITY | PRIORITY | FQDN | SCHEME | STATUS | SETID | NFSERVICEID
;
nfdata_endpoint_fields
	: IPV4_ADDRESS | IPV6_ADDRESS | PORT | TRANSPORT
;
key_val
	: BR_OPEN key=STRING BR_CLOSE
;

