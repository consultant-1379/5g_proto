BEGIN {
	printImports = 1;
}

{
	if (match($0, /^import.+com\.ericsson\.cnal\.openapi\.r17\..*$/))
	{
		if (printImports == 1)
		{
			print imports;
			printImports = 0;
		}
	}
	else
	{
		print $0;
	}
}
