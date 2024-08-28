{
	needle = "^import " gensub(/\//, ".", "g", pkg_root);

	if ($0 ~ needle)
	{
		if (match($0, /^[^ ]+ ([^;]+)/, a))
		{
			file = "tmp/tmp/src/main/java/" gensub(/\./, "/", "g", a[1]) ".java";
			
			if (system("test -f " file) == 0)
				print $0;
		}
	}
	else
	{
		print $0;
	}
}