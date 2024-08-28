BEGIN {
	inSchemas = 0;
	ownType = "";	
}

{
	if (inSchemas == 1)
	{
		if (match($0, /^[ ]{4}([A-Z0-9][A-Za-z0-9]+):/, a))
		{
			ownType = gensub(/^([0-9]+)/, "Model\\1", 1, a[1]);
			#printf ("line=%d, ownType=%s\n", NR, ownType);
			printf ("") >  path "/" ownType ".java";
		}
		else if (match($0, /^([ \t]+)\$ref: .(TS[0-9]+_[^\.]+).+schemas\/([A-Za-z0-9]+)/, a))
		{
			ts = tolower(a[2]);
			gsub("_", ".", ts);
			refType = ts "." a[3];
			#printf ("line=%d, ownType=%s, refType=%s\n", NR, ownType, refType);
			ownTypes[ownType][refType] = refType;
		}
		else if (also_local_refs == 1 && match($0, /^([ \t]+)\$ref: .([^\.]+).+schemas\/([A-Za-z0-9]+)/, a))
		{
			ts = path;
			gsub("tmp/meta/", "", ts);
			gsub("/", ".", ts);
			gsub("_", ".", ts);
			refType = ts "." gensub(/^([0-9]+)/, "Model\\1", 1, a[3]);
			#printf ("line=%d, ownType=%s, refType=%s\n", NR, ownType, refType);
			ownTypes[ownType][refType] = refType;
		}
	}
	else if (match($0, /^  schemas:/, a))
	{
		inSchemas = 1;
	}
}

END {
	for (ownType in ownTypes)
	{
		for (refType in ownTypes[ownType])
		{
			if (write_as == "paths")
			{
                fileName = "tmp/meta/" refType;	
	            gsub("[.]", "/", fileName);
				printf ("%s.java\n", fileName) >> path "/" ownType ".java";
			}
			else if (write_as == "imports")
			{
				printf ("import " pkg_root ".%s;\n", refType) >> path "/" ownType ".java";
			}
		}
	}	
}
