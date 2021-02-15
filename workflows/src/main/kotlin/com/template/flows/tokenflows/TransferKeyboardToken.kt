package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class TransferKeyboardToken(
    private val frameSerial: String,
    private val switchSerial: String,
    private val holder: Party
) : TokenBaseFlow() {

    @Suspendable
    override fun call(): String {
        //Step 1: Frame Token
        //get frame states on ledger
        val frameStateAndRef: StateAndRef<FrameTokenState> = getFrameStates(frameSerial)

        //get the TokenType object
        val frameTokenType = frameStateAndRef.state.data

        //get the pointer pointer to the frame
        val frameTokenPointer: TokenPointer<*> = frameTokenType.toPointer(frameTokenType.javaClass)

        //Step 2: Switch Token
        val switchStateAndRef: StateAndRef<SwitchTokenState> = getSwitchStates(switchSerial)

        //get the TokenType object
        val switchTokenType: SwitchTokenState = switchStateAndRef.state.data

        //get the pointer pointer to the switch
        val switchTokenPointer: TokenPointer<*> = switchTokenType.toPointer(switchTokenType.javaClass)

        //send tokens
        val session = initiateFlow(holder)
        val txBuilder = TransactionBuilder(getPreferredNotary(serviceHub))
        addMoveNonFungibleTokens(txBuilder, serviceHub, frameTokenPointer, holder)
        addMoveNonFungibleTokens(txBuilder, serviceHub, switchTokenPointer, holder)
        val ptx = serviceHub.signInitialTransaction(txBuilder)
        val stx = subFlow(CollectSignaturesFlow(ptx, listOf(session)))
        val ftx = subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))

        /* Distribution list is a list of identities that should receive updates. For this mechanism to behave correctly we call the UpdateDistributionListFlow flow */
        subFlow(UpdateDistributionListFlow(ftx))
        return "\nTransfer ownership of a keyboard (Frame serial#: ${this.frameSerial}, switch serial#: ${this.switchSerial}) " +
                "to ${holder.name.organisation}" +
                "\nTransaction IDs: ${ftx.id}"
    }
}

@InitiatedBy(TransferKeyboardToken::class)
class TransferBikeTokenResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call(): Unit {
        subFlow(ObserverAwareFinalityFlowHandler(flowSession))
    }
}
