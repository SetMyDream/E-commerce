# User to user chat
[![Services](https://img.shields.io/badge/%E2%AC%85-Back-green.svg)](../README.md)

- A user can initiate a one to one chat with some other specific user
- A user can initiate a group chat with some other specific users
- A user can access all chats in which he is participating
- Chat gives a user a way to use commands:
    - raise a dispute
    - list all disputes in which the user is involved
    - as a seller, approve refund as an action to a specific dispute
    - as a seller, request a product back as an action to a specific dispute

## Endpoints
1. Initiate a 1-to-1 chat
> (user_id: ID): chat_id
2. Initiate a group chat
> (user_ids: List[ID]): chat_id
3. Send a message to a chat
> (chat_id: ID, message: String): Ok ===>
> if (message is command) {proccess(command)};
> if (command fails) {send a message as a reply to the command message}

## Used
- ***Akka*** — concurrency is important for the chat service. Also actor system usage is intuitive for conversation participants
- ***Akka-http*** — implement a full server- and client-side HTTP stack **on top** of **akka-actor** and **akka-stream**. It's toolkit for providing and consuming HTTP-based services.
- ***tapir*** — we can describe **HTTP API endpoints** as **immutable Scala values**. Each endpoint can contain a number of input parameters, error-output parameters, and normal-output parameters.

---

Running the server:

sbt "runMain chat.Server [server] [port]"
i.e.:
`sbt "runMain Server 127.0.0.1 2844"`

Running the client connecting to the server:

sbt "runMain chat.Client [server] [port] [username]"
i.e.:
`sbt "runMain Client 127.0.0.1 2844 Artem"`

Client commands:

`text` send to all connected users

`/users` lists logged in users

`/w [username] *msg*` sends private message *msg* to user *username*

`/quit` disconnect and terminate client


