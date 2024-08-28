parser grammar TphExpressionParser;

options {
	tokenVocab = TphExpressionLexer;
}

root 
	: expression EOF
;


expression
	: left=TARGETNFTYPE op=EQ right=SUPPORTEDNFTYPES
	| left=TARGETNFTYPE op=EQ right=NRFTYPE
	| left=TARGETNFTYPE op=NEQ right=NRFTYPE
;

