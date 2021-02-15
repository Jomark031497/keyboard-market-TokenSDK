package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.template.states.FrameTokenState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
class CreateFrameToken(private val frameSerial: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {
        // Obtain a reference from a notary we wish to use.
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        //Create non-fungible frame token
        val uuid = UniqueIdentifier()
        val frame = FrameTokenState(ourIdentity, uuid,0,frameSerial)

        //warp it with transaction state specifying the notary
        val transactionState = frame withNotary notary

        subFlow(CreateEvolvableTokens(transactionState))

        return "\nCreated a frame token for keyboard frame. (Serial #" + this.frameSerial + ")."
    }
}