package com.template.states

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.template.contracts.SwitchContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(SwitchContract::class)
data class SwitchTokenState(
    val maintainer: Party,
    override val linearId: UniqueIdentifier,
    override val fractionDigits: Int,
    val serialNum: String,
    override val maintainers: List<Party> = listOf(maintainer)
) : EvolvableTokenType()