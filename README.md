# Keyboard Market - TokenSDK

## Introduction

This sample Cordapp demonstrate some simple flows related to the token SDK. In this Cordapp,there are four parties:

- The Keyboard Company (KeyboardCo): can manufacture, sell, and recall/total the keyboards(or parts).
- The Licensed Dealership: Buy the keyboards from the KeyboardCo
- Used Parts Agency: Buy used parts from the Licensed Dealership(or end-buyers)
- Buyer: Buy keyboard from the KeyboardCo or licensed dealership, or buy used parts from used parts agency.

We will be demonstrating one of the possible logic here:

1. KeyboardCo manufactures the keyboards
2. KeyboardCo can sell the keyboards to licensed dealership and buyers.
3. Used parts agency can get the used keyboard parts from the licensed dealership or buyers.
4. When there is a need of total the physical keyboard part, the current of the physical part will redeem the token with the
   KeyboardCo

Throughout the sample, we will see how to create, transact, and redeem a token.

## Running the sample

Deploy and run the nodes by:

```
./gradlew deployNodes
./build/nodes/runnodes
```

Once all four nodes are started up, in KeyboardCo's node shell, run:

```
flow start CreateFrameToken frameSerial: F4561
flow start CreateSwitchToken switchSerial: W7894 
```

After this step, we have created 2 tokens representing the physical keyboard part with unique serial number(which will be
unique in the manufacturing). Then run:

```
flow start IssueNewKeyboard frameSerial: F4561, switchSerial: W7894, holder: LicensedDealership
```

This line of command will transfer the tokens(2 tokens together represents a single keyboard) to the licensed dealership.

Now, at the licensed dealership's shell, we can see we did receive the tokens by running:

```
run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
```

Continue to the business flow, the licensed dealership will sell the keyboard to the Buyer. Run:

```
flow start TransferKeyboardToken frameSerial: F4561, switchSerial: W7894, holder: Buyer
```

Now we can check at the Buyer's node shell to see if the buyer receives the token by running the same `vaultQuery` we
just ran at the dealership's shell.

At the Buyer side, we would assume we got a recall notice and will send the physical keyboard frame back to the
manufacturer. The action will happen in real life, but on the ledger we will also need to "destroy"(process of redeem in
Corda TokenSDK) the frame token. Run:

```
flow start TotalPart part: frame, serial: F4561
```

At the buyer's shell, if we do
the `vaultQuery` again, we will see we now
only have a switch token(the frame token is gone). With the switch token, we can sell this pair of switch to the used
parts agency. We will achieve it by running:

```
flow start TransferPartToken part: switch, serial: W7894, holder: UsedPartsAgency
```

At the end of the flow logic, we will find the frame token is destroyed and the used parts agency holds the switch token. 