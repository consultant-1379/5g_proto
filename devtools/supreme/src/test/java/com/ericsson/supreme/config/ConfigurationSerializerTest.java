package com.ericsson.supreme.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

import com.ericsson.supreme.exceptions.ValidationException;

class ConfigurationSerializerTest
{
    public static Path testPath(String a)
    {
        return Path.of("./src/test/resources", a);
    }

    @Test
    void testValid()
    {
        try
        {
            var path = testPath("properties.yaml");
            var c = ConfigurationSerializer.getConfiguration(path.toString());
            c.validate(false);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    void testValidWithDefaultScenarios()
    {
        try
        {
            var path = testPath("properties.yaml");
            var c = ConfigurationSerializer.getConfiguration(path.toString());
            c.validate(true);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    void testInvalid()
    {
        var path = testPath("properties_invalid.yaml").toString();
        assertThrows(ConstructorException.class, () -> ConfigurationSerializer.getConfiguration(path));
    }

    @Test
    void testInvalidDefaultScenarios()
    {
        try
        {
            var path = testPath("properties2.yaml");
            var c = ConfigurationSerializer.getConfiguration(path.toString());
            assertThrows(ValidationException.class, () -> c.validate(true));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }

    @Test
    void testFileNotFound()
    {
        assertThrows(FileNotFoundException.class, () -> ConfigurationSerializer.getConfiguration("not_existing.yaml"));
    }

}
