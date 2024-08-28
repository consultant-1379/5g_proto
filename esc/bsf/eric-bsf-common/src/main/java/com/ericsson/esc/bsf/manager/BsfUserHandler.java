/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 2, 2020
 *     Author: ekoteva
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Handles BSF user in a Cassandra database
 */
public class BsfUserHandler
{
    private static final Logger log = LoggerFactory.getLogger(BsfUserHandler.class);
    private final RxSession rxSession;
    private final List<SimpleStatement> queries;

    private final String bsfUser;
    private final String bsfKeyspaceResource;
    private final String checkBsfUserPermissionsQuery;
    private final String revokeModifyPermissionsStatement;
    private static final String CHECK_PERMISSIONS_QUERY = "LIST ALL PERMISSIONS;";
    private static final String PERMISSION = "permission";
    private static final String ROLE = "role";
    private static final String RESOURCE = "resource";

    /**
     * Creates the handler. User is created in database upon subscription to
     * {@link #createBsfUser()}
     * 
     * @param rxSession A valid cassandra session
     * @param keyspace  The BSF keyspace
     * @param bsfUser   The BSF user name
     * @param password  The BSF user password
     */
    public BsfUserHandler(RxSession rxSession,
                          String keyspace,
                          String bsfUser,
                          String password)
    {
        this.rxSession = rxSession;
        this.bsfUser = bsfUser;
        this.bsfKeyspaceResource = String.format("<keyspace %s>", keyspace).toLowerCase();
        this.checkBsfUserPermissionsQuery = String.format("LIST ALL PERMISSIONS OF '%s';", bsfUser);
        this.revokeModifyPermissionsStatement = String.format("REVOKE MODIFY PERMISSION ON KEYSPACE %s FROM '%s' ;", keyspace, bsfUser);
        this.queries = BsfUserStatementBuilder.build(keyspace, bsfUser, password);
    }

    /**
     * Creates database user and assigns the required permissions on bsfKeyspace
     * 
     * @return the result of these operations
     */
    public Single<Boolean> createBsfUser()
    {
        final var retryFunction = new RetryFunction().withDelay(2000) // retry upon failure with 2 second delay
                                                     .withRetries(30) // maximum retries
                                                     .withRetryAction((error,
                                                                       retries) -> log.warn("Retrying user creation query, retries {} error {}",
                                                                                            retries,
                                                                                            error))
                                                     .create();

        return Observable.fromIterable(queries)
                         .concatMapCompletable(query -> rxSession.sessionHolder()
                                                                 .flatMapPublisher(sh -> sh.executeReactive(query))
                                                                 .retryWhen(retryFunction)
                                                                 .doOnError(err -> log.warn("Error while executing user creation query {}",
                                                                                            query.getQuery(),
                                                                                            err))
                                                                 .ignoreElements())
                         .toSingleDefault(true)
                         .onErrorReturnItem(false);
    }

    /**
     * Verifies existence of database user and it's assigned permissions on BSF
     * keyspace, without retrying
     * 
     * @return A Single that emits the verification result
     */
    public Single<Boolean> verifyBsfUser()
    {
        return rxSession.sessionHolder()
                        .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance(checkBsfUserPermissionsQuery)))
                        .toList()
                        .doOnError(err -> log.warn("Error while executing User query: {}", checkBsfUserPermissionsQuery, err))
                        .map(rows ->
                        {
                            final var createPermission = rows.stream()
                                                             .anyMatch(row -> row.getString(ROLE).equals(bsfUser)
                                                                              && row.getString(RESOURCE).equalsIgnoreCase(bsfKeyspaceResource)
                                                                              && row.getString(PERMISSION).equals("CREATE"));

                            final var selectPermission = rows.stream()
                                                             .anyMatch(row -> row.getString(ROLE).equals(bsfUser)
                                                                              && row.getString(RESOURCE).equalsIgnoreCase(bsfKeyspaceResource)
                                                                              && row.getString(PERMISSION).equals("SELECT"));

                            return createPermission && selectPermission;
                        })
                        .onErrorResumeNext(error ->
                        {
                            if (error instanceof InvalidQueryException) // handling case of 'bsf-user' not existing
                                return Single.just(false);
                            else
                                return Single.error(error);
                        });
    }

    /**
     * Verifies existence of database user and it's assigned MODIFY permissions on
     * BSF keyspace
     * 
     * @return A Single that emits the verification result
     */
    public Single<Boolean> verifyBsfUserModifyPermissions()
    {
        return rxSession.sessionHolder()
                        .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance(CHECK_PERMISSIONS_QUERY)))
                        .toList()
                        .doOnError(err -> log.error("Error while checking database permissions with query: {}", CHECK_PERMISSIONS_QUERY, err))
                        .map(rows -> rows.stream()
                                         .anyMatch(row -> row.getString(ROLE).equals(bsfUser) && row.getString(RESOURCE).equalsIgnoreCase(bsfKeyspaceResource)
                                                          && row.getString(PERMISSION).equals("MODIFY")))
                        .onErrorResumeNext(error ->
                        {
                            if (error instanceof NoNodeAvailableException)
                            {
                                log.error("All Cassandra nodes are down.");
                                return Single.just(false);
                            }
                            else
                            {
                                return Single.error(error);
                            }
                        });
    }

    /**
     * Verifies existence of database user and it's assigned permissions on BSF
     * keyspace, retrying upon error or forever if user is not yet configured
     * 
     * @return A Completable that completes as soon as BSF user has been verified
     */
    public Completable ensureBsfUserConfigured()
    {
        final var retryFunction = new RetryFunction().withDelay(2000) // retry upon failure with 2 second delay
                                                     .withRetries(-1) // keep retrying forever
                                                     .withRetryAction((error,
                                                                       retries) -> log.warn("Retrying verification of bsf-user assigned permissions, retries {}",
                                                                                            retries,
                                                                                            error))
                                                     .create();

        return verifyBsfUser().filter(userQueryResult -> userQueryResult)
                              .toObservable()
                              .firstOrError()
                              .ignoreElement()
                              .retryWhen(retryFunction)
                              .doOnError(e -> log.error("Verification of bsf-user existence and it's assigned permissions failed after maximum number of retries"));

    }

    /**
     * Revokes MODIFY assigned permissions from database user on BSF keyspace
     * 
     * @return A Single that emits the result of revoke MODIFY permissions statement
     */
    public Single<Boolean> revokeBsfUserModifyPermissions()
    {
        return rxSession.sessionHolder()
                        .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance(revokeModifyPermissionsStatement)))
                        .toList()
                        .map(result -> true)
                        .doOnError(err -> log.error("Error while executing statement: {}", revokeModifyPermissionsStatement, err))
                        .onErrorResumeNext(error ->
                        {
                            if (error instanceof NoNodeAvailableException)
                            {
                                log.error("All Cassandra nodes are down.");
                                return Single.just(false);
                            }
                            else
                            {
                                return Single.error(error);
                            }
                        });
    }
}
