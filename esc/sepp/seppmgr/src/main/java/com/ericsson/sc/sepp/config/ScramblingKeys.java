package com.ericsson.sc.sepp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.ericsson.sc.rxkms.KmsClientUtilities;
import com.ericsson.sc.rxkms.KmsParameters;
import com.ericsson.sc.sepp.model.TopologyHidingWithAdminState;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.FqdnScramblingTable;
import com.ericsson.sc.proxyal.service.ScramblingKeysRotationService;

import io.reactivex.Completable;

public class ScramblingKeys
{
    private static final Logger log = LoggerFactory.getLogger(ScramblingKeys.class);
    private static ScramblingKeys instance;
    private KmsClientUtilities kmsUtils;
    private ConcurrentHashMap<String, String> keysMap;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Date>> roamingKeysMap;
    private boolean decryptionStatus = false;
    private static ConcurrentHashMap<String, String> roamingActiveKey = new ConcurrentHashMap<>();
    private static final String KMS_ROLE = "eric-cm-key-role";

    public static ScramblingKeys getInstance()
    {
        if (ScramblingKeys.instance == null)
        {
            ScramblingKeys.instance = new ScramblingKeys();

            // TODO: Improve it
            ScramblingKeysRotationService.getInstance()
                                         .getFlowable()
                                         .subscribe(date -> ScramblingKeysRotationService.getInstance()
                                                                                         .startRotation(instance.updateActiveKeys(), roamingActiveKey));
        }
        return ScramblingKeys.instance;
    }

    private ScramblingKeys()
    {
        // TODO: Pass role as env variable and read it from parameters
        this.kmsUtils = KmsClientUtilities.get(KmsParameters.instance, KMS_ROLE);
        this.keysMap = new ConcurrentHashMap<>();
        this.roamingKeysMap = new ConcurrentHashMap<>();
    }

    private ConcurrentHashMap<String, Date> readKeysForTH(List<TopologyHidingWithAdminState> topologyHiding)
    {
        ConcurrentHashMap<String, Date> map = new ConcurrentHashMap<>();
        topologyHiding.forEach(th ->
        {
            if (th.getScramblingKey() != null)
            {
                th.getScramblingKey().forEach(key -> map.put(key.getKeyIdRef(), key.getActivationDate()));
            }
        });
        return map;
    }

    private void readRoamingKeyMap(NfInstance instance)
    {
        instance.getExternalNetwork().stream().forEach(network -> network.getRoamingPartner().stream().forEach(rp ->
        {
            var scramblingKeyForRp = readKeysForTH(rp.getTopologyHidingWithAdminState());
            if (!scramblingKeyForRp.isEmpty())
            {
                this.roamingKeysMap.put(rp.getName(), scramblingKeyForRp);
            }
            else
            {
                this.roamingKeysMap.put(rp.getName(), this.readKeysForTH(network.getTopologyHidingWithAdminState()));
            }
        }));
    }

    public Date updateActiveKeys()
    {
        Date now = new Date();
        AtomicReference<Date> nextActiveDate = new AtomicReference<>(new Date(Long.MAX_VALUE));

        roamingKeysMap.entrySet().forEach(roamingPartner ->
        {
            AtomicReference<Optional<String>> activeIdPerRp = new AtomicReference<>();
            activeIdPerRp.set(Optional.empty());
            AtomicReference<Date> activeDatePerRp = new AtomicReference<>(new Date(0L));
            AtomicReference<Date> nextActiveDatePerRp = new AtomicReference<>(new Date(Long.MAX_VALUE));

            if (roamingPartner.getValue().size() <= 0)
            {
                return;
            }
            roamingPartner.getValue()
                          .forEach((id,
                                    date) ->
                          {
                              if (date.compareTo(now) <= 0 && date.compareTo(activeDatePerRp.get()) > 0)
                              {
                                  activeDatePerRp.set(date);
                                  activeIdPerRp.set(Optional.ofNullable(id));
                              }
                              else if (date.compareTo(now) > 0 && date.compareTo(nextActiveDatePerRp.get()) < 0)
                              {
                                  nextActiveDatePerRp.set(date);
                              }
                          });

            if (activeIdPerRp.get().isEmpty())
            {
                throw new BadConfigurationException("For the FQDN Scrambing a least one key per roaming partner must be active");
            }
            log.debug("Roaming partner: {} --- Active key: {}", roamingPartner.getKey(), activeIdPerRp.get().get());
            roamingActiveKey.put(roamingPartner.getKey(), activeIdPerRp.get().get());

            if (nextActiveDatePerRp.get().compareTo(nextActiveDate.get()) < 0)
            {
                nextActiveDate.set(nextActiveDatePerRp.get());
            }
        });
        log.debug("Next active key: {}", nextActiveDate.get());

        return nextActiveDate.get();
    }

    public Completable updateConfig(Optional<NfInstance> instance)
    {
        this.decryptionStatus = true;
        if (instance.isEmpty() || instance.get().getFqdnScramblingTable() == null || instance.get().getFqdnScramblingTable().isEmpty())
        {
            return Completable.complete();
        }

        log.debug("Update scrambling keys");
        this.readRoamingKeyMap(instance.get());
        ScramblingKeysRotationService.getInstance().startRotation(this.updateActiveKeys(), roamingActiveKey);

        List<FqdnScramblingTable> scrKeyTableSet = instance.get().getFqdnScramblingTable();
        ArrayList<Completable> decryptedCompletables = new ArrayList<>(2 * scrKeyTableSet.size());

        scrKeyTableSet.forEach(keyTable ->
        {
            decryptedCompletables.add(decryptAndSet(keyTable.getId(), keyTable.getKey(), keyTable::setKey));
            decryptedCompletables.add(decryptAndSet(keyTable.getId(), keyTable.getInitialVector(), keyTable::setInitialVector));
        });

        log.debug("Keys Map: {}", this.keysMap);
        return Completable.merge(decryptedCompletables);
    }

    Completable decryptAndSet(String encryptionIdentifier,
                              String artifact,
                              Consumer<String> setter)
    {
        log.debug("Request of decryption for " + artifact);
        if (!artifact.startsWith("vault:v1:"))
        {
            log.debug("Request of decryption for " + artifact + " was not forwarded due to malformed prefix");
            return Completable.complete();
        }
        return kmsUtils.decrypt(artifact).flatMapCompletable(decValue ->
        {
            decValue.ifPresentOrElse(setter::accept,
                                     () -> Completable.error(new RuntimeException("Failed to decrypt artifact with ID " + encryptionIdentifier)));
            return Completable.complete();
        });
    }

    public void decryptionFailed()
    {
        this.decryptionStatus = false;
    }

    public void decryptionSuccess()
    {
        this.decryptionStatus = true;
    }

    public boolean getDecryptionStatus()
    {
        return this.decryptionStatus;
    }
}