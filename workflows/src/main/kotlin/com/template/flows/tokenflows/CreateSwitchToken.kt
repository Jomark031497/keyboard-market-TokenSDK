package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
class CreateSwitchToken(private val switchSerial: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        //Create non-fungible frame token
        val uuid = UniqueIdentifier()
        val switch = SwitchTokenState(ourIdentity, uuid,0,switchSerial)

        //warp it with transaction state specifying the notary
        val transactionState = switch withNotary notary

        subFlow(CreateEvolvableTokens(transactionState))

        return "\nCreated a switch token for keyboard switch. (Serial #" + this.switchSerial + ")."
    }
}