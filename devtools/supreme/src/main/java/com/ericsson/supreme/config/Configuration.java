package com.ericsson.supreme.config;

import java.util.List;

import com.ericsson.supreme.exceptions.ValidationException;

public class Configuration
{
    private Generation generation;
    private List<Installation> installations;
    private Admin admin;
    private DefaultScenarios defaultScenarios;

    public Generation getGeneration()
    {
        return generation;
    }

    public void setGeneration(Generation generation)
    {
        this.generation = generation;
    }

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallation(List<Installation> installations)
    {
        this.installations = installations;
    }

    public Admin getAdmin()
    {
        return admin;
    }

    public void setAdmin(Admin admin)
    {
        this.admin = admin;
    }

    public void setDefaultScenarios(DefaultScenarios defaultScenarios)
    {
        this.defaultScenarios = defaultScenarios;
    }

    public DefaultScenarios getDefaultScenarios()
    {
        return this.defaultScenarios;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  generation: ");
        sb.append(generation);
        sb.append("\n  installations: ");
        sb.append(installations);
        sb.append("\n  admin: ");
        sb.append(admin);
        sb.append("\n  defaultScenarios: ");
        sb.append(defaultScenarios);
        return sb.toString();
    }

    public void validate(boolean isDefaultScenario)
    {
        if (generation != null)
        {
            generation.validate();
        }
        if (installations != null)
        {
            installations.forEach(Installation::validate);

            installations.forEach(inst ->
            {
                if (inst.getTarget().getSecretName() != null && admin.getNamespace() == null)
                {
                    throw new ValidationException("Admin.namespace must be configured for installing certificates as secrets");
                }

                if (inst.getTarget().getNetconf() != null && admin.getYangProvider() == null)
                {
                    throw new ValidationException("Admin.yangProvider must be configured for installing certificates through netconf");
                }
            });
        }

        if (admin == null)
        {
            throw new ValidationException("Admin must be configured for certificate installation");
        }
        admin.validate();

        if (isDefaultScenario)
        {
            if (defaultScenarios == null)
            {
                throw new ValidationException("DefaultScenarios must be configured.");
            }
            this.defaultScenarios.validate();
        }

    }

}
