package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokensHandler
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class TransferPartToken(
    private val part: String,
    private val serial: String,
    private val holder: Party
) : TokenBaseFlow() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        when (part) {
            "frame" -> {
                val frameSerial = serial
                //transfer frame token
                val frameStateAndRef: StateAndRef<FrameTokenState> = getFrameStates(frameSerial)

                //get the TokenType object
                val frameTokenType = frameStateAndRef.state.data

                //get the pointer pointer to the frame
                val frameTokenPointer: TokenPointer<*> = frameTokenType.toPointer(frameTokenType.javaClass)
                val partyAndFrameToken = PartyAndToken(holder, frameTokenPointer)

                val stx = subFlow(MoveNonFungibleTokens(partyAndFrameToken))
                /* Distribution list is a list of identities that should receive updates. For this mechanism to behave correctly we call the UpdateDistributionListFlow flow */
                subFlow(UpdateDistributionListFlow(stx))
                return "Transfer ownership of the frame (${frameSerial}) to ${holder.name.organisation}" +
                        "\nTransaction ID: ${stx.id}"
            }
            "switch" -> {
                val switchSerial = serial
                //transfer switch token
                val switchStateAndRef: StateAndRef<SwitchTokenState> = getSwitchStates(switchSerial)

                //get the TokenType object
                val switchTokenType: SwitchTokenState = switchStateAndRef.state.data

                //get the pointer pointer to the switch
                val switchTokenPointer: TokenPointer<*> = switchTokenType.toPointer(switchTokenType.javaClass)
                val partyAndSwitchToken = PartyAndToken(holder, switchTokenPointer)

                val stx = subFlow(MoveNonFungibleTokens(partyAndSwitchToken))
                /* Distribution list is a list of identities that should receive updates. For this mechanism to behave correctly we call the UpdateDistributionListFlow flow */
                subFlow(UpdateDistributionListFlow(stx))
                return "Transfer ownership of the frame (${this.serial}) to ${holder.name.organisation}" +
                        "\nTransaction ID: ${stx.id}"
            }
            else -> {
                return "Please enter either frame or switch for parameter part."
            }
        }
    }
}

@InitiatedBy(TransferPartToken::class)
class TransferPartTokenResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
        subFlow(MoveNonFungibleTokensHandler(counterpartySession));
    }
}
