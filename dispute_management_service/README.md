# Dispute management service
[![Services](https://img.shields.io/badge/%E2%AC%85-Back-green.svg)](../README.md)

- When a user buys a product, he can raise a dispute against seller
- Any user can list the disputes in which they are involved
  > User shouldn't see disputes in which they are not involved
- Both users have options to resolve disputes in which the are involved
  > Users act on disputes with chat commands. System notifies the other party

## Dispute options

### Item is not as described
  - *(For the seller)* **provide refund to the buyer** *(approves refund)*
  - *(For the buyer)* **send the item back** *(automatically invokes refund)*
### Item has not arrived
  - *(For the seller)* **provide refund to the buyer** *(approves refund)*
  - *(For both)* **wait** *(system simulates item arrival)*

## Endpoints
1. List all disputes relevant to the user
> (user_auth_token)() => disputes_list
2. Start "Item is not as described" dispute
> (buyer_auth_token)(purchace_id: ID) => Ok
3. Start "Item has not arrived" dispute
> (buyer_auth_token)(purchace_id: ID) => Ok
4. Provide refund
> (seller_auth_token)(dispute_id: ID) => Ok
5. Return item
> (buyer_auth_token)(dispute_id: ID) => Ok
6. Wait
> (user_auth_token)(dispute_id: ID) => Ok

## Used
- http4s - as a minimal interface for building an HTTP service

## Infrastructure dependencies

### User management
- [x] Get self
> (auth_token)() => user_info

### Product inventory
- [ ] Get purchase info
> (buyer_auth_token OR seller_auth_token)(purchase_id) => purchase_info
- [ ] Update purchase status
> (purchase_id, new_status, vault_TOTP_code) => Ok
- [ ] Refund purchase
> (purchase_id, vault_TOTP_code) => Ok

### Chat
- [ ] Notify as system
> (user_id, message) => Ok
