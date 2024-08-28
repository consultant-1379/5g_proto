package com.ericsson.supreme.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigurationSerializer
{
    private ConfigurationSerializer()
    {
    }

    public static Configuration getConfiguration(String fileLocation) throws FileNotFoundException
    {
        Yaml yaml = new Yaml(new Constructor(Configuration.class, new LoaderOptions()));
        InputStream inputStream = new FileInputStream(new File(fileLocation));
        return yaml.load(inputStream);

    }

}
