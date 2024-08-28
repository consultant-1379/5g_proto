lexer grammar TphExpressionLexer;

// Operators
EQ			: '=='		;
NEQ			: '!='		;

//NF Data Fields
TARGETNFTYPE			: 'target-nf-type'			;
NRFTYPE             	: '\'' NRF '\''             ;
SUPPORTEDNFTYPES		: '\'' (AUSF | UDM | AMF | SMF | NEF | PCF | SMSF | NSSF | UDR | LMF | GMLC | EIR | SEPP | UPF | N3IWF | AF | UDSF | BSF | CHF | NWDAF | PCSCF | CBCF | HSS | UCMF | SOR_AF | SPAF | MME | SCSAS | SCEF | SCP | NSSAAF | ICSCF | SCSCF | DRA | IMS_AS | CEF) '\'';
AUSF					: [Aa][Uu][Ss][Ff]			;
UDM						: [Uu][Dd][Mm]				;
AMF						: [Aa][Mm][Ff]				;
NRF						: [Nn][Rr][Ff]				;
SMF						: [Ss][Mm][Ff]				;
NEF						: [Nn][Ee][Ff]				;
PCF						: [Pp][Cc][Ff]				;
SMSF					: [Ss][Mm][Ss][Ff]			;
NSSF					: [Nn][Ss][Ss][Ff]			;
UDR						: [Uu][Dd][Rr]				;
LMF						: [Ll][Mm][Ff]				;
GMLC					: [Gg][Mm][Ll][Cc]			;
EIR						: [5][Gg][_][Ee][Ii][Rr]	;
SEPP					: [Ss][Ee][Pp][Pp]			;
UPF						: [Uu][Pp][Ff]				;
N3IWF					: [Nn][3][Ii][Ww][Ff]		;
AF						: [Aa][Ff]					;
UDSF					: [Uu][Dd][Ss][Ff]			;
BSF						: [Bb][Ss][Ff]				;
CHF						: [Cc][Hh][Ff]				;
NWDAF					: [Nn][Ww][Dd][Aa][Ff]		;
PCSCF					: [Pp][Cc][Ss][Cc][Ff]		;
CBCF					: [Cc][Bb][Cc][Ff]			;
HSS						: [Hh][Ss][Ss]				;
UCMF					: [Uu][Cc][Mm][Ff]			;
SOR_AF					: [Ss][Oo][Rr][_][Aa][Ff]	;
SPAF					: [Ss][Pp][Aa][Ff]			;
MME						: [Mm][Mm][Ee]				;
SCSAS					: [Ss][Cc][Ss][Aa][Ss]		;
SCEF					: [Ss][Cc][Ee][Ff]			;
SCP						: [Ss][Cc][Pp]				;
NSSAAF					: [Nn][Ss][Ss][Aa][Aa][Ff]	;
ICSCF					: [Ii][Cc][Ss][Cc][Ff]		;
SCSCF					: [Ss][Cc][Ss][Cc][Ff]		;
DRA						: [Dd][Rr][Aa]				;
IMS_AS					: [Ii][Mm][Ss][_][Aa][Ss]	;
CEF						: [Cc][Ee][Ff]				;