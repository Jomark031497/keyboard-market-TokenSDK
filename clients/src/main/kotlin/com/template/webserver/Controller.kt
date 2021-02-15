package com.template.webserver

import com.template.flows.userflows.*
import com.template.states.UserState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


val SERVICE_NAMES = listOf("Notary", "Network Map Service")

@RestController
@RequestMapping("/")
class Controller(rpc: NodeRPCConnection) {

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    @GetMapping(value = ["me"], produces = [APPLICATION_JSON_VALUE])
    fun whoAmI() = mapOf("me" to myLegalName)

    @GetMapping(value = ["peers"], produces = [APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
            .map { it.legalIdentities.first().name }
            .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    @GetMapping(value = ["/all-users"], produces = [APPLICATION_JSON_VALUE])
    private fun getUsers(): List<UserModel> {
        return proxy.vaultQueryBy<UserState>().states.map {
            val data = it.state.data
            toUserModel(
                data.linearId,
                data.userType,
                data.name,
                data.age,
                data.gender,
                data.status
            )
        }
    }

    @GetMapping(value = ["/current-users"], produces = [APPLICATION_JSON_VALUE])
    private fun getCurrentUsers(): List<UserModel> {
        return proxy.vaultQueryBy<UserState>().states.filter { !(it.state.data.isDeleted) }
            .map {
                val data = it.state.data
                toUserModel(
                    data.linearId,
                    data.userType,
                    data.name,
                    data.age,
                    data.gender,
                    data.status
                )
            }
    }

    @GetMapping(value = ["/deleted-users"], produces = [APPLICATION_JSON_VALUE])
    private fun getDeletedUsers(): List<UserModel> {
        return proxy.vaultQueryBy<UserState>().states.filter { it.state.data.isDeleted }
            .map {
                val data = it.state.data
                toUserModel(
                    data.linearId,
                    data.userType,
                    data.name,
                    data.age,
                    data.gender,
                    data.status
                )
            }
    }

    @PostMapping(value = ["/search-user"], produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    private fun searchUser(@RequestBody params: SearchUserParams): ResponseEntity<UserModel> {

        val linearId: UniqueIdentifier = UniqueIdentifier.fromString(params.linearId)

        return try {
            val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
            val query = proxy.vaultQueryBy<UserState>(criteria).states.single()
            val dataRes = query.state.data
            val dataToModel =
                toUserModel(
                    dataRes.linearId,
                    dataRes.userType,
                    dataRes.name,
                    dataRes.age,
                    dataRes.gender,
                    dataRes.status
                )
            return ResponseEntity.status(HttpStatus.CREATED).body(dataToModel)
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().build()
        }


    }

    @PostMapping(value = ["create-user"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createUser(@RequestBody params: CreateUserParams): ResponseEntity<UserModel> {
        val userType = params.userType
        val name = params.name
        val age = params.age
        val gender = params.gender
        val status = params.status
        val counterParty = params.otherParty

        return try {
            val otherParty = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(counterParty))
                ?: return ResponseEntity.badRequest().build()

            proxy.startFlow(CreateUserFlow::Initiator, userType, name, age, gender, status, otherParty)
                .returnValue.getOrThrow()

            val users = proxy.vaultQueryBy<UserState>().states
            val getLatestUser = users[users.size - 1]
            val (data) = getLatestUser.state
            val userDetails = toUserModel(data.linearId, data.userType, data.name, data.age, data.gender, data.status)
            ResponseEntity.status(HttpStatus.CREATED).body(userDetails)
        } catch (e: Exception) {
            logger.error("Issue request failed", e)
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PostMapping(value = ["/create-user-own-nodes"],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE])
    fun createUserOwnNodes(@RequestBody params: CreateUserOwnNodesParams): ResponseEntity<UserModel> {

        val userType = params.userType
        val name = params.name
        val age = params.age
        val gender = params.gender
        val status = params.status

        return try {
            proxy.startFlow(CreateUserOwnNodesFlow::Initiator, userType, name, age, gender, status)
                .returnValue.getOrThrow()

            val users = proxy.vaultQueryBy<UserState>().states
            val getLatestUser = users[users.size - 1]
            val (data) = getLatestUser.state
            val userDetails = toUserModel(data.linearId, data.userType, data.name, data.age, data.gender, data.status)
            ResponseEntity.status(HttpStatus.CREATED).body(userDetails)
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(value = ["update-user"],
        produces = [TEXT_PLAIN_VALUE],
        consumes = [APPLICATION_JSON_VALUE])
    fun updateUser(@RequestBody params: UpdateUserParams): ResponseEntity<String> {
        val userType = params.userType
        val name = params.name
        val age = params.age
        val gender = params.gender
        val status = params.status
        val linearId = params.linearId

        val linearIdUI = UniqueIdentifier.fromString(linearId)
        return try {
            proxy.startTrackedFlow(UpdateUserFlow::Initiator,
                userType,
                name,
                age,
                gender,
                status,
                linearIdUI).returnValue.getOrThrow()

            ResponseEntity.status(HttpStatus.CREATED).body("$linearId successfully updated")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(value = ["update-name"],
        produces = [APPLICATION_JSON_VALUE],
        consumes = [APPLICATION_JSON_VALUE])
    fun updateName(@RequestBody params: UpdateNameParams): ResponseEntity<String> {

        val name = params.name
        val linearId = params.linearId

        val linearIdUI = UniqueIdentifier.fromString(linearId)
        return try {
            proxy.startTrackedFlow(UpdateNameFlow::Initiator, name, linearIdUI).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED)
                .body("$linearId's name successfully updated")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PutMapping(value = ["delete-user"],
        produces = [APPLICATION_JSON_VALUE],
        consumes = [APPLICATION_JSON_VALUE])
    fun deleteUser(@RequestBody params: DeleteUserParams): ResponseEntity<String> {
        val linearId = UniqueIdentifier.fromString(params.linearId)

        return try {
            proxy.startFlow(DeleteUserFlow::Initiator, linearId).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Successfully deleted")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().build()
        }
    }
}

