# E-commerce Sigma Software scala interns web app

## Technologies

- Scala 2.13
- sbt
> Scala syntax is used
> Good for Java and Scala projects build
- Play framework
> Scala framework
> Many functions and endpoints are
- Akka-http
> Extensive documentation, works naturally in Akka ecosystem
- tapir
> Good documentation, easier to bind with Swagger
- Akka
> To ease the work with concurrent tasks
- DB access lib slick
> ORM for Scala lang, nicely connected to RDBS
- JSON serde lib circe
> More widely used, easier to use
- PostgreSQL
> Free open-scource RDBS with wide selection of instruments and many examples
- docker
> Containerizing services
- Swagger
> Automatic API documentation

## Services

### User management service
- CRUD users
- Users have their wallets
  `registered users are provided with some significant amount of in-system currency`
- Username unique check
- Password constraints
- Password encryption (SHA-256?)
- Authentication with username and password
- Authorization for other services

#### Endpoints
1. Home page (with sign up and sign in)
   (is_logged_in :Boolean): main_url     //supposed to work as filter
2. Registration page
> (): registartion_url
3. Login page
> (): login_url
4. All users list
> (): users_all_url
5. Personal user page
> (user_id: ID): personal_page_url

#### Used
- Play - because it's one of the biggest services in the app, using a batteries-included framework might be a better option for quick and optimal rescourse management

---
### User to user chat

- A user can initiate a one to one chat with some other specific user
- A user can initiate a group chat with some other specific users
- A user can access all chats in which he is participating
- Chat gives a user a way to use commands:
    - raise a dispute
    - list all disputes in which the user is involved
    - as a seller, approve refund as an action to a specific dispute
    - as a seller, request a product back as an action to a specific dispute

#### Endpoints
1. Initiate a 1-to-1 chat
> (user_id: ID): chat_id
2. Initiate a group chat
> (user_ids: List[ID]): chat_id
3. Send a message to a chat
> (chat_id: ID, message: String): Ok ===>
> if (message is command) {proccess(command)};
> if (command fails) {send a message as a reply to the command message}

#### Used
- ***Akka*** — concurrency is important for the chat service. Also actor system usage is intuitive for conversation participants
- ***Akka-http*** — implement a full server- and client-side HTTP stack **on top** of **akka-actor** and **akka-stream**. It's toolkit for providing and consuming HTTP-based services.
- ***tapir*** — we can describe **HTTP API endpoints** as **immutable Scala values**. Each endpoint can contain a number of input parameters, error-output parameters, and normal-output parameters.

---
### Product inventory service
- Product inventory contains at least two different categories of products with at least 3 products in each
- Registered users could buy products from system or other users
    - Users should be able to add/update/delete their products
    - Users should be able to see other users' products
    - Users should be able to buy other users' products if they have enough currency
    - Users should be able to see all products that they bought
    - Buyer can send a product back if in the proccess of dispute, the seller requested it

#### Endpoints
1. List all categories
> (): List[category_data]
2. List all products in a category
> (category_id: ID): List[short_product_data]
3. View product in detail
> (product_id: ID): long_product_data
4. Add a product
> (product_creation_data): product_id
5. Update a product
> (product_id, product_creation_data): Ok
6. Delete a product
> (product_id): Ok
7. Buy other user's product
> (product_id): Ok
8. Return a bought product
> (product_id): Ok

#### Used
- Play - same as for user management: better option for quick and optimal rescourse management, quick realization


----------------------------------------
### Dispute management service

- Any user can list the disputes in which he is involved
  `User shouldn't see disputes in which he is not involved`

- When a user buys a product, he can raise a dispute against seller
    - If the buyer got the wrong item, the buyer wants to start a dispute. Dispute's topic is **"$Item not as described"**
        - When the **"Item not as described"** dispute has started, it notifies the seller
        - The seller has the right to demand the buyer to send the item back
        - There are three ways to act on the **"Item not as described"** dispute:
          `Any operation aside from "Wait" should be approved by the seller`
            - Wait (pauses any other operation over)
            - The seller gives a refund to the buyer
            - The seller resend propper product


- If in the described time the item hasn't arrived, the buyer wants to start a dispute. Dispute's topic is **"${Item not arrived}"**
    - When the **"Item not arrived"** dispute has started, it notifies the seller
    - There are two ways to resolve **"Item not arrived"** dispute:
        - Wait (pauses any other operation over the dispute;
          item arrives after a certain time)
        - The seller gives a refund to the buyer
          `Should be approved by the seller`

#### Endpoints
1. List all disputes, where the user is involved
> (user_id: ID): JSON(disputes_list)
2. Go to dispute
> (dispute_id: ID): JSON(dispute_page)

#### Used
- Akka-http & tapir - because there are not so many endpoints

---
### Reporting service
`reports are regarding user stats, disputes, chat stats`
- Stats on users
  - Users quantity
  - New users for last [time period]
  - ...
- Stats on disputes
- Chat stats

#### Endpoints
_To Be Discussed_

#### Used
_To Be Discussed_



