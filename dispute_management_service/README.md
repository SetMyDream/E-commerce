# Dispute management service
[![Services](https://img.shields.io/badge/%E2%AC%85-Back-green.svg)](../README.md)

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

## Endpoints
1. List all disputes, where the user is involved
> (user_id: ID): JSON(disputes_list)
2. Go to dispute
> (dispute_id: ID): JSON(dispute_page)

## Used
- Akka-http & tapir - because there are not so many endpoints

---