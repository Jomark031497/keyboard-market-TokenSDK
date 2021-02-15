package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokensHandler
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*

@InitiatingFlow
@StartableByRPC
class TotalPart(
    private val part: String,
    private val serial: String
) : TokenBaseFlow() {

    @Suspendable
    override fun call(): String {
        when (part) {
            "frame" -> {
                val frameSerial = serial
                //transfer frame token
                val frameStateAndRef: StateAndRef<FrameTokenState> = getFrameStates(frameSerial)

                //get the TokenType object
                val frameTokenType = frameStateAndRef.state.data
                val issuer = frameTokenType.maintainer

                //get the pointer pointer to the frame
                val frameTokenPointer: TokenPointer<*> = frameTokenType.toPointer(frameTokenType.javaClass)

                val stx = subFlow(RedeemNonFungibleTokens(frameTokenPointer, issuer))
                return "\nThe frame part is totaled, and the token is redeem to BikeCo " +
                        "\nTransaction ID: ${stx.id}"
            }

            "switch" -> {
                val switchSerial = serial
                //transfer switch token
                val switchStateAndRef : StateAndRef<SwitchTokenState> = getSwitchStates(switchSerial)

                //get the TokenType object
                val switchTokenType: SwitchTokenState = switchStateAndRef.state.data
                val issuer = switchTokenType.maintainer

                //get the pointer pointer to the switch
                val switchTokenPointer: TokenPointer<*> = switchTokenType.toPointer(switchTokenType.javaClass)

                val stx = subFlow(RedeemNonFungibleTokens(switchTokenPointer, issuer))
                return "\nThe switch part is totaled, and the token is redeem to KeyboardCo" +
                        "\nTransaction ID: ${stx.id}"

            }
            else -> {
                return "Please enter either frame or switch for parameter part."
            }
        }
    }
}

@InitiatedBy(TotalPart::class)
class TotalPartResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
        subFlow(RedeemNonFungibleTokensHandler(counterpartySession));
    }
}