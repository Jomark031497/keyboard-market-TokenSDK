package com.template.flows.tokenflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class IssueNewKeyboard(
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

        //get the pointer to the frame
        val frameTokenPointer = frameTokenType.toPointer(frameTokenType.javaClass)

        //assign the issuer to the frame type who will be issuing the tokens
        val frameIssuedTokenType = frameTokenPointer issuedBy ourIdentity

        //mention the current holder also
        val frameToken = frameIssuedTokenType heldBy holder

        //Step 2: switch Token
        val switchStateAndRef: StateAndRef<SwitchTokenState> = getSwitchStates(switchSerial)

        //get the TokenType object
        val switchTokenType: SwitchTokenState = switchStateAndRef.state.data

        //get the pointer pointer to the switch
        val switchTokenPointer: TokenPointer<*> = switchTokenType.toPointer(switchTokenType.javaClass)

        //assign the issuer to the switch type who will be issuing the tokens
        val switchIssuedTokenType = switchTokenPointer issuedBy ourIdentity

        //mention the current holder also
        val switchToken = switchIssuedTokenType heldBy holder

        //distribute the new keyboard (two token to be exact)
        //call built in flow to issue non fungible tokens
        val stx = subFlow(IssueTokens(listOf(frameToken, switchToken)))

        return "\nA new keyboard is being issued to ${holder.name.organisation} " +
                "with frame serial: ${this.frameSerial}; switch serial: ${this.switchSerial} " +
                "\nTransaction ID: ${stx.id}"

    }
}