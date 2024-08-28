package com.ericsson.sc.rxkms;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class KmsParameters
{

    public static final KmsParameters instance = new KmsParameters();
    public final String sipTlsRootCaPath;
    public final boolean globalTlsEnabled;
    public final String vaultHost;
    public final int vaultPort;
    public final String accountTokenPath;
    public final String keyName;

    private KmsParameters()
    {
        this.globalTlsEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));
        this.sipTlsRootCaPath = EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca");
        this.vaultHost = EnvVars.get("VAULT_HOST");
        this.vaultPort = Integer.parseInt(EnvVars.get("VAULT_PORT"));
        this.accountTokenPath = EnvVars.get("ACCOUNT_TOKEN_PATH");
        this.keyName = EnvVars.get("VAULT_KEY_NAME");
    }

    @Override
    public String toString()
    {
        var parameters = new JsonObject();
        parameters.put("sipTlsRootCaPath", sipTlsRootCaPath);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("vaultHost", this.vaultHost);
        parameters.put("vaultPort", this.vaultPort);
        parameters.put("accountTokenPath", this.accountTokenPath);

        return parameters.encode();
    }

}
