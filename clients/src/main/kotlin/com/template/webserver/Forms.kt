package com.template.webserver

import com.template.states.utils.GenderEnums
import com.template.states.utils.StatusEnums
import com.template.states.utils.UserTypeEnums

data class CreateUserParams(
    val userType: UserTypeEnums,
    val name: String,
    val age: Int,
    val gender: GenderEnums,
    val status: StatusEnums,
    val otherParty: String
)

data class CreateUserOwnNodesParams(
    val userType: UserTypeEnums,
    val name: String,
    val age: Int,
    val gender: GenderEnums,
    val status: StatusEnums
)

data class UpdateUserParams(
    val userType: UserTypeEnums,
    val name: String,
    val age: Int,
    val gender: GenderEnums,
    val status: StatusEnums,
    val linearId: String
)

data class UpdateNameParams(val name: String, val linearId: String)

data class DeleteUserParams(val linearId: String = "")

data class SearchUserParams(val linearId: String = "")
